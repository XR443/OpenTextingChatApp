package org.github.raspopov

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.ktor.util.reflect.instanceOf
import org.github.raspopov.composes.AccountsScreen
import org.github.raspopov.composes.AddPublicKeyContent
import org.github.raspopov.composes.ChatList
import org.github.raspopov.composes.ChatScreen
import org.github.raspopov.composes.LoadingWindow
import org.github.raspopov.composes.MasterPasswordScreen
import org.github.raspopov.composes.PublicKeyManagerScreen
import org.github.raspopov.navigation.AccountScreenRoute
import org.github.raspopov.navigation.ChatListRoute
import org.github.raspopov.navigation.ChatScreenRoute
import org.github.raspopov.navigation.MasterPasswordRoute
import org.github.raspopov.navigation.PublicKeysScreenRoute
import org.github.raspopov.navigation.config
import org.github.raspopov.views.ChatScreenViewModel
import org.github.raspopov.views.LoadingViewModel
import org.github.raspopov.views.MasterPasswordViewModel
import org.github.raspopov.views.ObserverViewModel
import org.github.raspopov.views.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi

@ExperimentalUuidApi
@Composable
fun App() {
    val settingsViewModel = koinViewModel<SettingsViewModel>()
    val observerViewModel = koinViewModel<ObserverViewModel>()

    val masterPasswordViewModel = koinViewModel<MasterPasswordViewModel>()

    val masterPasswordState by masterPasswordViewModel.uiState.collectAsState()

    var haveAccounts = remember { settingsViewModel.isHaveAccounts() }
    val nextScreen =
        if (!haveAccounts) AccountScreenRoute // todo а что если нет основного аккаунта
        else if (masterPasswordState.keys == null) MasterPasswordRoute
        else ChatListRoute

    val backStack = rememberNavBackStack(config, nextScreen)

    fun removeLastAndGoToMainScreen(mainScreenRoute: NavKey = ChatListRoute) {
        if (backStack.size == 1) {
            if (!backStack[0].instanceOf(mainScreenRoute::class)) {
                backStack.add(mainScreenRoute)
                backStack.removeFirst()
            }
        } else if (backStack.isEmpty()) {
            backStack.add(mainScreenRoute)
        } else {
            backStack.removeLast()
        }
    }

    fun backOrGoToMain() {
        removeLastAndGoToMainScreen()
        val keys = masterPasswordViewModel.uiState.value.keys
        if (keys == null) {
            backStack.add(MasterPasswordRoute)
        }
    }

    MaterialTheme {
        MaterialTheme {
            NavDisplay(
                backStack = backStack,
                onBack = { backOrGoToMain() },
                entryProvider = entryProvider {
                    entry<ChatListRoute> {
                        ChatList(
                            onChatRowClick = { backStack.add(ChatScreenRoute(it)) },
                            onToggleDrawer = { settingsViewModel.openDrawer() },
                            onNavigateToAccounts = { backStack.add(AccountScreenRoute) },
                            onNavigateToKeys = { backStack.add(PublicKeysScreenRoute) },
                        )
                    }
                    entry<ChatScreenRoute> { key ->
                        val chatScreenViewModel = koinViewModel<ChatScreenViewModel>()
                        val openedChat = key.chat
                        chatScreenViewModel.openChat(openedChat)
                        ChatScreen(
                            openedChat,
                            onBack = { backOrGoToMain() },
                        )
                    }
                    entry<AccountScreenRoute> { key ->
                        masterPasswordViewModel.reset()
                        AccountsScreen(
                            onAddAccount = {
                                if (!settingsViewModel.isHaveAccounts()) {
                                    haveAccounts = true
                                    settingsViewModel.closeDrawer()
                                    backOrGoToMain()
                                }
                            },
                            onUpdateAccount = {
                            },
                            onDeleteAccount = {
                            },
                            onSetMainAccount = {
                            },
                            onNavigateBack = {
                                    backOrGoToMain()
                            },
                            haveAccounts
                        )
                    }
                    entry<MasterPasswordRoute> { key ->
                        val loadingViewModel = koinViewModel<LoadingViewModel>()

                        LoadingWindow(
                            loadingViewModel,
//            modifier,
                            content = {
                                MasterPasswordScreen(
                                    onMasterKeyGenerated = { keys ->
                                        loadingViewModel.startLoading()
                                        settingsViewModel.isHaveAccounts()
                                        observerViewModel.runWatcher()
                                        loadingViewModel.reset()
                                        backOrGoToMain()
                                    },
                                    onCancel = {
                                        // Выход из приложения или переход на экран входа
                                    }
                                )
                            }

                        )
                    }
                    entry<PublicKeysScreenRoute> { key ->
                        PublicKeyManagerScreen(
                            onNavigateBack = { backOrGoToMain() }
                        )
                    }
                }
            )
        }
    }
}
