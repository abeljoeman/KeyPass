package com.yogeshpaliyal.keypass.ui.detail

import com.yogeshpaliyal.common.data.AccountModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyCredentialDetailAdapterTest {

    @Test
    fun toCredentialDetail_mapsPrototypeCredentialFields() {
        val account = AccountModel(
            id = 42L,
            title = "Example Account",
            username = "alice@example.com",
            password = "correct horse battery staple",
            site = "https://example.com/login",
            notes = "Recovery codes are stored elsewhere",
            tags = "legacy-only"
        )

        val credential = account.toCredentialDetail()

        assertEquals("42", credential.id)
        assertEquals("Example Account", credential.title)
        assertEquals("alice@example.com", credential.username)
        assertEquals("correct horse battery staple", credential.password)
        assertEquals("https://example.com/login", credential.url)
        assertEquals("Recovery codes are stored elsewhere", credential.notes)
    }

    @Test
    fun toCredentialDetail_handlesMissingLegacyValues() {
        val credential = AccountModel().toCredentialDetail()

        assertEquals("", credential.id)
        assertEquals("", credential.title)
        assertEquals("", credential.username)
        assertEquals("", credential.password)
        assertNull(credential.url)
        assertNull(credential.notes)
    }
}
