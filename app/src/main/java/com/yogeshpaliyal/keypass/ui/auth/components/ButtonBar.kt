package com.yogeshpaliyal.keypass.ui.auth.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    setPasswordError: (Int?) -> Unit,
    dispatchAction: (Action) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val (authenticationInProgress, setAuthenticationInProgress) =
        remember(state) { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth(1f), Arrangement.SpaceEvenly) {
        AnimatedVisibility(state is AuthState.ConfirmPassword) {
            Button(enabled = !authenticationInProgress, onClick = {
                dispatchAction(NavigationAction(AuthState.CreatePassword, true))
            }) {
                Text(text = stringResource(id = R.string.back))
            }
        }

        Button(onClick = {
            when (state) {
                is AuthState.CreatePassword -> {
                    if (password.isBlank()) {
                        setPasswordError(R.string.enter_password)
                    } else {
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
                                setPasswordError(R.string.vault_creation_failed)
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
        }, enabled = !authenticationInProgress) {
            Text(text = stringResource(id = R.string.str_continue))
        }
    }
}
