package org.github.raspopov.composes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.github.raspopov.composes.images.Icons
import org.github.raspopov.composes.images.icons.EyeClosed
import org.github.raspopov.composes.images.icons.EyeOpened
import org.github.raspopov.composes.images.icons.Lock
import org.github.raspopov.domain.MasterKeys
import org.github.raspopov.theme.DarkColorScheme
import org.github.raspopov.views.MasterPasswordViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MasterPasswordScreen(
    onMasterKeyGenerated: (MasterKeys) -> Unit,
    onCancel: () -> Unit,
    title: String = "Enter Master Password"
) {
    var masterPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var runTask by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isConfirmMode by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val masterPasswordViewModel = koinViewModel<MasterPasswordViewModel>()
    val density = LocalDensity.current

    // Получаем высоту клавиатуры
    val imeInsets = WindowInsets.ime
    val imeHeight = with(density) { imeInsets.getBottom(density).toDp() }

    // Проверяем, видна ли клавиатура
    val isKeyboardVisible = imeHeight > 0.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = DarkColorScheme.background)
            .navigationBarsPadding()
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .imePadding(), // Это поднимает контент
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Иконка замка
            Icon(
                Icons.Lock,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = DarkColorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isConfirmMode) "Confirm Master Password" else title,
                style = MaterialTheme.typography.headlineSmall,
                color = DarkColorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Поле ввода пароля
            OutlinedTextField(
                value = masterPassword,
                onValueChange = {
                    masterPassword = it
                    errorMessage = null
                },
                label = { Text("Master Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                isError = errorMessage != null,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.EyeClosed
                            else Icons.EyeOpened,
                            contentDescription = if (passwordVisible) "Hide password"
                            else "Show password"
                        )
                    }
                },
                enabled = !isLoading
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = DarkColorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Поле подтверждения (при создании нового пароля)
            if (isConfirmMode) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = { Text("Confirm Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    isError = errorMessage != null,
                    enabled = !isLoading,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Индикатор сложности пароля
            PasswordStrengthIndicator(password = masterPassword)

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопки действий
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val passwordsState by masterPasswordViewModel.uiState.collectAsState()
                if (passwordsState.keys != null)
                    TextButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }

                LaunchedEffect(runTask) {
                    if (runTask) {
                        runTask = false
                        validateAndGenerateKeys(
                            masterPassword = masterPassword,
                            confirmPassword = confirmPassword,
                            isConfirmMode = isConfirmMode,
                            masterPasswordViewModel = masterPasswordViewModel,
                            coroutineScope = coroutineScope,
                            onSuccess = { keys ->
                                onMasterKeyGenerated(keys)
                                isLoading = false
                            },
                            onError = { error ->
                                errorMessage = error
                                isLoading = false
                            }
                        )
                    }
                }

                Button(
                    onClick = {
                        isLoading = true
                        runTask = true
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (isConfirmMode) "Create" else "Unlock")
                    }
                }
            }

            // Динамический отступ снизу, который увеличивается когда клавиатура открыта
            Spacer(
                modifier = Modifier.height(
                    if (isKeyboardVisible) 32.dp else 16.dp
                )
            )
        }
    }
}

@Composable
fun PasswordStrengthIndicator(password: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        DarkColorScheme.surfaceVariant
                    )
            )
        }
    }
}

private fun validateAndGenerateKeys(
    masterPassword: String,
    confirmPassword: String,
    isConfirmMode: Boolean,
    masterPasswordViewModel: MasterPasswordViewModel,
    coroutineScope: CoroutineScope,
    onSuccess: (MasterKeys) -> Unit,
    onError: (String) -> Unit
) {
    when {
        masterPassword.isBlank() -> {
            onError("Master password cannot be empty")
            return
        }
        isConfirmMode && masterPassword != confirmPassword -> {
            onError("Passwords do not match")
            return
        }
    }
    coroutineScope.launch {
        try {
            val keys = masterPasswordViewModel.onMasterPasswordEntered(masterPassword)
            onSuccess(keys)
        } catch (e: Exception) {
            onError("Failed to generate keys: ${e.message}")
        }
    }
}