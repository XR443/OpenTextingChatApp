package org.github.raspopov.extensions

import kotlin.io.encoding.Base64

fun ByteArray.toBase64() = Base64.encode(this)

fun String.fromBase64() = Base64.decode(this)