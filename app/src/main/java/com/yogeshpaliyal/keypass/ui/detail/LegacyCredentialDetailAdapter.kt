package com.yogeshpaliyal.keypass.ui.detail

import com.yogeshpaliyal.common.data.AccountModel
import com.yogeshpaliyal.keypass.vault.Credential

/**
 * Temporary presentation adapter while detail data still comes from the inherited Room model.
 *
 * The detail and editor UI depend only on the prototype Credential model. Later CRUD tasks can
 * replace this boundary without changing those composables.
 */
internal fun AccountModel.toCredentialDetail(): Credential = Credential(
    id = id?.toString().orEmpty(),
    title = title.orEmpty(),
    username = username.orEmpty(),
    password = password.orEmpty(),
    url = site,
    notes = notes
)

/**
 * Copies only prototype credential fields back into the inherited model.
 *
 * Legacy metadata such as the Room id, unique id, tags, secret, and account type remain untouched
 * until the persistence layer is replaced by the KDBX-backed repository.
 */
internal fun AccountModel.withCredentialDetail(credential: Credential): AccountModel = copy(
    title = credential.title,
    username = credential.username,
    password = credential.password,
    site = credential.url,
    notes = credential.notes
)
