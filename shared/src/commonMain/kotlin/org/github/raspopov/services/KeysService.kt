package org.github.raspopov.services

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType.Application
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.github.raspopov.cache.DatabaseDriverFactory
import org.github.raspopov.cache.PublicKeysDatabase
import org.github.raspopov.domain.Account
import org.github.raspopov.domain.Chat
import org.github.raspopov.domain.Message
import org.github.raspopov.domain.Status
import org.github.raspopov.domain.Type
import org.github.raspopov.dto.AccessTokenDto
import org.github.raspopov.dto.MessageDto
import org.github.raspopov.dto.MessagesResponse
import org.github.raspopov.dto.UserInfoDto
import org.github.raspopov.views.MasterPasswordViewModel
import org.github.raspopov.views.SettingsViewModel
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
interface KeysService {
    fun addKeyFor(user: Uuid, account: Uuid, username: String, key: ByteArray)
    fun getPublicKeyOf(user: Uuid): ByteArray
    fun allUsers(account: Uuid): List<Chat>
    fun delete(userId: Uuid, accountId: Uuid)
}

class KeysServiceImpl(
    databaseDriverFactory: DatabaseDriverFactory,
    private val masterPasswordViewModel: MasterPasswordViewModel,
) : KeysService {

    private val keysDatabase: PublicKeysDatabase =
        PublicKeysDatabase(databaseDriverFactory, masterPasswordViewModel)

    override fun addKeyFor(user: Uuid, account: Uuid, username: String, key: ByteArray) {
        keysDatabase.addPublicKey(user, account, username, key)
    }

    override fun getPublicKeyOf(user: Uuid): ByteArray {
        return keysDatabase.getPublicKey(user)
    }

    override fun allUsers(account: Uuid): List<Chat> {
        return keysDatabase.allUsers(account)
    }

    override fun delete(userId: Uuid, accountId: Uuid) {
        keysDatabase.delete(userId, accountId)
    }
}