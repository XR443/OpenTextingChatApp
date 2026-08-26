package org.github.raspopov.services.platform

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual object ClipboardManager {
    actual fun init(context: Any) {
    }

    actual fun copyText(text: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val selection = StringSelection(text)
        clipboard.setContents(selection, null)
    }
}