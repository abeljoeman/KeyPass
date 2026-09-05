package com.yogeshpaliyal.keypass.ui.generate.ui.utils

import android.content.Context
import com.yogeshpaliyal.keypass.utils.copySensitiveTextToClipboard

fun copyTextToClipboard(
    context: Context,
    text: String,
    label: String
) {
    copySensitiveTextToClipboard(
        context = context,
        text = text,
        label = label
    )
}
