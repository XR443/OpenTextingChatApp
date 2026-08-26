package org.github.raspopov.domain

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

enum class Status() {
    SENDING,
    CONFIRMED,
    READ,
    UNKNOWN
}

enum class Type() {
    TEXT,
    IMAGE,
    AUDIO,
    VIDEO
}

@ExperimentalUuidApi
@Serializable
class Message(
    val id: Uuid?,

    val internalId: Uuid,

    val author: Uuid,
    val recipient: Uuid,

    var payload: ByteArray?,
    val type: Type,

    var status: Status,

    val created: LocalDateTime = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()), // todo default?
    var updated: LocalDateTime? = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()), // todo default?
) {
    val isRead: Boolean
        get() = status == Status.READ
}