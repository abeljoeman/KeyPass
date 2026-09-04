package com.yogeshpaliyal.keypass.ui.detail

import com.yogeshpaliyal.common.data.AccountModel
import com.yogeshpaliyal.keypass.vault.Credential

/**
 * Temporary presentation adapter while detail data still comes from the inherited Room model.
 *
 * The detail UI itself depends only on the prototype Credential model. Later CRUD tasks can
 * replace this boundary without changing the credential-detail composable.
 */
internal fun AccountModel.toCredentialDetail(): Credential = Credential(
    id = id?.toString().orEmpty(),
    title = title.orEmpty(),
    username = username.orEmpty(),
    password = password.orEmpty(),
    url = site,
    notes = notes
)
