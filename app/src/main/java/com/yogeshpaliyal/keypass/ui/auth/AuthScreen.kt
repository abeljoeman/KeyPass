package com.yogeshpaliyal.keypass.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.keypass.R
import com.yogeshpaliyal.keypass.ui.auth.components.ButtonBar
import com.yogeshpaliyal.keypass.ui.auth.components.PasswordInputField
import com.yogeshpaliyal.keypass.ui.nav.LocalVaultFile
import com.yogeshpaliyal.keypass.ui.nav.LocalVaultRepository
import com.yogeshpaliyal.keypass.ui.nav.LocalUserSettings
import com.yogeshpaliyal.keypass.ui.redux.actions.NavigationAction
import com.yogeshpaliyal.keypass.ui.redux.actions.ToastActionStr
import com.yogeshpaliyal.keypass.ui.redux.states.AuthState
import org.reduxkotlin.compose.rememberDispatcher

/**
 * Displays the authentication screen and manages user input and navigation based on the current authentication state.
 *
 * Renders the appropriate UI for login, password creation, or password confirmation, handling state transitions, input validation, and navigation actions according to the provided [state].
 *
 * @param state The current authentication state determining which UI and logic to display.
 */
@Composable
fun AuthScreen(state: AuthState) {
    val userSettings = LocalUserSettings.current
    val vaultFile = LocalVaultFile.current
    val vaultRepository = LocalVaultRepository.current
    val dispatchAction = rememberDispatcher()
    val passwordHint = if (state is AuthState.Login) userSettings.passwordHint else null

    val password = remember { mutableStateOf("") }
    val passwordVisible = rememberSaveable { mutableStateOf(false) }
    val passwordError = rememberSaveable { mutableStateOf<Int?>(null) }
    val actionError = rememberSaveable { mutableStateOf<Int?>(null) }
    val authenticationInProgress = remember(state) { mutableStateOf(false) }

    BackHandler(
        enabled = state is AuthState.ConfirmPassword && !authenticationInProgress.value
    ) {
        dispatchAction(NavigationAction(AuthState.CreatePassword, true))
    }

    LaunchedEffect(state) {
        if (state is AuthState.ConfirmPassword) {
            password.value = ""
            passwordVisible.value = false
        }
        passwordError.value = null
        actionError.value = null
    }

    LaunchedEffect(key1 = vaultFile) {
        if (vaultFile.isFile) {
            dispatchAction(NavigationAction(AuthState.Login, true))
        } else {
            dispatchAction(NavigationAction(AuthState.CreatePassword, true))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state is AuthState.ConfirmPassword) {
            IconButton(
                modifier = Modifier.align(Alignment.Start),
                enabled = !authenticationInProgress.value,
                onClick = {
                    dispatchAction(NavigationAction(AuthState.CreatePassword, true))
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        } else {
            Spacer(modifier = Modifier.height(32.dp))
            Image(
                modifier = Modifier.size(72.dp),
                painter = painterResource(R.drawable.logo_rahsa),
                contentDescription = null
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(if (state is AuthState.ConfirmPassword) 24.dp else 32.dp))

        Text(
            text = when (state) {
                is AuthState.CreatePassword ->
                    stringResource(R.string.welcome_to_product, stringResource(R.string.app_name))
                is AuthState.ConfirmPassword -> stringResource(R.string.confirm_password)
                is AuthState.Login -> stringResource(R.string.unlock_your_vault)
                else -> stringResource(state.title)
            },
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(
                when (state) {
                    is AuthState.CreatePassword -> R.string.create_master_password_description
                    is AuthState.ConfirmPassword -> R.string.confirm_password_description
                    is AuthState.Login -> R.string.unlock_vault_description
                    else -> state.description ?: state.title
                }
            ),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        PasswordInputField(
            label = if (state is AuthState.ConfirmPassword) {
                R.string.confirm_password
            } else {
                R.string.master_password
            },
            password = password.value,
            setPassword = {
                password.value = it
                passwordError.value = null
                actionError.value = null
            },
            passwordVisible = passwordVisible.value,
            setPasswordVisible = { passwordVisible.value = it },
            passwordError = passwordError.value,
            enabled = !authenticationInProgress.value
        )

        if (passwordHint != null) {
            TextButton(
                modifier = Modifier.align(Alignment.Start),
                enabled = !authenticationInProgress.value,
                onClick = {
                    dispatchAction(ToastActionStr(passwordHint))
                }
            ) {
                Text(stringResource(R.string.show_password_hint))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ButtonBar(
            state = state,
            password = password.value,
            vaultRepository = vaultRepository,
            authenticationInProgress = authenticationInProgress.value,
            setAuthenticationInProgress = { authenticationInProgress.value = it },
            setPasswordError = { passwordError.value = it },
            setActionError = { actionError.value = it }
        ) {
            dispatchAction(it)
        }

        actionError.value?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(error),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
