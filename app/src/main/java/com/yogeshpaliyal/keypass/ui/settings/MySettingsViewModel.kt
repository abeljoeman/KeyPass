package com.yogeshpaliyal.keypass.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MySettingsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application)
