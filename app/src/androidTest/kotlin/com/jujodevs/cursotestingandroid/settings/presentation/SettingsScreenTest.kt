package com.jujodevs.cursotestingandroid.settings.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jujodevs.cursotestingandroid.R
import com.jujodevs.cursotestingandroid.core.domain.model.ThemeMode
import com.jujodevs.cursotestingandroid.core.test.UiTestTag
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.SETTINGS_CONTENT
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.SETTINGS_IN_STOCK_SWITCH
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.SETTINGS_TAX_SWITCH
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.TOP_APP_BAR
import com.jujodevs.cursotestingandroid.ui.theme.CursoTestingAndroidTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun createSettingsScreen(
        uiState: SettingsUiState = SettingsUiState(),
        onAction: (SettingsAction) -> Unit = { }
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

    private fun getString(redId: Int): String = composeRule.activity.getString(redId)

    @Test
    fun givenDefaultSettingsState_whenRendered_thenShowsFilterAppearanceSections() {
        createSettingsScreen(uiState = SettingsUiState())

        composeRule.onNodeWithText(getString(R.string.settings_title)).isDisplayed()
        composeRule.onNodeWithText(getString(R.string.settings_filters_section)).isDisplayed()
        composeRule.onNodeWithText(getString(R.string.settings_in_stock_only)).isDisplayed()
        composeRule.onNodeWithText(getString(R.string.settings_in_stock_only_description)).isDisplayed()
        composeRule.onNodeWithText(getString(R.string.settings_show_taxes)).isDisplayed()
        composeRule.onNodeWithText(getString(R.string.settings_show_taxes_description)).isDisplayed()
        composeRule.onNodeWithText(getString(R.string.settings_appearance_section)).isDisplayed()
        composeRule.onNodeWithText(getString(R.string.settings_theme_label)).isDisplayed()
        composeRule.onNodeWithText(getString(R.string.settings_theme_description)).isDisplayed()
        composeRule.onNodeWithText(getString(R.string.settings_theme_light)).isDisplayed()
        composeRule.onNodeWithText(getString(R.string.settings_theme_dark)).isDisplayed()
        composeRule.onNodeWithText(getString(R.string.settings_theme_system)).isDisplayed()

        composeRule.onNodeWithTag(SETTINGS_CONTENT).isDisplayed()
        composeRule.onNodeWithTag(SETTINGS_IN_STOCK_SWITCH).assertIsOff()
        composeRule.onNodeWithTag(SETTINGS_TAX_SWITCH).assertIsOn()
    }

    @Test
    fun givenInStockOnlyFalse_whenRendered_thenSwitchIsOff() {
        createSettingsScreen(uiState = SettingsUiState(inStockOnly = false))
        composeRule.onNodeWithTag(SETTINGS_IN_STOCK_SWITCH).assertIsOff()
    }

    @Test
    fun givenInStockOnlyTrue_whenRendered_thenSwitchIsTrue() {
        createSettingsScreen(uiState = SettingsUiState(inStockOnly = true))
        composeRule.onNodeWithTag(SETTINGS_IN_STOCK_SWITCH).assertIsOn()
    }

    @Test
    fun givenLightTheme_whenRendered_thenLightOptionSelected() {
        createSettingsScreen(uiState = SettingsUiState(themeMode = ThemeMode.LIGHT))
        composeRule.onNodeWithTag(UiTestTag.settingsThemeOption("Light")).assertIsSelected()
    }

    @Test
    fun givenSystemTheme_whenRendered_thenLightOptionSelected() {
        createSettingsScreen(uiState = SettingsUiState(themeMode = ThemeMode.SYSTEM))
        composeRule.onNodeWithTag(UiTestTag.settingsThemeOption("System")).assertIsSelected()
    }

    @Test
    fun givenDarkTheme_whenRendered_thenLightOptionSelected() {
        createSettingsScreen(uiState = SettingsUiState(themeMode = ThemeMode.DARK))
        composeRule.onNodeWithTag(UiTestTag.settingsThemeOption("Dark")).assertIsSelected()
    }

    @Test
    fun givenSettingsRendered_whenBackClicked_thenEmitBackCallback() {
        var backClicked = false

        createSettingsScreen(
            onAction = {
                if (it is SettingsAction.OnBack) backClicked = true
            }
        )
        composeRule.onNodeWithTag(TOP_APP_BAR).performClick()

        assertTrue(backClicked)
    }

    @Test
    fun givenInStockSwitchOff_whenClicked_thenEmitsTrue() {
        var emitted = false

        createSettingsScreen(
            uiState = SettingsUiState(inStockOnly = false),
            onAction = { action -> if (action is SettingsAction.SetInStockOnly) emitted = action.inStockOnly }
        )

        composeRule.onNodeWithTag(SETTINGS_IN_STOCK_SWITCH).performClick()

        assertTrue(emitted)
    }

    @Test
    fun givenLightTheme_whenDarkClicked_thenEmitsDarkTheme() {
        var emitted: ThemeMode = ThemeMode.LIGHT

        createSettingsScreen(
            uiState = SettingsUiState(themeMode = ThemeMode.LIGHT),
            onAction = { action -> if (action is SettingsAction.SetThemeMode) emitted = action.themeMode }
        )

        composeRule.onNodeWithTag(UiTestTag.settingsThemeOption("Dark")).performClick()

        assertEquals(
            ThemeMode.DARK,
            emitted,
    )
    }
}