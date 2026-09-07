package com.yogeshpaliyal.keypass.ui.changeMasterPassword

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.nulabinc.zxcvbn.Zxcvbn
import com.yogeshpaliyal.common.utils.finalizeMasterPasswordChange
import com.yogeshpaliyal.keypass.R
import com.yogeshpaliyal.keypass.ui.auth.components.RecoveryAcknowledgment
import com.yogeshpaliyal.keypass.ui.nav.LocalVaultFile
import com.yogeshpaliyal.keypass.ui.nav.LocalVaultRepository
import com.yogeshpaliyal.keypass.ui.nav.LocalUserSettings
import com.yogeshpaliyal.keypass.ui.redux.actions.GoBackAction
import com.yogeshpaliyal.keypass.ui.redux.actions.ToastAction
import com.yogeshpaliyal.keypass.vault.InvalidCurrentMasterPasswordException
import com.yogeshpaliyal.keypass.vault.MasterPasswordChangeCoordinator
import com.yogeshpaliyal.keypass.vault.MasterPasswordChangeFinalizer
import com.yogeshpaliyal.keypass.vault.MasterPasswordFinalizationResult
import com.yogeshpaliyal.keypass.vault.masterPasswordFinalizationMarker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.reduxkotlin.compose.rememberDispatcher

@Composable
fun ChangeMasterPasswordScreen() {
    val context = LocalContext.current.applicationContext
    val vaultFile = LocalVaultFile.current
    val vaultRepository = LocalVaultRepository.current
    val userSettings = LocalUserSettings.current
    val dispatch = rememberDispatcher()
    val coroutineScope = rememberCoroutineScope()
    val zxcvbn = remember { Zxcvbn() }
    val finalizer = remember(context, vaultFile) {
        MasterPasswordChangeFinalizer(
            activeVaultFile = vaultFile,
            markerFile = masterPasswordFinalizationMarker(context.filesDir),
            applyMetadata = { passwordHint, disableBiometric ->
                context.finalizeMasterPasswordChange(passwordHint, disableBiometric)
            }
        )
    }
    val coordinator = remember(vaultRepository, finalizer) {
        MasterPasswordChangeCoordinator(vaultRepository, finalizer)
    }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var passwordHint by remember { mutableStateOf(userSettings.passwordHint.orEmpty()) }
    var currentVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmationVisible by remember { mutableStateOf(false) }
    var acknowledged by remember { mutableStateOf(false) }
    var inProgress by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<Int?>(null) }

    val strengthScore = remember(newPassword) {
        if (newPassword.isEmpty()) 0 else runCatching { zxcvbn.measure(newPassword).score }.getOrDefault(0)
    }
    val actionEnabled = isChangeMasterPasswordActionEnabled(
        currentPassword = currentPassword,
        newPassword = newPassword,
        confirmation = confirmation,
        acknowledged = acknowledged,
        inProgress = inProgress
    )

    BackHandler(enabled = inProgress) {
        // Once started, Back is not a transaction cancel. The repository operation is non-cancellable.
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.change_master_password),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(20.dp))

        MasterPasswordField(
            label = R.string.current_master_password,
            value = currentPassword,
            onValueChange = {
                currentPassword = it
                errorMessage = null
            },
            visible = currentVisible,
            onVisibilityChange = { currentVisible = it },
            enabled = !inProgress,
            testTag = "currentMasterPassword"
        )
        Spacer(Modifier.height(12.dp))
        MasterPasswordField(
            label = R.string.new_master_password,
            value = newPassword,
            onValueChange = {
                newPassword = it
                errorMessage = null
            },
            visible = newVisible,
            onVisibilityChange = { newVisible = it },
            enabled = !inProgress,
            testTag = "newMasterPassword"
        )
        Text(
            text = stringResource(
                R.string.password_strength_advisory,
                stringResource(passwordStrengthLabel(strengthScore))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .testTag("passwordStrengthAdvisory"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        MasterPasswordField(
            label = R.string.confirm_new_master_password,
            value = confirmation,
            onValueChange = {
                confirmation = it
                errorMessage = null
            },
            visible = confirmationVisible,
            onVisibilityChange = { confirmationVisible = it },
            enabled = !inProgress,
            testTag = "confirmNewMasterPassword"
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = passwordHint,
            onValueChange = { passwordHint = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("masterPasswordHint"),
            enabled = !inProgress,
            singleLine = true,
            label = { Text(stringResource(R.string.master_password_hint_optional)) }
        )
        Spacer(Modifier.height(20.dp))
        RecoveryAcknowledgment(
            acknowledged = acknowledged,
            enabled = !inProgress,
            onAcknowledged = { acknowledged = true },
            warningText = R.string.change_master_password_warning
        )
        Spacer(Modifier.height(20.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("changeMasterPasswordAction"),
            enabled = actionEnabled,
            onClick = {
                if (inProgress) return@Button
                inProgress = true
                errorMessage = null
                val currentChars = currentPassword.toCharArray()
                val newChars = newPassword.toCharArray()
                currentPassword = ""
                newPassword = ""
                confirmation = ""
                acknowledged = false
                coroutineScope.launch {
                    try {
                        val result = coordinator.changeMasterPassword(
                            currentMasterPassword = currentChars,
                            newMasterPassword = newChars,
                            passwordHint = passwordHint.ifEmpty { null },
                            biometricWasEnabled = userSettings.isBiometricEnable
                        )
                        dispatch(ToastAction(R.string.password_change_success))
                        if (result == MasterPasswordFinalizationResult.BIOMETRIC_INVALIDATED) {
                            dispatch(ToastAction(R.string.biometric_invalidated_after_password_change))
                        }
                        dispatch(GoBackAction)
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: InvalidCurrentMasterPasswordException) {
                        errorMessage = R.string.current_master_password_incorrect
                    } catch (_: Exception) {
                        errorMessage = R.string.change_master_password_failed
                    } finally {
                        currentChars.fill('\u0000')
                        newChars.fill('\u0000')
                        inProgress = false
                    }
                }
            }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (inProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.size(8.dp))
                }
                Text(
                    stringResource(
                        if (inProgress) {
                            R.string.change_master_password_in_progress
                        } else {
                            R.string.change_master_password
                        }
                    )
                )
            }
        }

        errorMessage?.let { message ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(message),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("changeMasterPasswordError"),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MasterPasswordField(
    @StringRes label: Int,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onVisibilityChange: (Boolean) -> Unit,
    enabled: Boolean,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        enabled = enabled,
        singleLine = true,
        label = { Text(stringResource(label)) },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(enabled = enabled, onClick = { onVisibilityChange(!visible) }) {
                Icon(
                    imageVector = if (visible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                    contentDescription = stringResource(
                        if (visible) R.string.a11y_hide_password else R.string.a11y_show_password
                    )
                )
            }
        }
    )
}

internal fun isChangeMasterPasswordActionEnabled(
    currentPassword: String,
    newPassword: String,
    confirmation: String,
    acknowledged: Boolean,
    inProgress: Boolean
): Boolean = currentPassword.isNotEmpty() &&
    newPassword.isNotEmpty() &&
    confirmation == newPassword &&
    acknowledged &&
    !inProgress

@StringRes
internal fun passwordStrengthLabel(score: Int): Int = when (score.coerceIn(0, 4)) {
    0 -> R.string.password_strength_very_weak
    1 -> R.string.password_strength_weak
    2 -> R.string.password_strength_fair
    3 -> R.string.password_strength_strong
    else -> R.string.password_strength_very_strong
}
