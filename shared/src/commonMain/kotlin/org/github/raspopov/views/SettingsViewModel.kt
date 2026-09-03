package org.github.raspopov.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.github.raspopov.cache.AccountDatabase
import org.github.raspopov.cache.DatabaseDriverFactory
import org.github.raspopov.domain.Account
import org.github.raspopov.domain.Message
import org.github.raspopov.services.AccountService
import org.github.raspopov.services.platform.CryptoPlatform
import org.github.raspopov.services.platform.E2eeCrypto
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class AppSettings(
    val pageSize: Long = 100,
) {

}

@ExperimentalUuidApi
class SettingsViewModel(
    private val accountService: Lazy<AccountService>,
    private val masterPasswordViewModel: MasterPasswordViewModel,
    private val databaseDriverFactory: DatabaseDriverFactory,
) : ViewModel() {

    private val accountDatabase: AccountDatabase =
        AccountDatabase(databaseDriverFactory, masterPasswordViewModel)

    private val _accounts = MutableStateFlow<List<Account>>(listOf())
    val accounts = _accounts.asStateFlow()

    private val _mainAccount = MutableStateFlow<Account?>(null)
    val mainAccount: StateFlow<Account?> = _mainAccount.asStateFlow()

    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    private val _isDrawerOpen = MutableStateFlow(false)
    val isDrawerOpen: StateFlow<Boolean> = _isDrawerOpen.asStateFlow()

    fun isHaveAccounts(): Boolean {
        val existentAccounts = accountDatabase.getAccounts()
        _mainAccount.update { existentAccounts.find { it.isMain } }
        _accounts.update { existentAccounts }
        return existentAccounts.isNotEmpty()
    }

    fun openDrawer() {
        _accounts.update { accountDatabase.getAccounts() }
        _isDrawerOpen.update { true }
    }

    fun closeDrawer() {
        _isDrawerOpen.update { false }
        _accounts.update { listOf() }
    }

    fun addAccount(account: Account, callback: () -> Unit) {
        val key = masterPasswordViewModel.uiState.value.keys!!.key
        viewModelScope.launch {
            updateAccountId(account, key).apply {
                val keyPair = E2eeCrypto.generateKeyPair()
                privateKey = CryptoPlatform.encrypt(key, keyPair.privateKey)
                publicKey = keyPair.publicKey
            }
            accountDatabase.insertNewAccount(account)
            isHaveAccounts()
            callback()
        }
    }

    private suspend fun updateAccountId(account: Account, key: ByteArray): Account {
        account.password = CryptoPlatform.encrypt(key, account.password)
        val userInfo = accountService.value.getInfoAboutMe(account)
        account.id = userInfo.id
        return account
    }

    fun updateAccount(account: Account, callback: () -> Unit) {
        val key = masterPasswordViewModel.uiState.value.keys!!.key
        viewModelScope.launch {
            updateAccountId(account, key)
            _accounts.update { accountDatabase.update(account) }
            callback()
        }
    }

    fun deleteAccount(accountId: Uuid) {
        _accounts.update { accountDatabase.delete(accountId) }
    }

    fun setMainAccount(accountId: Uuid) {
        val accountsWithNewMain: List<Account> = accountDatabase.setMain(accountId)
        _accounts.update { accountsWithNewMain }
        _mainAccount.update { accountsWithNewMain.find { it.isMain } }
    }

    fun isMy(message: Message): Boolean {
        return mainAccount.value?.id == message.author
    }
}

sealed class Screen {
    object Main : Screen()
    object Accounts : Screen()
}