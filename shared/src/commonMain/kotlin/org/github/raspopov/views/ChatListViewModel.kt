package org.github.raspopov.views

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.github.raspopov.domain.Chat
import org.github.raspopov.domain.Message
import org.github.raspopov.services.ChatService
import org.github.raspopov.services.MessageService
import kotlin.collections.forEach
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
class ChatListViewModel(
    private val chatService: ChatService,
    private val messageService: MessageService,
    private val masterPasswordViewModel: MasterPasswordViewModel,
) : ViewModel() {

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()
    private val _lastMessages: MutableMap<Uuid, Message?> = mutableStateMapOf()

    fun loadChats() {
        viewModelScope.launch {
            // todo настраиваемое количество попыток
            var attempt = 0
            while (attempt++ < 3) {
                if (masterPasswordViewModel.uiState.value.keys == null)
                    break
                val foundChats = chatService.getChats()
                if (foundChats.isNotEmpty()) {
                    _chats.update { foundChats }
                    break
                } else {
                    delay(1.toDuration(DurationUnit.SECONDS))
                }
            }
            _lastMessages.clear()
            chats.value.forEach { chat ->
                if (masterPasswordViewModel.uiState.value.keys == null)
                    _lastMessages[chat.userId] = null
                else messageService.findLastMessageWithUser(chat.userId).collect { messages ->
                    _lastMessages[chat.userId] = messages
                }
            }
        }
    }

    fun loadChat(author: Uuid) {
        viewModelScope.launch {
            chats.value.forEach { chat ->
                messageService.findLastMessageWithUser(chat.userId).collect { messages ->
                    _lastMessages[chat.userId] = messages
                }
            }
        }
    }

    fun getLastMessage(chat: Chat): Message? {
        return _lastMessages[chat.userId]
    }

}
