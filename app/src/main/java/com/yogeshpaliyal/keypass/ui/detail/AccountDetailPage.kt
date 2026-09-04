package com.yogeshpaliyal.keypass.ui.detail

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yogeshpaliyal.keypass.ui.detail.components.BottomBar
import com.yogeshpaliyal.keypass.ui.detail.components.CredentialDetail
import com.yogeshpaliyal.keypass.ui.detail.components.FABAddAccount
import com.yogeshpaliyal.keypass.ui.detail.components.Fields
import com.yogeshpaliyal.keypass.ui.nav.LocalVaultRepository
import com.yogeshpaliyal.keypass.ui.redux.actions.CopyToClipboard
import com.yogeshpaliyal.keypass.ui.redux.actions.GoBackAction
import com.yogeshpaliyal.keypass.ui.redux.actions.NavigationAction
import com.yogeshpaliyal.keypass.ui.redux.states.PasswordGeneratorState
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
    val isNewCredential = id == null
    var showEditor by rememberSaveable(id) { mutableStateOf(isNewCredential) }

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

    if (!isNewCredential && !showEditor) {
        CredentialDetail(
            credential = credential,
            onBack = goBack,
            onEdit = { showEditor = true },
            onDelete = null,
            onCopyToClipboard = copyToClipboard
        )
        return
    }

    Scaffold(
        topBar = {
            BottomBar(
                isNewCredential = isNewCredential,
                backPressed = goBack,
                onDeleteAccount = null,
                openPasswordConfiguration = {
                    dispatchAction(NavigationAction(PasswordGeneratorState()))
                }
            )
        },
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
