package com.yogeshpaliyal.keypass.ui.commonComponents

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import com.yogeshpaliyal.keypass.R

@Composable
fun PasswordTrailingIcon(
    passwordVisible: Boolean,
    changePasswordVisibility: (updatedValue: Boolean) -> Unit
) {
    val description = stringResource(
        if (passwordVisible) R.string.a11y_hide_password else R.string.a11y_show_password
    )

    val image = if (passwordVisible) {
        Icons.Rounded.Visibility
    } else {
        Icons.Rounded.VisibilityOff
    }

    IconButton(onClick = { changePasswordVisibility(!passwordVisible) }) {
        Icon(
            painter = rememberVectorPainter(image = image),
            contentDescription = description
        )
    }
}
