package com.jujodevs.cursotestingandroid.productlist.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jujodevs.cursotestingandroid.core.domain.model.ThemeMode
import com.jujodevs.cursotestingandroid.core.domain.safeRunCatching
import com.jujodevs.cursotestingandroid.productlist.domain.model.SortOption
import com.jujodevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) : SettingsRepository {
        companion object {
            private val IN_STOCK_ONLY_KEY = booleanPreferencesKey("IN_STOCK_ONLY_KEY")
            private val FILTER_VISIBLE_KEY = booleanPreferencesKey("FILTER_VISIBLE_KEY")
            private val SELECTED_CATEGORY_KEY = stringPreferencesKey("SELECTED_CATEGORY_KEY")
            private val THEME_MODE_KEY = intPreferencesKey("THEME_MODE_KEY")
            private val SORT_OPTION_KEY = stringPreferencesKey("SHORT_OPTION_KEY")
        }

        private val dataStoreFlow =
            dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }

        override val inStockOnly: Flow<Boolean> =
            dataStoreFlow.map { preferences -> preferences[IN_STOCK_ONLY_KEY] ?: false }
        override val themeMode: Flow<ThemeMode> =
            dataStoreFlow.map { preferences ->
                when (preferences[THEME_MODE_KEY]) {
                    ThemeMode.SYSTEM.id -> ThemeMode.SYSTEM
                    ThemeMode.DARK.id -> ThemeMode.DARK
                    ThemeMode.LIGHT.id -> ThemeMode.LIGHT
                    else -> ThemeMode.SYSTEM
                }
            }
        override val selectedCategory: Flow<String?> =
            dataStoreFlow.map { preferences -> preferences[SELECTED_CATEGORY_KEY] }
        override val filtersVisible: Flow<Boolean> =
            dataStoreFlow.map { preferences -> preferences[FILTER_VISIBLE_KEY] ?: true }
        override val sortOption: Flow<SortOption> =
            dataStoreFlow.map { preferences ->
                val raw = preferences[SORT_OPTION_KEY].orEmpty()
                safeRunCatching { SortOption.valueOf(raw) }.getOrDefault(SortOption.NONE)
            }

        override suspend fun setInStockOnly(value: Boolean) {
            dataStore.edit { preferences -> preferences[IN_STOCK_ONLY_KEY] = value }
        }

        override suspend fun setThemeMode(value: ThemeMode) {
            dataStore.edit { preferences -> preferences[THEME_MODE_KEY] = value.id }
        }

        override suspend fun setSelectedCategory(value: String?) {
            dataStore.edit { preferences ->
                if (value == null) {
                    preferences.remove(SELECTED_CATEGORY_KEY)
                } else {
                    preferences[SELECTED_CATEGORY_KEY] = value
                }
            }
        }

        override suspend fun setFiltersVisible(value: Boolean) {
            dataStore.edit { preferences -> preferences[FILTER_VISIBLE_KEY] = value }
        }

        override suspend fun setSortOption(value: SortOption) {
            dataStore.edit { preferences -> preferences[SORT_OPTION_KEY] = value.name }
        }
    }
