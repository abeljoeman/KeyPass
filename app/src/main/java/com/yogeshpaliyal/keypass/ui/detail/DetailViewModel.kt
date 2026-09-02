package com.yogeshpaliyal.keypass.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yogeshpaliyal.common.data.AccountModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
class DetailViewModel @Inject constructor(
    val app: Application,
    val appDb: com.yogeshpaliyal.common.AppDatabase
) : AndroidViewModel(app) {

    private val _accountModel by lazy { MutableStateFlow<AccountModel>(AccountModel()) }
    val accountModel: StateFlow<AccountModel> = _accountModel

    fun loadAccount(id: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (id == null) {
                _accountModel.emit(AccountModel())
            } else {
                _accountModel.emit(appDb.getDao().getAccount(id) ?: AccountModel())
            }
        }
    }

    fun setAccountModel(accountModel: AccountModel) {
        _accountModel.value = accountModel
    }

    fun deleteAccount(accountModel: AccountModel, onExecCompleted: () -> Unit) {
        viewModelScope.launch {
            accountModel.let {
                withContext(Dispatchers.IO) {
                    appDb.getDao().deleteAccount(it)
                }
                onExecCompleted()
            }
        }
    }
    fun insertOrUpdate(accountModel: AccountModel, onExecCompleted: () -> Unit) {
        viewModelScope.launch {
            accountModel.let {
                withContext(Dispatchers.IO) {
                    appDb.getDao().insertOrUpdateAccount(it)
                }
            }
            onExecCompleted()
        }
    }
}
