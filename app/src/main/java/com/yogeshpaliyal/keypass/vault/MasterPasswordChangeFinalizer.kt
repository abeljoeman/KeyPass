package com.yogeshpaliyal.keypass.vault

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

internal enum class MasterPasswordFinalizationResult {
    NONE,
    COMPLETED,
    BIOMETRIC_INVALIDATED
}

/**
 * Narrow T129 crash-finalization marker. It stores only non-secret metadata and the fingerprint of
 * the already validated encrypted candidate. A stale pre-commit marker cannot finalize metadata
 * because the active encrypted KDBX fingerprint will not match.
 */
internal class MasterPasswordChangeFinalizer(
    private val activeVaultFile: File,
    private val markerFile: File,
    private val applyMetadata: suspend (passwordHint: String?, disableBiometric: Boolean) -> Unit
) {
    fun prepare(
        encryptedCandidateFingerprint: String,
        passwordHint: String?,
        invalidateBiometric: Boolean
    ) {
        require(FingerprintPattern.matches(encryptedCandidateFingerprint)) {
            "Invalid encrypted candidate fingerprint."
        }
        val hintBytes = passwordHint?.toByteArray(Charsets.UTF_8)
        try {
            if (hintBytes != null && hintBytes.size > MaxHintBytes) {
                throw IOException("Password hint is too large to finalize safely.")
            }
            val parent = markerFile.absoluteFile.parentFile
                ?: throw IOException("Finalization marker must have a parent directory.")
            if (!parent.exists() && !parent.mkdirs()) {
                throw IOException("Could not create the finalization marker directory.")
            }

            val temporary = File(parent, "${markerFile.name}.tmp")
            try {
                FileOutputStream(temporary).use { stream ->
                    DataOutputStream(stream).use { output ->
                        output.writeUTF(MarkerMagic)
                        output.writeUTF(encryptedCandidateFingerprint)
                        output.writeBoolean(hintBytes != null)
                        if (hintBytes != null) {
                            output.writeInt(hintBytes.size)
                            output.write(hintBytes)
                        }
                        output.writeBoolean(invalidateBiometric)
                        output.flush()
                        stream.fd.sync()
                    }
                }
                if (markerFile.exists() && !markerFile.delete()) {
                    throw IOException("Could not replace the finalization marker.")
                }
                if (!temporary.renameTo(markerFile)) {
                    throw IOException("Could not install the finalization marker.")
                }
            } finally {
                if (temporary.exists() && !temporary.delete()) {
                    temporary.deleteOnExit()
                }
            }
        } finally {
            hintBytes?.fill(0)
        }
    }

    suspend fun finalizePending(): MasterPasswordFinalizationResult =
        withContext(Dispatchers.IO + NonCancellable) {
            val marker = readMarkerOrDiscard() ?: return@withContext MasterPasswordFinalizationResult.NONE
            if (!activeVaultFile.isFile || encryptedFingerprint(activeVaultFile) != marker.fingerprint) {
                deleteMarker()
                return@withContext MasterPasswordFinalizationResult.NONE
            }

            applyMetadata(marker.passwordHint, marker.invalidateBiometric)
            deleteMarker()
            if (marker.invalidateBiometric) {
                MasterPasswordFinalizationResult.BIOMETRIC_INVALIDATED
            } else {
                MasterPasswordFinalizationResult.COMPLETED
            }
        }

    private fun readMarkerOrDiscard(): Marker? {
        if (!markerFile.isFile) return null
        return try {
            DataInputStream(FileInputStream(markerFile)).use { input ->
                check(input.readUTF() == MarkerMagic) { "Unknown finalization marker." }
                val fingerprint = input.readUTF()
                check(FingerprintPattern.matches(fingerprint)) { "Invalid finalization fingerprint." }
                val hasHint = input.readBoolean()
                val passwordHint = if (hasHint) {
                    val length = input.readInt()
                    check(length in 0..MaxHintBytes) { "Invalid finalization hint length." }
                    val bytes = ByteArray(length)
                    try {
                        input.readFully(bytes)
                        bytes.toString(Charsets.UTF_8)
                    } finally {
                        bytes.fill(0)
                    }
                } else {
                    null
                }
                val invalidateBiometric = input.readBoolean()
                check(input.read() == -1) { "Unexpected finalization marker data." }
                Marker(fingerprint, passwordHint, invalidateBiometric)
            }
        } catch (_: Exception) {
            deleteMarker()
            null
        }
    }

    private fun encryptedFingerprint(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
            buffer.fill(0)
        }
        return digest.digest().joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    private fun deleteMarker() {
        if (markerFile.exists() && !markerFile.delete()) {
            throw IOException("Could not remove the finalization marker.")
        }
    }

    private data class Marker(
        val fingerprint: String,
        val passwordHint: String?,
        val invalidateBiometric: Boolean
    )

    private companion object {
        const val MarkerMagic = "RAHSA_T129_FINALIZATION_V1"
        const val MaxHintBytes = 1024 * 1024
        val FingerprintPattern = Regex("[0-9a-f]{64}")
    }
}

internal class MasterPasswordChangeCoordinator(
    private val vaultRepository: VaultRepository,
    private val finalizer: MasterPasswordChangeFinalizer
) {
    suspend fun changeMasterPassword(
        currentMasterPassword: CharArray,
        newMasterPassword: CharArray,
        passwordHint: String?,
        biometricWasEnabled: Boolean
    ): MasterPasswordFinalizationResult = withContext(NonCancellable) {
        finalizer.finalizePending()
        try {
            vaultRepository.changeMasterPassword(
                currentMasterPassword = currentMasterPassword,
                newMasterPassword = newMasterPassword
            ) { fingerprint ->
                finalizer.prepare(
                    encryptedCandidateFingerprint = fingerprint,
                    passwordHint = passwordHint,
                    invalidateBiometric = biometricWasEnabled
                )
            }
        } catch (failure: Exception) {
            finalizer.finalizePending()
            throw failure
        }
        finalizer.finalizePending()
    }
}

internal fun masterPasswordFinalizationMarker(filesDirectory: File): File =
    File(filesDirectory, ".rahsa-master-password-finalization")
