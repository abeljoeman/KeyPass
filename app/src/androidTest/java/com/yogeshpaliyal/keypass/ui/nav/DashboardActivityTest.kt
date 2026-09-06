package com.yogeshpaliyal.keypass.ui.nav

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.yogeshpaliyal.common.data.AccountModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import com.yogeshpaliyal.keypass.vault.KotpassVaultRepository
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidTest
class DashboardActivityTest {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var activityScenarioRule = createAndroidComposeRule<DashboardComposeActivity>()

    @Inject
    lateinit var appDatabase: com.yogeshpaliyal.common.AppDatabase

    @Before
    fun setUp() {
        hiltRule.inject()
        appDatabase.clearAllTables()
    }

    companion object {
        const val TEST_MASTER_PASSWORD = "Test1234!"

        @JvmStatic
        @BeforeClass
        fun prepareDisposableVault() = runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val vaultFile = File(context.filesDir, "vault.kdbx")
            if (vaultFile.exists() && !vaultFile.delete()) {
                error("Could not reset disposable test vault")
            }
            val password = TEST_MASTER_PASSWORD.toCharArray()
            try {
                KotpassVaultRepository(vaultFile).createVault(password)
            } finally {
                password.fill('\u0000')
            }
        }
    }

    private fun getDummyAccount(): AccountModel {
        val accountModel = AccountModel()
        accountModel.title = "Github ${System.currentTimeMillis()}"
        accountModel.username = "yogeshpaliyal"
        accountModel.password = "1234567890"
        accountModel.tags = "social"
        accountModel.site = "https://yogeshpaliyal.com"
        accountModel.notes = "Testing Notes"
        return accountModel
    }

    @Test
    fun addAccountAndDetailAndDeleteTest() {
        ensureVaultUnlocked()
        val accountModel = getDummyAccount()
        addAccount(accountModel)
        checkAccountDetail(accountModel)
        deleteAccount(accountModel)
    }

    private fun ensureVaultUnlocked() {
        activityScenarioRule.waitUntil(timeoutMillis = 30_000) {
            hasNodeWithTag("addCredential") ||
                activityScenarioRule.onAllNodesWithText("Unlock").fetchSemanticsNodes().isNotEmpty() ||
                activityScenarioRule.onAllNodesWithText("Continue").fetchSemanticsNodes().isNotEmpty()
        }

        if (hasNodeWithTag("addCredential")) return

        if (activityScenarioRule.onAllNodesWithText("Unlock").fetchSemanticsNodes().isNotEmpty()) {
            activityScenarioRule.onNodeWithTag("masterPassword").performTextInput(TEST_MASTER_PASSWORD)
            activityScenarioRule.onNodeWithText("Unlock").performClick()
        } else {
            activityScenarioRule.onNodeWithTag("masterPassword").performTextInput(TEST_MASTER_PASSWORD)
            activityScenarioRule.onNodeWithText("Continue").performClick()
            activityScenarioRule.onNodeWithTag("masterPassword").performTextInput(TEST_MASTER_PASSWORD)
            activityScenarioRule.onNodeWithText("Create vault").performClick()
        }

        activityScenarioRule.waitUntil(timeoutMillis = 30_000) { hasNodeWithTag("addCredential") }
    }

    private fun hasNodeWithTag(tag: String): Boolean {
        return try {
            activityScenarioRule.onNodeWithTag(tag).assertExists()
            true
        } catch (_: AssertionError) {
            false
        }
    }

    private fun addAccount(accountModel: AccountModel) {
        // Navigate to add screen
        activityScenarioRule.onNodeWithTag("addCredential").performClick()

        // Fill information on Detail Activity
        activityScenarioRule.onNodeWithTag("accountName").performTextInput(accountModel.title ?: "")

        // generate random password
        activityScenarioRule.onNodeWithTag("username").performTextInput(accountModel.username ?: "")

        activityScenarioRule.onNodeWithTag("password").performTextInput(accountModel.password ?: "")

        activityScenarioRule.onNodeWithTag("website").performScrollTo().performTextInput(accountModel.site ?: "")

        activityScenarioRule.onNodeWithTag("notes").performScrollTo().performTextInput(accountModel.notes ?: "")

        activityScenarioRule.onNodeWithTag("save").performScrollTo().performClick()

        // Save opens the read-only detail view for the new credential.
        activityScenarioRule.waitUntil(timeoutMillis = 30_000) {
            try {
                activityScenarioRule.onNodeWithText("Delete credential").assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    private fun checkAccountDetail(accountModel: AccountModel) {
        // Save opens the read-only detail view.
        activityScenarioRule.onNodeWithText(accountModel.title ?: "").assertIsDisplayed()
        activityScenarioRule.onNodeWithText(accountModel.username ?: "").assertIsDisplayed()
        activityScenarioRule.onNodeWithText(accountModel.site ?: "").assertIsDisplayed()
        activityScenarioRule.onNodeWithText(accountModel.notes ?: "").assertIsDisplayed()
        activityScenarioRule.onNodeWithText("Delete credential").assertIsDisplayed()
    }

    private fun deleteAccount(accountModel: AccountModel) {
        // delete account
        activityScenarioRule.onNodeWithText("Delete credential").performScrollTo().performClick()

        activityScenarioRule.onNodeWithTag("delete").performClick()

        // Delete completes asynchronously while the vault is re-encrypted.
        activityScenarioRule.waitUntil(timeoutMillis = 30_000) {
            try {
                activityScenarioRule.onNodeWithText(accountModel.username ?: "").assertDoesNotExist()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

}
