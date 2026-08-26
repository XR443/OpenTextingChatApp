package org.github.raspopov

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
    override val isDesktop: Boolean = true
}

actual fun getPlatform(): Platform = WasmPlatform()