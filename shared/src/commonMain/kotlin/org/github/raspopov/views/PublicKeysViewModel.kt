package org.github.raspopov.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.github.raspopov.domain.Chat
import org.github.raspopov.extensions.fromBase64
import org.github.raspopov.services.AccountService
import org.github.raspopov.services.KeysService
import org.github.raspopov.services.platform.ClipboardManager
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
class PublicKeysViewModel(
    private val keysService: KeysService,
    private val accountService: AccountService,
    private val masterPasswordViewModel: MasterPasswordViewModel,
    private val settingsViewModel: SettingsViewModel,
) : ViewModel() {

    private val _otherKeys = MutableStateFlow<List<Pair<Chat, ByteArray>>>(listOf())
    val otherKeys = _otherKeys.asStateFlow()

    private val _mainAccountKey = MutableStateFlow<ByteArray?>(null)
    val mainAccountKey: StateFlow<ByteArray?> = _mainAccountKey.asStateFlow()

    fun loadKeys() {
        _mainAccountKey.update { settingsViewModel.mainAccount.value!!.publicKey }
        _otherKeys.update {
            keysService.allUsers(settingsViewModel.mainAccount.value!!.id)
                .map { it to keysService.getPublicKeyOf(it.userId) }
        }
    }

    fun copyToClipboard(text: String) {
        ClipboardManager.copyText(text)
    }

    fun deleteKey(it: Pair<Chat, ByteArray>) {
        deleteKey(it.first, it.second)
    }

    fun deleteKey(chat: Chat, key: ByteArray) {
        keysService.delete(chat.userId, settingsViewModel.mainAccount.value!!.id)
    }

    fun addPublicKey(id: String, username: String, pubkey: String, callback: () -> Unit) {
        viewModelScope.launch {
            val info = accountService.getInfo(Uuid.parse(id))
            val pubkeyBytes = try {
                pubkey.fromBase64()
            } catch (_: Exception) {
                pubkey.encodeToByteArray()
            }
            val mainId = settingsViewModel.mainAccount.value!!.id
            keysService.addKeyFor(Uuid.parse(id), mainId, info.username, pubkeyBytes)
            callback()
        }
    }
    fun exportPublicKey(onSuccess: () -> Unit, onError: () -> Unit) {
        // Логика экспорта ключа на сервер
        viewModelScope.launch {
            try {
                val result = keysService.uploadPublicKey()
                if (result) {
                    onSuccess()
                } else {
                    onError()
                }
            } catch (e: Exception) {
                onError()
            }
        }
    }

    fun showToast(message: String) {
        // Показать Toast сообщение
    }
}
