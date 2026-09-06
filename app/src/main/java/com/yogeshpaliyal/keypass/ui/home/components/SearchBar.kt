package com.yogeshpaliyal.keypass.ui.home.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.keypass.R
import com.yogeshpaliyal.keypass.ui.home.SortingField
import com.yogeshpaliyal.keypass.ui.home.SortingOrder

@Composable
fun SearchBar(
    keyword: String?,
    updateKeyword: (keyword: String) -> Unit,
    updateSorting: (SortingField, SortingOrder) -> Unit
) {
    val (isMenuVisible, setMenuVisible) = rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val (isSearchFocused, setSearchFocused) = remember { mutableStateOf(false) }

    BackHandler(enabled = isSearchFocused || !keyword.isNullOrBlank()) {
        if (isSearchFocused) {
            focusManager.clearFocus()
        } else {
            updateKeyword("")
        }
    }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { setSearchFocused(it.isFocused) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        value = keyword ?: "",
        singleLine = true,
        placeholder = {
            Text(text = stringResource(R.string.search_credentials))
        },
        onValueChange = updateKeyword,
        trailingIcon = {
            Row {
                AnimatedVisibility(!keyword.isNullOrBlank()) {
                    IconButton(onClick = { updateKeyword("") }) {
                        Icon(
                            painter = rememberVectorPainter(image = Icons.Rounded.Close),
                            contentDescription = stringResource(R.string.a11y_clear_search)
                        )
                    }
                }

                IconButton(onClick = { setMenuVisible(!isMenuVisible) }) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.AutoMirrored.Rounded.Sort),
                        contentDescription = stringResource(R.string.a11y_sort_credentials)
                    )
                }
            }

            SortingMenu(isMenuVisible, setMenuVisible) { sortingField, order ->
                updateSorting(sortingField, order)
                setMenuVisible(false)
            }
        }
    )
}
