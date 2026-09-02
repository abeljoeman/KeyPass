package com.yogeshpaliyal.keypass.ui.detail

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.yogeshpaliyal.keypass.ui.detail.components.BottomBar
import com.yogeshpaliyal.keypass.ui.detail.components.FABAddAccount
import com.yogeshpaliyal.keypass.ui.detail.components.Fields
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
fun AccountDetailPage(
    id: Long?,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val dispatchAction = rememberDispatcher()

    // task value state
    val accountModel = viewModel.accountModel.collectAsState().value

    // Set initial object
    LaunchedEffect(key1 = id) {
        viewModel.loadAccount(id)
    }

    val goBack: () -> Unit = {
        dispatchAction(GoBackAction)
    }

    Scaffold(
        topBar = {
            BottomBar(
                accountModel,
                backPressed = goBack,
                onDeleteAccount = {
                    viewModel.deleteAccount(accountModel, goBack)
                },
                openPasswordConfiguration = {
                    dispatchAction(NavigationAction(PasswordGeneratorState()))
                }
            )
        },
        floatingActionButton = {
            FABAddAccount {
                viewModel.insertOrUpdate(accountModel, goBack)
            }
        }
    ) { paddingValues ->
        Surface(modifier = Modifier.padding(paddingValues)) {
            Fields(
                accountModel = accountModel,
                updateAccountModel = { newAccountModel ->
                    viewModel.setAccountModel(newAccountModel)
                },
                copyToClipboardClicked = { value ->
                    dispatchAction(CopyToClipboard(value))
                }
            )
        }
    }
}
