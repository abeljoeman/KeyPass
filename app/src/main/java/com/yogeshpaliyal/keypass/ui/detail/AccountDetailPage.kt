package com.yogeshpaliyal.keypass.ui.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yogeshpaliyal.keypass.R
import com.yogeshpaliyal.keypass.ui.detail.components.BottomBar
import com.yogeshpaliyal.keypass.ui.detail.components.CredentialDetail
import com.yogeshpaliyal.keypass.ui.detail.components.FABAddAccount
import com.yogeshpaliyal.keypass.ui.detail.components.Fields
import com.yogeshpaliyal.keypass.ui.generate.ui.GeneratePasswordScreen
import com.yogeshpaliyal.keypass.ui.nav.LocalVaultRepository
import com.yogeshpaliyal.keypass.ui.redux.actions.CopyToClipboard
import com.yogeshpaliyal.keypass.ui.redux.actions.GoBackAction
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
    val isNewCredential = id == null
    var showEditor by rememberSaveable(id) { mutableStateOf(isNewCredential) }
    var showPasswordGenerator by rememberSaveable(id) { mutableStateOf(false) }
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
    val cancelEdit: () -> Unit = {
        viewModel.cancelEdit()
        showEditor = false
    }

    val credential = credentialState ?: return

    if (showPasswordGenerator) {
        GeneratePasswordScreen(
            onUsePassword = { generatedPassword ->
                viewModel.setCredential(credential.copy(password = generatedPassword))
                showPasswordGenerator = false
            },
            onBack = {
                showPasswordGenerator = false
            }
        )
        return
    }

    BackHandler(enabled = !isNewCredential && showEditor) {
        cancelEdit()
    }

    if (!isNewCredential && !showEditor) {
        CredentialDetail(
            credential = credential,
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
        return
    }

    Scaffold(
        topBar = {
            BottomBar(
                isNewCredential = isNewCredential,
                backPressed = if (isNewCredential) goBack else cancelEdit,
                onDeleteAccount = if (isNewCredential) {
                    null
                } else {
                    {
                        viewModel.deleteCredential(
                            id = credential.id,
                            onExecCompleted = goBack
                        )
                    }
                },
                openPasswordConfiguration = {
                    showPasswordGenerator = true
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FABAddAccount {
                if (isNewCredential) {
                    viewModel.createCredential(
                        credential = credential,
                        onExecCompleted = goBack
                    )
                } else {
                    viewModel.updateCredential(
                        credential = credential,
                        onExecCompleted = goBack
                    )
                }
            }
        }
    ) { paddingValues ->
        Surface(modifier = Modifier.padding(paddingValues)) {
            Fields(
                credential = credential,
                isNewCredential = isNewCredential,
                updateCredential = viewModel::setCredential,
                copyToClipboardClicked = copyToClipboard
            )
        }
    }
}
