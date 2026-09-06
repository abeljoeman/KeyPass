package com.yogeshpaliyal.keypass.vault

import app.keemobile.kotpass.cryptography.EncryptedValue
import app.keemobile.kotpass.database.Credentials
import app.keemobile.kotpass.database.KeePassDatabase
import app.keemobile.kotpass.database.decode
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KotpassVaultRepositoryTest {

    @Test
    fun openVault_withKnownFixture_exposesExpectedCredentials() = runBlocking {
        withKnownVaultFixture { vaultFile ->
            val repository = KotpassVaultRepository(vaultFile)
            repository.openVault("test-password".toCharArray())

            val credentials = repository.listCredentials()
            val credentialsById = credentials.associateBy(Credential::id)
            val expectedCredentials = listOf(
                Credential(
                    id = "123e4567-e89b-12d3-a456-426614174000",
                    title = "Example Account",
                    username = "alice@example.com",
                    password = "fixture-password-1",
                    url = "https://example.com/login",
                    notes = "Primary fixture credential"
                ),
                Credential(
                    id = "223e4567-e89b-12d3-a456-426614174001",
                    title = "Work Portal",
                    username = "alice.work",
                    password = "fixture-password-2",
                    url = "https://work.example.test",
                    notes = "Secondary fixture credential"
                )
            )

            assertEquals(2, credentials.size)
            assertEquals(2, credentialsById.size)
            expectedCredentials.forEach { expected ->
                assertEquals(expected, credentialsById[expected.id])
            }
            repository.lock()
        }
    }

    @Test
    fun createCredential_persistsAfterVaultReopen() = runBlocking {
        withKnownVaultFixture { vaultFile ->
            val created = Credential(
                id = "323e4567-e89b-12d3-a456-426614174002",
                title = "Persisted Create",
                username = "created-user",
                password = "created-password",
                url = "https://created.example.test",
                notes = "Created before vault reopen"
            )

            val repository = openKnownVault(vaultFile)
            repository.createCredential(created)
            repository.lock()

            val reopenedRepository = openKnownVault(vaultFile)
            val persisted = reopenedRepository.listCredentials()
                .firstOrNull { it.id == created.id }

            assertEquals(created, persisted)
            reopenedRepository.lock()
        }
    }

    @Test
    fun updateCredential_persistsAfterVaultReopen() = runBlocking {
        withKnownVaultFixture { vaultFile ->
            val credentialId = "123e4567-e89b-12d3-a456-426614174000"
            val repository = openKnownVault(vaultFile)
            val updated = repository.listCredentials()
                .first { it.id == credentialId }
                .copy(
                    title = "Updated After Reopen",
                    username = "updated-user",
                    password = "updated-password",
                    url = "https://updated.example.test",
                    notes = "Updated before vault reopen"
                )

            repository.updateCredential(updated)
            repository.lock()

            val reopenedRepository = openKnownVault(vaultFile)
            val persisted = reopenedRepository.listCredentials()
                .firstOrNull { it.id == credentialId }

            assertEquals(updated, persisted)
            reopenedRepository.lock()
        }
    }

    @Test
    fun deleteCredential_persistsAfterVaultReopen() = runBlocking {
        withKnownVaultFixture { vaultFile ->
            val deletedId = "223e4567-e89b-12d3-a456-426614174001"
            val repository = openKnownVault(vaultFile)

            repository.deleteCredential(deletedId)
            repository.lock()

            val reopenedRepository = openKnownVault(vaultFile)
            val credentials = reopenedRepository.listCredentials()

            assertEquals(1, credentials.size)
            assertTrue(credentials.none { it.id == deletedId })
            reopenedRepository.lock()
        }
    }

    @Test
    fun openVault_withWrongPassword_failsClosed() = runBlocking {
        withKnownVaultFixture { vaultFile ->
            val repository = KotpassVaultRepository(vaultFile)
            repository.openVault("test-password".toCharArray())
            assertEquals(2, repository.listCredentials().size)

            val wrongPasswordOpen = runCatching {
                repository.openVault("wrong-password".toCharArray())
            }
            assertTrue("Wrong-password open must fail.", wrongPasswordOpen.isFailure)

            val credentialAccess = runCatching { repository.listCredentials() }
            assertTrue(
                "Repository must be locked after a failed open.",
                credentialAccess.exceptionOrNull() is VaultLockedException
            )
        }
    }

    @Test
    fun openVault_withCorruptedFile_doesNotOverwriteSource() = runBlocking {
        val vaultFile = File.createTempFile("corrupted-vault-", ".kdbx")
        try {
            val originalBytes = "not-a-valid-kdbx-vault".toByteArray()
            vaultFile.writeBytes(originalBytes)

            val repository = KotpassVaultRepository(vaultFile)
            val openResult = runCatching {
                repository.openVault("test-password".toCharArray())
            }

            assertTrue("Corrupted-vault open must fail.", openResult.isFailure)
            assertArrayEquals(
                "Failed decode must leave the source vault unchanged.",
                originalBytes,
                vaultFile.readBytes()
            )

            val credentialAccess = runCatching { repository.listCredentials() }
            assertTrue(
                "Repository must remain locked after a corrupted-vault open.",
                credentialAccess.exceptionOrNull() is VaultLockedException
            )
        } finally {
            if (vaultFile.exists() && !vaultFile.delete()) {
                vaultFile.deleteOnExit()
            }
        }
    }

    @Test
    fun successfulPromotion_replacesActiveAndRetainsPreviousActiveAsOnlyLkg() = runBlocking {
        withKnownVaultFixture { vaultFile ->
            val original = vaultFile.readBytes()
            val created = testCredential("B")
            val repository = openKnownVault(vaultFile)

            repository.createCredential(created)
            repository.lock()

            assertEquals(created, openKnownVault(vaultFile).listCredentials().first { it.id == created.id })
            assertArrayEquals(original, lkgFile(vaultFile).readBytes())
            assertDecryptableKdbx(vaultFile)
            assertDecryptableKdbx(lkgFile(vaultFile))
            assertEquals(1, lkgFiles(vaultFile).size)
        }
    }

    @Test
    fun repeatedPromotion_replacesRatherThanAccumulatesLkgHistory() = runBlocking {
        withKnownVaultFixture { vaultFile ->
            val repository = openKnownVault(vaultFile)
            val created = testCredential("B")
            repository.createCredential(created)
            val activeB = vaultFile.readBytes()
            repository.updateCredential(created.copy(title = "C"))
            repository.lock()

            assertEquals("C", openKnownVault(vaultFile).listCredentials().first { it.id == created.id }.title)
            assertArrayEquals(activeB, lkgFile(vaultFile).readBytes())
            assertEquals(1, lkgFiles(vaultFile).size)
        }
    }

    @Test
    fun freshVault_doesNotCreateSyntheticLkgUntilFirstMutation() = runBlocking {
        val directory = File.createTempFile("fresh-vault-", "").also { temporary ->
            check(temporary.delete() && temporary.mkdirs())
        }
        val vaultFile = File(directory, "vault.kdbx")
        try {
            val repository = KotpassVaultRepository(vaultFile)
            repository.createVault("test-password".toCharArray())
            assertFalse(lkgFile(vaultFile).exists())
            val original = vaultFile.readBytes()

            repository.createCredential(testCredential("first-mutation"))
            assertArrayEquals(original, lkgFile(vaultFile).readBytes())
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun candidateValidationFailure_leavesActiveAndLkgByteForByteUntouched() = runBlocking {
        withKnownVaultFixture { vaultFile ->
            val setup = openKnownVault(vaultFile)
            setup.createCredential(testCredential("B"))
            setup.lock()
            val activeBefore = vaultFile.readBytes()
            val lkgBefore = lkgFile(vaultFile).readBytes()
            val repository = KotpassVaultRepository(vaultFile, FailCandidateValidationOperations())
            repository.openVault("test-password".toCharArray())

            val result = runCatching {
                repository.createCredential(testCredential("invalid", "423e4567-e89b-12d3-a456-426614174003"))
            }

            assertTrue(result.isFailure)
            assertArrayEquals(activeBefore, vaultFile.readBytes())
            assertArrayEquals(lkgBefore, lkgFile(vaultFile).readBytes())
        }
    }

    @Test
    fun promotionFailureAfterValidation_restoresPreviousUsableStateWithoutExtraLkg() = runBlocking {
        withKnownVaultFixture { vaultFile ->
            val setup = openKnownVault(vaultFile)
            setup.createCredential(testCredential("B"))
            setup.lock()
            val activeBefore = vaultFile.readBytes()
            val lkgBefore = lkgFile(vaultFile).readBytes()
            val operations = FailCandidatePromotionOperations(vaultFile)
            val repository = KotpassVaultRepository(vaultFile, operations)
            repository.openVault("test-password".toCharArray())

            val result = runCatching {
                repository.createCredential(testCredential("should-fail", "423e4567-e89b-12d3-a456-426614174003"))
            }

            assertTrue(result.isFailure)
            assertTrue("Candidate must be validated before promotion is attempted.", operations.candidateWasRead)
            assertArrayEquals(activeBefore, vaultFile.readBytes())
            assertArrayEquals(lkgBefore, lkgFile(vaultFile).readBytes())
            assertEquals(1, lkgFiles(vaultFile).size)
            assertDecryptableKdbx(vaultFile)
            assertDecryptableKdbx(lkgFile(vaultFile))
        }
    }

    @Test
    fun corruptActiveWithValidLkg_doesNotSilentlyRestore() = runBlocking {
        withKnownVaultFixture { vaultFile ->
            val setup = openKnownVault(vaultFile)
            setup.createCredential(testCredential("B"))
            setup.lock()
            val lkgBefore = lkgFile(vaultFile).readBytes()
            val corrupt = "corrupt-active".toByteArray()
            vaultFile.writeBytes(corrupt)

            val result = runCatching { KotpassVaultRepository(vaultFile).openVault("test-password".toCharArray()) }

            assertTrue(result.isFailure)
            assertArrayEquals(corrupt, vaultFile.readBytes())
            assertArrayEquals(lkgBefore, lkgFile(vaultFile).readBytes())
            assertDecryptableKdbx(lkgFile(vaultFile))
        }
    }

    @Test
    fun corruptActiveWithoutUsableLkg_doesNotAutoRepair() = runBlocking {
        val vaultFile = File.createTempFile("invalid-no-lkg-", ".kdbx")
        try {
            val corrupt = "invalid-active".toByteArray()
            vaultFile.writeBytes(corrupt)

            val result = runCatching { KotpassVaultRepository(vaultFile).openVault("test-password".toCharArray()) }

            assertTrue(result.isFailure)
            assertArrayEquals(corrupt, vaultFile.readBytes())
            assertFalse(lkgFile(vaultFile).exists())
        } finally {
            vaultFile.delete()
        }
    }

    @Test
    fun wrongPasswordWithLkg_remainsFailedOpenAndDoesNotChangeArtifacts() = runBlocking {
        withKnownVaultFixture { vaultFile ->
            val setup = openKnownVault(vaultFile)
            setup.createCredential(testCredential("B"))
            setup.lock()
            val activeBefore = vaultFile.readBytes()
            val lkgBefore = lkgFile(vaultFile).readBytes()

            val result = runCatching { KotpassVaultRepository(vaultFile).openVault("wrong-password".toCharArray()) }

            assertTrue(result.isFailure)
            assertArrayEquals(activeBefore, vaultFile.readBytes())
            assertArrayEquals(lkgBefore, lkgFile(vaultFile).readBytes())
        }
    }

    @Test
    fun lkgArtifact_isEncryptedKdbxAndDoesNotContainKnownPlaintextCredential() = runBlocking {
        withKnownVaultFixture { vaultFile ->
            val repository = openKnownVault(vaultFile)
            repository.createCredential(testCredential("B"))
            repository.lock()
            val lkgBytes = lkgFile(vaultFile).readBytes()

            assertTrue(lkgBytes.size > 8)
            assertFalse(String(lkgBytes).contains("fixture-password-1"))
            assertDecryptableKdbx(lkgFile(vaultFile))
        }
    }

    private suspend fun openKnownVault(vaultFile: File): KotpassVaultRepository {
        val repository = KotpassVaultRepository(vaultFile)
        repository.openVault("test-password".toCharArray())
        return repository
    }

    private suspend fun withKnownVaultFixture(block: suspend (File) -> Unit) {
        val vaultFile = File.createTempFile("known-vault-", ".kdbx")
        try {
            val fixture = javaClass.getResourceAsStream("/vault/known-vault.kdbx")
                ?: throw AssertionError("Known vault fixture is missing.")
            fixture.use { input ->
                vaultFile.outputStream().use { output -> input.copyTo(output) }
            }
            block(vaultFile)
        } finally {
            if (vaultFile.exists() && !vaultFile.delete()) {
                vaultFile.deleteOnExit()
            }
        }
    }

    private fun lkgFile(vaultFile: File): File =
        File(vaultFile.parentFile, "${vaultFile.nameWithoutExtension}.lkg.kdbx")

    private fun lkgFiles(vaultFile: File): List<File> = vaultFile.parentFile
        ?.listFiles { _, name -> name == lkgFile(vaultFile).name }
        ?.toList()
        .orEmpty()

    private fun testCredential(
        suffix: String,
        id: String = "323e4567-e89b-12d3-a456-426614174002"
    ): Credential = Credential(
        id = id,
        title = "T128 $suffix",
        username = "user-$suffix",
        password = "password-$suffix",
        url = "https://$suffix.example.test",
        notes = "T128 promotion test"
    )

    private fun assertDecryptableKdbx(file: File) {
        FileInputStream(file).use { input ->
            KeePassDatabase.decode(input, testCredentials())
        }
    }

    private fun testCredentials(): Credentials =
        Credentials.from(EncryptedValue.fromString("test-password"))

    private open class DelegatingFileOperations : VaultFileOperations {
        override fun createTempFile(prefix: String, suffix: String, directory: File): File =
            File.createTempFile(prefix, suffix, directory)

        override fun openInput(file: File): FileInputStream = FileInputStream(file)

        override fun openOutput(file: File): FileOutputStream = FileOutputStream(file)

        override fun move(source: File, destination: File): Boolean = source.renameTo(destination)

        override fun delete(file: File): Boolean = file.delete()
    }

    private class FailCandidateValidationOperations : DelegatingFileOperations() {
        override fun openInput(file: File): FileInputStream {
            if (file.name.startsWith("rahsa-vault-candidate-")) {
                throw IOException("Injected candidate validation failure.")
            }
            return super.openInput(file)
        }
    }

    private class FailCandidatePromotionOperations(
        private val target: File
    ) : DelegatingFileOperations() {
        var candidateWasRead = false
            private set
        private var failed = false

        override fun openInput(file: File): FileInputStream {
            if (file.name.startsWith("rahsa-vault-candidate-")) {
                candidateWasRead = true
            }
            return super.openInput(file)
        }

        override fun move(source: File, destination: File): Boolean {
            if (!failed && source.name.startsWith("rahsa-vault-candidate-") && destination == target) {
                failed = true
                return false
            }
            return super.move(source, destination)
        }
    }
}
