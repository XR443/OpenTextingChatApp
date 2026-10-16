package org.github.raspopov.composes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.github.raspopov.composes.images.Icons
import org.github.raspopov.composes.images.icons.Burger
import org.github.raspopov.composes.images.icons.MailRead
import org.github.raspopov.composes.images.icons.MailUnread
import org.github.raspopov.domain.Chat
import org.github.raspopov.extensions.FormatPatterns
import org.github.raspopov.extensions.format
import org.github.raspopov.theme.DarkColorScheme
import org.github.raspopov.views.ChatListViewModel
import org.github.raspopov.views.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun ChatList(
    modifier: Modifier = Modifier,
    onToggleDrawer: () -> Unit,
    onChatRowClick: (chat: Chat) -> Unit = {},
    onNavigateToAccounts: () -> Unit = {},
    onNavigateToKeys: () -> Unit = {},
) {
    val settingsViewModel = koinViewModel<SettingsViewModel>()

    val chatListViewModel = koinViewModel<ChatListViewModel>()
    chatListViewModel.loadChats()
    val chats by chatListViewModel.chats.collectAsState()

    val isDrawerOpen by settingsViewModel.isDrawerOpen.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    LaunchedEffect(isDrawerOpen) {
        if (isDrawerOpen) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }

    LaunchedEffect(drawerState.isOpen) {
        if (!drawerState.isOpen && isDrawerOpen) {
            settingsViewModel.closeDrawer()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SettingsDrawer(
                onCloseDrawer = { settingsViewModel.closeDrawer() },
                onNavigateToAccounts = onNavigateToAccounts,
                onNavigateToKeys = onNavigateToKeys,
            )
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "OpenTexting",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onToggleDrawer) {
                            Icon(
                                Icons.Burger,
                                contentDescription = "Menu",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = DarkColorScheme.surface
                    ),
                    modifier = modifier
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                ) {
                    items(chats) { chat ->
                        ChatRow(
                            chat,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onChatRowClick(chat) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatRow(chat: Chat, onClick: (() -> Unit) = {}, modifier: Modifier = Modifier) {
    val chatListViewModel = koinViewModel<ChatListViewModel>()
    val settingsViewModel = koinViewModel<SettingsViewModel>()

    Row(
        modifier = modifier.border(1.dp, DarkColorScheme.surfaceVariant)
            .clickable(onClick = onClick)
    ) {
        Column(modifier.fillMaxWidth()) {
            val lastMessage = chatListViewModel.getLastMessage(chat)
            Row(
                modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(chat.name)
                if (lastMessage != null)
                    Text(lastMessage.created.format(FormatPatterns.DD_MM_YYYY_HH_MM))
                else
                    Text("???")
            }

            val payload =
                if (lastMessage != null && settingsViewModel.isMy(lastMessage)) "Me: " + lastMessage.payload?.decodeToString()
                else lastMessage?.payload?.decodeToString()
            Row(
//                modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp).fillMaxWidth(),
                modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                val hasUnread = !(lastMessage?.isRead ?: true)
                if (hasUnread && !settingsViewModel.isMy(lastMessage)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .requiredSize(24.dp) // Размер кружка
                            .clip(CircleShape)
                            .background(
                                color = DarkColorScheme.primary.copy(alpha = 0.2f),
//                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (hasUnread) Icons.MailUnread else Icons.MailRead,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = DarkColorScheme.background
                        )
                        Text(
                            text = if (hasUnread) "1+" else "",
                            color = DarkColorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    // Добавляем небольшой отступ между иконкой и текстом
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    payload ?: "Поиск последнего сообщения",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                    modifier = modifier.fillMaxWidth(0.8f)
                )
            }
        }
    }
}
