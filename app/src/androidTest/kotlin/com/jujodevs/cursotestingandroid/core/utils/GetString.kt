package com.jujodevs.cursotestingandroid.core.utils

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule

fun AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>.getString(
    redId: Int,
    vararg formatArgs: Any,
): String = activity.getString(
    redId,
    *formatArgs
)