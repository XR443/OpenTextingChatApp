package org.github.raspopov

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.github.raspopov.services.AccountService
import org.github.raspopov.services.ChatService
import org.github.raspopov.services.KeyDerivationService
import org.github.raspopov.services.KeysService
import org.github.raspopov.services.KeysServiceImpl
import org.github.raspopov.services.KtorAccountService
import org.github.raspopov.services.KtorChatService
import org.github.raspopov.services.KtorMessageService
import org.github.raspopov.services.MessageCache
import org.github.raspopov.services.MessageService
import org.github.raspopov.views.ChatListViewModel
import org.github.raspopov.views.ChatScreenViewModel
import org.github.raspopov.views.LoadingViewModel
import org.github.raspopov.views.MasterPasswordViewModel
import org.github.raspopov.views.ObserverViewModel
import org.github.raspopov.views.PublicKeysViewModel
import org.github.raspopov.views.SettingsViewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
val commonModule = module {
    // todo как делать выбор
    single<KtorMessageService>() bind MessageService::class
    single<KtorAccountService>() bind AccountService::class
    single<KtorChatService>() bind ChatService::class
    single<KeyDerivationService>() bind KeyDerivationService::class
    single<KeysServiceImpl>() bind KeysService::class

    single {
        HttpClient(CIO) {
            // Таймауты
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000      // Полное время запроса
                connectTimeoutMillis = 15_000      // Время соединения
                socketTimeoutMillis = 15_000       // Время чтения данных
            }
            install(WebSockets){
                maxFrameSize = 1024 * 1024 // 1 MB

                // Время ожидания ответа ping-pong
                pingInterval = 10_000.milliseconds // 10 секунд
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    // todo цикл с KtorAccountService
    single<SettingsViewModel>()

    single<ObserverViewModel>()
    single<ChatListViewModel>()
    single<ChatScreenViewModel>()
    single<MasterPasswordViewModel>()
    single<PublicKeysViewModel>()
    single<LoadingViewModel>()
}