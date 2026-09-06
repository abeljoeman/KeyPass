package com.yogeshpaliyal.keypass.ui.auth.components

import com.yogeshpaliyal.keypass.ui.redux.states.AuthState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ButtonBarTest {
    private val confirmation = AuthState.ConfirmPassword("correct password")

    @Test
    fun `matching password still requires recovery acknowledgment`() {
        assertFalse(
            isAuthActionEnabled(confirmation, "correct password", false, false)
        )
    }

    @Test
    fun `matching password and recovery acknowledgment enable create`() {
        assertTrue(
            isAuthActionEnabled(confirmation, "correct password", true, false)
        )
    }

    @Test
    fun `mismatched password remains disabled after acknowledgment`() {
        assertFalse(
            isAuthActionEnabled(confirmation, "incorrect password", true, false)
        )
    }

    @Test
    fun `create remains disabled while authentication is in progress`() {
        assertFalse(
            isAuthActionEnabled(confirmation, "correct password", true, true)
        )
    }

    @Test
    fun `existing login and create password enablement is unchanged`() {
        assertTrue(isAuthActionEnabled(AuthState.Login, "", false, false))
        assertTrue(isAuthActionEnabled(AuthState.CreatePassword, "", false, false))
    }
}
