package com.jujodevs.cursotestingandroid.productlist.presentation

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.jujodevs.cursotestingandroid.R
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.apple
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.bread
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.chicken
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.coffee
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.milk
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.yogurt
import com.jujodevs.cursotestingandroid.core.mothers.uistate.ProductListUiStateMother
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.FILTER_VIEW
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.PRODUCT_LIST_LIST
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.PRODUCT_LIST_LOADING
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.productListItem
import com.jujodevs.cursotestingandroid.core.utils.getString
import com.jujodevs.cursotestingandroid.core.utils.onListItemNodeWithTag
import org.junit.Rule
import org.junit.Test

class ProductListScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun createProductListScreen(
        uiState: ProductListUiState = ProductListUiStateMother.success,
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

    @Test
    fun givenLoadingState_whenRendered_thenShowLoading() = withComposeRule {
        createProductListScreen(uiState = ProductListUiState.Loading)

        onNodeWithTag(PRODUCT_LIST_LOADING).assertIsDisplayed()
    }

    @Test
    fun givenErrorState_whenRendered_thenShowErrorMessage() = withComposeRule {
        createProductListScreen(uiState = ProductListUiState.Error(""))

        onNodeWithText(getString(R.string.product_list_error)).assertIsDisplayed()
    }

    @Test
    fun givenSuccessState_whenRendered_thenShowProductsAndCount() = withComposeRule {
        createProductListScreen(uiState = ProductListUiStateMother.success)

        onNodeWithText(getString(R.string.product_list_count, 6)).assertIsDisplayed()
        onNodeWithTag(FILTER_VIEW).assertIsDisplayed()
        onListItemNodeWithTag(PRODUCT_LIST_LIST, productListItem(coffee.id)).assertIsDisplayed()
        onListItemNodeWithTag(PRODUCT_LIST_LIST, productListItem(milk.id)).assertIsDisplayed()
        onListItemNodeWithTag(PRODUCT_LIST_LIST, productListItem(bread.id)).assertIsDisplayed()
        onListItemNodeWithTag(PRODUCT_LIST_LIST, productListItem(apple.id)).assertIsDisplayed()
        onListItemNodeWithTag(PRODUCT_LIST_LIST, productListItem(yogurt.id)).assertIsDisplayed()
        onListItemNodeWithTag(PRODUCT_LIST_LIST, productListItem(chicken.id)).assertIsDisplayed()
    }

    @Test
    fun givenSuccessStateWithEmptyList_whenRendered_thenShowEmptyMessage() = withComposeRule {
        createProductListScreen(uiState = ProductListUiStateMother.success.copy(products = emptyList()))

        onNodeWithText(getString(R.string.product_list_no_products, 6)).assertIsDisplayed()
    }

    private fun withComposeRule(
        block: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>.() -> Unit,
    ): Unit = block(composeRule)
}