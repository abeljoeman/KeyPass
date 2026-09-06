package com.yogeshpaliyal.keypass.ui.generate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yogeshpaliyal.keypass.R

const val DEFAULT_PASSWORD_LENGTH = 20f
const val MIN_GENERATED_PASSWORD_LENGTH = 8f
const val MAX_GENERATED_PASSWORD_LENGTH = 64f
private const val PASSWORD_LENGTH_STEPS = 55

@Composable
fun PasswordLengthInput(
    length: Float,
    onPasswordLengthChange: (Float) -> Unit
) {
    Column {
        Text(stringResource(R.string.generator_password_length))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                enabled = length > MIN_GENERATED_PASSWORD_LENGTH,
                onClick = {
                    onPasswordLengthChange(
                        (length - 1f).coerceAtLeast(
                            MIN_GENERATED_PASSWORD_LENGTH
                        )
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Remove,
                    contentDescription = stringResource(
                        R.string.generator_decrease_length
                    )
                )
            }

            Text(
                text = length.toInt().toString()
            )

            IconButton(
                enabled = length < MAX_GENERATED_PASSWORD_LENGTH,
                onClick = {
                    onPasswordLengthChange(
                        (length + 1f).coerceAtMost(
                            MAX_GENERATED_PASSWORD_LENGTH
                        )
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(
                        R.string.generator_increase_length
                    )
                )
            }
        }

        Slider(
            value = length.coerceIn(
                MIN_GENERATED_PASSWORD_LENGTH,
                MAX_GENERATED_PASSWORD_LENGTH
            ),
            onValueChange = onPasswordLengthChange,
            valueRange = MIN_GENERATED_PASSWORD_LENGTH..MAX_GENERATED_PASSWORD_LENGTH,
            steps = PASSWORD_LENGTH_STEPS
        )
    }
}
