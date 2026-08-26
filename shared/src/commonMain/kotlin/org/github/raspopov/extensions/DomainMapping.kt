package org.github.raspopov.extensions

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.github.raspopov.domain.Account
import org.github.raspopov.domain.Message
import org.github.raspopov.domain.Status
import org.github.raspopov.domain.Type
import org.github.raspopov.entity.Accounts
import org.github.raspopov.entity.Messages
import kotlin.jvm.JvmName
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@ExperimentalUuidApi
@JvmName("accountToDomain")
fun List<Accounts>.toDomain(): List<Account> {
    return this.map { dbDto ->
        Account(
            Uuid.parse(dbDto.id),
            dbDto.username,
            dbDto.host,
            dbDto.password, // todo надо ли
            dbDto.is_main == 1L
        ).apply {
            salt = dbDto.salt
        }
    }
}
@ExperimentalUuidApi
@JvmName("accountToDomainSeq")
fun Sequence<Accounts>.toDomain(): Sequence<Account> {
    return this.map { dbDto ->
        Account(
            Uuid.parse(dbDto.id),
            dbDto.username,
            dbDto.host,
            dbDto.password, // todo надо ли
            dbDto.is_main == 1L
        ).apply {
            salt = dbDto.salt
            privateKey = dbDto.privateKey
            publicKey = dbDto.publicKey
        }
    }
}

@ExperimentalUuidApi
@JvmName("messagesToDomain")
fun List<Messages>.toDomain(): List<Message> {
    return this.map { it.toDomain() }
}

@ExperimentalUuidApi
@JvmName("messagesToDomainIntermediate")
fun Sequence<Messages>.toDomain(): Sequence<Message> {
    return this.map { it.toDomain() }
}

@ExperimentalUuidApi
@JvmName("messageToDomain")
fun Messages.toDomain(): Message {
    return Message(
        Uuid.parse(this.id),
        Uuid.parse(this.internal_id),
        Uuid.parse(this.author),
        Uuid.parse(this.recipient),
        this.payload, // todo
        mapType(this.type),
        mapStatus(this.status),
        convertUtcMillisToLocalDateTime(this.created),
        convertUtcMillisToLocalDateTime(this.updated)
    )

}

internal fun mapType(type: String): Type {
    return when (type.lowercase()) {
        "text" -> Type.TEXT
        "image" -> Type.IMAGE
        "audio" -> Type.AUDIO
        "video" -> Type.VIDEO
        else -> Type.TEXT // default
    }
}

internal fun mapStatus(status: String): Status {
    return when (status.lowercase()) {
        "sending" -> Status.SENDING
        "confirmed" -> Status.CONFIRMED
        "read" -> Status.READ
        else -> Status.UNKNOWN
    }
}

internal fun convertUtcMillisToLocalDateTime(timestamp: Long): LocalDateTime {
    // 1. Создаём Instant из миллисекунд (интерпретируется как UTC)
    val instant = Instant.fromEpochMilliseconds(timestamp)

    // 2. Преобразуем в LocalDateTime в системной временной зоне
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    return localDateTime
}