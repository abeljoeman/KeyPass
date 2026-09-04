package com.yogeshpaliyal.keypass.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yogeshpaliyal.keypass.vault.Credential
import com.yogeshpaliyal.keypass.vault.VaultRepository
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private val _credential = MutableStateFlow<Credential?>(null)
    val credential: StateFlow<Credential?> = _credential.asStateFlow()

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
                _credential.value = result
            }
        }.also { loadJob = it }
    }

    fun setCredential(credential: Credential) {
        _credential.value = credential
    }

    fun createCredential(
        credential: Credential,
        onExecCompleted: () -> Unit
    ): Job = workScope.launch {
        vaultRepository.createCredential(
            credential.copy(id = UUID.randomUUID().toString())
        )
        onExecCompleted()
    }

    fun updateCredential(
        credential: Credential,
        onExecCompleted: () -> Unit
    ): Job = workScope.launch {
        require(credential.id.isNotBlank()) { "Credential ID is required for update." }
        vaultRepository.updateCredential(credential)
        onExecCompleted()
    }

    fun clearSensitiveState() {
        loadGeneration.incrementAndGet()
        loadJob?.cancel()
        loadJob = null
        _credential.value = null
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

    private fun emptyCredential() = Credential(
        id = "",
        title = "",
        username = "",
        password = "",
        url = null,
        notes = null
    )
}
