package com.jujodevs.cursotestingandroid.settings.presentation

import com.jujodevs.cursotestingandroid.core.MainDispatcherRule
import com.jujodevs.cursotestingandroid.core.fakes.FakeSettingsRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun exampleTestCrash() = runTest(mainDispatcherRule.testDispatcher) {
        val viewmodel = SettingsViewModel(FakeSettingsRepository())
        backgroundScope.launch {
            viewmodel.uiState.collect()
        }

        viewmodel.onAction(SettingsAction.SetInStockOnly(true))

        assertTrue(viewmodel.uiState.value.inStockOnly)
    }
}