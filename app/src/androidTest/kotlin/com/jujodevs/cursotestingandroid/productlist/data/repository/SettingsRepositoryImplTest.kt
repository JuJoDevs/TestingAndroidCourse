package com.jujodevs.cursotestingandroid.productlist.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jujodevs.cursotestingandroid.core.MainDispatcherRule
import com.jujodevs.cursotestingandroid.core.domain.model.ThemeMode
import com.jujodevs.cursotestingandroid.productlist.domain.model.SortOption
import com.jujodevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsRepositoryImplTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    @get:Rule val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun givenNoDataSaved_whenInStockOnlyIsRead_thenReturnsDefaultFalse() = runTest {
        val result = settingsRepository.inStockOnly.first()

        assertEquals(false, result)
    }

    @Test
    fun givenNoDataSaved_whenFilterVisibleIsRead_thenReturnsDefaultTrue() = runTest {
        val result = settingsRepository.filtersVisible.first()

        assertEquals(true, result)
    }

    @Test
    fun givenNoDataSaved_whenSelectedCategoryIsRead_thenReturnsDefaultNull() = runTest {
        val result = settingsRepository.selectedCategory.first()

        assertNull(result)
    }

    @Test
    fun givenNoDataSaved_whenThemeModeIsRead_thenReturnsDefaultSystem() = runTest {
        val result = settingsRepository.themeMode.first()

        assertEquals(ThemeMode.SYSTEM, result)
    }

    @Test
    fun givenNoDataSaved_whenSortOptionIsRead_thenReturnsDefaultNone() = runTest {
        val result = settingsRepository.sortOption.first()

        assertEquals(SortOption.NONE, result)
    }

    @Test
    fun givenRepository_whenSetFilterVisibleToFalse_thenPersistValue() = runTest {
        settingsRepository.setFiltersVisible(false)

        val result = settingsRepository.filtersVisible.first()

        assertEquals(false, result)
    }

    @Test
    fun givenMultipleSettingsChanges_whenReadAll_thenStateIsConsistent() = runTest {
        with(settingsRepository) {
            setFiltersVisible(false)
            setInStockOnly(true)
            setSortOption(SortOption.DISCOUNT)
            setThemeMode(ThemeMode.DARK)
            setSelectedCategory("Electronics")

            assertTrue(!filtersVisible.first())
            assertTrue(inStockOnly.first())
            assertTrue(sortOption.first() == SortOption.DISCOUNT)
            assertTrue(themeMode.first() == ThemeMode.DARK)
            assertTrue(selectedCategory.first() == "Electronics")
        }

    }
}