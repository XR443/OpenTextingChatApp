package org.github.raspopov.services.platform

import android.content.ClipData
import android.content.Context
import androidx.core.content.ContextCompat

actual object ClipboardManager {
    private lateinit var appContext: Context

    actual fun init(context: Any) {
        appContext = (context as Context).applicationContext
    }

    actual fun copyText(text: String) {
        val clipboard = ContextCompat.getSystemService(
            appContext,
            android.content.ClipboardManager::class.java
        ) as android.content.ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("text", text))
    }
}