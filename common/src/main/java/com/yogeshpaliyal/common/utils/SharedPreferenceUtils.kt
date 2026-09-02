package com.yogeshpaliyal.common.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yogeshpaliyal.common.data.DEFAULT_PASSWORD_LENGTH
import com.yogeshpaliyal.common.data.PasswordConfig
import com.yogeshpaliyal.common.data.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

val Context.dataStore by preferencesDataStore(
    name = "settings"
)

private var userSettingsDataStore: DataStore<UserSettings>? = null
private fun Context.getUserSettingsDataStore(): DataStore<UserSettings> {
    val res = userSettingsDataStore ?: UserSettingsDataStore(this).getDataStore()
    userSettingsDataStore = res
    return res
}

suspend fun Context.setPasswordConfig(passwordConfig: PasswordConfig) {
    getUserSettingsDataStore().updateData {
        it.copy(passwordConfig = passwordConfig)
    }
}

suspend fun Context.getUserSettings(): UserSettings {
    return getUserSettingsDataStore().data.firstOrNull() ?: UserSettings()
}

fun Context.getUserSettingsFlow(): Flow<UserSettings> {
    return getUserSettingsDataStore().data
}

suspend fun Context.getUserSettingsOrNull(): UserSettings? {
    return getUserSettingsDataStore().data.firstOrNull()
}

suspend fun Context.setKeyPassPassword(password: String?) {
    getUserSettingsDataStore().updateData {
        it.copy(keyPassPassword = password)
    }
}

suspend fun Context.setDefaultPasswordLength(password: Float) {
    getUserSettingsDataStore().updateData {
        it.copy(passwordConfig = it.passwordConfig.copy(length = password))
    }
}

suspend fun Context.setBiometricEnable(isBiometricEnable: Boolean) {
    getUserSettingsDataStore().updateData {
        it.copy(isBiometricEnable = isBiometricEnable)
    }
}

suspend fun Context.setBiometricLoginTimeoutEnable(biometricLoginTimeoutEnable: Boolean) {
    getUserSettingsDataStore().updateData {
        it.copy(biometricLoginTimeoutEnable = biometricLoginTimeoutEnable)
    }
}


suspend fun Context.setDatabasePassword(databasePassword: String) {
    getUserSettingsDataStore().updateData {
        it.copy(dbPassword = databasePassword)
    }
}

suspend fun Context.setUserSettings(userSettings: UserSettings) {
    getUserSettingsDataStore().updateData {
        userSettings
    }
}

suspend fun Context.setPasswordHint(passwordHint: String?) {
    getUserSettingsDataStore().updateData {
        it.copy(passwordHint = passwordHint)
    }
}

suspend fun Context.getPasswordHint(): String? {
    return getUserSettings().passwordHint
}

suspend fun Context.updateLastPasswordLoginTime(lastPasswordLoginTime: Long?) {
    getUserSettingsDataStore().updateData {
        it.copy(lastPasswordLoginTime = lastPasswordLoginTime)
    }
}

private val BIOMETRIC_ENABLE = booleanPreferencesKey("biometric_enable")
private val KEYPASS_PASSWORD = stringPreferencesKey("keypass_password")
private val KEYPASS_PASSWORD_LENGTH = floatPreferencesKey("keypass_password_length")

suspend fun Context.migrateOldDataToNewerDataStore() {
    var userSettings = getUserSettingsOrNull() ?: return

    val olderData = this.dataStore.data.first()

    if (olderData.contains(BIOMETRIC_ENABLE)) {
        userSettings = userSettings.copy(isBiometricEnable = olderData[BIOMETRIC_ENABLE] ?: false)
    }

    if (olderData.contains(KEYPASS_PASSWORD)) {
        userSettings = userSettings.copy(keyPassPassword = olderData[KEYPASS_PASSWORD])
    }

    if (olderData.contains(KEYPASS_PASSWORD_LENGTH)) {
        userSettings = userSettings.copy(defaultPasswordLength = olderData[KEYPASS_PASSWORD_LENGTH] ?: DEFAULT_PASSWORD_LENGTH)
    }

    if (userSettings.defaultPasswordLength != DEFAULT_PASSWORD_LENGTH) {
        userSettings = userSettings.copy(passwordConfig = userSettings.passwordConfig.copy(length = userSettings.defaultPasswordLength))
    }

    clearDataStoreOld()
    setUserSettings(userSettings)
}

private suspend fun Context.clearDataStoreOld() {
    this.dataStore.edit {
        it.remove(BIOMETRIC_ENABLE)
        it.remove(KEYPASS_PASSWORD)
        it.remove(KEYPASS_PASSWORD_LENGTH)
    }
}
