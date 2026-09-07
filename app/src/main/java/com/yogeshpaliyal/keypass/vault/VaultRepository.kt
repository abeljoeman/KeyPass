package com.yogeshpaliyal.keypass.vault

class VaultLockedException : IllegalStateException("Vault is locked.")

class InvalidCurrentMasterPasswordException(cause: Throwable) :
    IllegalArgumentException("Current Master Password could not open the active vault.", cause)

interface VaultRepository {
    suspend fun createVault(masterPassword: CharArray)

    suspend fun openVault(masterPassword: CharArray)

    suspend fun changeMasterPassword(
        currentMasterPassword: CharArray,
        newMasterPassword: CharArray,
        onCandidateValidated: (encryptedCandidateFingerprint: String) -> Unit = {}
    ) {
        throw UnsupportedOperationException("Master Password change is not supported.")
    }

    fun lock()

    suspend fun listCredentials(): List<Credential>

    suspend fun createCredential(credential: Credential)

    suspend fun updateCredential(credential: Credential)

    suspend fun deleteCredential(id: String)

    suspend fun searchCredentials(query: String): List<Credential>
}
