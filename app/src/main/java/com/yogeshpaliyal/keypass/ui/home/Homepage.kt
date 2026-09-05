package com.yogeshpaliyal.keypass.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yogeshpaliyal.keypass.R
import com.yogeshpaliyal.keypass.ui.home.components.CredentialsList
import com.yogeshpaliyal.keypass.ui.home.components.SearchBar
import com.yogeshpaliyal.keypass.ui.nav.LocalVaultRepository
import com.yogeshpaliyal.keypass.ui.redux.KeyPassRedux
import com.yogeshpaliyal.keypass.ui.redux.actions.Action
import com.yogeshpaliyal.keypass.ui.redux.actions.BatchActions
import com.yogeshpaliyal.keypass.ui.redux.actions.NavigationAction
import com.yogeshpaliyal.keypass.ui.redux.actions.StateUpdateAction
import com.yogeshpaliyal.keypass.ui.redux.actions.UpdateViewModalAction
import com.yogeshpaliyal.keypass.ui.redux.states.AccountDetailState
import com.yogeshpaliyal.keypass.ui.redux.states.AuthState
import com.yogeshpaliyal.keypass.ui.redux.states.HomeState
import org.reduxkotlin.compose.rememberTypedDispatcher

/*
* @author Yogesh Paliyal
* techpaliyal@gmail.com
* https://techpaliyal.com
* created on 31-01-2021 09:25
*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Homepage(homeState: HomeState) {
    val tag = homeState.tag
    val keyword = homeState.keyword
    val sortField = homeState.sortField
    val sortAscendingOrder = homeState.sortAscending

    val vaultRepository = LocalVaultRepository.current
    val viewModelFactory = remember(vaultRepository) {
        DashboardViewModel.Factory(vaultRepository)
    }
    val mViewModel: DashboardViewModel = viewModel(factory = viewModelFactory)
    val credentials by mViewModel.credentials.collectAsState()

    val dispatchAction = rememberTypedDispatcher<Action>()
    val isFiltering = !keyword.isNullOrBlank() || tag != null
    val shouldShowSearch = credentials.isNotEmpty() || isFiltering

    LaunchedEffect(keyword, sortField, sortAscendingOrder) {
        mViewModel.loadCredentials(sortField, sortAscendingOrder, keyword)
    }

    DisposableEffect(KeyPassRedux, mViewModel) {
        dispatchAction(UpdateViewModalAction(mViewModel))
        onDispose {
            mViewModel.clearSensitiveState()
            dispatchAction(UpdateViewModalAction(null))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.your_vault)) },
                actions = {
                    IconButton(
                        onClick = {
                            mViewModel.clearSensitiveState()
                            vaultRepository.lock()
                            dispatchAction(
                                BatchActions(
                                    UpdateViewModalAction(null),
                                    NavigationAction(AuthState.Login, true)
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = stringResource(R.string.lock_vault)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    dispatchAction(NavigationAction(AccountDetailState()))
                },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null
                    )
                },
                text = { Text(stringResource(R.string.add_credential)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (shouldShowSearch) {
                SearchBar(
                    keyword = keyword,
                    updateKeyword = {
                        dispatchAction(StateUpdateAction(homeState.copy(keyword = it)))
                    },
                    updateSorting = { field, order ->
                        dispatchAction(
                            StateUpdateAction(
                                homeState.copy(
                                    sortField = field.value,
                                    sortAscending = order == SortingOrder.Ascending
                                )
                            )
                        )
                    }
                )
            }

            AnimatedVisibility(tag != null) {
                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    content = {
                        item {
                            AssistChip(
                                onClick = { },
                                label = { Text(text = tag ?: "") },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            dispatchAction(NavigationAction(HomeState(), true))
                                        }
                                    ) {
                                        Icon(
                                            painter = rememberVectorPainter(
                                                image = Icons.Rounded.Close
                                            ),
                                            contentDescription = stringResource(
                                                R.string.a11y_clear_tag_filter
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    }
                )
            }

            CredentialsList(
                credentials = credentials,
                isFiltering = isFiltering,
                onAddCredential = {
                    dispatchAction(NavigationAction(AccountDetailState()))
                },
                onCredentialClick = { credential ->
                    dispatchAction(NavigationAction(AccountDetailState(credential.id)))
                }
            )
        }
    }
}
