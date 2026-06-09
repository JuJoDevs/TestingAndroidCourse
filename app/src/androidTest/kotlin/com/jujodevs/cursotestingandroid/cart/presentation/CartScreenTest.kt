package com.jujodevs.cursotestingandroid.cart.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import com.jujodevs.cursotestingandroid.R
import com.jujodevs.cursotestingandroid.cart.domain.model.CartSummary
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.bread
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.coffee
import com.jujodevs.cursotestingandroid.core.mothers.uistate.CartUiStateMother.cartItemWithPromotion
import com.jujodevs.cursotestingandroid.core.mothers.uistate.CartUiStateMother.cartSuccess
import com.jujodevs.cursotestingandroid.core.presentation.ComposeTest
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.CART_EMPTY
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.CART_LOADING
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.CART_RETRY
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.cartItem
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.cartQuantityDecrease
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.cartQuantityIncrease
import com.jujodevs.cursotestingandroid.core.utils.getString
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertEquals

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

    @Test
    fun givenSuccessState_whenRendered_thenShowsQuantitiesAndSummary() = withComposeRule {
        createCartScreen(uiState = cartSuccess)

        onNodeWithText(coffee.name).assertIsDisplayed()
        onNodeWithText(bread.name).assertIsDisplayed()
        onNodeWithText(getString(R.string.cart_summary_title)).assertIsDisplayed()
        onNodeWithText(getString(R.string.cart_summary_subtotal)).assertIsDisplayed()
        onNodeWithText(getString(R.string.cart_summary_discount)).assertIsDisplayed()
        onNodeWithText(getString(R.string.cart_summary_total)).assertIsDisplayed()

        onNodeWithTag(cartItem(coffee.id)).assertIsDisplayed()
        onNodeWithTag(cartItem(bread.id)).assertIsDisplayed()
    }

    @Test
    fun givenInitialQuantity_whenIncreaseClicked_thenEmitsIncreaseQuantity() = withComposeRule {
        var emitted: Pair<String, Int>? = null
        val initialQuantity = 2
        createCartScreen(
            uiState = cartSuccess.copy(
                cartItems = listOf(
                    cartItemWithPromotion(
                        product = bread,
                        quantity = initialQuantity
                    )
                )
            ),
            onAction = { action ->
                if (action is CartAction.IncreaseQuantity) {
                    emitted = action.productId to action.currentQuantity
                }
            }
        )

        onNodeWithTag(cartQuantityIncrease(bread.id)).performClick()

        assertEquals(bread.id to initialQuantity, emitted)
    }

    @Test
    fun givenInitialQuantity_whenDecreaseClicked_thenEmitsDecreaseQuantity() = withComposeRule {
        var emitted: Pair<String, Int>? = null
        val initialQuantity = 2
        createCartScreen(
            uiState = cartSuccess.copy(
                cartItems = listOf(
                    cartItemWithPromotion(
                        product = bread,
                        quantity = initialQuantity
                    )
                )
            ),
            onAction = { action ->
                if (action is CartAction.DecreaseQuantity) {
                    emitted = action.productId to action.currentQuantity
                }
            }
        )

        onNodeWithTag(cartQuantityDecrease(bread.id)).performClick()

        assertEquals(bread.id to initialQuantity, emitted)
    }

    @Test
    fun givenCartItem_whenSwipedRight_thenEmitsRemoveCallback() = withComposeRule {
        var removeProductId: String? = null
        createCartScreen(
            uiState = cartSuccess.copy(
                cartItems = listOf(
                    cartItemWithPromotion(
                        product = bread,
                        quantity = 2
                    )
                )
            ),
            onAction = { action ->
                if (action is CartAction.RemoveFromCart) {
                    removeProductId = action.productId
                }
            }
        )

        onNodeWithTag(cartItem(bread.id))
            .performTouchInput { swipeRight() }
        waitUntil(timeoutMillis = 3_000) { removeProductId != null }

        assertEquals(bread.id, removeProductId)
    }

    @Test
    fun givenItemsAtStockEdges_whenRendered_thenInvalidControlsAreDisabled() {
        val fullStockItem = cartItemWithPromotion(
            product = bread.copy(stock = 7),
            quantity = 7,
        )
        createCartScreen(cartSuccess.copy(cartItems = listOf(fullStockItem)))

        composeRule.onNodeWithTag(cartQuantityIncrease(bread.id))
            .assertIsNotEnabled()
    }
}