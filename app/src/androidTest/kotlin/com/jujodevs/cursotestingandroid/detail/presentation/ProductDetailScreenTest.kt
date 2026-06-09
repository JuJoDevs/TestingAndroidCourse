package com.jujodevs.cursotestingandroid.detail.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jujodevs.cursotestingandroid.R
import com.jujodevs.cursotestingandroid.core.builders.product
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.bread
import com.jujodevs.cursotestingandroid.core.mothers.PromotionMother.buyXGetY
import com.jujodevs.cursotestingandroid.core.mothers.PromotionMother.percent
import com.jujodevs.cursotestingandroid.core.presentation.ComposeTest
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.PRODUCT_DETAIL_LOADING
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.TOP_APP_BAR_BACK
import com.jujodevs.cursotestingandroid.core.utils.getString
import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.jujodevs.cursotestingandroid.ui.theme.CursoTestingAndroidTheme
import org.junit.Test
import kotlin.test.assertTrue

class ProductDetailScreenTest : ComposeTest() {

    private fun createProductDetailScreen(
        uiState: ProductDetailUiState,
        snackbarHostState: SnackbarHostState = SnackbarHostState(),
        onAction: (ProductDetailAction) -> Unit = {},
    ) {
        composeRule.setContent {
            CursoTestingAndroidTheme {
                ProductDetailContain(
                    uiState = uiState,
                    snackbarHostState = snackbarHostState,
                    onAction = onAction
                )
            }
        }
    }

    @Test
    fun givenLoadingState_whenRendered_thenShowsLoading() = withComposeRule {
        createProductDetailScreen(uiState = ProductDetailUiState(isLoading = true))

        onNodeWithTag(PRODUCT_DETAIL_LOADING).isDisplayed()
    }

    @Test
    fun givenProduct_whenRendered_thenShowsProductDetails() = withComposeRule {
        val product = bread
        val uiState = ProductDetailUiState(
            item = ProductWithPromotion(product),
            isLoading = false
        )

        createProductDetailScreen(uiState = uiState)

        onAllNodesWithText(product.name).assertCountEquals(2)
        onNodeWithText(product.description).isDisplayed()
        onNodeWithText(product.category).isDisplayed()
        onNodeWithText(product.price.toString()).isDisplayed()
        onNodeWithText(getString(R.string.product_detail_stock_units, product.stock)).isDisplayed()
        onNodeWithText(getString(R.string.product_detail_add_to_cart)).isDisplayed()
    }

    @Test
    fun givenProductWithPercentPromotion_whenRendered_thenShowsDiscountedPriceAndPromo() =
        withComposeRule {
            val product = bread
            val promotion = percent
            val uiState = ProductDetailUiState(
                item = ProductWithPromotion(product, promotion),
                isLoading = false
            )

            createProductDetailScreen(uiState = uiState)

            onNodeWithText(bread.price.toString()).isDisplayed()
            onNodeWithText(promotion.discountedPrice.toString()).isDisplayed()
            onNodeWithText(getString(R.string.product_detail_percent_off, promotion.percent.toInt())).isDisplayed()
        }

    @Test
    fun givenProductWithBuyXPayYPromotion_whenRendered_thenShowsPromoLabel() = withComposeRule {
        val product = product()
        val promotion = buyXGetY
        val uiState = ProductDetailUiState(
            item = ProductWithPromotion(product, promotion),
            isLoading = false
        )

        createProductDetailScreen(uiState = uiState)

        onNodeWithText(getString(R.string.product_detail_promo, promotion.label)).isDisplayed()
    }

    @Test
    fun givenProductNoStock_whenRendered_thenShowsNoStockAndButtonDisabled() = withComposeRule {
        val product = product {
            withStock(0)
        }
        val uiState = ProductDetailUiState(
            item = ProductWithPromotion(product),
            isLoading = false
        )

        createProductDetailScreen(uiState = uiState)

        onNodeWithText(getString(R.string.product_detail_no_stock)).isDisplayed()
        onNodeWithText(getString(R.string.product_detail_no_stock_available)).isDisplayed()
        onNodeWithText(getString(R.string.product_detail_no_stock_available)).assertIsNotEnabled()
    }

    @Test
    fun givenProduct_whenAddToCartClicked_thenEmitsAddToCartAction() = withComposeRule {
        var addToCartClicked = false
        val product = product { withStock(5) }
        val uiState = ProductDetailUiState(
            item = ProductWithPromotion(product),
            isLoading = false
        )

        createProductDetailScreen(
            uiState = uiState,
            onAction = { action ->
                if (action is ProductDetailAction.AddToCart) addToCartClicked = true
            }
        )

        onNodeWithText(getString(R.string.product_detail_add_to_cart)).performClick()

        assertTrue(addToCartClicked)
    }

    @Test
    fun givenProduct_whenBackClicked_thenEmitsGoBackAction() = withComposeRule {
        var backClicked = false
        val product = product()
        val uiState = ProductDetailUiState(
            item = ProductWithPromotion(product),
            isLoading = false
        )

        createProductDetailScreen(
            uiState = uiState,
            onAction = { action ->
                if (action is ProductDetailAction.GoBack) backClicked = true
            }
        )

        onNodeWithTag(TOP_APP_BAR_BACK).performClick()

        assertTrue(backClicked)
    }
}
