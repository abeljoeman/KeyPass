package com.yogeshpaliyal.keypass.ui.detail

import com.yogeshpaliyal.common.data.AccountModel
import com.yogeshpaliyal.keypass.vault.Credential
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

    @Test
    fun withCredentialDetail_updatesPrototypeFieldsAndPreservesLegacyMetadata() {
        val account = AccountModel(
            id = 42L,
            title = "Old title",
            uniqueId = "legacy-unique-id",
            username = "old-user",
            password = "old-password",
            secret = "legacy-secret",
            site = "https://old.example.com",
            notes = "Old notes",
            tags = "legacy-only"
        )
        val credential = Credential(
            id = "ignored-by-legacy-boundary",
            title = "Updated title",
            username = "updated-user",
            password = "updated-password",
            url = "https://new.example.com",
            notes = "Updated notes"
        )

        val updated = account.withCredentialDetail(credential)

        assertEquals(42L, updated.id)
        assertEquals("legacy-unique-id", updated.uniqueId)
        assertEquals("legacy-secret", updated.secret)
        assertEquals("legacy-only", updated.tags)
        assertEquals("Updated title", updated.title)
        assertEquals("updated-user", updated.username)
        assertEquals("updated-password", updated.password)
        assertEquals("https://new.example.com", updated.site)
        assertEquals("Updated notes", updated.notes)
    }
}
