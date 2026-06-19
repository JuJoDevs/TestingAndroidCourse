package com.jujodevs.cursotestingandroid.productlist.presentation

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.ReceiveTurbine
import com.jujodevs.cursotestingandroid.core.MainDispatcherRule
import com.jujodevs.cursotestingandroid.core.mockwebserver.MiniMarketApiDispatcher
import com.jujodevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.jujodevs.cursotestingandroid.core.runTurbineTest
import com.jujodevs.cursotestingandroid.core.utils.asAsset
import com.jujodevs.cursotestingandroid.productlist.domain.model.SortOption
import com.jujodevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GetProductsUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ProductListViewModelTest {
    private companion object {
        private const val EXPECTED_PRODUCT_SIZE = 3
        private const val DAIRY_CATEGORY = "Lácteos"
    }

    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val mainDispatcherRule = MainDispatcherRule()

    @Inject
    lateinit var getProductsUseCase: GetProductsUseCase

    @Inject
    lateinit var settingsRepository: SettingsRepository

    lateinit var viewModel: ProductListViewModel

    @Before
    fun setUp() {
        mockWebServer.server.dispatcher =
            MiniMarketApiDispatcher(
                productJson = "product_list_default.json".asAsset(),
            )
        hiltRule.inject()

        viewModel = ProductListViewModel(getProductsUseCase, settingsRepository)
    }

    @Test
    fun givenSuccessfulApi_whenViewModelLoads_thenShowsProducts() =
        runTurbineTest {
            val state = viewModel.uiState.testIn(this)

            val result = state.awaitSuccessMatching { it.products.size == EXPECTED_PRODUCT_SIZE }
            assertTrue(result.products.isNotEmpty())
            assertTrue(result.products.size == EXPECTED_PRODUCT_SIZE)
            state.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun givenDairyCategorySelected_whenFiltering_thenOnlyDairyProductAreShown() =
        runTurbineTest {
            val state = viewModel.uiState.testIn(this)
            state.awaitSuccessMatching { it.products.size == EXPECTED_PRODUCT_SIZE }

            viewModel.onAction(ProductListAction.SetCategory(DAIRY_CATEGORY))

            val result = state.awaitSuccessMatching { it.selectedCategory == DAIRY_CATEGORY }
            assertTrue(result.selectedCategory == DAIRY_CATEGORY)
            assertTrue(result.products.size == 2)
            assertTrue(result.products.isNotEmpty())
            assertTrue(result.products.all { it.product.category == DAIRY_CATEGORY })
            state.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun givenProductsLoaded_whenSortingByPriceAsc_thenListIsCorrectlyOrdered() =
        runTurbineTest {
            val state = viewModel.uiState.testIn(this)
            state.awaitSuccessMatching { it.products.size == EXPECTED_PRODUCT_SIZE }

            viewModel.onAction(ProductListAction.SetOrderSelected(SortOption.PRICE_ASC))

            val result = state.awaitSuccessMatching { it.sortOption == SortOption.PRICE_ASC }

            assertEquals(listOf(10.0, 15.0, 20.0), result.products.map { it.product.price })
            state.cancelAndIgnoreRemainingEvents()
        }

    private suspend fun ReceiveTurbine<ProductListUiState>.awaitSuccessMatching(
        predicate: (ProductListUiState.Success) -> Boolean,
    ): ProductListUiState.Success {
        while (true) {
            when (val item = awaitItem()) {
                is ProductListUiState.Success -> if (predicate(item)) return item
                is ProductListUiState.Error -> error("Unexpected error: ${item.message}")
                is ProductListUiState.Loading -> Unit
            }
        }
    }
}
