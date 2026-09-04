package com.yogeshpaliyal.keypass.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yogeshpaliyal.common.AppDatabase
import com.yogeshpaliyal.common.data.AccountModel
import com.yogeshpaliyal.common.db.DbDao
import com.yogeshpaliyal.keypass.vault.Credential
import com.yogeshpaliyal.keypass.vault.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/*
* @author Yogesh Paliyal
* techpaliyal@gmail.com
* https://techpaliyal.com
* created on 31-01-2021 11:52
*/
@HiltViewModel
class DetailViewModel private constructor(
    private val appDao: DbDao,
    private val externalScope: CoroutineScope?,
    @Suppress("UNUSED_PARAMETER") constructorMarker: Unit
) : ViewModel() {

    @Inject
    constructor(appDb: AppDatabase) : this(appDb.getDao(), null, Unit)

    internal constructor(appDao: DbDao, scope: CoroutineScope) : this(appDao, scope, Unit)

    private val workScope: CoroutineScope
        get() = externalScope ?: viewModelScope
    private val loadGeneration = AtomicLong()
    private var loadJob: Job? = null

    private val _accountModel = MutableStateFlow(AccountModel())
    val accountModel: StateFlow<AccountModel> = _accountModel.asStateFlow()

    fun loadAccount(id: Long?): Job {
        val generation = loadGeneration.incrementAndGet()
        loadJob?.cancel()
        return workScope.launch {
            val result = withContext(Dispatchers.IO) {
                if (id == null) AccountModel() else appDao.getAccount(id) ?: AccountModel()
            }
            if (loadGeneration.get() == generation) {
                _accountModel.value = result
            }
        }.also { loadJob = it }
    }

    fun setAccountModel(accountModel: AccountModel) {
        _accountModel.value = accountModel
    }

    fun createCredential(
        vaultRepository: VaultRepository,
        credential: Credential,
        onExecCompleted: () -> Unit
    ): Job = workScope.launch {
        vaultRepository.createCredential(
            credential.copy(id = UUID.randomUUID().toString())
        )
        onExecCompleted()
    }

    fun clearSensitiveState() {
        loadGeneration.incrementAndGet()
        loadJob?.cancel()
        loadJob = null
        _accountModel.value = AccountModel()
    }

    fun deleteAccount(accountModel: AccountModel, onExecCompleted: () -> Unit) {
        workScope.launch {
            accountModel.let {
                withContext(Dispatchers.IO) {
                    appDao.deleteAccount(it)
                }
                onExecCompleted()
            }
        }
    }
    fun insertOrUpdate(accountModel: AccountModel, onExecCompleted: () -> Unit) {
        workScope.launch {
            accountModel.let {
                withContext(Dispatchers.IO) {
                    appDao.insertOrUpdateAccount(it)
                }
            }
            onExecCompleted()
        }
    }

    override fun onCleared() {
        clearSensitiveState()
        super.onCleared()
    }
}
