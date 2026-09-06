package com.yogeshpaliyal.keypass.vault

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Narrow filesystem seam for vault promotion. It keeps production storage local while making
 * promotion-boundary failures deterministic in repository tests.
 */
internal interface VaultFileOperations {
    fun createTempFile(prefix: String, suffix: String, directory: File): File
    fun openInput(file: File): FileInputStream
    fun openOutput(file: File): FileOutputStream
    fun move(source: File, destination: File): Boolean
    fun delete(file: File): Boolean
}

internal object DefaultVaultFileOperations : VaultFileOperations {
    override fun createTempFile(prefix: String, suffix: String, directory: File): File =
        File.createTempFile(prefix, suffix, directory)

    override fun openInput(file: File): FileInputStream = FileInputStream(file)

    override fun openOutput(file: File): FileOutputStream = FileOutputStream(file)

    override fun move(source: File, destination: File): Boolean = source.renameTo(destination)

    override fun delete(file: File): Boolean = file.delete()
}
