package org.github.raspopov.services

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType.Application
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import kotlinx.serialization.json.Json
import org.github.raspopov.cache.AccountDatabase
import org.github.raspopov.cache.DatabaseDriverFactory
import org.github.raspopov.domain.Account
import org.github.raspopov.domain.MasterKeys
import org.github.raspopov.dto.AccessTokenDto
import org.github.raspopov.dto.UserInfoDto
import org.github.raspopov.services.platform.CryptoPlatform
import org.github.raspopov.views.MasterPasswordViewModel
import org.github.raspopov.views.SettingsViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
class KtorAccountService(
    private val client: HttpClient,
    private val settings: SettingsViewModel,
    private val masterPasswordViewModel: MasterPasswordViewModel,
    private val databaseDriverFactory: DatabaseDriverFactory,
) : AccountService {

    private val accountDatabase: AccountDatabase =
        AccountDatabase(databaseDriverFactory, masterPasswordViewModel)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val baseUrl
        get() = settings.mainAccount.value?.host

    private val masterKeys: MasterKeys?
        get() = masterPasswordViewModel.uiState.value.keys

    override suspend fun login(): AccessTokenDto? {
        val account = settings.mainAccount.value ?: return null
        return login(account)
    }

    override suspend fun login(account: Account): AccessTokenDto? {
        if (masterKeys == null)
            return null
        try {
            val response = client.post("${account.host}/token") {
                contentType(Application.FormUrlEncoded)
                setBody(
                    listOf(
                        "username" to account.username,
                        "password" to CryptoPlatform.decrypt(masterKeys!!.key, account.password)
                            .decodeToString()
                    ).formUrlEncode()
                )
            }
            if (response.status != HttpStatusCode.OK)
                return null
            return response.body<AccessTokenDto>()
        } catch (e: Exception) {
//            e.printStackTrace()
            println("Exception was threw while trying to get token ${e.message}")
            return null
        }
    }

    override suspend fun login(
        host: String,
        username: String,
        password: String
    ): AccessTokenDto? {
        return login(
            Account(
                Uuid.random(),
                username,
                host,
                CryptoPlatform.encrypt(masterKeys!!.key, password.encodeToByteArray()),
                false
            )
        )
    }

    override suspend fun getInfo(userId: Uuid): UserInfoDto {
        val tokenVal = login(settings.mainAccount.value!!)!!.token
        val response = client.get("$baseUrl/info/about/$userId") {
            header(HttpHeaders.Authorization, "Bearer $tokenVal")
        }
        return response.body<UserInfoDto>()
    }

    override suspend fun getInfoAboutMe(account: Account?): UserInfoDto {
        val myAccount = account ?: settings.mainAccount.value!!
        val tokenVal = login(myAccount)!!.token
        val response = client.get("${myAccount.host}/info/about/me") {
            header(HttpHeaders.Authorization, "Bearer $tokenVal")
        }
        return response.body<UserInfoDto>()
    }

    override suspend fun getInfoAboutMe(
        host: String,
        username: String,
        password: String
    ): UserInfoDto {
        return getInfoAboutMe(
            Account(
                Uuid.random(),
                username,
                host,
                CryptoPlatform.encrypt(masterKeys!!.key, password.encodeToByteArray()),
                false
            )
        )
    }

}