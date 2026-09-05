package com.yogeshpaliyal.keypass.vault

import app.keemobile.kotpass.constants.BasicField
import app.keemobile.kotpass.cryptography.EncryptedValue
import app.keemobile.kotpass.database.Credentials
import app.keemobile.kotpass.database.KeePassDatabase
import app.keemobile.kotpass.database.decode
import app.keemobile.kotpass.database.encode
import app.keemobile.kotpass.database.getEntryBy
import app.keemobile.kotpass.database.modifiers.modifyEntry
import app.keemobile.kotpass.database.modifiers.modifyParentGroup
import app.keemobile.kotpass.database.modifiers.removeEntry
import app.keemobile.kotpass.models.Entry
import app.keemobile.kotpass.models.Meta
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KotpassVaultRepository(
    private val vaultFile: File
) : VaultRepository {
    private val operationLock = Any()
    private val stateLock = Any()
    private var database: KeePassDatabase? = null
    private var sessionVersion = 0L

    override suspend fun createVault(masterPassword: CharArray) {
        withContext(Dispatchers.IO) {
            synchronized(operationLock) {
                val version = beginUnlockAttempt()
                if (vaultFile.exists()) {
                    throw IOException("Vault file already exists.")
                }

                val credentials = credentialsFrom(masterPassword)
                val candidate = KeePassDatabase.Ver4x.create(
                    rootName = RootGroupName,
                    meta = Meta(name = DatabaseName),
                    credentials = credentials
                )
                val persisted = persistDatabase(candidate, targetMustExist = false)
                completeUnlock(version, persisted)
            }
        }
    }

    override suspend fun openVault(masterPassword: CharArray) {
        withContext(Dispatchers.IO) {
            synchronized(operationLock) {
                val version = beginUnlockAttempt()
                if (!vaultFile.isFile) {
                    throw FileNotFoundException("Vault file does not exist.")
                }

                val credentials = credentialsFrom(masterPassword)
                val decoded = FileInputStream(vaultFile).use { input ->
                    KeePassDatabase.decode(input, credentials)
                }
                completeUnlock(version, decoded)
            }
        }
    }

    override fun lock() {
        synchronized(stateLock) {
            database = null
            sessionVersion++
        }
    }

    override suspend fun listCredentials(): List<Credential> = withContext(Dispatchers.IO) {
        synchronized(operationLock) {
            unlockedDatabase().activeEntries().map(Entry::toCredential)
        }
    }

    override suspend fun createCredential(credential: Credential) {
        withContext(Dispatchers.IO) {
            synchronized(operationLock) {
                val (current, version) = unlockedSession()
                val entry = credential.toEntry()
                val duplicateEntry = current.getEntryBy { uuid == entry.uuid } != null
                val deletedEntry = current.content.deletedObjects.any { it.id == entry.uuid }
                require(!duplicateEntry && !deletedEntry) {
                    "Credential ID already exists."
                }

                val candidate = current.modifyParentGroup {
                    copy(entries = entries + entry)
                }
                val persisted = persistDatabase(candidate, targetMustExist = true)
                completeMutation(current, version, persisted)
            }
        }
    }

    override suspend fun updateCredential(credential: Credential) {
        withContext(Dispatchers.IO) {
            synchronized(operationLock) {
                val (current, version) = unlockedSession()
                val mappedEntry = credential.toEntry()
                if (current.findActiveEntry(mappedEntry.uuid) == null) {
                    throw NoSuchElementException("Credential does not exist.")
                }

                val candidate = current.modifyEntry(mappedEntry.uuid) {
                    copy(fields = fields + mappedEntry.fields)
                }
                val persisted = persistDatabase(candidate, targetMustExist = true)
                completeMutation(current, version, persisted)
            }
        }
    }

    override suspend fun deleteCredential(id: String) {
        withContext(Dispatchers.IO) {
            synchronized(operationLock) {
                val (current, version) = unlockedSession()
                val uuid = UUID.fromString(id)
                if (current.findActiveEntry(uuid) == null) {
                    throw NoSuchElementException("Credential does not exist.")
                }

                val candidate = current.removeEntry(uuid)
                val persisted = persistDatabase(candidate, targetMustExist = true)
                completeMutation(current, version, persisted)
            }
        }
    }

    override suspend fun searchCredentials(query: String): List<Credential> =
        withContext(Dispatchers.IO) {
            synchronized(operationLock) {
                unlockedDatabase()
                    .activeEntries()
                    .filter { entry ->
                        entry[BasicField.Title]?.content.orEmpty().contains(query, ignoreCase = true) ||
                            entry[BasicField.UserName]?.content.orEmpty().contains(query, ignoreCase = true)
                    }
                    .map(Entry::toCredential)
            }
        }

    private fun beginUnlockAttempt(): Long = synchronized(stateLock) {
        database = null
        ++sessionVersion
    }

    private fun completeUnlock(version: Long, unlockedDatabase: KeePassDatabase) {
        synchronized(stateLock) {
            check(sessionVersion == version) {
                "Vault was locked during the operation."
            }
            database = unlockedDatabase
        }
    }

    private fun unlockedDatabase(): KeePassDatabase = synchronized(stateLock) {
        database ?: throw VaultLockedException()
    }

    private fun unlockedSession(): Pair<KeePassDatabase, Long> = synchronized(stateLock) {
        val unlocked = database ?: throw VaultLockedException()
        unlocked to sessionVersion
    }

    private fun completeMutation(
        previousDatabase: KeePassDatabase,
        version: Long,
        persistedDatabase: KeePassDatabase
    ) {
        synchronized(stateLock) {
            if (sessionVersion == version && database === previousDatabase) {
                database = persistedDatabase
            }
        }
    }

    private fun credentialsFrom(masterPassword: CharArray): Credentials =
        Credentials.from(EncryptedValue.fromString(masterPassword.concatToString()))

    private fun KeePassDatabase.activeEntries(): List<Entry> = content.group
        .findChildEntries(recycleBinUuid = content.meta.recycleBinUuid) { true }
        .flatMap { (_, entries) -> entries }

    private fun KeePassDatabase.findActiveEntry(uuid: UUID): Entry? = content.group
        .findChildEntry(recycleBinUuid = content.meta.recycleBinUuid) { it.uuid == uuid }
        ?.second

    private fun persistDatabase(
        candidate: KeePassDatabase,
        targetMustExist: Boolean
    ): KeePassDatabase {
        val target = vaultFile.absoluteFile
        val parent = target.parentFile
            ?: throw IOException("Vault file must have a parent directory.")

        if (!parent.exists() && !parent.mkdirs()) {
            throw IOException("Could not create the vault directory.")
        }
        if (!parent.isDirectory) {
            throw IOException("Vault parent is not a directory.")
        }
        if (targetMustExist && !target.isFile) {
            throw FileNotFoundException("Vault file does not exist.")
        }
        if (!targetMustExist && target.exists()) {
            throw IOException("Vault file already exists.")
        }

        val temporary = File.createTempFile("keypass-vault-", ".tmp", parent)
        try {
            val encoded = FileOutputStream(temporary).use { output ->
                candidate.encode(output)
            }
            replaceVaultFile(temporary, target, targetMustExist, parent)
            return encoded
        } finally {
            if (temporary.exists() && !temporary.delete()) {
                temporary.deleteOnExit()
            }
        }
    }

    private fun replaceVaultFile(
        temporary: File,
        target: File,
        targetMustExist: Boolean,
        parent: File
    ) {
        if (!targetMustExist) {
            if (target.exists()) {
                throw IOException("Vault file already exists.")
            }
            if (!temporary.renameTo(target)) {
                throw IOException("Could not install the new vault file.")
            }
            return
        }

        if (!target.isFile) {
            throw FileNotFoundException("Vault file does not exist.")
        }

        val backup = File.createTempFile("keypass-vault-backup-", ".kdbx", parent)
        if (!backup.delete()) {
            throw IOException("Could not prepare a vault backup file.")
        }
        if (!target.renameTo(backup)) {
            throw IOException("Could not preserve the existing vault file.")
        }

        try {
            if (!temporary.renameTo(target)) {
                throw IOException("Could not install the updated vault file.")
            }
        } catch (failure: Exception) {
            if (!target.exists() && !backup.renameTo(target)) {
                failure.addSuppressed(
                    IOException("Could not restore the previous vault; its backup was preserved.")
                )
            }
            throw failure
        }

        if (backup.exists() && !backup.delete()) {
            backup.deleteOnExit()
        }
    }

    private companion object {
        const val DatabaseName = "KeyPass"
        const val RootGroupName = "Root"
    }
}
