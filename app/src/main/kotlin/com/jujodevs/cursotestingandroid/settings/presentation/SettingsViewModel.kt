package com.jujodevs.cursotestingandroid.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jujodevs.cursotestingandroid.core.domain.model.ThemeMode
import com.jujodevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>(extraBufferCapacity = 1)
    val uiEvent = _uiEvent.asSharedFlow()

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.OnBack -> onBack()
            is SettingsAction.SetInStockOnly -> setInStockOnly(action.inStockOnly)
            is SettingsAction.SetThemeMode -> setThemeMode(action.themeMode)
        }
    }

    private fun onBack() {
        viewModelScope.launch {
            _uiEvent.emit(SettingsUiEvent.onBack)
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