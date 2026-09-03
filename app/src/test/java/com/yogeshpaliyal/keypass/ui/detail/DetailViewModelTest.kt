package com.yogeshpaliyal.keypass.ui.detail

import com.yogeshpaliyal.common.data.AccountModel
import com.yogeshpaliyal.common.db.DbDao
import java.lang.reflect.Proxy
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DetailViewModelTest {

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
}
