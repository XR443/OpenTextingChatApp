package org.github.raspopov.composes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.github.raspopov.composes.images.Icons
import org.github.raspopov.composes.images.icons.Add
import org.github.raspopov.composes.images.icons.Copy
import org.github.raspopov.composes.images.icons.FileAdd
import org.github.raspopov.composes.images.icons.FileRemove
import org.github.raspopov.composes.images.icons.LeftArrow
import org.github.raspopov.composes.images.icons.Search
import org.github.raspopov.composes.images.icons.Upload
import org.github.raspopov.composes.images.icons.XCirce
import org.github.raspopov.domain.Chat
import org.github.raspopov.extensions.toBase64
import org.github.raspopov.theme.DarkColorScheme
import org.github.raspopov.views.LoadingViewModel
import org.github.raspopov.views.PublicKeysViewModel
import org.github.raspopov.views.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicKeyManagerScreen(
    onNavigateBack: () -> Unit = {}
) {
    val publicKeyViewModel = koinViewModel<PublicKeysViewModel>()
    val settingsViewModel = koinViewModel<SettingsViewModel>()

    publicKeyViewModel.loadKeys()

    val mainAccount by settingsViewModel.mainAccount.collectAsState()
    val myKey by publicKeyViewModel.mainAccountKey.collectAsState()
    val otherPublicKeys by publicKeyViewModel.otherKeys.collectAsState()

    // Состояние для экспорта
    var exportState by remember { mutableStateOf<ExportState>(ExportState.Idle) }
    var showExportDialog by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false) }

    // Состояния для поиска
    var searchQuery by remember { mutableStateOf("") }
    var searchMode by remember { mutableStateOf(SearchMode.ID) }

    // Фильтрованные ключи для поиска
    val filteredKeys = remember(otherPublicKeys, searchQuery) {
        if (searchQuery.isBlank()) {
            otherPublicKeys
        } else {
            otherPublicKeys.filter { keyEntry ->
                keyEntry.first.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Диалог добавления ключа
    if (showAddDialog) {
        val loadingViewModel = koinViewModel<LoadingViewModel>()

        LoadingWindow(
            loadingViewModel,
            content = {
                AddPublicKeyContent(
                    onDismiss = {
                        showAddDialog = false
                    },
                    onAdd = { userId, username, pubkey ->
                        loadingViewModel.startLoading()
                        publicKeyViewModel.addPublicKey(userId, username, pubkey) {
                            showAddDialog = false
                            loadingViewModel.reset()
                        }
                    }
                )
            }
        )
    }

    val coroutineScope = rememberCoroutineScope()
    // Диалог экспорта ключа
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = {
                showExportDialog = false
                exportState = ExportState.Idle
            },
            title = {
                Text("Экспорт ключа на сервер")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Вы уверены, что хотите экспортировать ваш публичный ключ на сервер ${mainAccount?.host ?: "неизвестный"}?"
                    )

                    Text(
                        text = "Ключ будет отправлен и сохранен на сервере для других пользователей.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkColorScheme.onSurfaceVariant
                    )

                    // Отображение статуса экспорта
                    when (exportState) {
                        ExportState.Loading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Экспорт ключа...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        ExportState.Success -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.FileAdd,
                                    contentDescription = null,
                                    tint = DarkColorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = "Ключ успешно экспортирован!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DarkColorScheme.primary
                                )
                            }
                        }

                        ExportState.Error -> {
                            Text(
                                text = "Ошибка при экспорте ключа. Попробуйте позже.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkColorScheme.error
                            )
                        }

                        ExportState.Idle -> {
                            // Ничего не показываем
                        }
                    }
                }
            },
            confirmButton = {
                if (exportState == ExportState.Idle) {
                    Button(
                        onClick = {
                            exportState = ExportState.Loading
                            publicKeyViewModel.exportPublicKey(
                                onSuccess = {
                                    exportState = ExportState.Success
                                    // Закрываем диалог через 2 секунды после успеха
                                    coroutineScope.launch {
                                        kotlinx.coroutines.delay(2000)
                                        showExportDialog = false
                                        exportState = ExportState.Idle
                                    }
                                },
                                onError = {
                                    exportState = ExportState.Error
                                    // Возвращаем в Idle через 3 секунды
                                    coroutineScope.launch {
                                        kotlinx.coroutines.delay(3000)
                                        exportState = ExportState.Idle
                                    }
                                }
                            )
                        },
                        enabled = myKey != null
                    ) {
                        Text("Экспортировать")
                    }
                } else {
                    // Кнопка для закрытия в состоянии успеха/ошибки
                    Button(
                        onClick = {
                            showExportDialog = false
                            exportState = ExportState.Idle
                        }
                    ) {
                        Text(if (exportState == ExportState.Success) "Готово" else "Закрыть")
                    }
                }
            },
            dismissButton = {
                if (exportState == ExportState.Idle || exportState == ExportState.Error) {
                    TextButton(
                        onClick = {
                            showExportDialog = false
                            exportState = ExportState.Idle
                        }
                    ) {
                        Text("Отмена")
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Управление аккаунтами") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        enabled = true
                    ) {
                        Icon(
                            Icons.LeftArrow,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            Icons.Add,
                            contentDescription = "Добавить"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues.plus(PaddingValues(16.dp)))
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Секция: Мой публичный ключ
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = DarkColorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Сервер: ${mainAccount?.host ?: "не выбран"}",
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkColorScheme.onPrimaryContainer
                        )

                        // КНОПКА ЭКСПОРТА
                        Button(
                            onClick = {
                                if (myKey != null) {
                                    showExportDialog = true
                                } else {
                                    // Показать сообщение, что ключ не найден
                                    publicKeyViewModel.showToast("Публичный ключ не найден")
                                }
                            },
                            enabled = myKey != null,
                            modifier = Modifier.size(height = 36.dp, width = 120.dp)
                        ) {
                            Icon(
                                Icons.Upload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = "Экспорт",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    Text(
                        text = "Мой ИД (${mainAccount?.username ?: "не выбран"})",
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkColorScheme.onPrimaryContainer,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mainAccount?.id?.toString() ?: "Не выбран",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkColorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.MiddleEllipsis
                        )

                        IconButton(
                            onClick = {
                                mainAccount?.id?.toString()?.let {
                                    publicKeyViewModel.copyToClipboard(it)
                                }
                            },
                            enabled = mainAccount != null
                        ) {
                            Icon(
                                Icons.Copy,
                                contentDescription = "Копировать ИД"
                            )
                        }
                    }

                    if (myKey != null) {
                        Text(
                            text = "Мой публичный ключ (base64)",
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkColorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = myKey!!.toBase64(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkColorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.MiddleEllipsis
                            )

                            IconButton(
                                onClick = {
                                    myKey?.let {
                                        publicKeyViewModel.copyToClipboard(it.toBase64())
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Copy,
                                    contentDescription = "Копировать ключ"
                                )
                            }
                        }
                    }
                }
            }

            // Заголовок списка с кнопкой добавления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ключи других пользователей (${filteredKeys.size})",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Поле поиска
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = {
                    Text(
                        "Поиск по имени",
                        fontSize = MaterialTheme.typography.labelMedium.fontSize
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.XCirce,
                                contentDescription = "Очистить",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkColorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = DarkColorScheme.outline.copy(alpha = 0.3f)
                ),
                textStyle = MaterialTheme.typography.bodySmall,
                shape = MaterialTheme.shapes.small
            )

            // Список ключей
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredKeys.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = DarkColorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isNotBlank())
                                        "Пользователи не найдены"
                                    else
                                        "Нет добавленных ключей",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DarkColorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredKeys) { keyEntry ->
                        OtherPublicKeyItem(
                            keyEntry = keyEntry,
                            onDelete = { publicKeyViewModel.deleteKey(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddPublicKeyContent(
    onDismiss: () -> Unit,
    onAdd: (userId: String, username: String, pubkey: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val settingsViewModel = koinViewModel<SettingsViewModel>()
    val mainAccount by settingsViewModel.mainAccount.collectAsState()

    var searchMode by remember { mutableStateOf(SearchMode.ID) }
    var userId by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var host by remember { mutableStateOf(mainAccount?.host ?: "") }
    var pubkey by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок
        Text(
            text = "Добавить ключ пользователя",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        // Toggle режима поиска
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = searchMode == SearchMode.ID,
                onClick = {
                    searchMode = SearchMode.ID
                    userId = ""
                    username = ""
                    errorMessage = null
                },
                label = { Text("По ID") },
                enabled = true,
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = searchMode == SearchMode.NAME,
                onClick = {
                    searchMode = SearchMode.NAME
                    userId = ""
                    username = ""
                    errorMessage = null
                },
                label = { Text("По имени") },
                enabled = true,
                modifier = Modifier.weight(1f)
            )
        }

        // Поля ввода в зависимости от режима
        when (searchMode) {
            SearchMode.ID -> {
                OutlinedTextField(
                    value = userId,
                    onValueChange = {
                        userId = it
                        errorMessage = null
                    },
                    label = { Text("ID пользователя") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = true
                )
            }
            SearchMode.NAME -> {
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        errorMessage = null
                    },
                    label = { Text("Имя пользователя") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = true
                )
            }
        }

        OutlinedTextField(
            value = host,
            onValueChange = { host = it },
            label = { Text("Хост") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            singleLine = true
        )

        OutlinedTextField(
            value = pubkey,
            onValueChange = {
                pubkey = it
                errorMessage = null
            },
            label = { Text("Публичный ключ (base64)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 1,
            maxLines = 5,
            enabled = true
        )

        // Отображение ошибки если есть
        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = DarkColorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Кнопки действий
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = onDismiss,
                enabled = true,
                modifier = Modifier.weight(1f)
            ) {
                Text("Отмена")
            }

            Button(
                onClick = {
                    val idToUse = when (searchMode) {
                        SearchMode.ID -> userId
                        SearchMode.NAME -> username
                    }

                    val nameToUse = when (searchMode) {
                        SearchMode.ID -> userId
                        SearchMode.NAME -> username
                    }

                    onAdd(idToUse, nameToUse, pubkey)
                },
                enabled = when (searchMode) {
                    SearchMode.ID -> userId.isNotBlank() && pubkey.isNotBlank()
                    SearchMode.NAME -> username.isNotBlank() && pubkey.isNotBlank()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Добавить")
            }
        }
    }
}

@Composable
fun OtherPublicKeyItem(
    keyEntry: Pair<Chat, ByteArray>,
    onDelete: (Pair<Chat, ByteArray>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${keyEntry.first.name} (ID: ${keyEntry.first.userId})",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = keyEntry.second.decodeToString().take(50) +
                            if (keyEntry.second.decodeToString().length > 50) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkColorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { onDelete(keyEntry) }) {
                Icon(
                    Icons.FileRemove,
                    contentDescription = "Удалить ключ"
                )
            }
        }
    }
}

enum class SearchMode {
    ID, NAME
}

// Состояния для экспорта
sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    object Success : ExportState()
    object Error : ExportState()
}