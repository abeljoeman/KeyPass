package com.yogeshpaliyal.keypass.vault

import app.keemobile.kotpass.constants.BasicField
import app.keemobile.kotpass.cryptography.EncryptedValue
import app.keemobile.kotpass.models.Entry
import app.keemobile.kotpass.models.EntryFields
import app.keemobile.kotpass.models.EntryValue
import java.util.UUID

fun Entry.toCredential() = Credential(
    id = uuid.toString(),
    title = this[BasicField.Title]?.content.orEmpty(),
    username = this[BasicField.UserName]?.content.orEmpty(),
    password = this[BasicField.Password]?.content.orEmpty(),
    url = this[BasicField.Url]?.content?.takeIf { it.isNotEmpty() },
    notes = this[BasicField.Notes]?.content?.takeIf { it.isNotEmpty() }
)

fun Credential.toEntry() = Entry(
    uuid = UUID.fromString(id),
    fields = EntryFields.of(
        BasicField.Title.key to EntryValue.Plain(title),
        BasicField.UserName.key to EntryValue.Plain(username),
        BasicField.Password.key to EntryValue.Encrypted(EncryptedValue.fromString(password)),
        BasicField.Url.key to EntryValue.Plain(url.orEmpty()),
        BasicField.Notes.key to EntryValue.Plain(notes.orEmpty())
    )
)
