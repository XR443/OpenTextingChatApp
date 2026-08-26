package org.github.raspopov.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.github.raspopov.services.AccountService
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock.System.now
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
class ObserverViewModel(
    private val accountService: AccountService,
    private val client: HttpClient,
    private val settingsViewModel: SettingsViewModel,
    private val masterPasswordViewModel: MasterPasswordViewModel,
    private val chatListViewModel: ChatListViewModel,
    private val chatScreenViewModel: ChatScreenViewModel,
) : ViewModel() {
    private val isRunning = MutableStateFlow(false)
    private val session = MutableStateFlow<WebSocketSession?>(null)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun runWatcher() {
        viewModelScope.launch {
            val keys = masterPasswordViewModel.uiState.value.keys
            if (!settingsViewModel.isHaveAccounts() || keys == null) {
                isRunning.update { false }
                return@launch
            }

            isRunning.update { true }

            // Бесконечный цикл переподключения
            while (isRunning.value) {
                var shouldReconnect = true
                try {
                    val token = accountService.login()?.token ?: break
                    val urlString =
                        "${settingsViewModel.mainAccount.value!!.host}/messages/new/notification/ws"

                    client.webSocket(
                        urlString,
                        {
                            url.protocol = URLProtocol.WS
                            header(HttpHeaders.Authorization, "Bearer $token")
                            // Важно: устанавливаем таймауты
                            timeout {
                                connectTimeoutMillis = 5000
                                requestTimeoutMillis = 60000
                            }
                        }
                    ) {
                        session.update { this }
                        println("WebSocket connected at ${now()}")

                        try {
                            // Обработка входящих сообщений
                            for (frame in incoming) {
                                if (processFrame(frame)) {
                                    shouldReconnect = true
                                    break
                                }
                            }
                        } catch (e: CancellationException) {
                            // Это нормальное закрытие от корутины
                            println("WebSocket cancelled manually")
                            shouldReconnect = false
                        } catch (e: ClosedReceiveChannelException) {
                            // Канал закрыт - обычно при разрыве соединения
                            // Это НЕ ошибка, а сигнал о разрыве
                            println("WebSocket receive channel closed - connection lost")
                            shouldReconnect = true
                        } catch (e: IOException) {
                            // Сетевые ошибки (обрыв соединения, таймаут)
                            println("WebSocket IO error: ${e.message}")
                            shouldReconnect = true
                        } catch (e: Exception) {
                            // Любые другие ошибки
                            println("WebSocket unexpected error: ${e.message}")
                            e.printStackTrace()
                            shouldReconnect = true
                        } finally {
                            // Закрываем соединение, если оно еще открыто
                            try {
                                if (session.value != null) {
                                    this.close(
                                        CloseReason(
                                            CloseReason.Codes.NORMAL,
                                            "Client shutdown"
                                        )
                                    )
                                }
                            } catch (e: Exception) {
                                // Игнорируем ошибки закрытия
                            }
                            session.update { null }
                            println("WebSocket disconnected at ${now()}")
                        }
                    }
                } catch (e: Exception) {
                    // Ошибка при установке соединения
                    println("Failed to connect WebSocket: ${e.message}")
                    shouldReconnect = true
                }

                // Решение о переподключении
                if (shouldReconnect && isRunning.value) {
                    println("Reconnecting in 3 seconds...")
                    delay(3000) // Задержка перед переподключением
                } else {
                    // Если не нужно переподключаться или isRunning=false - выходим из внешнего цикла
                    break
                }
            }

            isRunning.update { false }
        }
    }

    fun stopWatcher() {
        val currentSession = session.value
        isRunning.update { false }
        viewModelScope.launch {
            currentSession?.close(CloseReason(CloseReason.Codes.NORMAL, "Restart"))
        }
    }

    /**
     * true if socket closed
     */
    private fun processFrame(frame: Frame): Boolean {
        when (frame) {
            is Frame.Text -> {
                val readText = frame.readText()
                println("Received text: $readText")
                try {
                    val notification =
                        json.decodeFromString<Notification>(readText)

                    chatScreenViewModel.loadMessage(notification.message_from)
                    chatListViewModel.loadChat(notification.message_from)
                } catch (ex: Exception) {
                    when (ex) {
                        is IllegalArgumentException,
                        is SerializationException -> println("Получено ошибочное сообщение:\n$readText")

                        else -> throw ex
                    }
                }

                return false
            }

            is Frame.Binary -> {
                // Обработка бинарных данных
                println("Received binary data")
                return false
            }

            is Frame.Close -> {
                println("Server requested close: ${frame.data.decodeToString()}")
                return true
            }

            else -> {
                // Ping/Pong обрабатываются автоматически Ktor, сюда не попадают
                println("Unknown frame: $frame")

                return false
            }
        }
    }

}

@Serializable
data class Notification(val message_from: Uuid)