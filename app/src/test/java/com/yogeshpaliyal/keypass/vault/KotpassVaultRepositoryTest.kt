package com.yogeshpaliyal.keypass.vault

import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
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
                credentialAccess.exceptionOrNull() is IllegalStateException
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
                credentialAccess.exceptionOrNull() is IllegalStateException
            )
        } finally {
            if (vaultFile.exists() && !vaultFile.delete()) {
                vaultFile.deleteOnExit()
            }
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
}
