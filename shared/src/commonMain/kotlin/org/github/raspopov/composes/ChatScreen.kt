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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.github.raspopov.composes.images.Icons
import org.github.raspopov.composes.images.icons.EyeOpened
import org.github.raspopov.composes.images.icons.FileAdd
import org.github.raspopov.composes.images.icons.LeftArrow
import org.github.raspopov.composes.images.icons.MailRead
import org.github.raspopov.composes.images.icons.MailUnread
import org.github.raspopov.composes.images.icons.Upload
import org.github.raspopov.domain.Chat
import org.github.raspopov.domain.Message
import org.github.raspopov.extensions.FormatPatterns
import org.github.raspopov.extensions.format
import org.github.raspopov.theme.DarkColorScheme
import org.github.raspopov.views.ChatScreenViewModel
import org.github.raspopov.views.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


// Вспомогательный enum для направления
enum class ScrollDirection {
    TOP, BOTTOM
}

@OptIn(FlowPreview::class)
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

    val loadingState by chatScreenViewModel.loadingState.collectAsState()

    // Инициализация чата
    LaunchedEffect(Unit) {
        if (isFirstOpen) {
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
    val triggerMessages by chatScreenViewModel.triggerMessages.collectAsState()
    var isLoadingTriggered by remember { mutableStateOf(false) }
    // Отслеживание скролла для определения центра
    // Отслеживаем видимые сообщения и проверяем триггеры
    LaunchedEffect(listState, messages) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .debounce(100.milliseconds)
            .collect { visibleItems ->
                if (isReady && visibleItems.isNotEmpty() && messages.isNotEmpty()) {
                    // Если сообщение в списке триггеров - запускаем загрузку
                    val triggerItem =
                        visibleItems.find { itemInfo -> itemInfo.key is Uuid && itemInfo.key in triggerMessages }
                    if (triggerItem != null) {
                        if (chatScreenViewModel.checkAndTriggerLoadIfNeeded(triggerItem.key as Uuid))
                            return@collect
                    }
                    val totalMessages = messages.size

                    // Получаем индексы первого и последнего видимого элемента
                    val firstVisibleIndex = visibleItems.firstOrNull()?.index ?: return@collect
                    val lastVisibleIndex = visibleItems.lastOrNull()?.index ?: return@collect

                    // Определяем, достигли ли мы верха или низа
                    val isAtTop = firstVisibleIndex <= 0
                    val isAtBottom = lastVisibleIndex >= totalMessages - 1

                    // Определяем направление скролла
                    val currentDirection = when {
                        isAtTop -> ScrollDirection.TOP
                        isAtBottom -> ScrollDirection.BOTTOM
                        else -> null
                    }

                    // Сбрасываем флаг, если мы отошли от края
                    if (currentDirection == null && isLoadingTriggered) {
                        isLoadingTriggered = false
                    }

                    // Проверяем, нужно ли загрузить
                    val shouldLoad = when {
                        // Достигли верха и есть старые сообщения
                        isAtTop && !isLoadingTriggered -> true
                        // Достигли низа и есть новые сообщения
                        isAtBottom && !isLoadingTriggered -> true
                        else -> false
                    }

                    if (shouldLoad) {
                        val itemInfo = visibleItems[(lastVisibleIndex - firstVisibleIndex) / 2]
                        if (itemInfo.key is Uuid) {
                            isLoadingTriggered = true
                            chatScreenViewModel.loadMessagesAroundCenter(itemInfo.key as Uuid)
                        }
                    }
                }
            }
    }
    LaunchedEffect(listState, messages) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .debounce(100.milliseconds)
            .collect { visibleItems ->
                if (isReady && visibleItems.isNotEmpty() && messages.isNotEmpty()) {
                    if (isFollowing)
                        listState.scrollToItem(0)
                    val itemInfo = visibleItems[visibleItems.size / 2]
                    if (itemInfo.key is Uuid)
                        chatScreenViewModel.trimMessagesIfNeeded(itemInfo.key as Uuid)
                }
            }
    }

    // Отслеживание прокрутки для reverseLayout = true
    LaunchedEffect(listState) {
        var previousFirstVisible = 0

        snapshotFlow {
            listState.firstVisibleItemIndex to listState.layoutInfo.totalItemsCount
        }.collect { (firstVisible, totalItems) ->
            if (isFirstOpen || totalItems <= 3 || !isReady) return@collect

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
    var debounceJob by remember { mutableStateOf<Job?>(null) }

// Отслеживаем только конечное состояние после анимации
    val oldestVisibleItem = remember {
        derivedStateOf {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            if (visibleItems.isNotEmpty() && messages.isNotEmpty() && isReady) {
                visibleItems.minByOrNull { it.index }
            } else null
        }
    }

    LaunchedEffect(oldestVisibleItem.value) {
        val itemInfo = oldestVisibleItem.value ?: return@LaunchedEffect

        val message = messages.firstOrNull { it.internalId == itemInfo.key }

        if (message != null &&
            !settingsViewModel.isMy(message) &&
            !message.isRead
        ) {

            // Отменяем предыдущий дебаунс
            debounceJob?.cancel()

            // Создаем новый с задержкой
            debounceJob = coroutineScope.launch {
                delay(500)
                // Дополнительная проверка, что сообщение все еще не прочитано
                val currentMessage = messages.firstOrNull { it.internalId == message.internalId }
                if (currentMessage != null && !currentMessage.isRead) {
                    lastReadMessageId = message.internalId
                    onMessageRead(message.internalId)
                }
            }
        }
    }

    val hasNewMessages by chatScreenViewModel.newMessages.collectAsState()
    Surface() {
        Box(
            modifier = Modifier.fillMaxSize()
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
                        items = messages, key = { it.internalId }) { message ->
                        // todo отмечать в чате что сообщения не прочитаны
                        if (!message.isRead && !settingsViewModel.isMy(message) && false) {
                            Text(
                                text = "New messages",
                                modifier = modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                        MessageItem(
                            message = message, isMyMessage = settingsViewModel.isMy(message)
                        )
                    }

                    // Индикатор загрузки новых сообщений
                    if (loadingState.isLoadingAround) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }

                MessageInputFieldWithAttachment(
                    onSendMessage = { it ->
                        chatScreenViewModel.addMessage(it) {
                            if (isFollowing && messages.all { m -> settingsViewModel.isMy(m) || m.isRead }) {
                                isReady = false
                                coroutineScope.launch {
                                    // todo как подгрузить сообщения если они выгружен
                                    listState.animateScrollToItem(0)
                                    delay(250)
                                    isReady = true
                                }
                            }
                        }
                    },
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
                    modifier = Modifier.size(width = 64.dp, height = 64.dp + 64.dp)
                        .align(Alignment.BottomEnd)
                        .padding(
                            top = 0.dp,
                            bottom = 96.dp,
                            start = 16.dp,
                            end = 16.dp,
                        )
                ) {
                    val unreadCount = messages.count { !it.isRead && !settingsViewModel.isMy(it) }
                    Column {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (unreadCount > 0) Icons.MailUnread else Icons.EyeOpened,
                                contentDescription = null
                            )
                            if (unreadCount > 0) Text(
                                text = "$unreadCount",
                                color = DarkColorScheme.onPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                modifier = Modifier.align(Alignment.Center)
//                                    .offset(y = 16.dp) // Смещаем текст под иконку
                            )
                        }
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
                text = chat.name, style = MaterialTheme.typography.titleLarge
            )
        }, navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.LeftArrow, contentDescription = "Back", modifier = Modifier.size(24.dp)
                )
            }
        }, colors = topAppBarColors(DarkColorScheme.surface), modifier = modifier
    )
}

@Composable
@Preview(showBackground = true)
fun TopPanelPreview() {
    TopPanel(Chat(Uuid.random(), "Name"))
}

@Composable
fun MessageItem(
    message: Message, isMyMessage: Boolean
) {
    // Вариант 1: Использование Row с выравниванием
    Row(
        modifier = Modifier.fillMaxWidth().padding(
            horizontal = 8.dp, vertical = 4.dp
        ), horizontalArrangement = if (isMyMessage) {
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
    message: Message, isMyMessage: Boolean
) {
    val shape = if (isMyMessage) {
        // Мои сообщения - скругление справа
        RoundedCornerShape(
            topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp
        )
    } else {
        // Чужие сообщения - скругление слева
        RoundedCornerShape(
            topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp
        )
    }


    Box(
        modifier = Modifier.widthIn(max = 280.dp)  // todo Максимальная ширина сообщения
            .background(
//                color = Color.Red,
//                color = Color(0x9966CC),
//                color = Color(0xFFC8A2C8), shape = shape
                color = DarkColorScheme.surfaceVariant, shape = shape
            ).padding(
                horizontal = 12.dp, vertical = 8.dp
            )
    ) {
        Column {
            SelectionContainer {
                Text(
                    text = message.payload!!.decodeToString(),
                    color = DarkColorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    if (message.isRead) Icons.MailRead else Icons.MailUnread,
                    contentDescription = if (message.isRead) "Прочитано" else "Не прочитано",
                    tint = DarkColorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp).size(16.dp) // Авторазмер
                )
                // Время сообщения
                Text(
                    text = message.created.format(FormatPatterns.HH_MM),
                    color = DarkColorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}


@Composable
fun MessageInputFieldWithAttachment(
    onSendMessage: (String) -> Unit, onAttachFile: () -> Unit, modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    val isSendEnabled = text.text.isNotBlank()

    Surface(
        modifier = modifier.fillMaxWidth(), color = DarkColorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp).navigationBarsPadding()
                .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            // Кнопка вложения
            IconButton(
                onClick = onAttachFile, modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.FileAdd, contentDescription = "Прикрепить файл"
                )
            }

            // Поле ввода
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f).onKeyEvent { keyEvent ->
                    // Обработка Ctrl + !Enter
                    if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter && !keyEvent.isCtrlPressed && isSendEnabled) {
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
                    focusedBorderColor = DarkColorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = DarkColorScheme.surface,
                    unfocusedContainerColor = DarkColorScheme.surface
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
                }, enabled = isSendEnabled, modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Upload,
                    contentDescription = "Отправить",
                    tint = if (isSendEnabled) DarkColorScheme.primary
                    else DarkColorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun MessageInputFieldWithAttachmentPreview() {
    MessageInputFieldWithAttachment(onSendMessage = { it -> it }, onAttachFile = {})
}

@Composable
@Preview(showBackground = true)
fun ChatPreview() {
    ChatScreen(
        Chat(
            Uuid.random(), "Name"
        ),
    )
}