package com.jujodevs.cursotestingandroid.settings.presentation

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jujodevs.cursotestingandroid.R
import com.jujodevs.cursotestingandroid.core.domain.model.ThemeMode
import com.jujodevs.cursotestingandroid.core.presentation.ComposeTest
import com.jujodevs.cursotestingandroid.core.test.UiTestTag
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.SETTINGS_CONTENT
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.SETTINGS_IN_STOCK_SWITCH
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.SETTINGS_TAX_SWITCH
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.TOP_APP_BAR_BACK
import com.jujodevs.cursotestingandroid.core.utils.getString
import com.jujodevs.cursotestingandroid.ui.theme.CursoTestingAndroidTheme
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsScreenTest : ComposeTest() {
    private fun createSettingsScreen(
        uiState: SettingsUiState = SettingsUiState(),
        onAction: (SettingsAction) -> Unit = { },
    ) {
        composeRule.setContent {
            CursoTestingAndroidTheme {
                SettingsContent(
                    uiState = uiState,
                    onAction = onAction,
                )
            }
        }
    }

    @Test
    fun givenDefaultSettingsState_whenRendered_thenShowsFilterAppearanceSections() =
        withComposeRule {
            createSettingsScreen(uiState = SettingsUiState())

            onNodeWithText(getString(R.string.settings_title)).isDisplayed()
            onNodeWithText(getString(R.string.settings_filters_section)).isDisplayed()
            onNodeWithText(getString(R.string.settings_in_stock_only)).isDisplayed()
            onNodeWithText(getString(R.string.settings_in_stock_only_description)).isDisplayed()
            onNodeWithText(getString(R.string.settings_show_taxes)).isDisplayed()
            onNodeWithText(getString(R.string.settings_show_taxes_description)).isDisplayed()
            onNodeWithText(getString(R.string.settings_appearance_section)).isDisplayed()
            onNodeWithText(getString(R.string.settings_theme_label)).isDisplayed()
            onNodeWithText(getString(R.string.settings_theme_description)).isDisplayed()
            onNodeWithText(getString(R.string.settings_theme_light)).isDisplayed()
            onNodeWithText(getString(R.string.settings_theme_dark)).isDisplayed()
            onNodeWithText(getString(R.string.settings_theme_system)).isDisplayed()

            onNodeWithTag(SETTINGS_CONTENT).isDisplayed()
            onNodeWithTag(SETTINGS_IN_STOCK_SWITCH).assertIsOff()
            onNodeWithTag(SETTINGS_TAX_SWITCH).assertIsOn()
        }

    @Test
    fun givenInStockOnlyFalse_whenRendered_thenSwitchIsOff() =
        withComposeRule {
            createSettingsScreen(uiState = SettingsUiState(inStockOnly = false))
            onNodeWithTag(SETTINGS_IN_STOCK_SWITCH).assertIsOff()
        }

    @Test
    fun givenInStockOnlyTrue_whenRendered_thenSwitchIsTrue() =
        withComposeRule {
            createSettingsScreen(uiState = SettingsUiState(inStockOnly = true))
            onNodeWithTag(SETTINGS_IN_STOCK_SWITCH).assertIsOn()
        }

    @Test
    fun givenLightTheme_whenRendered_thenLightOptionSelected() =
        withComposeRule {
            createSettingsScreen(uiState = SettingsUiState(themeMode = ThemeMode.LIGHT))
            onNodeWithTag(UiTestTag.settingsThemeOption("Light")).assertIsSelected()
        }

    @Test
    fun givenSystemTheme_whenRendered_thenLightOptionSelected() =
        withComposeRule {
            createSettingsScreen(uiState = SettingsUiState(themeMode = ThemeMode.SYSTEM))
            onNodeWithTag(UiTestTag.settingsThemeOption("System")).assertIsSelected()
        }

    @Test
    fun givenDarkTheme_whenRendered_thenLightOptionSelected() =
        withComposeRule {
            createSettingsScreen(uiState = SettingsUiState(themeMode = ThemeMode.DARK))
            onNodeWithTag(UiTestTag.settingsThemeOption("Dark")).assertIsSelected()
        }

    @Test
    fun givenSettingsRendered_whenBackClicked_thenEmitBackCallback() =
        withComposeRule {
            var backClicked = false

            createSettingsScreen(
                onAction = {
                    if (it is SettingsAction.OnBack) backClicked = true
                },
            )
            onNodeWithTag(TOP_APP_BAR_BACK).performClick()

            assertTrue(backClicked)
        }

    @Test
    fun givenInStockSwitchOff_whenClicked_thenEmitsTrue() =
        withComposeRule {
            var emitted = false

            createSettingsScreen(
                uiState = SettingsUiState(inStockOnly = false),
                onAction = { action ->
                    if (action is SettingsAction.SetInStockOnly) emitted = action.inStockOnly
                },
            )

            onNodeWithTag(SETTINGS_IN_STOCK_SWITCH).performClick()

            assertTrue(emitted)
        }

    @Test
    fun givenLightTheme_whenDarkClicked_thenEmitsDarkTheme() =
        withComposeRule {
            var emitted: ThemeMode = ThemeMode.LIGHT

            createSettingsScreen(
                uiState = SettingsUiState(themeMode = ThemeMode.LIGHT),
                onAction = { action ->
                    if (action is SettingsAction.SetThemeMode) emitted = action.themeMode
                },
            )

            onNodeWithTag(UiTestTag.settingsThemeOption("Dark")).performClick()

            assertEquals(
                ThemeMode.DARK,
                emitted,
            )
        }
}
