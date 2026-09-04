package com.yogeshpaliyal.keypass.ui.home

import com.yogeshpaliyal.common.data.AccountModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyCredentialListAdapterTest {

    @Test
    fun toCredentialListItem_keepsOnlyFieldsUsedByTheList() {
        val account = AccountModel(
            id = 42L,
            title = "Example Account",
            username = "alice@example.com",
            password = "not-for-list-presentation",
            site = "https://example.com/login",
            notes = "Not shown in the list"
        )

        val item = account.toCredentialListItem()

        assertEquals("42", item.id)
        assertEquals("Example Account", item.title)
        assertEquals("alice@example.com", item.username)
        assertEquals("", item.password)
        assertNull(item.url)
        assertNull(item.notes)
    }
}
