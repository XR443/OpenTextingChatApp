package org.github.raspopov.domain

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
class Account(
    var id: Uuid, // из-за использования мусорного id
    var username: String,
    var host: String,
    // todo CharArray
    var password: ByteArray,
    // todo нужен ли main или лучше их разделить на вкладки как telegram
    val isMain: Boolean = false,
) {
    var salt: ByteArray? = null
    var privateKey: ByteArray? = null
    var publicKey: ByteArray? = null
}

@ExperimentalUuidApi
data class AppSettings(var mainAccount: Account?) {
}