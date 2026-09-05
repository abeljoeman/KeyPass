package com.yogeshpaliyal.keypass.ui.detail.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import com.yogeshpaliyal.keypass.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomBar(
    isNewCredential: Boolean,
    backPressed: () -> Unit,
    onDeleteAccount: (() -> Unit)?,
    openPasswordConfiguration: () -> Unit,
) {
    val openDialog = remember { mutableStateOf(false) }

    LargeFlexibleTopAppBar(
        title = {
            Text(
                text = stringResource(if (isNewCredential) R.string.create_account else R.string.edit_account),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = {
            IconButton(onClick = backPressed) {
                Icon(
                    painter = rememberVectorPainter(image = Icons.Rounded.ArrowBackIosNew),
                    contentDescription = "Go Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {

            IconButton(
                modifier = Modifier.testTag("action_configure_password"),
                onClick = { openPasswordConfiguration() }
            ) {
                Icon(
                    painter = rememberVectorPainter(image = Icons.Default.Password),
                    contentDescription = "Open Password Configuration",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            if (!isNewCredential && onDeleteAccount != null) {
                IconButton(
                    modifier = Modifier.testTag("action_delete"),
                    onClick = { openDialog.value = true }
                ) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Rounded.Delete),
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    )

    if (onDeleteAccount != null) {
        DeleteConfirmation(
            openDialog.value,
            updateDialogVisibility = {
                openDialog.value = it
            },
            onDeleteAccount
        )
    }
}

@Composable
fun FABAddAccount(
    enabled: Boolean,
    onSaveClicked: () -> Unit
) {
    val modifier = Modifier
        .testTag("save")
        .alpha(if (enabled) 1f else 0.5f)
        .semantics {
            if (!enabled) {
                disabled()
            }
        }

    FloatingActionButton(
        modifier = modifier,
        onClick = {
            if (enabled) {
                onSaveClicked()
            }
        }
    ) {
        Icon(
            painter = rememberVectorPainter(image = Icons.Rounded.Done),
            contentDescription = "Save Changes"
        )
    }
}
