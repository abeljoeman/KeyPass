package com.yogeshpaliyal.keypass.ui.home

import com.yogeshpaliyal.keypass.vault.Credential
import com.yogeshpaliyal.keypass.vault.VaultRepository
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardViewModelTest {

    @Test
    fun loadCredentialsReadsVaultAndAppliesRequestedSort() = runBlocking {
        val repository = FakeVaultRepository {
            listOf(
                credential(id = "2", title = "Zulu", username = "alice"),
                credential(id = "1", title = "Alpha", username = "zoe")
            )
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val viewModel = DashboardViewModel(repository, scope)

        try {
            viewModel.loadCredentials(SortingField.Title.value, sortAscending = true).join()
            assertEquals(
                listOf("Alpha", "Zulu"),
                viewModel.credentials.value.map(Credential::title)
            )

            viewModel.loadCredentials(SortingField.Username.value, sortAscending = false).join()
            assertEquals(
                listOf("zoe", "alice"),
                viewModel.credentials.value.map(Credential::username)
            )
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun clearSensitiveStateRejectsInFlightVaultResult() = runBlocking {
        val queryStarted = CountDownLatch(1)
        val releaseQuery = CountDownLatch(1)
        val sensitiveCredential = credential(
            id = "credential-id",
            title = "Account",
            username = "user",
            password = "secret"
        )
        var queryCount = 0
        val repository = FakeVaultRepository {
            queryCount++
            if (queryCount == 2) {
                queryStarted.countDown()
                assertTrue(releaseQuery.await(5, TimeUnit.SECONDS))
            }
            listOf(sensitiveCredential)
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val viewModel = DashboardViewModel(repository, scope)

        try {
            viewModel.loadCredentials(null).join()
            assertEquals(listOf(sensitiveCredential), viewModel.credentials.value)

            val staleQuery = viewModel.loadCredentials(null)
            assertTrue(queryStarted.await(5, TimeUnit.SECONDS))

            viewModel.clearSensitiveState()
            assertTrue(viewModel.credentials.value.isEmpty())

            releaseQuery.countDown()
            staleQuery.join()
            assertTrue(viewModel.credentials.value.isEmpty())
        } finally {
            releaseQuery.countDown()
            scope.cancel()
        }
    }

    private fun credential(
        id: String,
        title: String,
        username: String,
        password: String = "password"
    ) = Credential(
        id = id,
        title = title,
        username = username,
        password = password,
        url = null,
        notes = null
    )

    private class FakeVaultRepository(
        private val listBlock: suspend () -> List<Credential>
    ) : VaultRepository {
        override suspend fun createVault(masterPassword: CharArray) = unused()
        override suspend fun openVault(masterPassword: CharArray) = unused()
        override fun lock() = Unit
        override suspend fun listCredentials(): List<Credential> = listBlock()
        override suspend fun createCredential(credential: Credential) = unused()
        override suspend fun updateCredential(credential: Credential) = unused()
        override suspend fun deleteCredential(id: String) = unused()
        override suspend fun searchCredentials(query: String): List<Credential> = unused()

        private fun unused(): Nothing = error("Not used by DashboardViewModelTest")
    }
}
