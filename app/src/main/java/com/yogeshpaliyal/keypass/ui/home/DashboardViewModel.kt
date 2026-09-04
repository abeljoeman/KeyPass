package com.yogeshpaliyal.keypass.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yogeshpaliyal.keypass.vault.Credential
import com.yogeshpaliyal.keypass.vault.VaultRepository
import java.util.Locale
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
* created on 30-01-2021 23:02
*/
class DashboardViewModel internal constructor(
    private val vaultRepository: VaultRepository,
    private val externalScope: CoroutineScope? = null
) : ViewModel() {

    private val workScope: CoroutineScope
        get() = externalScope ?: viewModelScope
    private val queryGeneration = AtomicLong()
    private var queryJob: Job? = null

    private val _credentials = MutableStateFlow<List<Credential>>(emptyList())
    val credentials: StateFlow<List<Credential>> = _credentials.asStateFlow()

    fun loadCredentials(
        sortField: String?,
        sortAscending: Boolean = true
    ): Job {
        val generation = queryGeneration.incrementAndGet()
        queryJob?.cancel()
        return workScope.launch {
            val result = vaultRepository.listCredentials()
            if (queryGeneration.get() == generation) {
                _credentials.value = sortCredentials(result, sortField, sortAscending)
            }
        }.also { queryJob = it }
    }

    fun clearSensitiveState() {
        queryGeneration.incrementAndGet()
        queryJob?.cancel()
        queryJob = null
        _credentials.value = emptyList()
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
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                return DashboardViewModel(vaultRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

private fun sortCredentials(
    credentials: List<Credential>,
    sortField: String?,
    sortAscending: Boolean
): List<Credential> {
    val sorted = when (sortField) {
        SortingField.Title.value -> credentials.sortedBy { it.title.lowercase(Locale.ROOT) }
        SortingField.Username.value -> credentials.sortedBy { it.username.lowercase(Locale.ROOT) }
        else -> return credentials
    }
    return if (sortAscending) sorted else sorted.asReversed()
}
