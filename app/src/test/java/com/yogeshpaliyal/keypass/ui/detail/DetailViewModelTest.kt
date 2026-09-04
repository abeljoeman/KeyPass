package com.yogeshpaliyal.keypass.ui.detail

import com.yogeshpaliyal.keypass.vault.Credential
import com.yogeshpaliyal.keypass.vault.VaultRepository
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DetailViewModelTest {

    @Test
    fun loadCredentialReadsRequestedVaultCredential() = runBlocking {
        val expected = credential(id = "credential-2", title = "Second")
        val repository = FakeVaultRepository(
            listBlock = {
                listOf(
                    credential(id = "credential-1", title = "First"),
                    expected
                )
            }
        )
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val viewModel = DetailViewModel(repository, scope)

        try {
            viewModel.loadCredential(expected.id).join()
            assertEquals(expected, viewModel.credential.value)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun createCredentialWritesPrototypeCredentialToVault() = runBlocking {
        var createdCredential: Credential? = null
        var completionCalled = false
        val repository = FakeVaultRepository(
            createBlock = { credential ->
                createdCredential = credential
            }
        )
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val viewModel = DetailViewModel(repository, scope)
        val draft = credential(
            id = "",
            title = "Example Account",
            username = "alice@example.com",
            password = "correct horse battery staple",
            url = "https://example.com/login",
            notes = "Recovery codes stored elsewhere"
        )

        try {
            viewModel.createCredential(draft) {
                completionCalled = true
            }.join()

            val created = requireNotNull(createdCredential)
            UUID.fromString(created.id)
            assertNotEquals("", created.id)
            assertEquals(draft.title, created.title)
            assertEquals(draft.username, created.username)
            assertEquals(draft.password, created.password)
            assertEquals(draft.url, created.url)
            assertEquals(draft.notes, created.notes)
            assertTrue(completionCalled)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun updateCredentialWritesSameVaultCredentialId() = runBlocking {
        var updatedCredential: Credential? = null
        var completionCalled = false
        val repository = FakeVaultRepository(
            updateBlock = { credential ->
                updatedCredential = credential
            }
        )
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val viewModel = DetailViewModel(repository, scope)
        val edited = credential(
            id = "00000000-0000-0000-0000-000000000123",
            title = "Updated",
            username = "updated-user"
        )

        try {
            viewModel.updateCredential(edited) {
                completionCalled = true
            }.join()

            assertEquals(edited, updatedCredential)
            assertTrue(completionCalled)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun clearSensitiveStateRejectsInFlightVaultLoadResult() = runBlocking {
        val loadStarted = CountDownLatch(1)
        val releaseLoad = CountDownLatch(1)
        val sensitiveCredential = credential(
            id = "credential-id",
            title = "Account",
            username = "user",
            password = "secret"
        )
        var loadCount = 0
        val repository = FakeVaultRepository(
            listBlock = {
                loadCount++
                if (loadCount == 2) {
                    loadStarted.countDown()
                    assertTrue(releaseLoad.await(5, TimeUnit.SECONDS))
                }
                listOf(sensitiveCredential)
            }
        )
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val viewModel = DetailViewModel(repository, scope)

        try {
            viewModel.loadCredential(sensitiveCredential.id).join()
            assertEquals(sensitiveCredential, viewModel.credential.value)

            val staleLoad = viewModel.loadCredential(sensitiveCredential.id)
            assertTrue(loadStarted.await(5, TimeUnit.SECONDS))

            viewModel.clearSensitiveState()
            assertNull(viewModel.credential.value)

            releaseLoad.countDown()
            staleLoad.join()
            assertNull(viewModel.credential.value)
        } finally {
            releaseLoad.countDown()
            scope.cancel()
        }
    }

    private fun credential(
        id: String,
        title: String,
        username: String = "user",
        password: String = "password",
        url: String? = null,
        notes: String? = null
    ) = Credential(
        id = id,
        title = title,
        username = username,
        password = password,
        url = url,
        notes = notes
    )

    private class FakeVaultRepository(
        private val listBlock: suspend () -> List<Credential> = { error("Not used by DetailViewModelTest") },
        private val createBlock: suspend (Credential) -> Unit = { error("Not used by DetailViewModelTest") },
        private val updateBlock: suspend (Credential) -> Unit = { error("Not used by DetailViewModelTest") }
    ) : VaultRepository {
        override suspend fun createVault(masterPassword: CharArray) = unused()
        override suspend fun openVault(masterPassword: CharArray) = unused()
        override fun lock() = Unit
        override suspend fun listCredentials(): List<Credential> = listBlock()
        override suspend fun createCredential(credential: Credential) = createBlock(credential)
        override suspend fun updateCredential(credential: Credential) = updateBlock(credential)
        override suspend fun deleteCredential(id: String) = unused()
        override suspend fun searchCredentials(query: String): List<Credential> = unused()

        private fun unused(): Nothing = error("Not used by DetailViewModelTest")
    }
}
