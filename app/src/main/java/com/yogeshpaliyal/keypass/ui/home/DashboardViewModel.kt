package com.yogeshpaliyal.keypass.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yogeshpaliyal.common.AppDatabase
import com.yogeshpaliyal.common.data.AccountModel
import com.yogeshpaliyal.common.db.DbDao
import dagger.hilt.android.lifecycle.HiltViewModel
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
* created on 30-01-2021 23:02
*/
@HiltViewModel
class DashboardViewModel private constructor(
    private val appDao: DbDao,
    private val externalScope: CoroutineScope?,
    @Suppress("UNUSED_PARAMETER") constructorMarker: Unit
) : ViewModel() {

    @Inject
    constructor(appDb: AppDatabase) : this(appDb.getDao(), null, Unit)

    internal constructor(appDao: DbDao, scope: CoroutineScope) : this(appDao, scope, Unit)

    private val workScope: CoroutineScope
        get() = externalScope ?: viewModelScope
    private val queryGeneration = AtomicLong()
    private var queryJob: Job? = null

    private val _accounts = MutableStateFlow<List<AccountModel>>(emptyList())
    val accounts: StateFlow<List<AccountModel>> = _accounts.asStateFlow()

    fun queryUpdated(
        keyword: String?,
        tag: String?,
        sortField: String?,
        sortAscending: Boolean = true
    ): Job {
        val generation = queryGeneration.incrementAndGet()
        queryJob?.cancel()
        return workScope.launch {
            val result = withContext(Dispatchers.IO) {
                if (sortAscending) {
                    appDao.getAllAccountsAscending(keyword ?: "", tag, sortField)
                } else {
                    appDao.getAllAccountsDescending(keyword ?: "", tag, sortField)
                }
            }
            if (queryGeneration.get() == generation) {
                _accounts.value = result
            }
        }.also { queryJob = it }
    }

    fun clearSensitiveState() {
        queryGeneration.incrementAndGet()
        queryJob?.cancel()
        queryJob = null
        _accounts.value = emptyList()
    }

    override fun onCleared() {
        clearSensitiveState()
        super.onCleared()
    }
}
