package com.yogeshpaliyal.keypass.ui.nav.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
    val vaultSelected = currentScreen is HomeState
    val generatorSelected = currentScreen is PasswordGeneratorState
    val settingsSelected = currentScreen is SettingsState
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = Color.Transparent,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f),
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f)
    )

    Column {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f)
        )

        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            NavigationBarItem(
                selected = vaultSelected,
                onClick = {
                    if (!vaultSelected) {
                        dispatchAction(NavigationAction(HomeState(), true))
                    }
                },
                colors = itemColors,
                icon = {
                    RahsaNavIconContainer(selected = vaultSelected) {
                        Icon(
                            painter = painterResource(R.drawable.ic_vault_shield_lock),
                            contentDescription = null
                        )
                    }
                },
                label = { Text(stringResource(R.string.nav_vault)) }
            )

            NavigationBarItem(
                selected = generatorSelected,
                onClick = {
                    if (!generatorSelected) {
                        dispatchAction(NavigationAction(PasswordGeneratorState(), true))
                    }
                },
                colors = itemColors,
                icon = {
                    RahsaNavIconContainer(selected = generatorSelected) {
                        Icon(
                            imageVector = Icons.Default.Password,
                            contentDescription = null
                        )
                    }
                },
                label = { Text(stringResource(R.string.nav_generator)) }
            )

            NavigationBarItem(
                selected = settingsSelected,
                onClick = {
                    if (!settingsSelected) {
                        dispatchAction(NavigationAction(SettingsState, true))
                    }
                },
                colors = itemColors,
                icon = {
                    RahsaNavIconContainer(selected = settingsSelected) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = null
                        )
                    }
                },
                label = { Text(stringResource(R.string.nav_settings)) }
            )
        }
    }
}

@Composable
private fun RahsaNavIconContainer(
    selected: Boolean,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 36.dp, height = 28.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)
                } else {
                    Color.Transparent
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
