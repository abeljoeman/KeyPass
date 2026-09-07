package com.yogeshpaliyal.keypass.vault

import java.io.File
import java.io.IOException
import java.security.MessageDigest
import com.yogeshpaliyal.common.data.UserSettings
import com.yogeshpaliyal.common.utils.finalizedForMasterPasswordChange
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MasterPasswordChangeFinalizerTest {

    @Test
    fun stalePreCommitMarkerDoesNotApplyMetadata() = runBlocking {
        withFinalizer { active, marker, applied, finalizer ->
            active.writeBytes("old-active".toByteArray())
            finalizer.prepare(fingerprint("validated-candidate".toByteArray()), "new hint", true)

            assertEquals(MasterPasswordFinalizationResult.NONE, finalizer.finalizePending())

            assertTrue(applied.isEmpty())
            assertFalse(marker.exists())
            assertEquals("old-active", active.readText())
        }
    }

    @Test
    fun matchingPostCommitMarkerFinalizesHintAndBiometricInvalidation() = runBlocking {
        withFinalizer { active, marker, applied, finalizer ->
            val candidate = "validated-candidate".toByteArray()
            active.writeBytes("old-active".toByteArray())
            finalizer.prepare(fingerprint(candidate), "edited hint", true)
            active.writeBytes(candidate)

            val result = finalizer.finalizePending()

            assertEquals(MasterPasswordFinalizationResult.BIOMETRIC_INVALIDATED, result)
            assertEquals(listOf(AppliedMetadata("edited hint", true)), applied)
            assertFalse(marker.exists())
            assertEquals("validated-candidate", active.readText())
        }
    }

    @Test
    fun replayTwiceIsHarmlessAndDoesNotRepeatFinalization() = runBlocking {
        withFinalizer { active, marker, applied, finalizer ->
            active.writeBytes("new-active".toByteArray())
            finalizer.prepare(fingerprint(active.readBytes()), "retained hint", false)

            assertEquals(MasterPasswordFinalizationResult.COMPLETED, finalizer.finalizePending())
            assertEquals(MasterPasswordFinalizationResult.NONE, finalizer.finalizePending())

            assertEquals(listOf(AppliedMetadata("retained hint", false)), applied)
            assertFalse(marker.exists())
        }
    }

    @Test
    fun editedHintIsNotAppliedUntilEncryptedCandidateIsAuthoritative() = runBlocking {
        withFinalizer { active, _, applied, finalizer ->
            val candidate = "new-encrypted-active".toByteArray()
            active.writeBytes("old-encrypted-active".toByteArray())
            finalizer.prepare(fingerprint(candidate), "edited hint", false)

            assertTrue(applied.isEmpty())
            active.writeBytes(candidate)
            finalizer.finalizePending()

            assertEquals(listOf(AppliedMetadata("edited hint", false)), applied)
        }
    }

    @Test
    fun clearedHintAndNoBiometricStateFinalizeWithoutFabricatedInvalidation() = runBlocking {
        withFinalizer { active, _, applied, finalizer ->
            active.writeBytes("new-active".toByteArray())
            finalizer.prepare(fingerprint(active.readBytes()), null, false)

            val result = finalizer.finalizePending()

            assertEquals(MasterPasswordFinalizationResult.COMPLETED, result)
            assertEquals(1, applied.size)
            assertNull(applied.single().passwordHint)
            assertFalse(applied.single().disableBiometric)
        }
    }

    @Test
    fun markerContainsNoMasterPasswordOrDecryptedVaultData() = runBlocking {
        withFinalizer { active, marker, _, finalizer ->
            active.writeBytes("encrypted-kdbx-bytes".toByteArray())
            finalizer.prepare(fingerprint(active.readBytes()), "ordinary hint", true)

            val markerText = marker.readBytes().toString(Charsets.ISO_8859_1)
            assertFalse(markerText.contains("old-master-password"))
            assertFalse(markerText.contains("new-master-password"))
            assertFalse(markerText.contains("decrypted-vault-secret"))
            assertTrue(markerText.contains("ordinary hint"))
        }
    }

    @Test
    fun malformedMarkerFailsClosedWithoutChangingActiveOrMetadata() = runBlocking {
        withFinalizer { active, marker, applied, finalizer ->
            val activeBytes = "authoritative-active".toByteArray()
            active.writeBytes(activeBytes)
            marker.writeBytes("not-a-valid-t129-marker".toByteArray())

            assertEquals(MasterPasswordFinalizationResult.NONE, finalizer.finalizePending())

            assertTrue(applied.isEmpty())
            assertFalse(marker.exists())
            assertTrue(activeBytes.contentEquals(active.readBytes()))
        }
    }

    @Test
    fun oversizedHintFailsBeforeInstallingFinalizationMarker() = runBlocking {
        withFinalizer { active, marker, applied, finalizer ->
            active.writeBytes("old-active".toByteArray())

            try {
                finalizer.prepare(
                    fingerprint("validated-candidate".toByteArray()),
                    "h".repeat(1024 * 1024 + 1),
                    false
                )
                throw AssertionError("Expected oversized finalization hint to fail.")
            } catch (_: IOException) {
                // Expected: promotion has not begun because the marker was not installed.
            }

            assertFalse(marker.exists())
            assertTrue(applied.isEmpty())
            assertEquals("old-active", active.readText())
        }
    }

    @Test
    fun metadataFinalizationDoesNotPopulateOrChangeLegacyDatabasePassword() {
        val withoutLegacyPassword = UserSettings(
            dbPassword = null,
            passwordHint = "old hint",
            isBiometricEnable = true
        ).finalizedForMasterPasswordChange("new hint", disableBiometric = true)
        val withUnrelatedLegacyDatabaseKey = UserSettings(
            dbPassword = "unrelated-room-database-key",
            passwordHint = "old hint",
            isBiometricEnable = false
        ).finalizedForMasterPasswordChange("new hint", disableBiometric = false)

        assertNull(withoutLegacyPassword.dbPassword)
        assertEquals("unrelated-room-database-key", withUnrelatedLegacyDatabaseKey.dbPassword)
        assertEquals("new hint", withoutLegacyPassword.passwordHint)
        assertFalse(withoutLegacyPassword.isBiometricEnable)
    }

    private suspend fun withFinalizer(
        block: suspend (
            active: File,
            marker: File,
            applied: MutableList<AppliedMetadata>,
            finalizer: MasterPasswordChangeFinalizer
        ) -> Unit
    ) {
        val directory = File.createTempFile("t129-finalizer-", "").also {
            check(it.delete() && it.mkdirs())
        }
        try {
            val active = File(directory, "vault.kdbx")
            val marker = File(directory, ".rahsa-master-password-finalization")
            val applied = mutableListOf<AppliedMetadata>()
            val finalizer = MasterPasswordChangeFinalizer(active, marker) { hint, disableBiometric ->
                applied += AppliedMetadata(hint, disableBiometric)
            }
            block(active, marker, applied, finalizer)
        } finally {
            directory.deleteRecursively()
        }
    }

    private fun fingerprint(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString(separator = "") { byte -> "%02x".format(byte) }

    private data class AppliedMetadata(
        val passwordHint: String?,
        val disableBiometric: Boolean
    )
}
