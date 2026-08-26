package org.github.raspopov

import org.github.raspopov.services.platform.CryptoPlatform
import org.github.raspopov.services.platform.E2eeCrypto

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
    override val isDesktop: Boolean = true
}

actual fun getPlatform(): Platform = JVMPlatform()
