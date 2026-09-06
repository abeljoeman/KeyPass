package com.yogeshpaliyal.keypass.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.common.utils.openLink
import com.yogeshpaliyal.common.utils.setUserSettings
import com.yogeshpaliyal.keypass.BuildConfig
import com.yogeshpaliyal.keypass.R
import com.yogeshpaliyal.keypass.ui.nav.LocalUserSettings
import com.yogeshpaliyal.keypass.ui.redux.actions.Action
import com.yogeshpaliyal.keypass.ui.redux.actions.IntentNavigation
import com.yogeshpaliyal.keypass.ui.redux.actions.NavigationAction
import com.yogeshpaliyal.keypass.ui.redux.states.AboutState
import com.yogeshpaliyal.keypass.ui.redux.states.ChangeAppHintState
import kotlinx.coroutines.launch
import org.reduxkotlin.compose.rememberTypedDispatcher

@Composable
fun MySettingCompose() {
    val dispatchAction = rememberTypedDispatcher<Action>()
    val context = LocalContext.current
    val userSettings = LocalUserSettings.current
    val coroutineScope = rememberCoroutineScope()
    val autoLockEnabled = userSettings.autoLockEnabled == true

    fun setAutoLockEnabled(enabled: Boolean) {
        coroutineScope.launch {
            context.setUserSettings(
                userSettings.copy(autoLockEnabled = enabled)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        SettingsSectionHeader(stringResource(R.string.settings_security))

        SettingsNavigationRow(
            title = stringResource(R.string.settings_password_hint),
            onClick = {
                dispatchAction(NavigationAction(ChangeAppHintState))
            }
        )

        ListItem(
            modifier = Modifier.clickable {
                setAutoLockEnabled(!autoLockEnabled)
            },
            headlineContent = {
                Text(stringResource(R.string.settings_auto_lock))
            },
            trailingContent = {
                Switch(
                    checked = autoLockEnabled,
                    onCheckedChange = ::setAutoLockEnabled
                )
            }
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp)
        )

        SettingsSectionHeader(stringResource(R.string.settings_help_about))

        SettingsNavigationRow(
            title = stringResource(R.string.send_feedback),
            onClick = {
                context.openLink("https://github.com/abeljoeman/KeyPass/issues")
            }
        )

        SettingsNavigationRow(
            title = stringResource(R.string.settings_share_keypass),
            onClick = {
                dispatchAction(IntentNavigation.ShareApp)
            }
        )

        SettingsNavigationRow(
            title = stringResource(R.string.settings_about),
            onClick = {
                dispatchAction(NavigationAction(AboutState()))
            }
        )

        Text(
            text = stringResource(
                R.string.settings_version,
                BuildConfig.VERSION_NAME
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 8.dp
        ),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(title)
        },
        trailingContent = {
            Text(
                text = "\u203A",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
