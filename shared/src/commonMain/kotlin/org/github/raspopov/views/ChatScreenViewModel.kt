package org.github.raspopov.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.github.raspopov.domain.Chat
import org.github.raspopov.domain.Message
import org.github.raspopov.services.MessageCache
import org.github.raspopov.services.MessageService
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@ExperimentalUuidApi
class ChatScreenViewModel(
    private val messageService: MessageService,
    private val messageCache: MessageCache,
    private val settingsViewModel: SettingsViewModel
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    val currentChat: MutableStateFlow<Chat?> = MutableStateFlow(null)

    private val _loadingState = MutableStateFlow(ChatScreenViewModel.LoadingState())
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    private val _repliedMessage = MutableStateFlow<Message?>(null)
    val repliedMessage: StateFlow<Message?> = _repliedMessage.asStateFlow()

    private val _newMessages = MutableStateFlow(true)
    val newMessages: StateFlow<Boolean> = _newMessages.asStateFlow()


    // Центральное сообщение (ядро вокруг которого строится список)
    private var centerMessage: Message? = null
    private var centerMessageIndex: Int = 0


    // Ограничения
    private val maxMessagesInMemory = 300

    private val _triggerMessages = MutableStateFlow<Set<Uuid>>(emptySet())
    val triggerMessages: StateFlow<Set<Uuid>> = _triggerMessages.asStateFlow()

    // Флаг для предотвращения множественных загрузок
    private var isLoadingTriggered = false
    private var lastTriggerId: Uuid? = null

    data class LoadingState(
        val totalMessages: Int = 0,
        val isLoadingAround: Boolean = false
    )

    fun openChat(chat: Chat) {
        currentChat.update { chat }

        loadMessagesFromCache()

        viewModelScope.launch {
            _loadingState.update { it.copy(isLoadingAround = true) }
            // Загружаем начальную порцию вокруг текущего времени
            messageService.getMessagesAroundFirstUnread(
                userId = chat.userId,
                limit = settingsViewModel.appSettings.value.pageSize
            )
                .onCompletion {
                    loadMessagesFromCache()
                }
                .collect { }
        }
    }

    private fun loadMessagesFromCache() {
        val messages = loadMessagesAroundSpecificOrFirst(centerMessage?.internalId)
        // Обновляем центр если нужно
        if (messages.isNotEmpty()) {
            centerMessageIndex = messages.size / 2
            centerMessage = messages[centerMessageIndex]
            updateTriggerMessages()
        }
        _loadingState.update { it.copy(isLoadingAround = false) }
    }

    fun closeChat() {
        currentChat.update { null }
        _messages.update { listOf() }
    }

    fun loadNewMessages(from: Uuid) {
        if (currentChat.value?.userId == from)
            viewModelScope.launch {
                _loadingState.update { it.copy(isLoadingAround = true) }
                // Загружаем начальную порцию вокруг текущего времени
                messageService.getMessagesAroundFirstUnread(
                    userId = currentChat.value!!.userId,
                    limit = settingsViewModel.appSettings.value.pageSize
                )
                    .onCompletion {
                        loadMessagesAroundSpecificOrFirst()
                        _loadingState.update { it.copy(isLoadingAround = false) }
                    }
                    .collect { }
            }
    }

    fun addMessage(text: String, callback: () -> Unit) {
        viewModelScope.launch {
            val currentChatForMessage: Chat? = currentChat.value
            if (currentChatForMessage != null) {
                messageService.create(text, currentChatForMessage.userId, _repliedMessage.value)
                    .onCompletion {
                        replyTo(null)
                        loadMessagesAroundSpecificOrFirst()
                        callback()
                    }
                    .collect { newMessage ->
                        // todo
                        // Обновляем список сообщений, сохраняя порядок
//                        val currentMessages = _messages.value.toMutableList()

                        // Объединяем старые и новые сообщения
//                        addUniqueMessages(currentMessages, listOf(newMessage))

//                        trimMessagesIfNeeded(centerMessage!!.internalId)
                        // Обновляем триггерные сообщения
//                        updateTriggerMessages()
                    }
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

    private fun loadMessagesAroundSpecificOrFirst(internalMessageId: Uuid? = null): List<Message> {
        // Обновляем список сообщений, сохраняя порядок
        val currentMessages = _messages.value.toMutableList()
        val initialMessages = messageCache.getMessageBetween(
            settingsViewModel.mainAccount.value!!.id,
            currentChat.value!!.userId,
            internalMessageId,
            settingsViewModel.appSettings.value.pageSize
        )
        var allMessages: List<Message> = listOf()
        if (initialMessages.isNotEmpty()) {
            allMessages = addUniqueMessages(currentMessages, initialMessages)
            if (centerMessage != null) {
                trimMessagesIfNeeded(centerMessage!!.internalId)
                // Обновляем триггерные сообщения
                updateTriggerMessages()
            }
        }
        _loadingState.update { it.copy(isLoadingAround = false) }
        _newMessages.update { true }
        return allMessages
    }

    private fun addUniqueMessages(
        currentMessages: List<Message>,
        newMessages: List<Message>
    ): List<Message> {
        val existingIds = currentMessages.map { it.internalId }.toSet()
        val uniqueMessages = newMessages.filter { it.internalId !in existingIds }

        if (uniqueMessages.isNotEmpty()) {
            //todo sort
            val updatedMessages = currentMessages + uniqueMessages
            val updatedAndSortedMessages = updatedMessages.sortedByDescending { it.created }

            _messages.update { updatedAndSortedMessages }

            return updatedAndSortedMessages
        }
        return currentMessages
    }

    // Обрезка сообщений с сохранением центра
    fun trimMessagesIfNeeded(newCenter: Uuid) {
        val currentMessages = _messages.value
        if (currentMessages.size <= maxMessagesInMemory) return

        val center = _messages.value.find { it.internalId == newCenter }
        if (center == null) return
        val centerIdx = currentMessages.indexOf(center)
        if (centerIdx == -1) return

        // Оставляем 150 до и 150 после центра
        val startIdx = maxOf(0, centerIdx - 150)
        val endIdx = minOf(currentMessages.size - 1, centerIdx + 150)

        val trimmedMessages = currentMessages.slice(startIdx..endIdx)
        _messages.value = trimmedMessages

        // Обновляем центр
        centerMessage = center
        centerMessageIndex = trimmedMessages.indexOf(center)
    }


    fun loadMessagesAroundCenter(internalMessageId: Uuid? = null) {
        if (internalMessageId != null) {
            val messageIndex = messages.value.indexOfFirst { it.internalId == internalMessageId }
            if (messageIndex == -1)
                return
            centerMessageIndex = messageIndex
            centerMessage = messages.value[centerMessageIndex]
        }
        _loadingState.update { it.copy(isLoadingAround = true) }
        viewModelScope.launch {
            try {
                messageService.getMessagesAround(
                    userId = currentChat.value!!.userId,
                    internalMessage = centerMessage,
                    limit = settingsViewModel.appSettings.value.pageSize,
                )
                    .onCompletion {
                        loadMessagesFromCache()
                    }
                    .collect { }

            } catch (e: Exception) {
                // Обработка ошибки
            } finally {
            }
        }
    }

    /**
     * Обновляет список сообщений-триггеров на основе текущего списка
     * Помечает сообщения на расстоянии pageSize от центра в обе стороны
     * Только если эти сообщения реально существуют
     */
    private fun updateTriggerMessages() {
        val currentMessages = _messages.value
        if (currentMessages.isEmpty() || centerMessage == null) {
            _triggerMessages.update { emptySet() }
            return
        }

        val totalSize = currentMessages.size
        val pageSize = settingsViewModel.appSettings.value.pageSize

        // Находим индекс центрального сообщения
        val centerIndex = centerMessageIndex
        if (centerIndex == -1) {
            _triggerMessages.update { emptySet() }
            return
        }

        // Вычисляем индексы для триггеров на расстоянии pageSize от центра
        val topTriggerIndex = if (centerIndex - pageSize >= 0) centerIndex - pageSize else 0
        val bottomTriggerIndex = if (centerIndex + pageSize < _messages.value.size)
            centerIndex + pageSize
        else
            _messages.value.size - 1

        val triggerIds = mutableSetOf<Uuid>()

        // Добавляем триггер сверху (старые сообщения), если индекс существует
        if (topTriggerIndex in 0..<totalSize) {
            currentMessages.getOrNull(topTriggerIndex.toInt())?.let {
                triggerIds.add(it.internalId)
            }
        }

        // Добавляем триггер снизу (новые сообщения), если индекс существует
        if (bottomTriggerIndex in 0..<totalSize) {
            currentMessages.getOrNull(bottomTriggerIndex.toInt())?.let {
                triggerIds.add(it.internalId)
            }
        }

        _triggerMessages.update { triggerIds }
    }

    /**
     * Проверяет, является ли сообщение триггером для загрузки
     * Вызывается из UI при появлении сообщения на экране
     */
    fun checkAndTriggerLoadIfNeeded(messageId: Uuid): Boolean {
        // Проверяем, является ли сообщение триггерным
        if (messageId !in _triggerMessages.value) return false

        // Проверяем, не загружаем ли уже
        if (isLoadingTriggered) return false

        // Проверяем, не загружаем ли вокруг центра
//        if (_loadingState.value.isLoadingAround) return

        // Проверяем, не загружали ли уже это сообщение
        if (lastTriggerId == messageId) return false

        // Проверяем доступность данных для загрузки
        val currentMessages = _messages.value
        val messageIndex = currentMessages.indexOfFirst { it.internalId == messageId }
        if (messageIndex == -1) return false

        val totalSize = currentMessages.size
        val topThreshold = (totalSize * 0.2).toInt()
        val bottomThreshold = (totalSize * 0.8).toInt()

        // Определяем, с какой стороны нужно загружать
        val shouldLoad = when {
            // Если сообщение в верхней части (20% от начала)
            messageIndex <= topThreshold -> {
                isLoadingTriggered = true
                lastTriggerId = messageId
                true
            }
            // Если сообщение в нижней части (20% от конца)
            messageIndex >= bottomThreshold -> {
                isLoadingTriggered = true
                lastTriggerId = messageId
                true
            }

            else -> false
        }

        if (shouldLoad) {
            viewModelScope.launch {
                // Сброс флага после загрузки
                try {
                    loadMessagesAroundCenter(messageId)
                } finally {
                    // Сбрасываем через некоторое время, чтобы дать возможность загрузиться
                    delay(2000.milliseconds)
                    isLoadingTriggered = false
                }
            }
        }
        return shouldLoad
    }

    fun replyTo(message: Message?) {
        _repliedMessage.update { message }
    }

}