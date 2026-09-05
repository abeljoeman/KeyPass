package com.yogeshpaliyal.keypass.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yogeshpaliyal.keypass.vault.Credential

@Composable
fun CredentialListItem(
    modifier: Modifier,
    credential: Credential,
    onClick: (Credential) -> Unit
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = { onClick(credential) }
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(60.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = credential.getInitials(),
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = credential.title,
                    style = MaterialTheme.typography.headlineSmall.merge(
                        TextStyle(
                            fontSize = 16.sp
                        )
                    )
                )

                RenderUserName(credential)
            }
        }
    }
}

@Composable
fun RenderUserName(credential: Credential) {
    Text(
        text = credential.username,
        style = MaterialTheme.typography.bodyMedium.merge(
            TextStyle(
                fontSize = 14.sp
            )
        )
    )
}

private fun Credential.getInitials() =
    (title.firstOrNull() ?: username.firstOrNull() ?: 'K').toString()
