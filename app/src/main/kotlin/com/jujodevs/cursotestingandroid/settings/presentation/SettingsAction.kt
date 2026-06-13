package com.jujodevs.cursotestingandroid.settings.presentation

import com.jujodevs.cursotestingandroid.core.domain.model.ThemeMode

sealed interface SettingsAction {
    data object OnBack : SettingsAction

    data class SetInStockOnly(
        val inStockOnly: Boolean,
    ) : SettingsAction

    data class SetThemeMode(
        val themeMode: ThemeMode,
    ) : SettingsAction
}
