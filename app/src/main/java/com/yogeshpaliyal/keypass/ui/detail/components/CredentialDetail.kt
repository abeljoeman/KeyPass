package com.yogeshpaliyal.keypass.ui.detail.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.keypass.R
import com.yogeshpaliyal.keypass.ui.commonComponents.PasswordTrailingIcon
import com.yogeshpaliyal.keypass.vault.Credential

private const val PASSWORD_MASK = "••••••••"

@Composable
fun CredentialDetail(
    credential: Credential,
    isSaving: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: (() -> Unit)?,
    onCopyToClipboard: (String) -> Unit
) {
    var passwordVisible by remember(credential.id) { mutableStateOf(false) }
    var deleteConfirmationVisible by remember(credential.id) { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val websiteUrl = credential.url?.let(::normalizeWebsiteUrl)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Text(
                        text = stringResource(R.string.credential_detail_screen_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = rememberVectorPainter(image = Icons.AutoMirrored.Rounded.ArrowBack),
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    TextButton(
                        enabled = !isSaving,
                        onClick = onEdit
                    ) {
                        Text(
                            text = stringResource(R.string.credential_detail_edit),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = credential.title.ifBlank {
                    stringResource(R.string.credential_detail_untitled)
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            CredentialDetailField(
                label = stringResource(R.string.credential_detail_username),
                value = credential.username,
                trailingContent = {
                    CopyCredentialValueButton(
                        value = credential.username,
                        onCopyToClipboard = onCopyToClipboard
                    )
                }
            )

            CredentialDetailField(
                label = stringResource(R.string.password),
                value = if (passwordVisible) {
                    credential.password
                } else if (credential.password.isNotEmpty()) {
                    PASSWORD_MASK
                } else {
                    ""
                },
                trailingContent = {
                    if (credential.password.isNotEmpty()) {
                        PasswordTrailingIcon(
                            passwordVisible = passwordVisible,
                            changePasswordVisibility = { passwordVisible = it }
                        )
                    }
                    CopyCredentialValueButton(
                        value = credential.password,
                        onCopyToClipboard = onCopyToClipboard
                    )
                }
            )

            credential.url?.takeIf { it.isNotBlank() }?.let { url ->
                CredentialDetailField(
                    label = stringResource(R.string.credential_detail_website),
                    value = url,
                    trailingContent = if (websiteUrl != null) {
                        {
                            TextButton(
                                onClick = {
                                    runCatching {
                                        uriHandler.openUri(websiteUrl)
                                    }
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.credential_detail_open),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    } else {
                        null
                    }
                )
            }

            credential.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                CredentialDetailField(
                    label = stringResource(R.string.credential_detail_notes),
                    value = notes,
                    maxLines = Int.MAX_VALUE
                )
            }

            if (onDelete != null) {
                TextButton(
                    enabled = !isSaving,
                    onClick = { deleteConfirmationVisible = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = stringResource(R.string.credential_detail_delete),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }

    if (onDelete != null) {
        DeleteConfirmation(
            openDialog = deleteConfirmationVisible,
            isDeleting = isSaving,
            updateDialogVisibility = { deleteConfirmationVisible = it },
            onDelete = onDelete
        )
    }
}

@Composable
private fun CredentialDetailField(
    label: String,
    value: String,
    maxLines: Int = 1,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = maxLines,
                overflow = if (maxLines == Int.MAX_VALUE) {
                    TextOverflow.Clip
                } else {
                    TextOverflow.Ellipsis
                }
            )
            trailingContent?.invoke()
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        )
    }
}

@Composable
private fun CopyCredentialValueButton(
    value: String,
    onCopyToClipboard: (String) -> Unit
) {
    IconButton(
        enabled = value.isNotEmpty(),
        onClick = { onCopyToClipboard(value) }
    ) {
        Icon(
            painter = rememberVectorPainter(image = Icons.Rounded.ContentCopy),
            contentDescription = stringResource(R.string.a11y_copy_to_clipboard),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun normalizeWebsiteUrl(rawUrl: String): String? {
    val trimmed = rawUrl.trim()
    if (trimmed.isEmpty()) {
        return null
    }

    val candidate = if ("://" in trimmed) {
        trimmed
    } else {
        "https://$trimmed"
    }
    val uri = Uri.parse(candidate)
    val scheme = uri.scheme?.lowercase()

    if (scheme != "http" && scheme != "https") {
        return null
    }
    if (uri.host.isNullOrBlank()) {
        return null
    }

    return candidate
}
