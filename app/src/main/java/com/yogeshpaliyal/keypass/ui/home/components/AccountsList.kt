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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yogeshpaliyal.common.data.AccountModel
import com.yogeshpaliyal.keypass.R
import com.yogeshpaliyal.keypass.ui.redux.actions.CopyToClipboard
import com.yogeshpaliyal.keypass.ui.redux.actions.NavigationAction
import com.yogeshpaliyal.keypass.ui.redux.states.AccountDetailState
import org.reduxkotlin.compose.rememberDispatcher

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun AccountsList(accounts: List<AccountModel>? = null) {
    val dispatch = rememberDispatcher()

    if (accounts?.isNotEmpty() == true) {
        AnimatedContent(targetState = accounts) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(it) { account ->
                    Account(
                        modifier = Modifier,
                        account,
                        onClick = {
                            dispatch(NavigationAction(AccountDetailState(it.id)))
                        }
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
fun Account(
    modifier: Modifier,
    accountModel: AccountModel,
    onClick: (AccountModel) -> Unit
) {
    val dispatch = rememberDispatcher()

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = { onClick(accountModel) }
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
                    text = accountModel.getInitials(),
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
                    text = accountModel.title ?: "",
                    style = MaterialTheme.typography.headlineSmall.merge(
                        TextStyle(
                            fontSize = 16.sp
                        )
                    )
                )

                RenderUserName(accountModel)
            }

            IconButton(
                modifier = Modifier.align(Alignment.CenterVertically),
                onClick = { dispatch(CopyToClipboard(getPassword(accountModel))) }
            ) {
                Icon(
                    painter = rememberVectorPainter(image = Icons.TwoTone.ContentCopy),
                    contentDescription = "Copy To Clipboard"
                )
            }
        }
    }
}

@Composable
fun RenderUserName(accountModel: AccountModel) {
    Text(
        text = accountModel.username ?: "",
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

private fun getPassword(model: AccountModel): String {
    return model.password.orEmpty()
}
