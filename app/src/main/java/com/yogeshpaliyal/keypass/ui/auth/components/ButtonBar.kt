package com.yogeshpaliyal.keypass.ui.auth.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.keypass.R
import com.yogeshpaliyal.keypass.ui.redux.actions.Action
import com.yogeshpaliyal.keypass.ui.redux.actions.NavigationAction
import com.yogeshpaliyal.keypass.ui.redux.states.AuthState
import com.yogeshpaliyal.keypass.ui.redux.states.HomeState
import com.yogeshpaliyal.keypass.vault.VaultRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun ButtonBar(
    state: AuthState,
    password: String,
    vaultRepository: VaultRepository,
    authenticationInProgress: Boolean,
    setAuthenticationInProgress: (Boolean) -> Unit,
    setPasswordError: (Int?) -> Unit,
    setActionError: (Int?) -> Unit,
    dispatchAction: (Action) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Button(
        modifier = Modifier.fillMaxWidth(),
        enabled = !authenticationInProgress,
        onClick = {
            setActionError(null)
            when (state) {
                is AuthState.CreatePassword -> {
                    if (password.isBlank()) {
                        setPasswordError(R.string.enter_password)
                    } else {
                        setPasswordError(null)
                        dispatchAction(NavigationAction(AuthState.ConfirmPassword(password)))
                    }
                }

                is AuthState.ConfirmPassword -> {
                    if (authenticationInProgress) {
                        return@Button
                    }
                    if (state.password == password) {
                        setAuthenticationInProgress(true)
                        setPasswordError(null)
                        coroutineScope.launch {
                            val masterPassword = password.toCharArray()
                            try {
                                vaultRepository.createVault(masterPassword)
                                dispatchAction(NavigationAction(HomeState(), true))
                            } catch (cancelled: CancellationException) {
                                throw cancelled
                            } catch (_: Exception) {
                                setActionError(R.string.vault_creation_failed)
                            } finally {
                                masterPassword.fill('\u0000')
                                setAuthenticationInProgress(false)
                            }
                        }
                    } else {
                        setPasswordError(R.string.password_no_match)
                    }
                }

                is AuthState.Login -> {
                    if (authenticationInProgress) {
                        return@Button
                    }
                    if (password.isBlank()) {
                        setPasswordError(R.string.enter_password)
                    } else {
                        setAuthenticationInProgress(true)
                        setPasswordError(null)
                        coroutineScope.launch {
                            val masterPassword = password.toCharArray()
                            try {
                                vaultRepository.openVault(masterPassword)
                                dispatchAction(NavigationAction(HomeState(), true))
                            } catch (cancelled: CancellationException) {
                                throw cancelled
                            } catch (_: Exception) {
                                setPasswordError(R.string.incorrect_password)
                            } finally {
                                masterPassword.fill('\u0000')
                                setAuthenticationInProgress(false)
                            }
                        }
                    }
                }
            }
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (authenticationInProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = stringResource(
                    when {
                        authenticationInProgress && state is AuthState.ConfirmPassword ->
                            R.string.creating_vault
                        authenticationInProgress && state is AuthState.Login ->
                            R.string.unlocking
                        state is AuthState.ConfirmPassword -> R.string.create_vault
                        state is AuthState.Login -> R.string.unlock
                        else -> R.string.str_continue
                    }
                )
            )
        }
    }
}
