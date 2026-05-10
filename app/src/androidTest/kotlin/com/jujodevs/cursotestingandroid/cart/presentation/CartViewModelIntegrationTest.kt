package com.jujodevs.cursotestingandroid.cart.presentation

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.testIn
import com.jujodevs.cursotestingandroid.cart.domain.repository.CartRepository
import com.jujodevs.cursotestingandroid.cart.domain.usecase.GetCartItemWithPromotionUseCase
import com.jujodevs.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.jujodevs.cursotestingandroid.cart.domain.usecase.UpdateCartItemUseCase
import com.jujodevs.cursotestingandroid.core.MainDispatcherRule
import com.jujodevs.cursotestingandroid.core.mockwebserver.MiniMarketApiDispatcher
import com.jujodevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.jujodevs.cursotestingandroid.core.runTurbineTest
import com.jujodevs.cursotestingandroid.core.utils.asAsset
import com.jujodevs.cursotestingandroid.core.utils.awaitStateMatching
import com.jujodevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.jujodevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CartViewModelIntegrationTest {

    private companion object {
        private const val PRODUCT_ID = "p1"
        private const val UPDATED_QUANTITY = 2
        private const val INITIAL_QUANTITY = 1
    }

    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val mainDispatcherRule = MainDispatcherRule()

    @Inject
    lateinit var cartRepository: CartRepository
    @Inject
    lateinit var productRepository: ProductRepository
    @Inject
    lateinit var promotionRepository: PromotionRepository
    @Inject
    lateinit var getCartSummaryUseCase: GetCartSummaryUseCase
    @Inject
    lateinit var updateCartItemUseCase: UpdateCartItemUseCase
    @Inject
    lateinit var getCartItemWithPromotionUseCase: GetCartItemWithPromotionUseCase

    lateinit var viewModel: CartViewModel

    @Before
    fun setUp() = runTest(mainDispatcherRule.testDispatcher) {
        mockWebServer.server.dispatcher = MiniMarketApiDispatcher(
            productJson = "product_list_default.json".asAsset(),
        )
        hiltRule.inject()

        productRepository.refreshProducts()
        promotionRepository.refreshPromotions()
        viewModel = createViewModel()
    }

    @Test
    fun givenCartWithItems_whenViewModelCollectsUiState_thenSuccessWithSummary() = runTurbineTest {
        cartRepository.addToCart(PRODUCT_ID, UPDATED_QUANTITY)
        val state = viewModel.uiState.testIn(this)

        val result = state.awaitStateMatching { state ->
            state is CartUiState.Success && state.cartItems.isNotEmpty()
        } as CartUiState.Success

        assertTrue(result.cartItems.isNotEmpty())
        assertTrue(result.summary.subtotal == 20.0)
        state.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun givenSingleProduct_whenIncreaseQuantity_thenQuantityUpdates() = runTurbineTest {
        cartRepository.addToCart(PRODUCT_ID, INITIAL_QUANTITY)
        val state = viewModel.uiState.testIn(this)
        val initialResult = state.awaitSuccessState { state ->
            state.cartItems.any {
                it.cartItem.productId == PRODUCT_ID &&
                        it.cartItem.quantity == INITIAL_QUANTITY
            }
        }

        viewModel.onAction(CartAction.IncreaseQuantity(PRODUCT_ID, INITIAL_QUANTITY))

        val updatedResult = state.awaitSuccessState { state ->
            state.cartItems.any {
                it.cartItem.productId == PRODUCT_ID &&
                        it.cartItem.quantity == UPDATED_QUANTITY
            }
        }
        assertEquals(INITIAL_QUANTITY, initialResult.cartItems.first().cartItem.quantity)
        assertEquals(UPDATED_QUANTITY, updatedResult.cartItems.first().cartItem.quantity)
        state.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun givenSingleProduct_whenDecreaseToZero_thenCartBecomesEmpty() = runTurbineTest {
        cartRepository.addToCart(PRODUCT_ID, INITIAL_QUANTITY)
        val state = viewModel.uiState.testIn(this)
        val initialResult = state.awaitSuccessState { state ->
            state.cartItems.any {
                it.cartItem.productId == PRODUCT_ID &&
                        it.cartItem.quantity == INITIAL_QUANTITY
            }
        }

        viewModel.onAction(CartAction.DecreaseQuantity(PRODUCT_ID, INITIAL_QUANTITY))

        val updatedResult = state.awaitSuccessState { state ->
            state.cartItems.isEmpty()
        }
        assertEquals(INITIAL_QUANTITY, initialResult.cartItems.first().cartItem.quantity)
        assertTrue(updatedResult.cartItems.isEmpty())
        state.cancelAndIgnoreRemainingEvents()
    }

    private suspend fun ReceiveTurbine<CartUiState>.awaitSuccessState(
        predicate: (CartUiState.Success) -> Boolean,
    ): CartUiState.Success =
        awaitStateMatching { state ->
            state is CartUiState.Success && predicate(state)
        } as CartUiState.Success

    private fun createViewModel(): CartViewModel =
        CartViewModel(
            cartRepository = cartRepository,
            getCartSummaryUseCase = getCartSummaryUseCase,
            updateCartItemUseCase = updateCartItemUseCase,
            getCartItemWithPromotionUseCase = getCartItemWithPromotionUseCase,
        )
}