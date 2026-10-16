package org.github.raspopov.services

import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.github.raspopov.domain.Chat
import org.github.raspopov.dto.UserInfoDto
import org.github.raspopov.views.SettingsViewModel
import kotlin.uuid.ExperimentalUuidApi

@ExperimentalUuidApi
class KtorChatService(
    private val client: HttpClient,
    private val messageService: MessageService,
    private val accountService: AccountService,
    private val keysService: KeysService,
    private val settings: SettingsViewModel
) : ChatService {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val mainAccount
        get() = settings.mainAccount.value!!
    private val baseUrl
        get() = settings.mainAccount.value!!.host

    override suspend fun getChats(): List<Chat> {
        if (settings.mainAccount.value == null)
            return emptyList()

//        val url = "$baseUrl/chats/"
//
//        val tokenVal = accountService.login()?.token ?: return listOf()
//
//        val response = client.get(url) {
//            header(HttpHeaders.Authorization, "Bearer $tokenVal")
//        }

//        val userInfo = response.body<List<UserInfoDto>>()
        val userInfo = listOf<UserInfoDto>()

        val chatsFromServer = userInfo.map { mapToChat(it) }.toMutableList()

        for (chat in keysService.allUsers(mainAccount.id)) {
            // todo
//            val info = accountService.getInfo(chat.userId)
//            chatsFromServer.add(mapToChat(info))
            chatsFromServer.add(chat)
        }

        return chatsFromServer
    }

    private fun mapToChat(dto: UserInfoDto): Chat {
        return Chat(dto.id, dto.username)
//            .apply {
//            if (dto.publicKey != null)
//                keysService.addKeyFor(
//                    dto.id,
//                    mainAccount.id,
//                    dto.username,
//                    dto.publicKey
//                )
//        }
    }

}
