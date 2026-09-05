package com.yogeshpaliyal.keypass.utils

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import androidx.core.content.ContextCompat
import java.util.UUID

private const val ClipboardClearDelayMillis = 60_000L

fun copySensitiveTextToClipboard(
    context: Context,
    text: String,
    label: String
) {
    val clipboard = ContextCompat.getSystemService(context, ClipboardManager::class.java)
        ?: return
    val ownedLabel = "$label:${UUID.randomUUID()}"
    val clip = ClipData.newPlainText(ownedLabel, text).apply {
        description.extras = PersistableBundle().apply {
            putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
        }
    }
    clipboard.setPrimaryClip(clip)

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Handler(Looper.getMainLooper()).postDelayed(
            { clearClipboardIfOwned(clipboard, ownedLabel) },
            ClipboardClearDelayMillis
        )
    }
}

private fun clearClipboardIfOwned(
    clipboard: ClipboardManager,
    ownedLabel: String
) {
    if (clipboard.primaryClipDescription?.label?.toString() != ownedLabel) {
        return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        clipboard.clearPrimaryClip()
    } else {
        clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
    }
}
