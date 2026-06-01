package com.jujodevs.cursotestingandroid.productlist.presentation

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule

class ProductListScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun createProductListScreen(
        uiState: ProductListUiState = ProductListUiState.Loading,
        cartItemCount: Int = 0,
        filterVisible: Boolean = true,
        onAction: (ProductListAction) -> Unit = { },
    ) {
        composeRule.setContent {
            ProductListContent(
                uiState = uiState,
                cartItemCount = cartItemCount,
                filterVisible = filterVisible,
                snackbarHostState = remember { SnackbarHostState() },
                onAction = onAction
            )
        }
    }
}