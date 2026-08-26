package org.github.raspopov.views
// LoadingViewModel.kt
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoadingState(
    val isLoading: Boolean = false,
    val error: String? = null
) {
    fun isSuccess() = error == null && !isLoading
    fun isError() = error != null
}

class LoadingViewModel : ViewModel() {
    private val _state = MutableStateFlow(LoadingState())
    val state: StateFlow<LoadingState> = _state.asStateFlow()

    fun startLoading() {
        _state.update { it.copy(isLoading = true, error = null) }
    }

    fun onSuccess(data: Any) {
        _state.update { it.copy(isLoading = false, error = null) }
    }

    fun onError(error: String) {
        _state.update { it.copy(isLoading = false, error = error) }
    }

    fun reset() {
        _state.update { LoadingState() }
    }

    fun clearData() {
        _state.update { it.copy() }
    }
}