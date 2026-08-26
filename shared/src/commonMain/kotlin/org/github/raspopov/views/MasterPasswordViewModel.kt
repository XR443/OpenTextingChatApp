package org.github.raspopov.views

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.github.raspopov.domain.Account
import org.github.raspopov.domain.MasterKeys
import org.github.raspopov.services.AccountService
import org.github.raspopov.services.KeyDerivationService

class MasterPasswordViewModel(
    private val keyService: KeyDerivationService,
    private val settingsViewModel: Lazy<SettingsViewModel>,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MasterPasswordUiState())
    val uiState: StateFlow<MasterPasswordUiState> = _uiState.asStateFlow()

    fun onMasterPasswordEntered(password: String): MasterKeys {
        val mainAccount = settingsViewModel.value.mainAccount.value
        return onMasterPasswordEntered(password, mainAccount!!)
    }

    fun onMasterPasswordEntered(password: String, account: Account): MasterKeys {
        try {
            if (account.salt == null)
                account.apply {
                    // Генерируем три разных ключа с разными солями
                    salt = keyService.generateSalt()
                }
            val keys = keyService.generateMasterKeys(password, account)
            _uiState.update {
                MasterPasswordUiState(
                    isLoading = false,
                    keys = keys,
                    error = null
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                MasterPasswordUiState(
                    isLoading = false,
                    keys = null,
                    error = e.message ?: "Unknown error"
                )
            }
        }

        return _uiState.value.keys!!
    }

    fun reset() {
        _uiState.value = MasterPasswordUiState()
    }
}

data class MasterPasswordUiState(
    val isLoading: Boolean = false,
    val keys: MasterKeys? = null,
    val error: String? = null,
)