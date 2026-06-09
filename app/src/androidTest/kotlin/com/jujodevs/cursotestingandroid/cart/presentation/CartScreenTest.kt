package com.jujodevs.cursotestingandroid.cart.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jujodevs.cursotestingandroid.core.presentation.ComposeTest
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.CART_LOADING
import com.jujodevs.cursotestingandroid.R
import com.jujodevs.cursotestingandroid.cart.domain.model.CartSummary
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.CART_EMPTY
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.CART_RETRY
import com.jujodevs.cursotestingandroid.core.utils.getString
import org.junit.Assert.assertTrue
import org.junit.Test

class CartScreenTest: ComposeTest() {

    private fun createCartScreen(
        uiState: CartUiState,
        onAction: (CartAction) -> Unit = { },
    ) {
        composeRule.setContent {
            CartContent(
                uiState = uiState,
                snackbarHostState = remember { SnackbarHostState() },
                onAction = onAction
            )
        }
    }

    @Test
    fun givenLoadingState_whenRendered_thenShowProgressBar() = withComposeRule {
       createCartScreen(uiState = CartUiState.Loading)

        onNodeWithTag(CART_LOADING).assertIsDisplayed()
    }

    @Test
    fun givenErrorState_whenRendered_thenShowTextAndRetryButton() = withComposeRule {
        val errorText = "Error test"
        createCartScreen(uiState = CartUiState.Error(errorText))

        onNodeWithText(getString(R.string.cart_error_message, errorText))
            .assertIsDisplayed()
        onNodeWithText(getString(R.string.cart_retry))
            .assertIsDisplayed()
    }

    @Test
    fun givenErrorState_whenRetryClicked_thenEmitsRetryAction() = withComposeRule {
        var retryClicked = false
        val errorText = "Error test"
        createCartScreen(
            uiState = CartUiState.Error(errorText),
            onAction = { action ->
                if (action is CartAction.LoadCart) retryClicked = true
            }
        )

        onNodeWithTag(CART_RETRY).performClick()

        assertTrue(retryClicked)
    }

    @Test
    fun givenEmptySuccessState_whenRendered_thenShowsEmptyCartMessage() = withComposeRule {
        createCartScreen(uiState = CartUiState.Success(
            summary = CartSummary(
                subtotal = 0.0,
                discountTotal = 0.0,
                finalTotal = 0.0,
            ),
            cartItems = emptyList(),
            isLoading = false
        ))

        onNodeWithTag(CART_EMPTY).assertIsDisplayed()
        onNodeWithText(getString(R.string.cart_empty_icon)).assertIsDisplayed()
        onNodeWithText(getString(R.string.cart_empty_title)).assertIsDisplayed()
        onNodeWithText(getString(R.string.cart_empty_subtitle)).assertIsDisplayed()
    }
}