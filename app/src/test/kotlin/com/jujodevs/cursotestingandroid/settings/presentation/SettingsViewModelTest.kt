package com.jujodevs.cursotestingandroid.settings.presentation

import app.cash.turbine.turbineScope
import com.jujodevs.cursotestingandroid.core.MainDispatcherRule
import com.jujodevs.cursotestingandroid.core.domain.model.ThemeMode
import com.jujodevs.cursotestingandroid.core.fakes.FakeSettingsRepository
import com.jujodevs.cursotestingandroid.core.runTurbineTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `GIVEN repository with values WHEN viewmodel is initialized THEN ui state is updated`() =
        runTest {
            turbineScope {
                val settingsRepository = FakeSettingsRepository().apply { setInStockOnly(true) }
                val viewmodel = SettingsViewModel(settingsRepository)
                val state = viewmodel.uiState.testIn(this)

                val result = state.awaitItem()

                assertTrue(result.inStockOnly)
                state.cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN viewmodel WHEN mode is changed THEN ui stated and repository are updated`() =
        runTurbineTest {
            val settingsRepository = FakeSettingsRepository().apply { setThemeMode(ThemeMode.LIGHT) }
            val viewmodel = SettingsViewModel(settingsRepository)
            val state = viewmodel.uiState.testIn(this)
            state.awaitItem()

            viewmodel.onAction(SettingsAction.SetThemeMode(ThemeMode.DARK))

            val updateState = state.awaitItem()
            assertEquals(ThemeMode.DARK, updateState.themeMode)
            assertEquals(ThemeMode.DARK, settingsRepository.themeMode.first())
            state.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `GIVEN viewmodel WHEN stock only is changed THEN ui stated and repository are updated`() =
        runTurbineTest {
            val settingsRepository = FakeSettingsRepository().apply { setInStockOnly(false) }
            val viewmodel = SettingsViewModel(settingsRepository)
            val state = viewmodel.uiState.testIn(this)
            state.awaitItem()

            viewmodel.onAction(SettingsAction.SetInStockOnly(true))

            val updateState = state.awaitItem()
            assertEquals(true, updateState.inStockOnly)
            assertEquals(true, settingsRepository.inStockOnly.first())
            state.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `GIVEN viewmodel WHEN change externally THEN ui stated update automatically`() =
        runTurbineTest {
            val settingsRepository = FakeSettingsRepository().apply { setInStockOnly(false) }
            val viewmodel = SettingsViewModel(settingsRepository)
            val state = viewmodel.uiState.testIn(this)
            state.awaitItem()

            settingsRepository.setInStockOnly(true)

            val updateState = state.awaitItem()
            assertEquals(true, updateState.inStockOnly)
            assertEquals(true, settingsRepository.inStockOnly.first())
            state.cancelAndIgnoreRemainingEvents()
        }
}
