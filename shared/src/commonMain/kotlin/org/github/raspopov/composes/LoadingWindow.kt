package org.github.raspopov.composes
// LoadingWindow.kt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.github.raspopov.composes.images.Icons
import org.github.raspopov.composes.images.icons.XCirce
import org.github.raspopov.theme.DarkColorScheme
import org.github.raspopov.views.LoadingViewModel

@Composable
fun LoadingWindow(
    viewModel: LoadingViewModel,
    modifier: Modifier = Modifier,
    loadingContent: @Composable () -> Unit = { DefaultLoadingContent() },
    errorContent: @Composable (String) -> Unit = { DefaultErrorContent(it) },
    content: @Composable () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

//    AlertDialog(
//        containerColor = DarkColorScheme.background,
//        onDismissRequest = {},
//        title = { },
//        text = {
//            Box(
//                modifier = modifier
//                    .fillMaxWidth()
//                    .wrapContentHeight()
//                    .background(color = DarkColorScheme.background),
//                contentAlignment = Alignment.Center
//            ) {
//                when {
//                    state.isLoading -> {
//                        loadingContent()
//                    }
//
//                    state.error != null -> {
//                        errorContent(state.error!!)
//                    }
//
//                    else -> {
//                        content()
//                    }
//                }
//            }
//        },
//        confirmButton = {},
//        dismissButton = {}
//    )
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = false, // Отключаем стандартную ширину
            dismissOnClickOutside = false
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = DarkColorScheme.background,
//                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> {
                    loadingContent()
                }

                state.error != null -> {
                    errorContent(state.error!!)
                }

                else -> {
                    content()
                }
            }
        }
    }
}

// Дефолтные компоненты для загрузки и ошибок
@Composable
fun DefaultLoadingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text("Загрузка...")
    }
}

@Composable
fun DefaultErrorContent(error: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.XCirce,
            contentDescription = "Error",
            tint = DarkColorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error,
            color = DarkColorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun EmptyContent() {
    Text("Нет данных")
}
