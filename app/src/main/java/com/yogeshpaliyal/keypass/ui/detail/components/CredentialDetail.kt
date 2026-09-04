package com.yogeshpaliyal.keypass.ui.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.keypass.R
import com.yogeshpaliyal.keypass.ui.commonComponents.PasswordTrailingIcon
import com.yogeshpaliyal.keypass.vault.Credential

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CredentialDetail(
    credential: Credential,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: (() -> Unit)?,
    onCopyToClipboard: (String) -> Unit
) {
    var passwordVisible by rememberSaveable(credential.id) { mutableStateOf(false) }
    var deleteConfirmationVisible by rememberSaveable(credential.id) { mutableStateOf(false) }
    val fallbackTitle = stringResource(R.string.edit_account)

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Unspecified,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Text(text = credential.title.ifBlank { fallbackTitle })
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = rememberVectorPainter(image = Icons.Rounded.ArrowBackIosNew),
                            contentDescription = "Go Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(
                            painter = rememberVectorPainter(image = Icons.Rounded.Edit),
                            contentDescription = stringResource(R.string.edit_account)
                        )
                    }
                    if (onDelete != null) {
                        IconButton(onClick = { deleteConfirmationVisible = true }) {
                            Icon(
                                painter = rememberVectorPainter(image = Icons.Rounded.Delete),
                                contentDescription = stringResource(R.string.delete)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CredentialDetailField(
                label = stringResource(R.string.account_name),
                value = credential.title
            )
            CredentialDetailField(
                label = stringResource(R.string.username_email_phone),
                value = credential.username,
                onCopy = { onCopyToClipboard(credential.username) }
            )
            CredentialDetailField(
                label = stringResource(R.string.password),
                value = credential.password,
                password = true,
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = it },
                onCopy = { onCopyToClipboard(credential.password) }
            )
            credential.url?.takeIf { it.isNotBlank() }?.let { url ->
                CredentialDetailField(
                    label = stringResource(R.string.website_url_optional),
                    value = url
                )
            }
            credential.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                CredentialDetailField(
                    label = stringResource(R.string.notes_optional),
                    value = notes,
                    singleLine = false
                )
            }
        }
    }

    if (onDelete != null) {
        DeleteConfirmation(
            openDialog = deleteConfirmationVisible,
            updateDialogVisibility = { deleteConfirmationVisible = it },
            onDelete = onDelete
        )
    }
}

@Composable
private fun CredentialDetailField(
    label: String,
    value: String,
    singleLine: Boolean = true,
    password: Boolean = false,
    passwordVisible: Boolean = true,
    onPasswordVisibilityChange: (Boolean) -> Unit = {},
    onCopy: (() -> Unit)? = null
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = {},
        readOnly = true,
        singleLine = singleLine,
        label = { Text(label) },
        visualTransformation = if (password && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = if (password || onCopy != null) {
            {
                Row {
                    if (password) {
                        PasswordTrailingIcon(
                            passwordVisible = passwordVisible,
                            changePasswordVisibility = onPasswordVisibilityChange
                        )
                    }
                    if (onCopy != null) {
                        IconButton(onClick = onCopy) {
                            Icon(
                                painter = rememberVectorPainter(image = Icons.Rounded.ContentCopy),
                                contentDescription = "Copy To Clipboard"
                            )
                        }
                    }
                }
            }
        } else {
            null
        }
    )
}
