package com.jujodevs.cursotestingandroid.core.test

object UiTestTag {

    const val TOP_APP_BAR = "top_app_bar"

    // SETTINGS
    const val SETTINGS_CONTENT = "settings_content"
    const val SETTINGS_IN_STOCK_SWITCH = "settings_in_stock_switch"
    const val SETTINGS_TAX_SWITCH = "settings_tax_switch"

    fun settingsThemeOption(themeModeName: String) = "settings_theme_${themeModeName.lowercase()}"
}