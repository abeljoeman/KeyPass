package com.yogeshpaliyal.keypass.ui.changeMasterPassword

import com.yogeshpaliyal.keypass.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangeMasterPasswordValidationTest {

    @Test
    fun currentPasswordIsRequired() {
        assertFalse(enabled(current = ""))
    }

    @Test
    fun newPasswordIsRequired() {
        assertFalse(enabled(new = "", confirmation = ""))
    }

    @Test
    fun matchingConfirmationIsRequired() {
        assertFalse(enabled(confirmation = "different"))
    }

    @Test
    fun acknowledgmentIsRequired() {
        assertFalse(enabled(acknowledged = false))
    }

    @Test
    fun inProgressOperationCannotBeSubmittedAgain() {
        assertFalse(enabled(inProgress = true))
    }

    @Test
    fun weakPasswordDoesNotBecomeAPolicyGate() {
        assertTrue(
            isChangeMasterPasswordActionEnabled(
                currentPassword = "current",
                newPassword = "a",
                confirmation = "a",
                acknowledged = true,
                inProgress = false
            )
        )
        assertEquals(R.string.password_strength_very_weak, passwordStrengthLabel(0))
    }

    @Test
    fun strengthLabelsCoverEstimatorRange() {
        assertEquals(R.string.password_strength_very_weak, passwordStrengthLabel(0))
        assertEquals(R.string.password_strength_weak, passwordStrengthLabel(1))
        assertEquals(R.string.password_strength_fair, passwordStrengthLabel(2))
        assertEquals(R.string.password_strength_strong, passwordStrengthLabel(3))
        assertEquals(R.string.password_strength_very_strong, passwordStrengthLabel(4))
    }

    private fun enabled(
        current: String = "current",
        new: String = "new",
        confirmation: String = new,
        acknowledged: Boolean = true,
        inProgress: Boolean = false
    ): Boolean = isChangeMasterPasswordActionEnabled(
        currentPassword = current,
        newPassword = new,
        confirmation = confirmation,
        acknowledged = acknowledged,
        inProgress = inProgress
    )
}
