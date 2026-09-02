package com.yogeshpaliyal.keypass

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yogeshpaliyal.common.data.DEFAULT_PASSWORD_LENGTH
import com.yogeshpaliyal.common.data.UserSettings
import com.yogeshpaliyal.common.utils.getUserSettings
import com.yogeshpaliyal.common.utils.setDefaultPasswordLength
import com.yogeshpaliyal.common.utils.setUserSettings
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedPreferenceUtilsTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun getKeyPassPasswordLength_test() = runBlocking {
        val result = context.getUserSettings().passwordConfig.length
        assertEquals(DEFAULT_PASSWORD_LENGTH, result)
    }

    @Test
    fun setKeyPassPasswordLength_test() = runBlocking {
        val expectedLength = 8f
        context.setDefaultPasswordLength(expectedLength)
        val result = context.getUserSettings().passwordConfig.length
        assertEquals(expectedLength, result)
    }

    @After
    fun clear() {
        runBlocking {
            context.setUserSettings(UserSettings())
        }
    }
}
