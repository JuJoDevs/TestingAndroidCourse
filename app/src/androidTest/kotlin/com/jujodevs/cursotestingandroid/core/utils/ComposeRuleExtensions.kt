package com.jujodevs.cursotestingandroid.core.utils

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode

fun SemanticsNodeInteractionsProvider.onListItemNodeWithTag(
    listTestTag: String,
    testTag: String,
    useUnmergedTree: Boolean = false
): SemanticsNodeInteraction = run {
    onNodeWithTag(listTestTag).performScrollToNode(hasTestTag(testTag))
    onNodeWithTag(testTag, useUnmergedTree)
}
