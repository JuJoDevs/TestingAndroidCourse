package com.jujodevs.cursotestingandroid.settings.presentation

import com.jujodevs.cursotestingandroid.core.domain.model.ThemeMode

data class SettingsUiState(
    val inStockOnly: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
