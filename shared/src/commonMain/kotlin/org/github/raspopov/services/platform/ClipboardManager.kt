package org.github.raspopov.services.platform

expect object ClipboardManager {
    fun init(context: Any)
    fun copyText(text: String)
}