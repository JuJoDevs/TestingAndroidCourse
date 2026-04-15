package com.jujodevs.cursotestingandroid.core.fakes

import com.jujodevs.cursotestingandroid.core.domain.model.ThemeMode
import com.jujodevs.cursotestingandroid.productlist.domain.model.SortOption
import com.jujodevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeSettingsRepository: SettingsRepository {
    private val _inStockOnly = MutableStateFlow(false)
    private val _themeMode = MutableStateFlow<ThemeMode>(ThemeMode.SYSTEM)
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _filtersVisible = MutableStateFlow(true)
    private val _sortOption = MutableStateFlow(SortOption.NONE)

    override val inStockOnly: Flow<Boolean> = _inStockOnly.asStateFlow()
    override val themeMode: Flow<ThemeMode> = _themeMode.asStateFlow()
    override val selectedCategory: Flow<String?> = _selectedCategory.asStateFlow()
    override val filtersVisible: Flow<Boolean> = _filtersVisible.asStateFlow()
    override val sortOption: Flow<SortOption> = _sortOption.asStateFlow()

    override suspend fun setInStockOnly(value: Boolean) {
        _inStockOnly.update { value }
    }

    override suspend fun setThemeMode(value: ThemeMode) {
        _themeMode.update { value }
    }

    override suspend fun setSelectedCategory(value: String?) {
        _selectedCategory.update { value }
    }

    override suspend fun setFiltersVisible(value: Boolean) {
        _filtersVisible.update { value }
    }

    override suspend fun setSortOption(value: SortOption) {
        _sortOption.update { value }
    }
}