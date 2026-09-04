package com.yogeshpaliyal.keypass.ui.nav

import com.yogeshpaliyal.keypass.ui.redux.KeyPassRedux
import com.yogeshpaliyal.keypass.ui.redux.actions.Action
import com.yogeshpaliyal.keypass.ui.redux.actions.BatchActions
import com.yogeshpaliyal.keypass.ui.redux.actions.GoBackAction
import com.yogeshpaliyal.keypass.ui.redux.actions.NavigationAction
import com.yogeshpaliyal.keypass.ui.redux.actions.UpdateViewModalAction
import com.yogeshpaliyal.keypass.ui.redux.states.AccountDetailState
import com.yogeshpaliyal.keypass.ui.redux.states.AuthState
import com.yogeshpaliyal.keypass.ui.redux.states.HomeState
import com.yogeshpaliyal.keypass.vault.Credential
import com.yogeshpaliyal.keypass.vault.VaultRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackgroundAutoLockTest {

    @Test
    fun genuineBackgroundFromUnlockedContextClearsLocksAndNavigatesFailClosed() {
        val events = mutableListOf<String>()
        val repository = RecordingVaultRepository { events += "lock" }
        var dispatchedAction: Action? = null

        val handled = handleBackgroundAutoLock(
            autoLockEnabled = true,
            isUnlockedContext = true,
            isKnownActivityLaunch = { false },
            clearDashboardSensitiveState = { events += "clear" },
            vaultRepository = repository,
            dispatchAction = {
                events += "dispatch"
                dispatchedAction = it
            }
        )

        assertTrue(handled)
        assertEquals(listOf("clear", "lock", "dispatch"), events)
        assertEquals(1, repository.lockCount)

        val actions = (dispatchedAction as BatchActions).actions
        val releaseAction = actions[0] as UpdateViewModalAction
        val navigationAction = actions[1] as NavigationAction
        assertNull(releaseAction.viewModal)
        assertTrue(navigationAction.state is AuthState.Login)
        assertTrue(navigationAction.clearBackStack)
    }

    @Test
    fun knownInternalActivityLaunchDoesNotLockOrClearState() {
        val repository = RecordingVaultRepository()
        var cleared = false
        var dispatched = false

        val handled = handleBackgroundAutoLock(
            autoLockEnabled = true,
            isUnlockedContext = true,
            isKnownActivityLaunch = { true },
            clearDashboardSensitiveState = { cleared = true },
            vaultRepository = repository,
            dispatchAction = { dispatched = true }
        )

        assertFalse(handled)
        assertFalse(cleared)
        assertEquals(0, repository.lockCount)
        assertFalse(dispatched)
    }

    @Test
    fun backgroundLockClearsCredentialNavigationBackStack() {
        val store = KeyPassRedux.createStore()
        store.dispatch(NavigationAction(AuthState.Login, clearBackStack = true))
        store.dispatch(NavigationAction(HomeState()))
        store.dispatch(NavigationAction(AccountDetailState(accountId = "00000000-0000-0000-0000-000000000001")))

        handleBackgroundAutoLock(
            autoLockEnabled = true,
            isUnlockedContext = true,
            isKnownActivityLaunch = { false },
            clearDashboardSensitiveState = {},
            vaultRepository = RecordingVaultRepository(),
            dispatchAction = { store.dispatch(it) }
        )
        store.dispatch(GoBackAction)

        assertTrue(store.state.currentScreen is AuthState.Login)
        assertTrue(store.state.systemBackPress)
    }

    @Test
    fun disabledOrAlreadyLockedContextDoesNotConsumeLaunchGuardOrLock() {
        val repository = RecordingVaultRepository()
        var guardChecks = 0

        val disabledHandled = handleBackgroundAutoLock(
            autoLockEnabled = false,
            isUnlockedContext = true,
            isKnownActivityLaunch = { guardChecks++; false },
            clearDashboardSensitiveState = {},
            vaultRepository = repository,
            dispatchAction = {}
        )
        val lockedHandled = handleBackgroundAutoLock(
            autoLockEnabled = true,
            isUnlockedContext = false,
            isKnownActivityLaunch = { guardChecks++; false },
            clearDashboardSensitiveState = {},
            vaultRepository = repository,
            dispatchAction = {}
        )

        assertFalse(disabledHandled)
        assertFalse(lockedHandled)
        assertEquals(0, guardChecks)
        assertEquals(0, repository.lockCount)
    }

    private class RecordingVaultRepository(
        private val onLock: () -> Unit = {}
    ) : VaultRepository {
        var lockCount = 0
            private set

        override suspend fun createVault(masterPassword: CharArray): Unit = unsupported()
        override suspend fun openVault(masterPassword: CharArray): Unit = unsupported()

        override fun lock() {
            lockCount++
            onLock()
        }

        override suspend fun listCredentials(): List<Credential> = unsupported()
        override suspend fun createCredential(credential: Credential): Unit = unsupported()
        override suspend fun updateCredential(credential: Credential): Unit = unsupported()
        override suspend fun deleteCredential(id: String): Unit = unsupported()
        override suspend fun searchCredentials(query: String): List<Credential> = unsupported()

        private fun <T> unsupported(): T = throw UnsupportedOperationException()
    }
}
