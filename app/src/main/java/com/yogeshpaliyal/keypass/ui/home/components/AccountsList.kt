package com.yogeshpaliyal.keypass.ui.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yogeshpaliyal.keypass.R
import com.yogeshpaliyal.keypass.vault.Credential

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun CredentialsList(
    credentials: List<Credential> = emptyList(),
    onCredentialClick: (Credential) -> Unit = {}
) {
    if (credentials.isNotEmpty()) {
        AnimatedContent(targetState = credentials, label = "credential-list") {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(it, key = Credential::id) { credential ->
                    CredentialListItem(
                        modifier = Modifier,
                        credential = credential,
                        onClick = onCredentialClick
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    } else {
        NoDataFound()
    }
}

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

@Composable
fun NoDataFound() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.message_no_accounts),
                modifier = Modifier
                    .padding(32.dp)
                    .align(alignment = Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
        Image(
            painter = painterResource(R.drawable.ic_undraw_empty_street_sfxm),
            contentDescription = ""
        )
    }
}

private fun Credential.getInitials() = (title.firstOrNull() ?: username.firstOrNull() ?: 'K').toString()
