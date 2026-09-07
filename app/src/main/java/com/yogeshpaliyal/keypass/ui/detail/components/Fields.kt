package com.yogeshpaliyal.keypass.ui.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.common.utils.PasswordGenerator
import com.yogeshpaliyal.keypass.R
import com.yogeshpaliyal.keypass.ui.commonComponents.PasswordTrailingIcon
import com.yogeshpaliyal.keypass.ui.nav.LocalUserSettings
import com.yogeshpaliyal.keypass.vault.Credential

private const val MAX_CREDENTIAL_TITLE_LENGTH = 100

@Composable
fun Fields(
    modifier: Modifier = Modifier,
    credential: Credential,
    isNewCredential: Boolean,
    isSaving: Boolean,
    updateCredential: (Credential) -> Unit,
    onOpenPasswordOptions: () -> Unit,
    onSaveClicked: () -> Unit
) {
    val passwordConfig = LocalUserSettings.current.passwordConfig
    var passwordVisible by remember(credential.id) { mutableStateOf(false) }
    var titleTouched by remember(credential.id) { mutableStateOf(false) }
    val titleInvalid = titleTouched && credential.title.isBlank()
    val saveEnabled = credential.title.isNotBlank() && !isSaving
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f),
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        cursorColor = MaterialTheme.colorScheme.primary
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("accountName"),
            value = credential.title,
            onValueChange = { value ->
                titleTouched = true
                updateCredential(
                    credential.copy(
                        title = value.take(MAX_CREDENTIAL_TITLE_LENGTH)
                    )
                )
            },
            label = { Text(stringResource(R.string.credential_editor_title_label)) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = fieldColors,
            isError = titleInvalid,
            supportingText = if (titleInvalid) {
                {
                    Text(stringResource(R.string.credential_editor_title_required))
                }
            } else {
                null
            }
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("username"),
            value = credential.username,
            onValueChange = {
                updateCredential(credential.copy(username = it))
            },
            label = { Text(stringResource(R.string.credential_editor_username_label)) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = fieldColors
        )

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password"),
                value = credential.password,
                onValueChange = {
                    updateCredential(credential.copy(password = it))
                },
                label = { Text(stringResource(R.string.password)) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = fieldColors,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    PasswordTrailingIcon(passwordVisible) {
                        passwordVisible = it
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    enabled = !isSaving,
                    onClick = {
                        updateCredential(
                            credential.copy(
                                password = PasswordGenerator(passwordConfig).generatePassword()
                            )
                        )
                    }
                ) {
                    Text(stringResource(R.string.credential_editor_generate))
                }
                TextButton(
                    enabled = !isSaving,
                    onClick = onOpenPasswordOptions
                ) {
                    Text(stringResource(R.string.credential_editor_options))
                }
            }
        }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("website"),
            value = credential.url.orEmpty(),
            onValueChange = {
                updateCredential(credential.copy(url = it))
            },
            label = { Text(stringResource(R.string.credential_editor_website_label)) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = fieldColors
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("notes"),
            value = credential.notes.orEmpty(),
            onValueChange = {
                updateCredential(credential.copy(notes = it))
            },
            label = { Text(stringResource(R.string.credential_editor_notes_label)) },
            minLines = 3,
            shape = MaterialTheme.shapes.medium,
            colors = fieldColors
        )

        Spacer(modifier = Modifier.size(2.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .testTag("save"),
            enabled = saveEnabled,
            shape = MaterialTheme.shapes.medium,
            onClick = onSaveClicked
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text(
                    text = when {
                        isSaving -> stringResource(R.string.credential_editor_saving)
                        isNewCredential -> stringResource(R.string.credential_editor_save_new)
                        else -> stringResource(R.string.credential_editor_save_changes)
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.size(12.dp))
    }
}
