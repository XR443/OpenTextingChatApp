package org.github.raspopov.composes

// AccountsScreen.kt
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.github.raspopov.composes.images.Icons
import org.github.raspopov.composes.images.icons.Edit
import org.github.raspopov.composes.images.icons.EyeClosed
import org.github.raspopov.composes.images.icons.EyeOpened
import org.github.raspopov.composes.images.icons.LeftArrow
import org.github.raspopov.composes.images.icons.Person
import org.github.raspopov.composes.images.icons.PersonAdd
import org.github.raspopov.composes.images.icons.PersonRemove
import org.github.raspopov.domain.Account
import org.github.raspopov.theme.DarkColorScheme
import org.github.raspopov.views.LoadingViewModel
import org.github.raspopov.views.MasterPasswordViewModel
import org.github.raspopov.views.ObserverViewModel
import org.github.raspopov.views.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalUuidApi
@Composable
fun AccountsScreen(
    onAddAccount: (Account) -> Unit,
    onUpdateAccount: (Account) -> Unit,
    onDeleteAccount: (Uuid) -> Unit,
    onSetMainAccount: (Uuid) -> Unit,
    onNavigateBack: () -> Unit,
    backButtonEnabled: Boolean
) {
    val settingsViewModel = koinViewModel<SettingsViewModel>()

    settingsViewModel.isHaveAccounts()

    val masterPasswordViewModel = koinViewModel<MasterPasswordViewModel>()
    val observerViewModel = koinViewModel<ObserverViewModel>()

    val accounts by settingsViewModel.accounts.collectAsState()
    val mainAccount by settingsViewModel.mainAccount.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<Account?>(null) }

    fun stopDoAndRun(doFunction: () -> Unit) {
        observerViewModel.stopWatcher()
        doFunction()
        masterPasswordViewModel.reset()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Управление аккаунтами") },
                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
                    IconButton(
                        onClick = {
                            stopDoAndRun {
                                onNavigateBack()
                            }
                        },
                        enabled = backButtonEnabled || mainAccount != null
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
                            Icons.PersonAdd,
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
                .padding(paddingValues)
        ) {
            if (accounts.isEmpty()) {
                // Пустое состояние
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = DarkColorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Нет аккаунтов",
                            style = MaterialTheme.typography.titleLarge,
                            color = DarkColorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Нажмите кнопку + чтобы добавить",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkColorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(accounts) { account ->
                        AccountCard(
                            account = account,
                            onEdit = {
                                editingAccount = account
                            },
                            onDelete = {
                                stopDoAndRun {
                                    settingsViewModel.deleteAccount(account.id)
                                    onDeleteAccount(account.id)
                                }
                            },
                            onSetMain = {
                                stopDoAndRun {
                                    masterPasswordViewModel.reset()
                                    settingsViewModel.setMainAccount(account.id)
                                    onSetMainAccount(account.id)
                                }
                            },
                            isOnlyAccount = accounts.size == 1
                        )
                    }
                }
            }
        }
    }

    val loadingViewModel = koinViewModel<LoadingViewModel>()
    // Диалог добавления
    if (showAddDialog) {
        LoadingWindow(
            loadingViewModel,
            content = {
                AddAccountDialog(
                    onDismiss = { showAddDialog = false },
                    onAdd = { account, masterPassword ->
                        stopDoAndRun {
                            loadingViewModel.startLoading()
                            masterPasswordViewModel.onMasterPasswordEntered(masterPassword, account)
                            settingsViewModel.addAccount(account) {
                                loadingViewModel.reset()
                                showAddDialog = false
                                onAddAccount(account)
                            }
                        }
                    }
                )
            }
        )
    }

    // Диалог редактирования
    if (editingAccount != null) {
        LoadingWindow(
            loadingViewModel,
            content = {
                AddAccountDialog(
                    initialAccount = editingAccount,
                    onDismiss = { editingAccount = null },
                    onAdd = { account, masterPassword ->
                        stopDoAndRun {
                            loadingViewModel.startLoading()
                            stopDoAndRun {
                                settingsViewModel.updateAccount(account) {
                                    loadingViewModel.reset()
                                    editingAccount = null
                                    onUpdateAccount(account)
                                }
                            }
                        }
                    }
                )
            }
        )
    }
}

@ExperimentalUuidApi
@Composable
fun AccountCard(
    account: Account,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetMain: () -> Unit,
    isOnlyAccount: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkColorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = account.username,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (account.isMain)
                                DarkColorScheme.primary
                            else
                                DarkColorScheme.onSurface
                        )
                        if (account.isMain) {
                            Surface(
                                color = DarkColorScheme.primary,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "Основной",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DarkColorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Хост: ${account.host}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkColorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Пароль: ${"•".repeat(account.password.size)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkColorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (!account.isMain && !isOnlyAccount) {
                        TextButton(
                            onClick = onSetMain,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Сделать основным", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Edit,
                            contentDescription = "Редактировать",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (!isOnlyAccount) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.PersonRemove,
                                contentDescription = "Удалить",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AddAccountDialog(
    initialAccount: Account? = null,
    onDismiss: () -> Unit,
    onAdd: (Account, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(initialAccount?.username ?: "") }
    var host by remember { mutableStateOf(initialAccount?.host ?: "http://192.168.31.10:8000") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var masterPassword by remember { mutableStateOf("") }
    var masterPasswordVisible by remember { mutableStateOf(false) }
    val isEditing = initialAccount != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок
        Text(
            text = if (isEditing) "✏️ Редактировать аккаунт" else "➕ Добавить аккаунт",
            style = MaterialTheme.typography.headlineSmall,
            color = DarkColorScheme.onSurface
        )

        // Поля ввода
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Имя аккаунта") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = host,
            onValueChange = { host = it },
            label = { Text("Хост сервера") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            placeholder = { Text("Пароль от аккаунта на сервере") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            trailingIcon = {
                val image = if (passwordVisible) {
                    Icons.EyeOpened
                } else {
                    Icons.EyeClosed
                }
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(image, contentDescription = null)
                }
            }
        )

        OutlinedTextField(
            value = masterPassword,
            onValueChange = { masterPassword = it },
            label = { Text("Мастер-пароль") },
            placeholder = { Text("Пароль шифрования данных на устройстве") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (masterPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            trailingIcon = {
                val image = if (masterPasswordVisible) {
                    Icons.EyeOpened
                } else {
                    Icons.EyeClosed
                }
                IconButton(onClick = { masterPasswordVisible = !masterPasswordVisible }) {
                    Icon(image, contentDescription = null)
                }
            }
        )

        // Кнопки
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }

            Button(
                onClick = {
                    val account = Account(
                        id = initialAccount?.id ?: Uuid.random(),
                        username = name,
                        host = host,
                        password = password.encodeToByteArray(),
                        isMain = initialAccount?.isMain ?: false
                    )
                    onAdd(account, masterPassword)
                },
                enabled = name.isNotBlank() && host.isNotBlank() && password.isNotBlank()
            ) {
                Text(if (isEditing) "Сохранить" else "Добавить")
            }
        }
    }
}