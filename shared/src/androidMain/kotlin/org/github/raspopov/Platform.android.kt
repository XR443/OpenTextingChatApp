package org.github.raspopov

import android.os.Build
import org.github.raspopov.services.platform.CryptoPlatform

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val isDesktop: Boolean = false
}

actual fun getPlatform(): Platform = AndroidPlatform()
