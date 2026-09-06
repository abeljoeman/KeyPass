package com.yogeshpaliyal.keypass.ui.generate.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import com.yogeshpaliyal.common.data.PasswordConfig
import com.yogeshpaliyal.keypass.R
import com.yogeshpaliyal.keypass.ui.commonComponents.PasswordTrailingIcon
import com.yogeshpaliyal.keypass.ui.generate.ui.components.CheckboxWithLabel
import com.yogeshpaliyal.keypass.ui.generate.ui.components.PasswordLengthInput

@Composable
fun GeneratePasswordContent(
    viewState: PasswordConfig,
    onCopyPasswordClick: (() -> Unit)?,
    onGeneratePasswordClick: () -> Unit,
    onUsePasswordClick: (() -> Unit)? = null,
    onPasswordLengthChange: (Float) -> Unit,
    onUppercaseCheckedChange: (Boolean) -> Unit,
    onLowercaseCheckedChange: (Boolean) -> Unit,
    onNumbersCheckedChange: (Boolean) -> Unit,
    onSymbolsCheckedChange: (Boolean) -> Unit
) {
    val enabledCategoryCount = listOf(
        viewState.includeUppercaseLetters,
        viewState.includeLowercaseLetters,
        viewState.includeNumbers,
        viewState.includeSymbols
    ).count { it }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PasswordTextField(viewState.password)

        if (onUsePasswordClick != null) {
            Button(
                onClick = onUsePasswordClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = viewState.password.isNotBlank()
            ) {
                Text(stringResource(R.string.generator_use_password))
            }
        } else if (onCopyPasswordClick != null) {
            Button(
                onClick = onCopyPasswordClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = viewState.password.isNotBlank()
            ) {
                Text(stringResource(R.string.generator_copy_password))
            }
        }

        PasswordLengthInput(
            length = viewState.length,
            onPasswordLengthChange = onPasswordLengthChange
        )

        CheckboxWithLabel(
            label = stringResource(R.string.generator_uppercase),
            checked = viewState.includeUppercaseLetters,
            enabled = !viewState.includeUppercaseLetters || enabledCategoryCount > 1,
            onCheckedChange = onUppercaseCheckedChange
        )

        CheckboxWithLabel(
            label = stringResource(R.string.generator_lowercase),
            checked = viewState.includeLowercaseLetters,
            enabled = !viewState.includeLowercaseLetters || enabledCategoryCount > 1,
            onCheckedChange = onLowercaseCheckedChange
        )

        CheckboxWithLabel(
            label = stringResource(R.string.generator_numbers),
            checked = viewState.includeNumbers,
            enabled = !viewState.includeNumbers || enabledCategoryCount > 1,
            onCheckedChange = onNumbersCheckedChange
        )

        CheckboxWithLabel(
            label = stringResource(R.string.generator_symbols),
            checked = viewState.includeSymbols,
            enabled = !viewState.includeSymbols || enabledCategoryCount > 1,
            onCheckedChange = onSymbolsCheckedChange
        )

        OutlinedButton(
            onClick = onGeneratePasswordClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.generator_regenerate))
        }
    }
}

@Composable
private fun PasswordTextField(password: String) {
    var passwordVisible by remember(password) { mutableStateOf(false) }

    OutlinedTextField(
        value = password,
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            PasswordTrailingIcon(passwordVisible) {
                passwordVisible = it
            }
        },
        label = {
            Text(stringResource(R.string.password))
        }
    )
}

@Preview(
    name = "Night Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "Day Mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
@Suppress("UnusedPrivateMember")
private fun GeneratePasswordContentPreview() {
    val viewState = PasswordConfig.Initial

    Mdc3Theme {
        GeneratePasswordContent(
            viewState = viewState,
            onGeneratePasswordClick = {},
            onCopyPasswordClick = {},
            onPasswordLengthChange = {},
            onUppercaseCheckedChange = {},
            onLowercaseCheckedChange = {},
            onNumbersCheckedChange = {},
            onSymbolsCheckedChange = {}
        )
    }
}
