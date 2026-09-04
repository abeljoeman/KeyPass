package com.yogeshpaliyal.keypass.ui.nav

import com.yogeshpaliyal.keypass.ui.redux.actions.Action
import com.yogeshpaliyal.keypass.ui.redux.actions.BatchActions
import com.yogeshpaliyal.keypass.ui.redux.actions.NavigationAction
import com.yogeshpaliyal.keypass.ui.redux.actions.UpdateViewModalAction
import com.yogeshpaliyal.keypass.ui.redux.states.AuthState
import com.yogeshpaliyal.keypass.vault.VaultRepository

internal fun handleBackgroundAutoLock(
    autoLockEnabled: Boolean,
    isUnlockedContext: Boolean,
    isKnownActivityLaunch: () -> Boolean,
    clearDashboardSensitiveState: () -> Unit,
    vaultRepository: VaultRepository,
    dispatchAction: (Action) -> Unit
): Boolean {
    if (!autoLockEnabled || !isUnlockedContext || isKnownActivityLaunch()) {
        return false
    }

    clearDashboardSensitiveState()
    vaultRepository.lock()
    dispatchAction(
        BatchActions(
            UpdateViewModalAction(null),
            NavigationAction(AuthState.Login, clearBackStack = true)
        )
    )
    return true
}
