package com.jujodevs.cursotestingandroid.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jujodevs.cursotestingandroid.core.domain.model.ThemeMode
import com.jujodevs.cursotestingandroid.core.presentation.components.MarketTopAppBar
import com.jujodevs.cursotestingandroid.ui.theme.CursoTestingAndroidTheme

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            MarketTopAppBar(
                title = "Ajustes",
                onBack = { onBack() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )

                        Text(
                            text = "Filtros y visualización",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    HorizontalDivider()

                    SwitchableOptionRow(
                        title = "Solo productos en Stock",
                        description = "Muestra únicamente productos disponibles",
                        checked = uiState.inStockOnly,
                        onCheckedChange = { newState ->
                            settingsViewModel.onAction(SettingsAction.SetInStockOnly(newState))
                        }
                    )

                    HorizontalDivider()

                    SwitchableOptionRow(
                        title = "Mostrar impuestos incluídos",
                        description = "Incluir impuestos de los precios mostrados",
                        checked = true,
                        onCheckedChange = { }
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )

                        Text(
                            text = "Apariencia",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    HorizontalDivider()

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Tema de la aplicación",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "Elige entre modo claro, oscuro o seguir el sistema",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    0,
                                    3
                                ),
                                onClick = {
                                    settingsViewModel.onAction(
                                        SettingsAction.SetThemeMode(ThemeMode.SYSTEM)
                                    )
                                },
                                selected = uiState.themeMode == ThemeMode.SYSTEM,
                                label = { Text("Sistema") }
                            )
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    1,
                                    3
                                ),
                                onClick = {
                                    settingsViewModel.onAction(
                                        SettingsAction.SetThemeMode(ThemeMode.LIGHT)
                                    )
                                },
                                selected = uiState.themeMode == ThemeMode.LIGHT,
                                label = { Text("Claro") }
                            )
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    2,
                                    3
                                ),
                                onClick = {
                                    settingsViewModel.onAction(
                                        SettingsAction.SetThemeMode(ThemeMode.DARK)
                                    )
                                },
                                selected = uiState.themeMode == ThemeMode.DARK,
                                label = { Text("Oscuro") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SwitchableOptionRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenPreview() {
    CursoTestingAndroidTheme {
        SettingsScreen(onBack = { })
    }
}
