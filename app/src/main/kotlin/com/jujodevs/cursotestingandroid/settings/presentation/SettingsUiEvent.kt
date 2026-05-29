package com.jujodevs.cursotestingandroid.settings.presentation

sealed interface SettingsUiEvent {
    data object onBack : SettingsUiEvent
}