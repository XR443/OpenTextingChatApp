package org.github.raspopov.composes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.github.raspopov.composes.images.Icons
import org.github.raspopov.composes.images.icons.Edit
import org.github.raspopov.composes.images.icons.Info
import org.github.raspopov.composes.images.icons.LeftArrow
import org.github.raspopov.composes.images.icons.Lock
import org.github.raspopov.composes.images.icons.Person
import org.github.raspopov.theme.DarkColorScheme
import org.github.raspopov.views.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsDrawer(
    onCloseDrawer: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToKeys: () -> Unit,
) {
    val settingsViewModel = koinViewModel<SettingsViewModel>()
    val accounts by settingsViewModel.accounts.collectAsState()
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .width(320.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Меню",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onCloseDrawer) {
                    Icon(
                        Icons.LeftArrow,
//                        modifier = Modifier.size(24.dp),
                        contentDescription = "Закрыть"
                    )
                }
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Пункты меню
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    // Кнопка "Аккаунты"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onNavigateToAccounts()
                            onCloseDrawer()
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = DarkColorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Person,
                                contentDescription = "Аккаунты",
                                modifier = Modifier.size(24.dp),
                                tint = DarkColorScheme.primary
                            )
                            Text(
                                text = "Аккаунты",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${accounts.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkColorScheme.onSurfaceVariant
                            )
                            Icon(
                                Icons.Person,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = DarkColorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Можно добавить другие пункты меню
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkColorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Edit,
                                contentDescription = "Настройки",
                                modifier = Modifier.size(24.dp),
                                tint = DarkColorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Настройки",
                                style = MaterialTheme.typography.titleMedium,
                                color = DarkColorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Можно добавить другие пункты меню
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onNavigateToKeys()
                            onCloseDrawer()
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = DarkColorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Lock,
                                contentDescription = "Управление ключами",
                                modifier = Modifier.size(24.dp),
                                tint = DarkColorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Управление ключами",
                                style = MaterialTheme.typography.titleMedium,
                                color = DarkColorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkColorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Info,
                                contentDescription = "О программе",
                                modifier = Modifier.size(24.dp),
                                tint = DarkColorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "О программе",
                                style = MaterialTheme.typography.titleMedium,
                                color = DarkColorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}