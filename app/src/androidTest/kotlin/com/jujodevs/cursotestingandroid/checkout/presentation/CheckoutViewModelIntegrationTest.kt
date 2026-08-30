package com.jujodevs.cursotestingandroid.checkout.presentation

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.ReceiveTurbine
import com.jujodevs.cursotestingandroid.cart.domain.repository.CartRepository
import com.jujodevs.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.jujodevs.cursotestingandroid.checkout.domain.usecase.IsCartEmptyUseCase
import com.jujodevs.cursotestingandroid.checkout.domain.usecase.PlaceOrderUseCase
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * EXAMEN — Tests de INTEGRACIÓN del ViewModel de checkout (extremo a extremo).
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [CheckoutViewModel] con casos de uso reales + Room + MockWebServer.
 * Pistas: inyecta dependencias con Hilt, prepara el carrito real, observa `uiState` con Turbine,
 * y verifica que tras un pedido OK el estado es Success y el carrito queda vacío.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CheckoutViewModelIntegrationTest {
    private companion object {
        private const val PRODUCT_ID = "p1"
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
    lateinit var placeOrderUseCase: PlaceOrderUseCase

    @Inject
    lateinit var isCartEmptyUseCase: IsCartEmptyUseCase

    lateinit var viewModel: CheckoutViewModel

    @Before
    fun setUp() =
        runTest(mainDispatcherRule.testDispatcher) {
            mockWebServer.server.dispatcher =
                MiniMarketApiDispatcher(
                    productJson = "product_list_default.json".asAsset(),
                    orderJson = "order_confirmation_default.json".asAsset(),
                )
            hiltRule.inject()
            productRepository.refreshProducts()
            promotionRepository.refreshPromotions()
            viewModel = createViewModel()
        }

    @Test
    fun givenItemsInCart_whenViewModelInitialized_thenIdleStateWithSummary() =
        runTurbineTest {
            cartRepository.addToCart(PRODUCT_ID, INITIAL_QUANTITY)

            val state = viewModel.uiState.testIn(this)

            val idleState =
                state.awaitIdleState { it.summary.finalTotal == 10.0 }
            assertEquals(10.0, idleState.summary.finalTotal, 0.0)
            assertTrue(!idleState.isCartEmpty)
            state.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun givenValidFormAndSuccessfulOrder_whenOnConfirm_thenSuccessStateAndCartCleared() =
        runTurbineTest {
            cartRepository.addToCart(PRODUCT_ID, INITIAL_QUANTITY)
            val state = viewModel.uiState.testIn(this)
            completeValidForm()

            viewModel.onAction(CheckoutAction.Confirm)

            val successState =
                state.awaitStateMatching {
                    it is CheckoutUiState.Success
                } as CheckoutUiState.Success
            assertEquals("order-1", successState.confirmation.orderId)
            assertTrue(cartRepository.getCartItems().first().isEmpty())
            state.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun givenOrderEndpointFails_whenOnConfirm_thenErrorState() =
        runTurbineTest {
            mockWebServer.server.dispatcher =
                MiniMarketApiDispatcher(
                    productJson = "product_list_default.json".asAsset(),
                    orderJson = "order_confirmation_default.json".asAsset(),
                    orderResponseCode = 404,
                )
            cartRepository.addToCart(PRODUCT_ID, INITIAL_QUANTITY)
            val state = viewModel.uiState.testIn(this)
            completeValidForm()

            viewModel.onAction(CheckoutAction.Confirm)

            val errorState =
                state.awaitStateMatching {
                    it is CheckoutUiState.Error
                } as CheckoutUiState.Error
            assertEquals("", errorState.message)
            state.cancelAndIgnoreRemainingEvents()
        }

    private fun createViewModel(): CheckoutViewModel =
        CheckoutViewModel(
            placeOrderUseCase = placeOrderUseCase,
            getCartSummaryUseCase = getCartSummaryUseCase,
            isCartEmptyUseCase = isCartEmptyUseCase,
        )

    private fun completeValidForm() {
        viewModel.onAction(CheckoutAction.ChangeName("Juan"))
        viewModel.onAction(CheckoutAction.ChangeAddress("Calle Mayor 1"))
        viewModel.onAction(CheckoutAction.ChangeEmail("test@example.com"))
    }

    private suspend fun ReceiveTurbine<CheckoutUiState>.awaitIdleState(predicate: (CheckoutUiState.Idle) -> Boolean): CheckoutUiState.Idle =
        awaitStateMatching { state ->
            state is CheckoutUiState.Idle && predicate(state)
        } as CheckoutUiState.Idle
}
