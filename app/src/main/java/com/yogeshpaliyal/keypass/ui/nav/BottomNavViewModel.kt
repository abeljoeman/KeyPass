package com.yogeshpaliyal.keypass.ui.nav

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/*
* @author Yogesh Paliyal
* techpaliyal@gmail.com
* https://techpaliyal.com
* created on 31-01-2021 14:11
*/
@HiltViewModel
class BottomNavViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    private val _navigationList =
        MutableLiveData<List<NavigationModelItem>>(NavigationModel.navigationMenuItems)

    val navigationList: LiveData<List<NavigationModelItem>>
        get() = _navigationList
}
