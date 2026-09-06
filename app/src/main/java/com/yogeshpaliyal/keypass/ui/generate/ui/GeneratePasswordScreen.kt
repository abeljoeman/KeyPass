package com.yogeshpaliyal.keypass.ui.generate.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yogeshpaliyal.keypass.R
import com.yogeshpaliyal.keypass.ui.generate.GeneratePasswordViewModel
import com.yogeshpaliyal.keypass.utils.copySensitiveTextToClipboard
import kotlinx.coroutines.launch

@Composable
fun GeneratePasswordScreen(
    viewModel: GeneratePasswordViewModel = hiltViewModel(),
    onUsePassword: ((String) -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val copiedMessage = stringResource(R.string.copied_to_clipboard)
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    val isStandalone = onUsePassword == null

    LaunchedEffect(Unit) {
        viewModel.retrieveSavedPasswordConfig(context)
    }

    DisposableEffect(viewModel) {
        onDispose(viewModel::clearGeneratedPassword)
    }

    if (onBack != null) {
        BackHandler(onBack = onBack)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GeneratePasswordContent(
            viewState = viewState,
            onGeneratePasswordClick = viewModel::generatePassword,
            onCopyPasswordClick = if (isStandalone) {
                {
                    copySensitiveTextToClipboard(
                        context = context,
                        text = viewState.password,
                        label = "KeyPass"
                    )
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(copiedMessage)
                    }
                }
            } else {
                null
            },
            onUsePasswordClick = onUsePassword?.let { usePassword ->
                { usePassword(viewState.password) }
            },
            onPasswordLengthChange = viewModel::onPasswordLengthSliderChange,
            onUppercaseCheckedChange = viewModel::onUppercaseCheckedChange,
            onLowercaseCheckedChange = viewModel::onLowercaseCheckedChange,
            onNumbersCheckedChange = viewModel::onNumbersCheckedChange,
            onSymbolsCheckedChange = viewModel::onSymbolsCheckedChange
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
