package com.yogeshpaliyal.keypass.vault

import app.keemobile.kotpass.constants.BasicField
import app.keemobile.kotpass.cryptography.EncryptedValue
import app.keemobile.kotpass.database.Credentials
import app.keemobile.kotpass.database.KeePassDatabase
import app.keemobile.kotpass.database.decode
import app.keemobile.kotpass.database.encode
import app.keemobile.kotpass.database.getEntryBy
import app.keemobile.kotpass.database.modifiers.modifyEntry
import app.keemobile.kotpass.database.modifiers.modifyCredentials
import app.keemobile.kotpass.database.modifiers.modifyParentGroup
import app.keemobile.kotpass.database.modifiers.removeEntry
import app.keemobile.kotpass.models.Entry
import app.keemobile.kotpass.models.Meta
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class KotpassVaultRepository(
    private val vaultFile: File
) : VaultRepository {
    private var fileOperations: VaultFileOperations = DefaultVaultFileOperations
    private val operationLock = Any()
    private val stateLock = Any()
    private val masterPasswordChangeInProgress = AtomicBoolean(false)
    private var database: KeePassDatabase? = null
    private var sessionVersion = 0L

    internal constructor(
        vaultFile: File,
        fileOperations: VaultFileOperations
    ) : this(vaultFile) {
        this.fileOperations = fileOperations
    }

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
                val decoded = fileOperations.openInput(vaultFile).use { input ->
                    KeePassDatabase.decode(input, credentials)
                }
                completeUnlock(version, decoded)
            }
        }
    }

    override suspend fun changeMasterPassword(
        currentMasterPassword: CharArray,
        newMasterPassword: CharArray,
        onCandidateValidated: (encryptedCandidateFingerprint: String) -> Unit
    ) {
        check(masterPasswordChangeInProgress.compareAndSet(false, true)) {
            "A Master Password change is already in progress."
        }
        try {
            withContext(Dispatchers.IO + NonCancellable) {
                synchronized(operationLock) {
                    val (currentSession, version) = unlockedSession()
                    if (!vaultFile.isFile) {
                        throw FileNotFoundException("Vault file does not exist.")
                    }

                    val currentCredentials = credentialsFrom(currentMasterPassword)
                    val activeDatabase = try {
                        decode(vaultFile, currentCredentials)
                    } catch (failure: Exception) {
                        throw InvalidCurrentMasterPasswordException(failure)
                    }
                    val newCredentials = credentialsFrom(newMasterPassword)
                    val candidate = activeDatabase.modifyCredentials { newCredentials }
                    val persisted = persistDatabase(
                        candidate = candidate,
                        targetMustExist = true,
                        activeCredentials = currentCredentials,
                        candidateCredentials = newCredentials,
                        onCandidateValidated = onCandidateValidated
                    )
                    completeMutation(currentSession, version, persisted)
                }
            }
        } finally {
            masterPasswordChangeInProgress.set(false)
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
        targetMustExist: Boolean,
        activeCredentials: Credentials = candidate.credentials,
        candidateCredentials: Credentials = candidate.credentials,
        onCandidateValidated: (encryptedCandidateFingerprint: String) -> Unit = {}
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

        val temporary = fileOperations.createTempFile("rahsa-vault-candidate-", ".kdbx", parent)
        try {
            fileOperations.openOutput(temporary).use { output ->
                candidate.encode(output)
            }
            val validated = decode(temporary, candidateCredentials)
            onCandidateValidated(encryptedFingerprint(temporary))
            promoteValidatedCandidate(
                candidate = temporary,
                target = target,
                targetMustExist = targetMustExist,
                parent = parent,
                activeCredentials = activeCredentials,
                candidateCredentials = candidateCredentials
            )
            return validated
        } finally {
            if (temporary.exists() && !fileOperations.delete(temporary)) {
                temporary.deleteOnExit()
            }
        }
    }

    private fun decode(file: File, credentials: Credentials): KeePassDatabase =
        fileOperations.openInput(file).use { input -> KeePassDatabase.decode(input, credentials) }

    private fun promoteValidatedCandidate(
        candidate: File,
        target: File,
        targetMustExist: Boolean,
        parent: File,
        activeCredentials: Credentials,
        candidateCredentials: Credentials
    ) {
        if (!targetMustExist) {
            if (target.exists()) {
                throw IOException("Vault file already exists.")
            }
            if (!fileOperations.move(candidate, target)) {
                throw IOException("Could not install the new vault file.")
            }
            try {
                decode(target, candidateCredentials)
            } catch (failure: Exception) {
                if (!fileOperations.move(target, candidate)) {
                    failure.addSuppressed(IOException("Could not preserve the failed vault candidate."))
                }
                throw failure
            }
            return
        }

        if (!target.isFile) {
            throw FileNotFoundException("Vault file does not exist.")
        }

        // Confirm the on-disk source before it is allowed to become the next LKG.
        decode(target, activeCredentials)

        val previousLkg = lkgFile(target)
        val displacedLkg = if (previousLkg.exists()) {
            prepareEmptyTempFile("rahsa-vault-displaced-lkg-", parent)
        } else {
            null
        }
        var priorLkgDisplaced = false
        var activePreservedAsLkg = false
        var promoted = false

        try {
            if (displacedLkg != null && !fileOperations.move(previousLkg, displacedLkg)) {
                throw IOException("Could not preserve the previous LKG.")
            }
            priorLkgDisplaced = displacedLkg != null

            if (!fileOperations.move(target, previousLkg)) {
                throw IOException("Could not preserve the existing vault file as LKG.")
            }
            activePreservedAsLkg = true

            if (!fileOperations.move(candidate, target)) {
                throw IOException("Could not install the updated vault file.")
            }
            decode(target, candidateCredentials)
            promoted = true
        } catch (failure: Exception) {
            rollbackPromotion(
                target,
                candidate,
                previousLkg,
                displacedLkg,
                activePreservedAsLkg,
                priorLkgDisplaced,
                failure
            )
            throw failure
        } finally {
            if (promoted) {
                displacedLkg?.let(::deleteIfPresent)
            }
        }
    }

    private fun rollbackPromotion(
        target: File,
        candidate: File,
        previousLkg: File,
        displacedLkg: File?,
        activePreservedAsLkg: Boolean,
        priorLkgDisplaced: Boolean,
        failure: Exception
    ) {
        if (activePreservedAsLkg && !target.exists() && previousLkg.exists()) {
            if (!fileOperations.move(previousLkg, target)) {
                failure.addSuppressed(IOException("Could not restore the previous active vault."))
            }
        } else if (activePreservedAsLkg && target.exists() && previousLkg.exists()) {
            if (candidate.exists()) {
                deleteIfPresent(candidate)
            }
            if (!fileOperations.move(target, candidate) || !fileOperations.move(previousLkg, target)) {
                failure.addSuppressed(IOException("Could not restore the previous active vault."))
            }
        }
        if (priorLkgDisplaced && displacedLkg != null && displacedLkg.exists() && !fileOperations.move(displacedLkg, previousLkg)) {
            failure.addSuppressed(IOException("Could not restore the previous LKG."))
        }
    }

    private fun prepareEmptyTempFile(prefix: String, parent: File): File =
        fileOperations.createTempFile(prefix, ".kdbx", parent).also { temporary ->
            if (!fileOperations.delete(temporary)) {
                throw IOException("Could not prepare a vault transaction file.")
            }
        }

    private fun lkgFile(target: File): File = File(target.parentFile, "${target.nameWithoutExtension}.lkg.kdbx")

    private fun encryptedFingerprint(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        fileOperations.openInput(file).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
            buffer.fill(0)
        }
        return digest.digest().joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    private fun deleteIfPresent(file: File) {
        if (file.exists() && !fileOperations.delete(file)) {
            file.deleteOnExit()
        }
    }

    private companion object {
        const val DatabaseName = "RAHSA"
        const val RootGroupName = "Root"
    }
}
