package org.github.raspopov.services

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType.Application
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import org.github.raspopov.cache.DatabaseDriverFactory
import org.github.raspopov.cache.PublicKeysDatabase
import org.github.raspopov.domain.Chat
import org.github.raspopov.dto.PublicKeyInfo
import org.github.raspopov.views.MasterPasswordViewModel
import org.github.raspopov.views.SettingsViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
interface KeysService {
    fun addKeyFor(user: Uuid, account: Uuid, username: String, key: ByteArray)
    fun getPublicKeyOf(user: Uuid): ByteArray
    fun allUsers(account: Uuid): List<Chat>
    fun delete(userId: Uuid, accountId: Uuid)
    suspend fun uploadPublicKey(): Boolean
    suspend fun searchUsersWithKeys(username: String): List<PublicKeyInfo>
    suspend fun loadPublicKeyOf(userId: Uuid): ByteArray
}

class KeysServiceImpl(
    databaseDriverFactory: DatabaseDriverFactory,
    private val settings: SettingsViewModel,
    private val masterPasswordViewModel: MasterPasswordViewModel,
    private val accountService: AccountService,
    private val client: HttpClient,
) : KeysService {

    private val mainAccount
        get() = settings.mainAccount.value!!
    private val baseUrl
        get() = mainAccount.host
    private val masterKeys
        get() = masterPasswordViewModel.uiState.value.keys

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

    override suspend fun uploadPublicKey(): Boolean {
        val infoUrl = "$baseUrl/keys/upload"

        val token = accountService.login()?.token ?: return false

        val response = client.post(infoUrl) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(Application.OctetStream)
            setBody(mainAccount.publicKey)
        }

        return response.status == HttpStatusCode.OK
    }

    override suspend fun searchUsersWithKeys(username: String): List<PublicKeyInfo> {
        val infoUrl = "$baseUrl/keys/search"

        val token = accountService.login()?.token ?: return listOf()

        val response = client.get(infoUrl) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(Application.Json)
            setBody(SearchKeysRequest(username))
        }

        return if (response.status == HttpStatusCode.OK)
            response.body<List<PublicKeyInfo>>()
        else listOf()
    }

    override suspend fun loadPublicKeyOf(userId: Uuid): ByteArray {
        val infoUrl = "$baseUrl/keys/$userId"

        val token = accountService.login()!!.token

        val response = client.get(infoUrl) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(Application.Json)
        }

        if (response.status == HttpStatusCode.OK)
            return response.bodyAsBytes()
        else
            throw IllegalArgumentException("Not Found $response")
    }
}

@Serializable
data class SearchKeysRequest(val username: String)