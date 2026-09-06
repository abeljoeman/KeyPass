package com.yogeshpaliyal.keypass.ui.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yogeshpaliyal.keypass.R
import com.yogeshpaliyal.keypass.ui.detail.components.BottomBar
import com.yogeshpaliyal.keypass.ui.detail.components.CredentialDetail
import com.yogeshpaliyal.keypass.ui.detail.components.Fields
import com.yogeshpaliyal.keypass.ui.generate.ui.GeneratePasswordScreen
import com.yogeshpaliyal.keypass.ui.nav.LocalVaultRepository
import com.yogeshpaliyal.keypass.ui.redux.actions.CopyToClipboard
import com.yogeshpaliyal.keypass.ui.redux.actions.GoBackAction
import com.yogeshpaliyal.keypass.ui.redux.actions.StateUpdateAction
import com.yogeshpaliyal.keypass.ui.redux.states.AccountDetailState
import org.reduxkotlin.compose.rememberDispatcher

/*
* @author Yogesh Paliyal
* yogeshpaliyal.foss@gmail.com
* https://techpaliyal.com
* created on 31-01-2021 10:38
*/

@Composable
fun AccountDetailPage(id: String?) {
    val dispatchAction = rememberDispatcher()
    val vaultRepository = LocalVaultRepository.current
    val viewModelFactory = remember(vaultRepository) {
        DetailViewModel.Factory(vaultRepository)
    }
    val viewModel: DetailViewModel = viewModel(factory = viewModelFactory)
    val credentialState by viewModel.credential.collectAsState()
    val operationError by viewModel.operationError.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val isNewCredential = id == null
    var showEditor by rememberSaveable(id) { mutableStateOf(isNewCredential) }
    var showPasswordOptions by remember(id) { mutableStateOf(false) }
    var showDiscardConfirmation by remember(id) { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val vaultWriteFailedMessage = stringResource(R.string.vault_write_failed)

    LaunchedEffect(operationError) {
        if (operationError == DetailOperationError.VaultWriteFailed) {
            snackbarHostState.showSnackbar(vaultWriteFailedMessage)
            viewModel.clearOperationError()
        }
    }

    LaunchedEffect(key1 = id) {
        viewModel.loadCredential(id)
    }

    DisposableEffect(viewModel) {
        onDispose(viewModel::clearSensitiveState)
    }

    val goBack: () -> Unit = {
        dispatchAction(GoBackAction)
    }
    val copyToClipboard: (String) -> Unit = { value ->
        dispatchAction(CopyToClipboard(value))
    }

    val credential = credentialState ?: return

    val exitEditor: () -> Unit = {
        if (isNewCredential) {
            goBack()
        } else {
            viewModel.cancelEdit()
            showEditor = false
        }
    }
    val requestEditorExit: () -> Unit = {
        if (viewModel.hasUnsavedChanges(isNewCredential)) {
            showDiscardConfirmation = true
        } else {
            exitEditor()
        }
    }

    BackHandler(enabled = showEditor && !showPasswordOptions) {
        if (!isSaving) {
            requestEditorExit()
        }
    }

    if (!isNewCredential && !showEditor) {
        BackHandler(enabled = isSaving) { }
        Box(modifier = Modifier.fillMaxSize()) {
            CredentialDetail(
                credential = credential,
                isSaving = isSaving,
                onBack = goBack,
                onEdit = {
                    viewModel.beginEdit()
                    showEditor = true
                },
                onDelete = {
                    viewModel.deleteCredential(
                        id = credential.id,
                        onExecCompleted = goBack
                    )
                },
                onCopyToClipboard = copyToClipboard
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        return
    }

    Scaffold(
        topBar = {
            BottomBar(
                isNewCredential = isNewCredential,
                backPressed = requestEditorExit
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Surface(modifier = Modifier.padding(paddingValues)) {
            Fields(
                credential = credential,
                isNewCredential = isNewCredential,
                isSaving = isSaving,
                updateCredential = viewModel::setCredential,
                onOpenPasswordOptions = {
                    showPasswordOptions = true
                },
                onSaveClicked = {
                    if (isNewCredential) {
                        viewModel.createCredential(
                            credential = credential,
                            onExecCompleted = { createdId ->
                                dispatchAction(
                                    StateUpdateAction(
                                        AccountDetailState(createdId)
                                    )
                                )
                            }
                        )
                    } else {
                        viewModel.updateCredential(
                            credential = credential,
                            onExecCompleted = {
                                showEditor = false
                            }
                        )
                    }
                }
            )
        }
    }

    if (showPasswordOptions) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                showPasswordOptions = false
            },
            sheetState = sheetState
        ) {
            GeneratePasswordScreen(
                onUsePassword = { generatedPassword ->
                    viewModel.setCredential(
                        credential.copy(password = generatedPassword)
                    )
                    showPasswordOptions = false
                },
                onBack = {
                    showPasswordOptions = false
                }
            )
        }
    }

    if (showDiscardConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showDiscardConfirmation = false
            },
            title = {
                Text(stringResource(R.string.credential_editor_discard_title))
            },
            text = {
                Text(stringResource(R.string.credential_editor_discard_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardConfirmation = false
                        exitEditor()
                    }
                ) {
                    Text(stringResource(R.string.credential_editor_discard))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDiscardConfirmation = false
                    }
                ) {
                    Text(stringResource(R.string.credential_editor_keep_editing))
                }
            }
        )
    }
}
