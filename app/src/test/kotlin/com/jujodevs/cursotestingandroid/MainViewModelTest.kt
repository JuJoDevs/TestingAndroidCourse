package com.jujodevs.cursotestingandroid

import com.jujodevs.cursotestingandroid.core.MainDispatcherRule
import com.jujodevs.cursotestingandroid.core.domain.model.ThemeMode
import com.jujodevs.cursotestingandroid.core.fakes.FakeSettingsRepository
import com.jujodevs.cursotestingandroid.core.runTurbineTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    lateinit var settingsRepository: FakeSettingsRepository

    lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        settingsRepository = FakeSettingsRepository()
        viewModel = MainViewModel(settingsRepository)
    }

    @Test
    fun `GIVEN repository with dark mode WHEN initialized THEN emits dark theme mode`() = runTurbineTest {
        settingsRepository.setThemeMode(ThemeMode.DARK)
        val state = viewModel.themeMode.testIn(this)

        val updatedState = state.awaitItem()

        assertEquals(ThemeMode.DARK, updatedState)
        state.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN default repository WHEN initialized THEN system theme mode`() = runTurbineTest {
        val state = viewModel.themeMode.testIn(this)

        val updatedState = state.awaitItem()

        assertEquals(ThemeMode.SYSTEM, updatedState)
        state.cancelAndIgnoreRemainingEvents()
    }
}