package com.jujodevs.cursotestingandroid.cart.presentation

import com.jujodevs.cursotestingandroid.cart.domain.repository.CartRepository
import com.jujodevs.cursotestingandroid.cart.domain.usecase.GetCartItemWithPromotionUseCase
import com.jujodevs.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.jujodevs.cursotestingandroid.cart.domain.usecase.UpdateCartItemUseCase
import com.jujodevs.cursotestingandroid.core.MainDispatcherRule
import com.jujodevs.cursotestingandroid.core.builders.cartItem
import com.jujodevs.cursotestingandroid.core.builders.product
import com.jujodevs.cursotestingandroid.core.domain.time.Clock
import com.jujodevs.cursotestingandroid.core.fakes.FakeCartRepository
import com.jujodevs.cursotestingandroid.core.fakes.FakeClock
import com.jujodevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.jujodevs.cursotestingandroid.core.fakes.FakePromotionRepository
import com.jujodevs.cursotestingandroid.core.runTurbineTest
import com.jujodevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.jujodevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GroupPromotionsByProductId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CartViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    lateinit var productRepository: FakeProductRepository
    lateinit var cartRepository: FakeCartRepository
    lateinit var promotionRepository: FakePromotionRepository
    lateinit var clock: FakeClock

    lateinit var viewModel: CartViewModel

    @Before
    fun setUp() {
        productRepository = FakeProductRepository()
        cartRepository = FakeCartRepository()
        promotionRepository = FakePromotionRepository()
        clock = FakeClock()

        viewModel = createViewModel()
    }

    private fun createViewModel(
        productRepository: ProductRepository = this.productRepository,
        cartRepository: CartRepository = this.cartRepository,
        promotionRepository: PromotionRepository = this.promotionRepository,
        clock: Clock = this.clock,
    ) = CartViewModel(
        cartRepository = cartRepository,
        getCartSummaryUseCase = GetCartSummaryUseCase(
            cartRepository = cartRepository,
            productRepository = productRepository,
            promotionRepository = promotionRepository,
            groupPromotionsByProductId = GroupPromotionsByProductId(),
            getPromotionForProduct = GetPromotionForProduct(),
            clock = clock,
        ),
        updateCartItemUseCase = UpdateCartItemUseCase(
            cartRepository = cartRepository,
            productRepository = productRepository,
        ),
        getCartItemWithPromotionUseCase = GetCartItemWithPromotionUseCase(
            cartRepository = cartRepository,
            productRepository = productRepository,
            promotionRepository = promotionRepository,
            groupPromotionsByProductId = GroupPromotionsByProductId(),
            getPromotionForProduct = GetPromotionForProduct(),
            clock = clock,
        ),
    )

    @Test
    fun `GIVEN cart data WHEN initialized THEN emit success state`() = runTurbineTest {
        val productId = "1"
        val p = product { withId(productId).withName("Pan").withPrice(2.0) }
        val cartItem = cartItem { withProductId(productId).withQuantity(3) }
        productRepository.setProducts(listOf(p))
        cartRepository.setCartItems(listOf(cartItem))

        val state = viewModel.uiState.testIn(this)

        val updatedState = state.awaitItem() as CartUiState.Success
        assertEquals(1, updatedState.cartItems.size)
        assertEquals(6.0, updatedState.summary.subtotal, 0.0)
        state.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN quantity one WHEN decrease quantity THEN removes item from cart`() = runTurbineTest {
        val productId = "1"
        val p = product { withId(productId).withName("Pan").withPrice(2.0) }
        val cartItem = cartItem { withProductId(productId).withQuantity(1) }
        productRepository.setProducts(listOf(p))
        cartRepository.setCartItems(listOf(cartItem))
        val state = viewModel.uiState.testIn(this).apply { awaitItem() }

        viewModel.onAction(CartAction.DecreaseQuantity(productId, 1))

        val updatedState = state.awaitItem() as CartUiState.Success
        assertEquals(0, updatedState.cartItems.size)
        assertEquals(0.0, updatedState.summary.subtotal, 0.0)
        state.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN insufficient stock WHEN update quantity THEN emits an error event`() = runTurbineTest {
        val productId = "1"
        val p = product { withId(productId).withStock(2) }
        val cartItem = cartItem { withProductId(productId).withQuantity(1) }
        productRepository.setProducts(listOf(p))
        cartRepository.setCartItems(listOf(cartItem))
        val event = viewModel.events.testIn(this)

        viewModel.onAction(CartAction.IncreaseQuantity(productId, 5))

        val updatedEvent = event.awaitItem()
        assertTrue(updatedEvent is CartEvent.ShowMessage)
        event.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `WHEN receive a go back action THEN emits a go back event`() = runTurbineTest {
        val event = viewModel.events.testIn(this)

        viewModel.onAction(CartAction.GoBack)

        val updatedEvent = event.awaitItem()
        assertTrue(updatedEvent is CartEvent.GoBack)
        event.cancelAndIgnoreRemainingEvents()
    }
}