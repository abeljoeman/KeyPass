package com.yogeshpaliyal.keypass.vault

interface VaultRepository {
    suspend fun createVault(masterPassword: CharArray)

    suspend fun openVault(masterPassword: CharArray)

    fun lock()

    suspend fun listCredentials(): List<Credential>

    suspend fun createCredential(credential: Credential)

    suspend fun updateCredential(credential: Credential)

    suspend fun deleteCredential(id: String)

    suspend fun searchCredentials(query: String): List<Credential>
}
