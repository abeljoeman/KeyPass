package com.yogeshpaliyal.keypass.ui.detail.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.keypass.R

@Composable
fun DeleteConfirmation(
    openDialog: Boolean,
    isDeleting: Boolean,
    updateDialogVisibility: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    if (openDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) {
                    updateDialogVisibility(false)
                }
            },
            title = {
                Text(text = stringResource(id = R.string.delete_account_title))
            },
            confirmButton = {
                TextButton(
                    modifier = Modifier.testTag("delete"),
                    enabled = !isDeleting,
                    onClick = {
                        onDelete()
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.error,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = stringResource(
                                if (isDeleting) R.string.credential_detail_deleting else R.string.delete
                            )
                        )
                    }
                }
            },
            text = {
                Text(text = stringResource(id = R.string.delete_account_msg))
            },
            dismissButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        updateDialogVisibility(false)
                    }
                ) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }
}
