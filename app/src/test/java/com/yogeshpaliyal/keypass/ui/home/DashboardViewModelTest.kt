package com.yogeshpaliyal.keypass.ui.home

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
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardViewModelTest {

    @Test
    fun clearSensitiveStateRejectsInFlightQueryResult() = runBlocking {
        val queryStarted = CountDownLatch(1)
        val releaseQuery = CountDownLatch(1)
        val sensitiveAccount = AccountModel(username = "user", password = "secret")
        var queryCount = 0
        val dao = fakeDao { methodName ->
            if (methodName == "getAllAccountsAscending") {
                queryCount++
                if (queryCount == 2) {
                    queryStarted.countDown()
                    assertTrue(releaseQuery.await(5, TimeUnit.SECONDS))
                }
                listOf(sensitiveAccount)
            } else {
                null
            }
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val viewModel = DashboardViewModel(dao, scope)

        try {
            viewModel.queryUpdated(null, null, null).join()
            assertEquals(listOf(sensitiveAccount), viewModel.accounts.value)

            val staleQuery = viewModel.queryUpdated(null, null, null)
            assertTrue(queryStarted.await(5, TimeUnit.SECONDS))

            viewModel.clearSensitiveState()
            assertTrue(viewModel.accounts.value.isEmpty())

            releaseQuery.countDown()
            staleQuery.join()
            assertTrue(viewModel.accounts.value.isEmpty())
        } finally {
            releaseQuery.countDown()
            scope.cancel()
        }
    }

    private fun fakeDao(result: (String) -> Any?): DbDao =
        Proxy.newProxyInstance(
            DbDao::class.java.classLoader,
            arrayOf(DbDao::class.java)
        ) { _, method, _ -> result(method.name) } as DbDao
}
