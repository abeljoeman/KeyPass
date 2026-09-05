package com.yogeshpaliyal.keypass.ui.detail

import com.yogeshpaliyal.keypass.vault.Credential
import com.yogeshpaliyal.keypass.vault.VaultRepository
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class DetailWriteFailureTest {

    @Test
    fun updateWriteFailureReportsErrorWithoutCompletingOrClearingDraft() = runBlocking {
        val edited = Credential(
            id = "00000000-0000-0000-0000-000000000123",
            title = "Updated",
            username = "updated-user",
            password = "updated-password",
            url = null,
            notes = "unsaved edit"
        )
        var completionCalled = false
        val repository = FakeVaultRepository(
            updateBlock = { throw IOException("Permission denied") }
        )
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val viewModel = DetailViewModel(repository, scope)

        try {
            viewModel.setCredential(edited)

            val saveJob = viewModel.updateCredential(edited) {
                completionCalled = true
            }
            saveJob.join()

            assertFalse(saveJob.isCancelled)
            assertFalse(viewModel.isSaving.value)
            assertFalse(completionCalled)
            assertEquals(edited, viewModel.credential.value)
            assertEquals(
                DetailOperationError.VaultWriteFailed,
                viewModel.operationError.value
            )

            viewModel.clearOperationError()
            assertNull(viewModel.operationError.value)
        } finally {
            scope.cancel()
        }
    }

    private class FakeVaultRepository(
        private val updateBlock: suspend (Credential) -> Unit
    ) : VaultRepository {
        override suspend fun createVault(masterPassword: CharArray) = unused()
        override suspend fun openVault(masterPassword: CharArray) = unused()
        override fun lock() = Unit
        override suspend fun listCredentials(): List<Credential> = unused()
        override suspend fun createCredential(credential: Credential) = unused()
        override suspend fun updateCredential(credential: Credential) = updateBlock(credential)
        override suspend fun deleteCredential(id: String) = unused()
        override suspend fun searchCredentials(query: String): List<Credential> = unused()

        private fun unused(): Nothing = error("Not used by DetailWriteFailureTest")
    }
}
