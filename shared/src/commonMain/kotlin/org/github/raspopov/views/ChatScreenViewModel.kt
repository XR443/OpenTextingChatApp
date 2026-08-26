package org.github.raspopov.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.github.raspopov.domain.Chat
import org.github.raspopov.domain.Message
import org.github.raspopov.services.MessageService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@ExperimentalUuidApi
class ChatScreenViewModel(private val messageService: MessageService) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    val currentChat: MutableStateFlow<Chat?> = MutableStateFlow(null)

    fun openChat(chat: Chat) {
        currentChat.update { chat }
        viewModelScope.launch {
            messageService.findMessagesWithUser(chat.userId)
                .collect { newMessages ->
                    _messages.update { newMessages }
                }
        }
    }

    fun closeChat() {
        currentChat.update { null }
        _messages.update { listOf() }
    }

    fun loadMessage(text: Uuid) {
        viewModelScope.launch {
            val currentChatForMessage: Chat? = currentChat.value
            if (currentChatForMessage != null) {
                messageService.findMessagesWithUser(currentChatForMessage.userId)
                    .collect { newMessages ->
                        _messages.update { newMessages }
                    }
            }
        }
    }


    fun addMessage(text: String) {
        viewModelScope.launch {
            val currentChatForMessage: Chat? = currentChat.value
            if (currentChatForMessage != null) {
                messageService.create(text, currentChatForMessage.userId)
                    .onCompletion {
                        messageService.findMessagesWithUser(currentChatForMessage.userId)
                            .collect { newMessages ->
                                _messages.update { newMessages }
                            }
                    }
                    .collect { }
            }
        }
    }


    fun markMessageAsRead(internalId: Uuid) {
        viewModelScope.launch {
            messageService.markMessageAsRead(internalId).collect { readMessages ->
                val messagesMap = readMessages.associateBy { it.internalId }
                if (messagesMap.isNotEmpty())
                    _messages.update { messages ->
                        messages.map {
                            if (it.internalId in messagesMap)
                                return@map messagesMap[it.internalId]!!
                            else
                                return@map it
                        }
                    }
            }
        }
    }

}