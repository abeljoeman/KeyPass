package com.yogeshpaliyal.common.utils

import com.yogeshpaliyal.common.data.PasswordConfig
import java.security.SecureRandom

class PasswordGenerator(
    val passwordConfig: PasswordConfig
) {
    companion object {
        val totalSymbol = listOf('!', '@', '#', '$', '%', '&', '*', '+', '=', '-', '~', '?', '/', '_')
        private val secureRandom = SecureRandom()
    }

    fun generatePassword(): String {
        val allowedCharacters = ArrayList<Char>()

        if (passwordConfig.includeUppercaseLetters) {
            allowedCharacters.addAll('A'..'Z')
        }
        if (passwordConfig.includeLowercaseLetters) {
            allowedCharacters.addAll('a'..'z')
        }
        if (passwordConfig.includeNumbers) {
            allowedCharacters.addAll('0'..'9')
        }
        if (passwordConfig.includeSymbols) {
            allowedCharacters.addAll(passwordConfig.listOfSymbols)
        }
        if (passwordConfig.includeBlankSpaces) {
            allowedCharacters.add(' ')
        }

        val passwordLength = passwordConfig.length.toInt()
        if (passwordLength <= 0 || allowedCharacters.isEmpty()) {
            return ""
        }

        return buildString(passwordLength) {
            repeat(passwordLength) {
                append(allowedCharacters[secureRandom.nextInt(allowedCharacters.size)])
            }
        }
    }
}
