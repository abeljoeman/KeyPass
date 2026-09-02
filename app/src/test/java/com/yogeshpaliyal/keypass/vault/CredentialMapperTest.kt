package com.yogeshpaliyal.keypass.vault

import app.keemobile.kotpass.constants.BasicField
import app.keemobile.kotpass.cryptography.EncryptedValue
import app.keemobile.kotpass.models.Entry
import app.keemobile.kotpass.models.EntryFields
import app.keemobile.kotpass.models.EntryValue
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CredentialMapperTest {

    @Test
    fun credentialToEntry_preservesFieldsAndProtectsPassword() {
        val credential = representativeCredential()

        val entry = credential.toEntry()

        assertEquals(UUID.fromString(credential.id), entry.uuid)
        assertEquals(credential.title, entry[BasicField.Title]?.content)
        assertEquals(credential.username, entry[BasicField.UserName]?.content)
        assertEquals(credential.password, entry[BasicField.Password]?.content)
        assertEquals(credential.url, entry[BasicField.Url]?.content)
        assertEquals(credential.notes, entry[BasicField.Notes]?.content)
        assertTrue(entry[BasicField.Password] is EntryValue.Encrypted)
    }

    @Test
    fun entryToCredential_preservesFields() {
        val uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        val entry = Entry(
            uuid = uuid,
            fields = EntryFields.of(
                BasicField.Title.key to EntryValue.Plain("Example account"),
                BasicField.UserName.key to EntryValue.Plain("user@example.com"),
                BasicField.Password.key to EntryValue.Encrypted(EncryptedValue.fromString("correct horse battery staple")),
                BasicField.Url.key to EntryValue.Plain("https://example.com/login"),
                BasicField.Notes.key to EntryValue.Plain("Representative note")
            )
        )

        assertEquals(representativeCredential(), entry.toCredential())
    }

    @Test
    fun credentialRoundTrip_preservesRepresentativeValues() {
        val credential = representativeCredential()

        assertEquals(credential, credential.toEntry().toCredential())
    }

    @Test
    fun nullOptionalFields_roundTripAsEmptyKdbxValues() {
        val credential = representativeCredential().copy(url = null, notes = null)

        val entry = credential.toEntry()

        assertNotNull(entry[BasicField.Url])
        assertNotNull(entry[BasicField.Notes])
        assertEquals("", entry[BasicField.Url]?.content)
        assertEquals("", entry[BasicField.Notes]?.content)
        assertNull(entry.toCredential().url)
        assertNull(entry.toCredential().notes)
    }

    @Test
    fun missingAndEmptyFields_mapToCanonicalEmptyOrNullValues() {
        val uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        val missingFieldsEntry = Entry(uuid = uuid, fields = EntryFields.of())
        val emptyOptionalFieldsEntry = Entry(
            uuid = uuid,
            fields = EntryFields.of(
                BasicField.Url.key to EntryValue.Plain(""),
                BasicField.Notes.key to EntryValue.Plain("")
            )
        )

        val missingFieldsCredential = missingFieldsEntry.toCredential()
        assertEquals("", missingFieldsCredential.title)
        assertEquals("", missingFieldsCredential.username)
        assertEquals("", missingFieldsCredential.password)
        assertNull(missingFieldsCredential.url)
        assertNull(missingFieldsCredential.notes)
        assertNull(emptyOptionalFieldsEntry.toCredential().url)
        assertNull(emptyOptionalFieldsEntry.toCredential().notes)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidCredentialId_failsExplicitly() {
        representativeCredential().copy(id = "invalid-id").toEntry()
    }

    private fun representativeCredential() = Credential(
        id = "123e4567-e89b-12d3-a456-426614174000",
        title = "Example account",
        username = "user@example.com",
        password = "correct horse battery staple",
        url = "https://example.com/login",
        notes = "Representative note"
    )
}
