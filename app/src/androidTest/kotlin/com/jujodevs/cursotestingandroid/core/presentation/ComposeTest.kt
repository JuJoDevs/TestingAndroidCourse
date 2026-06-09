package com.jujodevs.cursotestingandroid.core.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule

open class ComposeTest {
    @get:Rule
    internal val composeRule = createAndroidComposeRule<ComponentActivity>()

    internal fun withComposeRule(
        block: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>.() -> Unit,
    ): Unit = block(composeRule)
}