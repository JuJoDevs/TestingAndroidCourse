package com.jujodevs.cursotestingandroid.productlist.presentation

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.jujodevs.cursotestingandroid.R
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.apple
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.bread
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.chicken
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.coffee
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.milk
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.yogurt
import com.jujodevs.cursotestingandroid.core.mothers.uistate.ProductListUiStateMother
import com.jujodevs.cursotestingandroid.core.test.UiTestTag
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.FILTER_VIEW
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.PRODUCT_LIST_LIST
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.PRODUCT_LIST_LOADING
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.TOP_APP_BAR_BADGE
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.TOP_APP_BAR_CART
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.TOP_APP_BAR_FILTER
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.TOP_APP_BAR_SETTINGS
import com.jujodevs.cursotestingandroid.core.test.UiTestTag.productListItem
import com.jujodevs.cursotestingandroid.core.utils.getString
import com.jujodevs.cursotestingandroid.core.utils.onListItemNodeWithTag
import com.jujodevs.cursotestingandroid.productlist.domain.model.SortOption
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

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

    @Test
    fun givenNoCategorySelected_whenRendered_thenMarkAllChip() = withComposeRule {
        createProductListScreen(ProductListUiStateMother.success.copy(selectedCategory = null))

        onNodeWithTag(UiTestTag.productListCategory(null)).assertIsSelected()
    }

    @Test
    fun givenCategorySelected_whenRendered_thenMarkThatChip() = withComposeRule {
        val category = "drinks"
        createProductListScreen(ProductListUiStateMother.success.copy(
            categories = listOf("bread", "dairy", category),
            selectedCategory = category,
        ))

        onNodeWithTag(UiTestTag.productListCategory("drinks")).assertIsSelected()
    }

    @Test
    fun givenNotSortOptionSelected_whenRendered_thenNotMarkSorterChips() = withComposeRule {
        createProductListScreen(
            uiState = ProductListUiStateMother.success.copy(sortOption = SortOption.NONE)
        )

        onNodeWithTag(UiTestTag.productListSortOption(SortOption.PRICE_ASC))
            .assertIsNotSelected()
        onNodeWithTag(UiTestTag.productListSortOption(SortOption.PRICE_DESC))
            .assertIsNotSelected()
        onNodeWithTag(UiTestTag.productListSortOption(SortOption.DISCOUNT))
            .assertIsNotSelected()
    }

    @Test
    fun givenSortOptionSelected_whenRendered_thenMarkChipWithSortOptionSelected() = withComposeRule {
        createProductListScreen(
            uiState = ProductListUiStateMother.success.copy(sortOption = SortOption.PRICE_ASC)
        )

        onNodeWithTag(UiTestTag.productListSortOption(SortOption.PRICE_ASC))
            .assertIsSelected()
        onNodeWithTag(UiTestTag.productListSortOption(SortOption.PRICE_DESC))
            .assertIsNotSelected()
        onNodeWithTag(UiTestTag.productListSortOption(SortOption.DISCOUNT))
            .assertIsNotSelected()
    }

    @Test
    fun givenRendered_whenSelectSortOption_thenMarkChipWithSortOptionSelected() = withComposeRule {
        val expectedSortOption = SortOption.DISCOUNT
        var sortOptionResult:SortOption = SortOption.NONE

        createProductListScreen(
            uiState = ProductListUiStateMother.success.copy(sortOption = SortOption.NONE),
            onAction = {
                if (it is ProductListAction.SetOrderSelected) {
                    sortOptionResult = it.sortOption
                }
            }
        )

        onNodeWithTag(UiTestTag.productListSortOption(expectedSortOption))
            .performClick()

        assertEquals(expectedSortOption, sortOptionResult)
    }

    @Test
    fun givenCartItemCountZero_whenRendered_thenHidesBadge() = withComposeRule {
        createProductListScreen(cartItemCount = 0)

        onNodeWithTag(TOP_APP_BAR_BADGE).assertDoesNotExist()
    }

    @Test
    fun givenCartItemCountOne_whenRendered_thenShowsOne() = withComposeRule {
        createProductListScreen(cartItemCount = 1)

        onNodeWithTag(TOP_APP_BAR_BADGE).assertIsDisplayed()
        onNodeWithText("1").assertIsDisplayed()
    }

    @Test
    fun givenCartItemCountNinetyNine_whenRendered_thenShowsNinetyNine() = withComposeRule {
        createProductListScreen(cartItemCount = 99)

        onNodeWithTag(TOP_APP_BAR_BADGE).assertIsDisplayed()
        onNodeWithText("99").assertIsDisplayed()
    }

    @Test
    fun givenCartItemCountOverNinetyNine_whenRendered_thenShowsNinetyNinePlus() = withComposeRule {
        createProductListScreen(cartItemCount = 100)

        onNodeWithTag(TOP_APP_BAR_BADGE).assertIsDisplayed()
        onNodeWithText(getString(R.string.top_app_bar_badge_ninety_nine_plus)).assertIsDisplayed()
    }

    @Test
    fun givenFilterVisible_whenToggleClicked_thenEmitFalse() = withComposeRule {
        var emitted: Boolean? = null
        createProductListScreen(
            filterVisible = true,
            onAction = {
                if (it is ProductListAction.SetFiltersVisible) {
                    emitted = it.showFilters
                }
            }
        )

        onNodeWithTag(TOP_APP_BAR_FILTER).performClick()

        assertEquals(false, emitted)
    }

    @Test
    fun givenFilterHidden_whenToggleClicked_thenEmitTrue() = withComposeRule {
        var emitted: Boolean? = null
        createProductListScreen(
            filterVisible = false,
            onAction = {
                if (it is ProductListAction.SetFiltersVisible) {
                    emitted = it.showFilters
                }
            }
        )

        onNodeWithTag(TOP_APP_BAR_FILTER).performClick()

        assertEquals(true, emitted)
    }

    @Test
    fun givenProductListRendered_whenSettingsIconClicked_thenEmitCallback() = withComposeRule {
        var settingClicked = false
        createProductListScreen(
            onAction = {
                if (it is ProductListAction.NavToSettings) settingClicked = true
            }
        )

        onNodeWithTag(TOP_APP_BAR_SETTINGS).performClick()

        assertTrue(settingClicked)
    }

    @Test
    fun givenProductListRendered_whenCartIconClicked_thenEmitCallback() = withComposeRule {
        var cartClicked = false
        createProductListScreen(
            onAction = {
                if (it is ProductListAction.NavToCart) cartClicked = true
            }
        )

        onNodeWithTag(TOP_APP_BAR_CART).performClick()

        assertTrue(cartClicked)
    }

    private fun withComposeRule(
        block: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>.() -> Unit,
    ): Unit = block(composeRule)
}