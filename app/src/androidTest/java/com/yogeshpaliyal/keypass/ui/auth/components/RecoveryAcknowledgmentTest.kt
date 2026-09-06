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
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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

        composeRule
            .onNodeWithTag("recoveryAcknowledgment")
            .assert(hasStateDescription("Belum dipahami"))
            .performSemanticsAction(SemanticsActions.CustomActions) { actions ->
                actions.single().action()
            }

        composeRule.runOnIdle {
            assertEquals(1, acknowledgmentCount)
        }
    }
}
