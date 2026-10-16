package org.github.raspopov.domain

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

enum class Status() {
    SENDING,
    CONFIRMED,
    READ,
    UNKNOWN
}

class Extra {
    enum class Params {
        REPLY,
        FORWARD_FROM
    }

    companion object {
        fun reply(replied: Message?): Map<String, String> {
            if (replied?.id != null)
                return mapOf(Params.REPLY.name to replied.id.toString())
            return emptyMap()
        }

        fun forward(replied: Message?): Map<String, String> {
            if (replied?.id != null)
                return mapOf(Params.FORWARD_FROM.name to replied.author.toString())
            return emptyMap()
        }
    }
}

@Serializable
data class ContentType(
    val mimeType: String,        // "video/mp4"
    val encrypted: Boolean = false,
    val compression: Compression? = null,
    val quality: Quality? = null,
    val metadata: Map<String, String>? = null,
    val extraParams: Map<String, String> = emptyMap()
) {
    // Полный идентификатор для БД
    val fullType: String
        get() = buildString {
            append(mimeType)

            // Добавляем только не-null параметры
            val params = mutableListOf<String>()

            encrypted.let { params.add("encrypted=${it.toString().lowercase()}") }
            compression?.let { params.add("compression=${it}") }
            quality?.let { params.add("quality=${it}") }
            // Metadata сериализуем в JSON
            metadata?.let { meta ->
                if (meta.isNotEmpty()) {
                    val jsonStr = Json.encodeToString(meta)
                    params.add("metadata=$jsonStr")
                }
            }
            extraParams.let { extra ->
                if (extra.isNotEmpty()) {
                    val jsonStr = Json.encodeToString(extra)
                    params.add("extra_params=$jsonStr")
                }
            }
            if (params.isNotEmpty()) {
                append("; ")
                append(params.joinToString("; "))
            }
        }

    // Расширение файла
    val extension: String?
        get() = when (mimeType) {
            "video/mp4" -> "mp4"
            "image/jpeg" -> "jpg"
            "text/plain" -> "txt"
            "application/pdf" -> "pdf"
            else -> null
        }

    companion object {
        val TEXT_PLAIN = ContentType("text/plain")
        val TEXT_ENCRYPTED = ContentType("text/plain", encrypted = true)

        fun textPlain(
            metadata: Map<String, String>? = null,
            extraParams: Map<String, String> = emptyMap()
        ) =
            ContentType("text/plain", metadata = metadata, extraParams = extraParams)

        fun textEncrypted(
            metadata: Map<String, String>? = null,
            extraParams: Map<String, String> = emptyMap()
        ) =
            ContentType(
                "text/plain",
                encrypted = true,
                metadata = metadata,
                extraParams = extraParams
            )

        fun videoMp4(
            encrypted: Boolean = false,
            quality: Quality? = null,
            metadata: Map<String, String>? = null,
            extraParams: Map<String, String> = emptyMap()
        ) = ContentType(
            mimeType = "video/mp4",
            encrypted = encrypted,
            quality = quality,
            metadata = metadata,
            extraParams = extraParams
        )

        fun imageJpeg(
            encrypted: Boolean = false,
            quality: Quality? = null,
            metadata: Map<String, String>? = null,
            extraParams: Map<String, String> = emptyMap()
        ) = ContentType(
            mimeType = "image/jpeg",
            encrypted = encrypted,
            quality = quality,
            metadata = metadata,
            extraParams = extraParams
        )

        fun pdfEncrypted(
            metadata: Map<String, String>? = null,
            extraParams: Map<String, String> = emptyMap()
        ) =
            ContentType(
                "application/pdf",
                encrypted = true,
                metadata = metadata,
                extraParams = extraParams
            )

        fun jsonEncryptedGzip(
            metadata: Map<String, String>? = null,
            extraParams: Map<String, String> = emptyMap()
        ) =
            ContentType(
                mimeType = "application/json",
                encrypted = true,
                compression = Compression.GZIP,
                metadata = metadata,
                extraParams = extraParams
            )

        fun fromFullType(fullType: String): ContentType {
            val parts = fullType.split("; ")
            val mimeType = parts[0].trim()

            val params = mutableMapOf<String, String>()
            for (i in 1 until parts.size) {
                val keyValue = parts[i].split("=", limit = 2)
                if (keyValue.size == 2) {
                    params[keyValue[0].trim()] = keyValue[1].trim()
                }
            }

            // Парсим известные параметры
            val encrypted = params["encrypted"]?.toBooleanStrictOrNull() ?: false
            val compression = params["compression"]?.let {
                Compression.entries.find { value -> value.name == it }
            }
            val quality = params["quality"]?.let {
                Quality.entries.find { value -> value.name == it }
            }
            // Парсим metadata из JSON
            val metadata = params["metadata"]?.let { metaStr ->
                try {
                    Json.decodeFromString<Map<String, String>>(metaStr)
                } catch (e: Exception) {
                    // Если невалидный JSON, сохраняем как строку
                    mapOf("raw" to metaStr)
                }
            }

            val extra = params["extra_params"]?.let { metaStr ->
                try {
                    Json.decodeFromString<Map<String, String>>(metaStr)
                } catch (e: Exception) {
                    // Если невалидный JSON, сохраняем как строку
                    mapOf("raw" to metaStr)
                }
            }
            return ContentType(
                mimeType = mimeType,
                encrypted = encrypted,
                compression = compression,
                quality = quality,
                metadata = metadata,
                extraParams = extra ?: emptyMap()
            )
        }
    }
}

// Типы для безопасности
enum class Compression { GZIP, ZSTD }
enum class Quality { LOW, MEDIUM, HIGH }

@ExperimentalUuidApi
@Serializable
class Message(
    val id: Uuid?,

    val internalId: Uuid,

    val author: Uuid,
    val recipient: Uuid,

    var payload: ByteArray?,

    val contentType: ContentType,

    var status: Status,

    val created: LocalDateTime = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()), // todo default?
    var updated: LocalDateTime? = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()), // todo default?
    var payloadKey: ByteArray?,
) {

    var replied: Message? = null

    val isRead: Boolean
        get() = status == Status.READ
}