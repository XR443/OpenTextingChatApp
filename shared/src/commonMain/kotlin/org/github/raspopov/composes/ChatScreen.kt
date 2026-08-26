package org.github.raspopov.composes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.github.raspopov.composes.images.Icons
import org.github.raspopov.composes.images.icons.EyeOpened
import org.github.raspopov.composes.images.icons.FileAdd
import org.github.raspopov.composes.images.icons.LeftArrow
import org.github.raspopov.composes.images.icons.MailUnread
import org.github.raspopov.composes.images.icons.MessageBubble
import org.github.raspopov.composes.images.icons.Upload
import org.github.raspopov.domain.Chat
import org.github.raspopov.domain.Message
import org.github.raspopov.extensions.FormatPatterns
import org.github.raspopov.extensions.format
import org.github.raspopov.views.ChatScreenViewModel
import org.github.raspopov.views.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@ExperimentalUuidApi
@Composable
fun ChatScreen(
    chat: Chat, modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    val settingsViewModel = koinViewModel<SettingsViewModel>()

    val chatScreenViewModel = koinViewModel<ChatScreenViewModel>()

    val messages by chatScreenViewModel.messages.collectAsState()

    // Состояние для последнего прочитанного сообщения
//    var lastReadMessageId by remember { mutableStateOf<Uuid?>(null) }
    var lastReadMessageId by rememberSaveable { mutableStateOf<Uuid?>(null) }

    // Лямбда для уведомления сервера
    val onMessageRead = remember(chatScreenViewModel) {
        { messageId: Uuid -> chatScreenViewModel.markMessageAsRead(messageId) }
    }

    var scrolled by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var isFirstOpen by remember { mutableStateOf(true) }
    var lastMessageCount by remember { mutableStateOf(0) }
    var isFollowing by remember { mutableStateOf(true) }
    var showFollowButton by remember { mutableStateOf(false) }
    var isReady by remember { mutableStateOf(false) }

    // Первоначальная настройка
    LaunchedEffect(Unit) {
        if (messages.isNotEmpty()) {
            // Ищем первое непрочитанное сообщение (с конца списка)
            // Так как список в reverseLayout, индекс считается от новых к старым
            val unreadIndex = messages.indexOfLast { !it.isRead && !settingsViewModel.isMy(it) }

            val targetIndex = if (unreadIndex != -1) {
                // unreadIndex - это индекс в обычном списке (от старых к новым)
                // В reverseLayout это будет: messages.size - 1 - unreadIndex
                unreadIndex
            } else {
                0 // Иначе показываем самое новое сообщение (индекс 0)
            }

            listState.scrollToItem(targetIndex)
            lastMessageCount = messages.size
            isFirstOpen = false
            isFollowing = true
            showFollowButton = false

            delay(500)
            isReady = true
        }
    }

    // Обработка новых сообщений
    LaunchedEffect(messages.size) {
        if (!isFirstOpen && messages.isNotEmpty()) {
            val currentSize = messages.size

            // Если появились новые сообщения и мы в режиме слежения
            if (currentSize > lastMessageCount && isFollowing) {
                // Новое сообщение всегда добавляется в начало (индекс 0)
//                listState.animateScrollToItem(0)
                val unreadIndex = messages.indexOfLast {
                    !it.isRead && !settingsViewModel.isMy(it)
                }
                if (unreadIndex >= 0) {
                    listState.animateScrollToItem(unreadIndex)
                } else {
                    listState.animateScrollToItem(0)
                }
            }

            lastMessageCount = currentSize
        }
    }

    // Отслеживание прокрутки для reverseLayout = true
    LaunchedEffect(listState) {
        var previousFirstVisible = 0

        snapshotFlow {
            listState.firstVisibleItemIndex to listState.layoutInfo?.totalItemsCount
        }
            .collect { (firstVisible, totalItems) ->
                if (isFirstOpen || totalItems == null || totalItems <= 3 || !isReady) return@collect

                // Проверяем, что это прокрутка пользователя
                val isScrolling = previousFirstVisible != firstVisible
                previousFirstVisible = firstVisible

                if (isScrolling && isFollowing) {
                    // При reverseLayout = true:
                    // Индекс 0 = самое новое сообщение (внизу)
                    // Чем больше индекс, тем старше сообщение
                    // Пользователь прокрутил вверх, если firstVisible > 2 (не видно 3 самых новых)
                    if (firstVisible > 2) {
                        isFollowing = false
                        showFollowButton = true
                    }
                }
            }
    }

    var pendingReadMessageId by remember { mutableStateOf<String?>(null) }
    var debounceJob by remember { mutableStateOf<Job?>(null) }
    // Отслеживание видимых элементов для пометки прочитанных
    LaunchedEffect(listState, messages) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                if (visibleItems.isNotEmpty() && messages.isNotEmpty() && isReady) {
                    // При reverseLayout = true индексы идут от новых к старым
                    // Нам нужно найти самый "старый" видимый элемент (с наибольшим индексом)
                    // Так как мы читаем сообщения сверху вниз, то самое старое видимое - это то, что вверху
                    val oldestVisibleItem = visibleItems.minByOrNull { it.index }

                    oldestVisibleItem?.let { itemInfo ->
                        val message = messages.first { it.internalId == itemInfo.key }

                        // Помечаем прочитанным, если сообщение не от меня и не прочитано
                        if (!settingsViewModel.isMy(message) && !message.isRead) {
                            // Отменяем предыдущую задачу дебаунса
                            debounceJob?.cancel()

                            // Создаем новую задачу с задержкой
                            debounceJob = launch {
                                delay(500) // Ждем 500ms чтобы пользователь остановился
                                lastReadMessageId = message.internalId
                                onMessageRead(message.internalId)
                            }
                        }
                    }
                }
            }
    }
    Surface() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()  // ← Автоматически подстраивается под клавиатуру
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TopPanel(chat, modifier) {
                    chatScreenViewModel.closeChat()
                    onBack()
                }

                LazyColumn(
                    state = listState,
                    modifier = modifier.weight(1f).fillMaxSize(),
                    reverseLayout = true,
                    verticalArrangement = Arrangement.Top
                ) {
                    items(
                        items = messages,
                        key = { it.internalId }
                    ) { message ->
                        MessageItem(
                            message = message,
                            isMyMessage = settingsViewModel.isMy(message)
                        )
                    }
                }
                MessageInputFieldWithAttachment(
                    onSendMessage = { chatScreenViewModel.addMessage(it) },
                    onAttachFile = {},
                    modifier = modifier
                )
            }
            if (showFollowButton)
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            isFollowing = true
                            showFollowButton = false
                            isReady = false
                            val unreadIndex = messages.indexOfLast {
                                !it.isRead && !settingsViewModel.isMy(it)
                            }
                            if (unreadIndex >= 0) {
                                listState.animateScrollToItem(unreadIndex)
                            } else {
                                listState.animateScrollToItem(0)
                            }
                            delay(250)
                            isReady = true
                        }
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    val unreadCount =
                        messages.count { !it.isRead && !settingsViewModel.isMy(it) }
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (unreadCount > 0) Icons.MailUnread else Icons.EyeOpened,
                            contentDescription = null
                        )
                        if (unreadCount > 0)
                            Text(
                                text = "$unreadCount",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .align(Alignment.Center)
//                                    .offset(y = 16.dp) // Смещаем текст под иконку
                            )
                    }
                }

        }
    }

}

@Composable
fun TopPanel(chat: Chat, modifier: Modifier = Modifier, onBack: () -> Unit = {}) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = chat.name,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.LeftArrow,
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = topAppBarColors(MaterialTheme.colorScheme.surface),
        modifier = modifier
    )
}

@Composable
@Preview(showBackground = true)
fun TopPanelPreview() {
    TopPanel(Chat(Uuid.random(), "Name"))
}

@Composable
fun MessageItem(
    message: Message,
    isMyMessage: Boolean
) {
    // Вариант 1: Использование Row с выравниванием
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
        horizontalArrangement = if (isMyMessage) {
            Arrangement.End  // Мои справа
        } else {
            Arrangement.Start  // Чужие слева
        }
    ) {
        MessageBubble(message, isMyMessage)
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isMyMessage: Boolean
) {
    val shape = if (isMyMessage) {
        // Мои сообщения - скругление справа
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 4.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )
    } else {
        // Чужие сообщения - скругление слева
        RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )
    }


    Box(
        modifier = Modifier
            .widthIn(max = 280.dp)  // todo Максимальная ширина сообщения
            .background(
//                color = Color.Red,
//                color = Color(0x9966CC),
                color = Color(0xFFC8A2C8),
                shape = shape
            )
            .padding(
                horizontal = 12.dp,
                vertical = 8.dp
            )
    ) {
        Column {
            Text(
                text = message.payload!!.decodeToString(),
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )

            // Время сообщения
            Text(
                text = message.created.format(FormatPatterns.HH_MM),
                color = Color.Black.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp)
            )
        }
    }
}


@Composable
fun MessageInputFieldWithAttachment(
    onSendMessage: (String) -> Unit,
    onAttachFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    val isSendEnabled = text.text.isNotBlank()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .navigationBarsPadding()
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Кнопка вложения
            IconButton(
                onClick = onAttachFile,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.FileAdd,
                    contentDescription = "Прикрепить файл"
                )
            }

            // Поле ввода
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f)
                    .onKeyEvent { keyEvent ->
                        // Обработка Ctrl + !Enter
                        if (keyEvent.type == KeyEventType.KeyUp &&
                            keyEvent.key == Key.Enter &&
                            !keyEvent.isCtrlPressed &&
                            isSendEnabled
                        ) {
                            onSendMessage(text.text)
                            text = TextFieldValue("")
                            true // Поглощаем событие
                        } else {
                            false // Пропускаем событие дальше
                        }

                    },
                placeholder = { Text("Сообщение...") },
                shape = RoundedCornerShape(24.dp),
                singleLine = false,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
//                keyboardOptions = KeyboardOptions(
//                    // Это позволяет отправлять через обычный Enter на десктопе
//                    // Но лучше оставить только Ctrl+Enter, чтобы избежать случайных отправок
//                    imeAction = ImeAction.None,
//                ),
//                keyboardActions = KeyboardActions(
//                    onAny = {
//                        if (getPlatform().isDesktop) {
//                            if (isSendEnabled) {
//                                onSendMessage(text.text)
//                                text = TextFieldValue("")
//                                    keyboardController?.hide()
//                            }
//                        }
//                    }
//                )
            )

            // Кнопка отправки
            IconButton(
                onClick = {
                    if (isSendEnabled) {
                        onSendMessage(text.text)
                        text = TextFieldValue("")
                    }
                },
                enabled = isSendEnabled,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Upload,
                    contentDescription = "Отправить",
                    tint = if (isSendEnabled)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun MessageInputFieldWithAttachmentPreview() {
    MessageInputFieldWithAttachment(
        onSendMessage = { it -> it },
        onAttachFile = {}
    )
}

@Composable
@Preview(showBackground = true)
fun ChatPreview() {
    ChatScreen(
        Chat(
            Uuid.random(),
            "Name"
        ),
    )
}