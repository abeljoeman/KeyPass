package com.yogeshpaliyal.keypass.ui.detail.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import com.yogeshpaliyal.keypass.R

@Composable
fun BottomBar(
    isNewCredential: Boolean,
    backPressed: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(
                    if (isNewCredential) {
                        R.string.credential_editor_add_title
                    } else {
                        R.string.credential_editor_edit_title
                    }
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = backPressed) {
                Icon(
                    painter = rememberVectorPainter(image = Icons.AutoMirrored.Rounded.ArrowBack),
                    contentDescription = stringResource(R.string.back)
                )
            }
        }
    )
}
