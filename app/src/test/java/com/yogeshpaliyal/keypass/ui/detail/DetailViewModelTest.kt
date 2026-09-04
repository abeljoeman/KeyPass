package com.yogeshpaliyal.keypass.ui.detail

import com.yogeshpaliyal.common.data.AccountModel
import com.yogeshpaliyal.common.db.DbDao
import com.yogeshpaliyal.keypass.vault.Credential
import com.yogeshpaliyal.keypass.vault.VaultRepository
import java.lang.reflect.Proxy
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
    fun createCredentialWritesPrototypeCredentialToVault() = runBlocking {
        var createdCredential: Credential? = null
        var completionCalled = false
        val repository = FakeVaultRepository { credential ->
            createdCredential = credential
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val viewModel = DetailViewModel(fakeDao { null }, scope)
        val draft = Credential(
            id = "",
            title = "Example Account",
            username = "alice@example.com",
            password = "correct horse battery staple",
            url = "https://example.com/login",
            notes = "Recovery codes stored elsewhere"
        )

        try {
            viewModel.createCredential(repository, draft) {
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
    fun clearSensitiveStateRejectsInFlightLoadResult() = runBlocking {
        val loadStarted = CountDownLatch(1)
        val releaseLoad = CountDownLatch(1)
        val sensitiveAccount = AccountModel(id = 1, username = "user", password = "secret")
        var loadCount = 0
        val dao = fakeDao { methodName ->
            if (methodName == "getAccount") {
                loadCount++
                if (loadCount == 2) {
                    loadStarted.countDown()
                    assertTrue(releaseLoad.await(5, TimeUnit.SECONDS))
                }
                sensitiveAccount
            } else {
                null
            }
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val viewModel = DetailViewModel(dao, scope)

        try {
            viewModel.loadAccount(sensitiveAccount.id).join()
            assertEquals(sensitiveAccount, viewModel.accountModel.value)

            val staleLoad = viewModel.loadAccount(sensitiveAccount.id)
            assertTrue(loadStarted.await(5, TimeUnit.SECONDS))

            viewModel.clearSensitiveState()
            assertNull(viewModel.accountModel.value.username)
            assertNull(viewModel.accountModel.value.password)

            releaseLoad.countDown()
            staleLoad.join()
            assertNull(viewModel.accountModel.value.username)
            assertNull(viewModel.accountModel.value.password)
        } finally {
            releaseLoad.countDown()
            scope.cancel()
        }
    }

    private fun fakeDao(result: (String) -> Any?): DbDao =
        Proxy.newProxyInstance(
            DbDao::class.java.classLoader,
            arrayOf(DbDao::class.java)
        ) { _, method, _ -> result(method.name) } as DbDao

    private class FakeVaultRepository(
        private val onCreate: suspend (Credential) -> Unit
    ) : VaultRepository {
        override suspend fun createVault(masterPassword: CharArray) = unused()
        override suspend fun openVault(masterPassword: CharArray) = unused()
        override fun lock() = Unit
        override suspend fun listCredentials(): List<Credential> = unused()
        override suspend fun createCredential(credential: Credential) = onCreate(credential)
        override suspend fun updateCredential(credential: Credential) = unused()
        override suspend fun deleteCredential(id: String) = unused()
        override suspend fun searchCredentials(query: String): List<Credential> = unused()

        private fun unused(): Nothing = error("Not used by DetailViewModelTest")
    }
}
