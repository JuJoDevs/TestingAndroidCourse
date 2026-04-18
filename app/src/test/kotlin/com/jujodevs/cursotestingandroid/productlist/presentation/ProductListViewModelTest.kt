package com.jujodevs.cursotestingandroid.productlist.presentation

import com.jujodevs.cursotestingandroid.core.MainDispatcherRule
import com.jujodevs.cursotestingandroid.core.builders.product
import com.jujodevs.cursotestingandroid.core.domain.time.Clock
import com.jujodevs.cursotestingandroid.core.fakes.FakeClock
import com.jujodevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.jujodevs.cursotestingandroid.core.fakes.FakePromotionRepository
import com.jujodevs.cursotestingandroid.core.fakes.FakeSettingsRepository
import com.jujodevs.cursotestingandroid.core.runTurbineTest
import com.jujodevs.cursotestingandroid.core.stubs.FailingProductRepositoryStub
import com.jujodevs.cursotestingandroid.productlist.domain.model.SortOption
import com.jujodevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.jujodevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.jujodevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GetProductsUseCase
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GroupPromotionsByProductId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class ProductListViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    lateinit var productRepository: FakeProductRepository
    lateinit var settingsRepository: FakeSettingsRepository
    lateinit var promotionRepository: FakePromotionRepository
    lateinit var clock: FakeClock

    lateinit var viewModel: ProductListViewModel

    @Before
    fun setUp() {
        productRepository = FakeProductRepository()
        settingsRepository = FakeSettingsRepository()
        promotionRepository = FakePromotionRepository()
        clock = FakeClock()

        viewModel = createViewModel()
    }

    private fun createViewModel(
        productRepository: ProductRepository = this.productRepository,
        settingsRepository: SettingsRepository = this.settingsRepository,
        promotionRepository: PromotionRepository = this.promotionRepository,
        clock: Clock = this.clock
    ): ProductListViewModel {
        return ProductListViewModel(
            getProductsUseCase = GetProductsUseCase(
                productRepository = productRepository,
                promotionRepository = promotionRepository,
                getPromotionForProduct = GetPromotionForProduct(),
                groupPromotionsByProductId = GroupPromotionsByProductId(),
                settingsRepository = settingsRepository,
                clock = clock,
            ),
            settingsRepository = settingsRepository,
        )
    }

    @Test
    fun `GIVEN products WHEN initialized THEN emits success state`() = runTurbineTest {
        val productId = "id1"
        val p1 = product { withId(productId) }
        productRepository.setProducts(listOf(p1))
        val state = viewModel.uiState.testIn(this)

        val initializedState = state.awaitItem()

        assertTrue(initializedState is ProductListUiState.Success)
        assertEquals(1, (initializedState as ProductListUiState.Success).products.size)
        state.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN selected category WHEN set category THEN filter products`() = runTurbineTest {
        val p1 = product { withId("1").withCategory("meat") }
        val p2 = product { withId("2").withCategory("pasta") }
        productRepository.setProducts(listOf(p1, p2))
        val state = viewModel.uiState.testIn(this)
        state.awaitItem()

        viewModel.onAction(ProductListAction.SetCategory("pasta"))

        val updatedState = state.awaitItem()
        assertTrue(updatedState is ProductListUiState.Success)
        assertEquals(1, (updatedState as ProductListUiState.Success).products.size)
        assertEquals("pasta", updatedState.selectedCategory)
        state.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN price asc sort option WHEN set sort option THEN sorts by effective price`() = runTurbineTest {
        val p1 = product { withId("1").withPrice(30.0) }
        val p2 = product { withId("2").withPrice(15.0) }
        productRepository.setProducts(listOf(p1, p2))
        val state = viewModel.uiState.testIn(this)
        state.awaitItem()

        viewModel.onAction(ProductListAction.SetOrderSelected(SortOption.PRICE_ASC))

        val updatedState = state.awaitItem() as ProductListUiState.Success

        assertEquals(15.0, updatedState.products[0].product.price, 0.0)
        assertEquals(30.0, updatedState.products[1].product.price, 0.0)
        assertEquals(SortOption.PRICE_ASC, updatedState.sortOption)
        state.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN repository error WHEN loading products THEN emits error state`() = runTurbineTest {
        val failingRepository = FailingProductRepositoryStub(Exception("Test"))
        viewModel = createViewModel(productRepository = failingRepository)

        val state = viewModel.uiState.testIn(this)

        val updatedState = state.awaitItem()
        assertTrue(updatedState is ProductListUiState.Error)
        assertEquals("Test", (updatedState as ProductListUiState.Error).message)
        state.cancelAndIgnoreRemainingEvents()
    }
}