package com.yogeshpaliyal.keypass.ui.nav.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.yogeshpaliyal.keypass.R
import com.yogeshpaliyal.keypass.ui.nav.BottomNavViewModel
import com.yogeshpaliyal.keypass.ui.redux.actions.NavigationAction
import com.yogeshpaliyal.keypass.ui.redux.selectState
import com.yogeshpaliyal.keypass.ui.redux.states.HomeState
import com.yogeshpaliyal.keypass.ui.redux.states.KeyPassState
import com.yogeshpaliyal.keypass.ui.redux.states.PasswordGeneratorState
import com.yogeshpaliyal.keypass.ui.redux.states.ScreenState
import com.yogeshpaliyal.keypass.ui.redux.states.SettingsState
import org.reduxkotlin.compose.rememberDispatcher

@Composable
fun KeyPassBottomBar(viewModel: BottomNavViewModel) {
    val currentScreen: ScreenState by
        selectState<KeyPassState, ScreenState> { this.currentScreen }

    if (!currentScreen.showMainBottomAppBar) {
        return
    }

    val dispatchAction = rememberDispatcher()

    NavigationBar {
        NavigationBarItem(
            selected = currentScreen is HomeState,
            onClick = {
                if (currentScreen !is HomeState) {
                    dispatchAction(NavigationAction(HomeState(), true))
                }
            },
            icon = {
                Image(
                    painter = painterResource(R.drawable.logo_rahsa),
                    contentDescription = stringResource(R.string.nav_vault)
                )
            },
            label = { Text(stringResource(R.string.nav_vault)) }
        )

        NavigationBarItem(
            selected = currentScreen is PasswordGeneratorState,
            onClick = {
                if (currentScreen !is PasswordGeneratorState) {
                    dispatchAction(NavigationAction(PasswordGeneratorState(), true))
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Password,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.nav_generator)) }
        )

        NavigationBarItem(
            selected = currentScreen is SettingsState,
            onClick = {
                if (currentScreen !is SettingsState) {
                    dispatchAction(NavigationAction(SettingsState, true))
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.nav_settings)) }
        )
    }
}
