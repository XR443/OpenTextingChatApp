package org.github.raspopov.composes
// LoadingWindow.kt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.github.raspopov.composes.images.Icons
import org.github.raspopov.composes.images.icons.XCirce
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

    AlertDialog(
        onDismissRequest = {},
        title = { },
        text = {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
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
        },
        confirmButton = {},
        dismissButton = {}
    )
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
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun EmptyContent() {
    Text("Нет данных")
}