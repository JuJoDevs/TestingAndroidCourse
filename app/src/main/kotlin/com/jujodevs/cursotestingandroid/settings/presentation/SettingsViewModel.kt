package com.jujodevs.cursotestingandroid.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jujodevs.cursotestingandroid.core.domain.model.ThemeMode
import com.jujodevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState = combine(
        settingsRepository.inStockOnly,
        settingsRepository.themeMode,
    ) { inStockOnly, themeMode ->
        SettingsUiState(
            inStockOnly = inStockOnly,
            themeMode = themeMode
        )
    }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = SettingsUiState(),
        )

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SetInStockOnly -> setInStockOnly(action.inStockOnly)
            is SettingsAction.SetThemeMode -> setThemeMode(action.themeMode)
        }
    }

    private fun setInStockOnly(inStockOnly: Boolean) {
        viewModelScope.launch {
            settingsRepository.setInStockOnly(inStockOnly)
        }
    }

    private fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(themeMode)
        }
    }
}