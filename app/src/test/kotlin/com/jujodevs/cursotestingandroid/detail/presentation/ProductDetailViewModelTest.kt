package com.jujodevs.cursotestingandroid.detail.presentation

import com.jujodevs.cursotestingandroid.cart.domain.usecase.AddToCartUseCase
import com.jujodevs.cursotestingandroid.core.MainDispatcherRule
import com.jujodevs.cursotestingandroid.core.builders.ProductBuilder
import com.jujodevs.cursotestingandroid.core.fakes.FakeCartRepository
import com.jujodevs.cursotestingandroid.core.fakes.FakeClock
import com.jujodevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.jujodevs.cursotestingandroid.core.fakes.FakePromotionRepository
import com.jujodevs.cursotestingandroid.core.runTurbineTest
import com.jujodevs.cursotestingandroid.core.domain.model.AppError
import com.jujodevs.cursotestingandroid.core.stubs.FailingProductRepositoryStub
import com.jujodevs.cursotestingandroid.detail.domain.usecase.GetProductDetailWithPromotionUseCase
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GroupPromotionsByProductId
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProductDetailViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private lateinit var productRepository: FakeProductRepository
    private lateinit var promotionRepository: FakePromotionRepository
    private lateinit var cartRepository: FakeCartRepository
    private lateinit var clock: FakeClock
    private lateinit var getProductDetailWithPromotionUseCase: GetProductDetailWithPromotionUseCase
    private lateinit var addToCartUseCase: AddToCartUseCase

    @Before
    fun setUp() {
        productRepository = FakeProductRepository()
        promotionRepository = FakePromotionRepository()
        cartRepository = FakeCartRepository()
        clock = FakeClock()
        getProductDetailWithPromotionUseCase = GetProductDetailWithPromotionUseCase(
            productRepository,
            promotionRepository,
            GroupPromotionsByProductId(),
            GetPromotionForProduct(),
            clock
        )
        addToCartUseCase = AddToCartUseCase(cartRepository, productRepository)
    }

    private fun createViewModel(
        productId: String,
        getProductDetailWithPromotion: GetProductDetailWithPromotionUseCase = getProductDetailWithPromotionUseCase,
    ) = ProductDetailViewModel(
        getProductDetailWithPromotion = getProductDetailWithPromotion,
        addToCartUseCase = addToCartUseCase,
        productId = productId
    )


    @Test
    fun `GIVEN a product WHEN viewModel starts THEN uiState has product detail`() = runTurbineTest {
        val product = ProductBuilder().build()
        productRepository.setProducts(listOf(product))
        val viewModel = createViewModel(product.id)
        val state = viewModel.uiState.testIn(this)

        val result = state.awaitItem()

        assertEquals(product.id, result.item?.product?.id)
        state.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN a product WHEN addToCart action THEN success event is emitted`() = runTurbineTest {
        val product = ProductBuilder().build()
        productRepository.setProducts(listOf(product))
        val viewModel = createViewModel(product.id)
        val state = viewModel.uiState.testIn(this)
        val events = viewModel.events.testIn(this)
        state.awaitItem()

        viewModel.onAction(ProductDetailAction.AddToCart)

        assertEquals(ProductDetailEvent.SuccessAddToCart, events.awaitItem())
        assertEquals(1, cartRepository.getCartItemById(product.id)?.quantity)
        state.cancelAndIgnoreRemainingEvents()
        events.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN a product with limited stock WHEN addToCart action THEN insufficient stock error event is emitted`() = runTurbineTest {
        val product = ProductBuilder().withStock(0).build()
        productRepository.setProducts(listOf(product))
        val viewModel = createViewModel(product.id)
        val state = viewModel.uiState.testIn(this)
        val events = viewModel.events.testIn(this)
        state.awaitItem()

        viewModel.onAction(ProductDetailAction.AddToCart)

        assertEquals(ProductDetailEvent.InsufficientStockError, events.awaitItem())
        state.cancelAndIgnoreRemainingEvents()
        events.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN a network error WHEN viewModel starts THEN network error event is emitted`() = runTurbineTest {
        val productId = "product-1"
        val failingRepo = FailingProductRepositoryStub(AppError.NetworkError)
        val useCaseWithFailingRepo = GetProductDetailWithPromotionUseCase(
            productRepository = failingRepo,
            promotionRepository = promotionRepository,
            groupPromotionsByProductId = GroupPromotionsByProductId(),
            getPromotionForProduct = GetPromotionForProduct(),
            clock = clock
        )
        val viewModel = createViewModel(productId, getProductDetailWithPromotion = useCaseWithFailingRepo)
        val events = viewModel.events.testIn(this)
        val state = viewModel.uiState.testIn(this).apply { awaitItem() }

        val event = events.awaitItem()

        assertEquals(ProductDetailEvent.NetworkError,event)
        state.cancelAndIgnoreRemainingEvents()
        events.cancelAndIgnoreRemainingEvents()
    }
}
