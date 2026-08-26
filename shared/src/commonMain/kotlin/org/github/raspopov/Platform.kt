package org.github.raspopov

import org.github.raspopov.services.platform.CryptoPlatform
import org.github.raspopov.services.platform.E2eeCrypto

interface Platform {
    val name: String

    val isDesktop: Boolean
}

expect fun getPlatform(): Platform