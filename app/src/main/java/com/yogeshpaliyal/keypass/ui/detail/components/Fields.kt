package com.yogeshpaliyal.keypass.ui.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.common.utils.PasswordGenerator
import com.yogeshpaliyal.keypass.R
import com.yogeshpaliyal.keypass.ui.commonComponents.KeyPassInputField
import com.yogeshpaliyal.keypass.ui.commonComponents.PasswordTrailingIcon
import com.yogeshpaliyal.keypass.ui.nav.LocalUserSettings
import com.yogeshpaliyal.keypass.vault.Credential

@Composable
fun Fields(
    modifier: Modifier = Modifier,
    credential: Credential,
    isNewCredential: Boolean,
    updateCredential: (Credential) -> Unit,
    copyToClipboardClicked: (String) -> Unit
) {
    val passwordConfig = LocalUserSettings.current.passwordConfig

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KeyPassInputField(
            modifier = Modifier.testTag("accountName"),
            placeholder = R.string.account_name,
            value = credential.title,
            setValue = {
                updateCredential(credential.copy(title = it))
            },
            copyToClipboardClicked = copyToClipboardClicked
        )

        KeyPassInputField(
            modifier = Modifier.testTag("username"),
            placeholder = R.string.username_email_phone,
            value = credential.username,
            setValue = {
                updateCredential(credential.copy(username = it))
            },
            copyToClipboardClicked = copyToClipboardClicked
        )

        Column {
            val passwordVisible = rememberSaveable { mutableStateOf(false) }

            val visualTransformation =
                if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation()

            KeyPassInputField(
                modifier = Modifier.testTag("password"),
                placeholder = R.string.password,
                value = credential.password,
                setValue = {
                    updateCredential(credential.copy(password = it))
                },
                trailingIcon = {
                    PasswordTrailingIcon(passwordVisible.value) {
                        passwordVisible.value = it
                    }
                },
                leadingIcon = if (isNewCredential) {
                    {
                        IconButton(
                            onClick = {
                                updateCredential(
                                    credential.copy(
                                        password = PasswordGenerator(passwordConfig).generatePassword()
                                    )
                                )
                            }
                        ) {
                            Icon(
                                painter = rememberVectorPainter(image = Icons.Rounded.Refresh),
                                contentDescription = ""
                            )
                        }
                    }
                } else {
                    null
                },
                visualTransformation = visualTransformation,
                copyToClipboardClicked = copyToClipboardClicked
            )
        }

        KeyPassInputField(
            modifier = Modifier.testTag("website"),
            placeholder = R.string.website_url_optional,
            value = credential.url,
            setValue = {
                updateCredential(credential.copy(url = it))
            },
            copyToClipboardClicked = copyToClipboardClicked
        )

        KeyPassInputField(
            modifier = Modifier.testTag("notes"),
            placeholder = R.string.notes_optional,
            value = credential.notes,
            setValue = {
                updateCredential(credential.copy(notes = it))
            },
            copyToClipboardClicked = copyToClipboardClicked
        )
    }
}
