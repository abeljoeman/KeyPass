package com.yogeshpaliyal.keypass.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yogeshpaliyal.keypass.vault.Credential
import com.yogeshpaliyal.keypass.vault.VaultRepository
import java.io.IOException
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal enum class DetailOperationError {
    VaultWriteFailed
}

/*
* @author Yogesh Paliyal
* techpaliyal@gmail.com
* https://techpaliyal.com
* created on 31-01-2021 11:52
*/
class DetailViewModel internal constructor(
    private val vaultRepository: VaultRepository,
    private val externalScope: CoroutineScope? = null
) : ViewModel() {

    private val workScope: CoroutineScope
        get() = externalScope ?: viewModelScope
    private val loadGeneration = AtomicLong()
    private var loadJob: Job? = null
    private var editBaseline: Credential? = null

    private val _credential = MutableStateFlow<Credential?>(null)
    val credential: StateFlow<Credential?> = _credential.asStateFlow()

    private val _operationError = MutableStateFlow<DetailOperationError?>(null)
    internal val operationError: StateFlow<DetailOperationError?> = _operationError.asStateFlow()

    fun loadCredential(id: String?): Job {
        val generation = loadGeneration.incrementAndGet()
        loadJob?.cancel()
        return workScope.launch {
            val result = if (id == null) {
                emptyCredential()
            } else {
                vaultRepository.listCredentials().firstOrNull { it.id == id }
                    ?: throw NoSuchElementException("Credential does not exist.")
            }
            if (loadGeneration.get() == generation) {
                editBaseline = null
                _credential.value = result
            }
        }.also { loadJob = it }
    }

    fun beginEdit() {
        val current = checkNotNull(_credential.value) { "Credential is not loaded." }
        require(current.id.isNotBlank()) { "Credential ID is required for edit." }
        editBaseline = current
    }

    fun setCredential(credential: Credential) {
        _credential.value = credential
    }

    fun cancelEdit() {
        editBaseline?.let { baseline ->
            _credential.value = baseline
        }
        editBaseline = null
    }

    fun createCredential(
        credential: Credential,
        onExecCompleted: () -> Unit
    ): Job = workScope.launch {
        _operationError.value = null
        val saved = runVaultMutation {
            vaultRepository.createCredential(
                credential.copy(id = UUID.randomUUID().toString())
            )
        }
        if (!saved) return@launch
        onExecCompleted()
    }

    fun updateCredential(
        credential: Credential,
        onExecCompleted: () -> Unit
    ): Job = workScope.launch {
        require(credential.id.isNotBlank()) { "Credential ID is required for update." }
        _operationError.value = null
        val saved = runVaultMutation {
            vaultRepository.updateCredential(credential)
        }
        if (!saved) return@launch
        editBaseline = null
        onExecCompleted()
    }

    fun deleteCredential(
        id: String,
        onExecCompleted: () -> Unit
    ): Job = workScope.launch {
        require(id.isNotBlank()) { "Credential ID is required for delete." }
        _operationError.value = null
        val saved = runVaultMutation {
            vaultRepository.deleteCredential(id)
        }
        if (!saved) return@launch
        editBaseline = null
        _credential.value = null
        onExecCompleted()
    }

    fun clearOperationError() {
        _operationError.value = null
    }

    fun clearSensitiveState() {
        loadGeneration.incrementAndGet()
        loadJob?.cancel()
        loadJob = null
        editBaseline = null
        _credential.value = null
        _operationError.value = null
    }

    override fun onCleared() {
        clearSensitiveState()
        super.onCleared()
    }

    class Factory(
        private val vaultRepository: VaultRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
                return DetailViewModel(vaultRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    private suspend fun runVaultMutation(block: suspend () -> Unit): Boolean =
        try {
            block()
            true
        } catch (_: IOException) {
            _operationError.value = DetailOperationError.VaultWriteFailed
            false
        }

    private fun emptyCredential() = Credential(
        id = "",
        title = "",
        username = "",
        password = "",
        url = null,
        notes = null
    )
}
