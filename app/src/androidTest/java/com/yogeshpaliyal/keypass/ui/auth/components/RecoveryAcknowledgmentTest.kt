package com.yogeshpaliyal.keypass.ui.auth.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.yogeshpaliyal.keypass.R

@RunWith(AndroidJUnit4::class)
class RecoveryAcknowledgmentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun accessibilityActionAcknowledgesWithoutStartingAnotherAction() {
        var acknowledged by mutableStateOf(false)
        var acknowledgmentCount = 0

        composeRule.setContent {
            MaterialTheme {
                RecoveryAcknowledgment(
                    acknowledged = acknowledged,
                    enabled = true,
                    onAcknowledged = {
                        acknowledgmentCount++
                        acknowledged = true
                    }
                )
            }
        }

        val node = composeRule
            .onNodeWithTag("recoveryAcknowledgment")
            .assert(hasStateDescription("Belum dipahami"))
            .fetchSemanticsNode()
        composeRule.runOnIdle {
            node.config[SemanticsActions.CustomActions].single().action()
        }

        composeRule.runOnIdle {
            assertEquals(1, acknowledgmentCount)
        }
    }

    @Test
    fun changeMasterPasswordWarningReusesAccessibleAcknowledgmentWithoutSubmitting() {
        var acknowledged by mutableStateOf(false)
        var acknowledgmentCount = 0

        composeRule.setContent {
            MaterialTheme {
                RecoveryAcknowledgment(
                    acknowledged = acknowledged,
                    enabled = true,
                    onAcknowledged = {
                        acknowledgmentCount++
                        acknowledged = true
                    },
                    warningText = R.string.change_master_password_warning
                )
            }
        }

        composeRule
            .onNodeWithText(
                "The old Master Password will stop working. If you forget the new Master Password, " +
                    "RAHSA cannot open or recover this vault."
            )
            .assertExists()
        val node = composeRule
            .onNodeWithTag("recoveryAcknowledgment")
            .fetchSemanticsNode()
        composeRule.runOnIdle {
            node.config[SemanticsActions.CustomActions].single().action()
        }

        composeRule.runOnIdle {
            assertEquals(1, acknowledgmentCount)
        }
    }
}
