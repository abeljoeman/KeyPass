package com.yogeshpaliyal.keypass.ui.home

import com.yogeshpaliyal.common.data.AccountModel
import com.yogeshpaliyal.keypass.vault.Credential

/**
 * Temporary display adapter while the inherited Room-backed home data source remains in place.
 *
 * T053 will replace this with the vault-backed credential source. The list does not need a
 * password, URL, or notes, so those values are intentionally not copied into its UI model.
 */
internal fun AccountModel.toCredentialListItem(): Credential = Credential(
    id = id?.toString().orEmpty(),
    title = title.orEmpty(),
    username = username.orEmpty(),
    password = "",
    url = null,
    notes = null
)
