package com.yogeshpaliyal.keypass.ui.auth.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.keypass.R

@Composable
fun RecoveryAcknowledgment(
    acknowledged: Boolean,
    enabled: Boolean,
    onAcknowledged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actionLabel = stringResource(R.string.recovery_acknowledgment_swipe)
    val acknowledgmentState = stringResource(
        if (acknowledged) {
            R.string.recovery_acknowledgment_complete
        } else {
            R.string.recovery_acknowledgment_required
        }
    )
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd && enabled && !acknowledged) {
                onAcknowledged()
            }
            false
        }
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = stringResource(R.string.create_vault_recovery_warning),
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        SwipeToDismissBox(
            state = swipeState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .testTag(RECOVERY_ACKNOWLEDGMENT_TEST_TAG)
                .semantics {
                    stateDescription = acknowledgmentState
                    if (enabled && !acknowledged) {
                        customActions = listOf(
                            CustomAccessibilityAction(actionLabel) {
                                onAcknowledged()
                                true
                            }
                        )
                    }
                },
            enableDismissFromStartToEnd = enabled && !acknowledged,
            enableDismissFromEndToStart = false,
            gesturesEnabled = enabled && !acknowledged,
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = stringResource(R.string.recovery_acknowledgment_complete),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (acknowledged) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = if (acknowledged) acknowledgmentState else actionLabel,
                    modifier = Modifier.padding(16.dp),
                    color = if (acknowledged) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

private const val RECOVERY_ACKNOWLEDGMENT_TEST_TAG = "recoveryAcknowledgment"
