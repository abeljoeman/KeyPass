package com.yogeshpaliyal.common.utils

import androidx.datastore.core.Serializer
import com.yogeshpaliyal.common.data.UserSettings
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class UserSettingsSerializer(
    private val cryptoManager: CryptoManager
) : Serializer<UserSettings> {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override val defaultValue: UserSettings
        get() = UserSettings()

    override suspend fun readFrom(input: InputStream): UserSettings {
        val decryptedBytes = cryptoManager.decrypt(input)

        val decodedString = decryptedBytes.decodeToString()
        return json.decodeFromString(
            deserializer = UserSettings.serializer(),
            string = decodedString
        )

    }

    override suspend fun writeTo(t: UserSettings, output: OutputStream) {
        val encodedSettings = json.encodeToString(
            serializer = UserSettings.serializer(),
            value = t
        )
        cryptoManager.encrypt(
            bytes = encodedSettings.encodeToByteArray(),
            outputStream = output
        )
    }
}
