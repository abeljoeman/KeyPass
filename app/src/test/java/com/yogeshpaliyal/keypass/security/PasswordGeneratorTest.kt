package com.yogeshpaliyal.keypass.security

import com.yogeshpaliyal.common.data.PasswordConfig
import com.yogeshpaliyal.common.utils.PasswordGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordGeneratorTest {

    @Test
    fun generatePassword_usesRequestedLengthAndCombinedAllowedAlphabet() {
        val symbols = listOf('!', '?')
        val config = config(
            length = 256,
            uppercase = true,
            lowercase = true,
            numbers = true,
            symbols = true,
            symbolList = symbols,
            blankSpaces = true,
        )
        val allowed = (
            ('A'..'Z').toList() +
                ('a'..'z').toList() +
                ('0'..'9').toList() +
                symbols +
                listOf(' ')
            ).toSet()

        repeat(5) {
            val generated = PasswordGenerator(config).generatePassword()

            assertEquals(256, generated.length)
            assertTrue(generated.all { it in allowed })
        }
    }

    @Test
    fun generatePassword_respectsUppercaseOnlyConfiguration() {
        val generated = PasswordGenerator(
            config(length = 64, uppercase = true)
        ).generatePassword()

        assertEquals(64, generated.length)
        assertTrue(generated.all { it in 'A'..'Z' })
    }

    @Test
    fun generatePassword_respectsCustomSymbolAlphabet() {
        val symbols = listOf('^', ':')
        val generated = PasswordGenerator(
            config(length = 64, symbols = true, symbolList = symbols)
        ).generatePassword()

        assertEquals(64, generated.length)
        assertTrue(generated.all { it in symbols })
    }

    @Test
    fun generatePassword_respectsBlankSpaceOnlyConfiguration() {
        val generated = PasswordGenerator(
            config(length = 12, blankSpaces = true)
        ).generatePassword()

        assertEquals(" ".repeat(12), generated)
    }

    @Test
    fun generatePassword_returnsEmptyWhenNoCharactersAreEnabled() {
        val generated = PasswordGenerator(config(length = 32)).generatePassword()

        assertTrue(generated.isEmpty())
    }

    private fun config(
        length: Int,
        uppercase: Boolean = false,
        lowercase: Boolean = false,
        numbers: Boolean = false,
        symbols: Boolean = false,
        symbolList: List<Char> = PasswordGenerator.totalSymbol,
        blankSpaces: Boolean = false,
    ): PasswordConfig = PasswordConfig(
        length = length.toFloat(),
        includeUppercaseLetters = uppercase,
        includeLowercaseLetters = lowercase,
        includeSymbols = symbols,
        listOfSymbols = symbolList,
        includeNumbers = numbers,
        includeBlankSpaces = blankSpaces,
        password = "",
    )
}
