package com.yogeshpaliyal.keypass.vault

data class Credential(
    val id: String,
    val title: String,
    val username: String,
    val password: String,
    val url: String?,
    val notes: String?
)
