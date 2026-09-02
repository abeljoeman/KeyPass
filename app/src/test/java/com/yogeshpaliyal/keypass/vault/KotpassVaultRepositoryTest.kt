package com.yogeshpaliyal.keypass.vault

import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class KotpassVaultRepositoryTest {

    @Test
    fun openVault_withKnownFixture_exposesExpectedCredentials() = runBlocking {
        val fixture = javaClass.getResourceAsStream("/vault/known-vault.kdbx")
            ?: throw AssertionError("Known vault fixture is missing.")
        val vaultFile = File.createTempFile("known-vault-", ".kdbx")

        try {
            fixture.use { input ->
                vaultFile.outputStream().use { output -> input.copyTo(output) }
            }

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
        } finally {
            if (vaultFile.exists() && !vaultFile.delete()) {
                vaultFile.deleteOnExit()
            }
        }
    }
}
