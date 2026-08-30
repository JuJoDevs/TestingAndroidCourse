package com.jujodevs.cursotestingandroid.checkout.presentation

import com.jujodevs.cursotestingandroid.cart.domain.repository.CartRepository
import com.jujodevs.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.jujodevs.cursotestingandroid.checkout.domain.model.OrderConfirmation
import com.jujodevs.cursotestingandroid.checkout.domain.repository.OrderRepository
import com.jujodevs.cursotestingandroid.checkout.domain.usecase.IsCartEmptyUseCase
import com.jujodevs.cursotestingandroid.checkout.domain.usecase.PlaceOrderUseCase
import com.jujodevs.cursotestingandroid.core.MainDispatcherRule
import com.jujodevs.cursotestingandroid.core.builders.cartItem
import com.jujodevs.cursotestingandroid.core.builders.product
import com.jujodevs.cursotestingandroid.core.fakes.FakeCartRepository
import com.jujodevs.cursotestingandroid.core.fakes.FakeClock
import com.jujodevs.cursotestingandroid.core.fakes.FakeOrderRepository
import com.jujodevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.jujodevs.cursotestingandroid.core.fakes.FakePromotionRepository
import com.jujodevs.cursotestingandroid.core.runTurbineTest
import com.jujodevs.cursotestingandroid.core.utils.awaitStateMatching
import com.jujodevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.jujodevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GroupPromotionsByProductId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.awaitCancellation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * EXAMEN — Tests UNITARIOS del ViewModel de checkout.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [CheckoutViewModel] — estados [CheckoutUiState], `canSubmit`, `onConfirm`, eventos.
 * Pistas: usa Turbine sobre `uiState`/`event`, `runTest(mainDispatcherRule.scheduler)`,
 * fakes (FakeCartItemRepository, FakeProductRepository, FakePromotionRepository, FakeSystemClock)
 * y un fake de OrderRepository que tendrás que crear.
 */
class CheckoutViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    lateinit var productRepository: FakeProductRepository
    lateinit var cartRepository: FakeCartRepository
    lateinit var promotionRepository: FakePromotionRepository
    lateinit var orderRepository: FakeOrderRepository
    lateinit var clock: FakeClock

    lateinit var viewModel: CheckoutViewModel

    @Before
    fun setUp() {
        productRepository = FakeProductRepository()
        cartRepository = FakeCartRepository()
        promotionRepository = FakePromotionRepository()
        orderRepository = FakeOrderRepository()
        clock = FakeClock()

        viewModel = createViewModel()
    }

    private fun createViewModel(
        orderRepository: OrderRepository = this.orderRepository,
        cartRepository: CartRepository = this.cartRepository,
        productRepository: ProductRepository = this.productRepository,
        promotionRepository: PromotionRepository = this.promotionRepository,
        clock: FakeClock = this.clock,
    ): CheckoutViewModel =
        CheckoutViewModel(
            placeOrderUseCase = PlaceOrderUseCase(orderRepository, cartRepository),
            getCartSummaryUseCase =
                GetCartSummaryUseCase(
                    cartRepository = cartRepository,
                    productRepository = productRepository,
                    promotionRepository = promotionRepository,
                    groupPromotionsByProductId = GroupPromotionsByProductId(),
                    getPromotionForProduct = GetPromotionForProduct(),
                    clock = clock,
                ),
            isCartEmptyUseCase = IsCartEmptyUseCase(cartRepository),
        )

    private fun givenCartWithProduct(
        productId: String,
        price: Double = 10.0,
    ) {
        productRepository.setProducts(listOf(product { withId(productId).withPrice(price) }))
        cartRepository.setCartItems(listOf(cartItem { withProductId(productId) }))
    }

    @Test
    fun `given empty cart when initialized then canSubmit is false`() =
        runTurbineTest {
            val state = viewModel.uiState.testIn(this)

            val idleState =
                state.awaitStateMatching { it is CheckoutUiState.Idle } as CheckoutUiState.Idle

            assertTrue(idleState.isCartEmpty)
            assertFalse(idleState.canSubmit)
            state.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `given valid form and non empty cart when form completed then canSubmit is true`() =
        runTurbineTest {
            val productId = "product-1"
            givenCartWithProduct(productId)
            val state = viewModel.uiState.testIn(this)

            viewModel.onAction(CheckoutAction.ChangeName("Juan"))
            viewModel.onAction(CheckoutAction.ChangeAddress("Calle Mayor 1"))
            viewModel.onAction(CheckoutAction.ChangeEmail("test@example.com"))

            val idleState =
                state.awaitStateMatching {
                    it is CheckoutUiState.Idle && it.canSubmit
                } as CheckoutUiState.Idle
            assertFalse(idleState.isCartEmpty)
            assertTrue(idleState.canSubmit)
            state.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `given malformed email when email changed then emailError is INVALID_EMAIL and canSubmit is false`() =
        runTurbineTest {
            val productId = "product-1"
            givenCartWithProduct(productId)
            val state = viewModel.uiState.testIn(this)

            viewModel.onAction(CheckoutAction.ChangeName("Juan"))
            viewModel.onAction(CheckoutAction.ChangeAddress("Calle Mayor 1"))
            viewModel.onAction(CheckoutAction.ChangeEmail("invalid-email"))

            val idleState =
                state.awaitStateMatching {
                    it is CheckoutUiState.Idle &&
                        it.errors.emailError == FieldError.INVALID_EMAIL
                } as CheckoutUiState.Idle
            assertEquals(FieldError.INVALID_EMAIL, idleState.errors.emailError)
            assertFalse(idleState.canSubmit)
            state.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `given valid form when onConfirm succeeds then emits Success state`() =
        runTurbineTest {
            val confirmation = OrderConfirmation("order-1", 30, 25.0)
            val productId = "product-1"
            givenCartWithProduct(productId, price = 25.0)
            orderRepository = FakeOrderRepository(Result.success(confirmation))
            viewModel = createViewModel()
            val state = viewModel.uiState.testIn(this)
            viewModel.onAction(CheckoutAction.ChangeName("Juan"))
            viewModel.onAction(CheckoutAction.ChangeAddress("Calle Mayor 1"))
            viewModel.onAction(CheckoutAction.ChangeEmail("test@example.com"))

            viewModel.onAction(CheckoutAction.Confirm)

            val successState =
                state.awaitStateMatching {
                    it is CheckoutUiState.Success
                } as CheckoutUiState.Success
            assertEquals(confirmation, successState.confirmation)
            state.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `given place order fails when onConfirm then emits Error state and ShowMessage event`() =
        runTurbineTest {
            val error = IllegalStateException("Order failed")
            val productId = "product-1"
            givenCartWithProduct(productId, price = 25.0)
            orderRepository = FakeOrderRepository(Result.failure(error))
            viewModel = createViewModel()
            val state = viewModel.uiState.testIn(this)
            val event = viewModel.events.testIn(this)
            viewModel.onAction(CheckoutAction.ChangeName("Juan"))
            viewModel.onAction(CheckoutAction.ChangeAddress("Calle Mayor 1"))
            viewModel.onAction(CheckoutAction.ChangeEmail("test@example.com"))

            viewModel.onAction(CheckoutAction.Confirm)

            val errorState =
                state.awaitStateMatching {
                    it is CheckoutUiState.Error
                } as CheckoutUiState.Error
            val showMessageEvent = event.awaitItem()
            assertEquals("Order failed", errorState.message)
            assertEquals(CheckoutEvent.ShowMessage("Order failed"), showMessageEvent)
            state.cancelAndIgnoreRemainingEvents()
            event.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `given invalid form when onConfirm then does not place order`() =
        runTurbineTest {
            val orderRepository = mockk<OrderRepository>()
            viewModel = createViewModel(orderRepository = orderRepository)
            val state = viewModel.uiState.testIn(this)

            viewModel.onAction(CheckoutAction.Confirm)

            val idleState =
                state.awaitStateMatching { it is CheckoutUiState.Idle } as CheckoutUiState.Idle
            assertFalse(idleState.errors.isValid)
            coVerify(exactly = 0) { orderRepository.placeOrder() }
            state.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `when go back then emits GoBack event`() =
        runTurbineTest {
            val event = viewModel.events.testIn(this)

            viewModel.onAction(CheckoutAction.GoBack)

            val updatedEvent = event.awaitItem()
            assertTrue(updatedEvent is CheckoutEvent.GoBack)
            event.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `given error state when retry then returns to idle state`() =
        runTurbineTest {
            val error = IllegalStateException("Order failed")
            val productId = "product-1"
            givenCartWithProduct(productId, price = 25.0)
            orderRepository = FakeOrderRepository(Result.failure(error))
            viewModel = createViewModel()
            val state = viewModel.uiState.testIn(this)
            viewModel.onAction(CheckoutAction.ChangeName("Juan"))
            viewModel.onAction(CheckoutAction.ChangeAddress("Calle Mayor 1"))
            viewModel.onAction(CheckoutAction.ChangeEmail("test@example.com"))
            viewModel.onAction(CheckoutAction.Confirm)
            state.awaitStateMatching { it is CheckoutUiState.Error }

            viewModel.onAction(CheckoutAction.Retry)

            val idleState =
                state.awaitStateMatching {
                    it is CheckoutUiState.Idle
                } as CheckoutUiState.Idle
            assertTrue(idleState.canSubmit)
            state.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `when name changed then updates form name`() =
        runTurbineTest {
            val state = viewModel.uiState.testIn(this)

            viewModel.onAction(CheckoutAction.ChangeName("Juan"))

            val idleState =
                state.awaitStateMatching {
                    it is CheckoutUiState.Idle && it.form.name == "Juan"
                } as CheckoutUiState.Idle
            assertEquals("Juan", idleState.form.name)
            state.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `when address changed then updates form address`() =
        runTurbineTest {
            val state = viewModel.uiState.testIn(this)

            viewModel.onAction(CheckoutAction.ChangeAddress("Calle Mayor 1"))

            val idleState =
                state.awaitStateMatching {
                    it is CheckoutUiState.Idle && it.form.address == "Calle Mayor 1"
                } as CheckoutUiState.Idle
            assertEquals("Calle Mayor 1", idleState.form.address)
            state.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `given valid form and empty cart when onConfirm then does not place order`() =
        runTurbineTest {
            val orderRepository = mockk<OrderRepository>()
            viewModel = createViewModel(orderRepository = orderRepository)
            val state = viewModel.uiState.testIn(this)
            viewModel.onAction(CheckoutAction.ChangeName("Juan"))
            viewModel.onAction(CheckoutAction.ChangeAddress("Calle Mayor 1"))
            viewModel.onAction(CheckoutAction.ChangeEmail("test@example.com"))

            viewModel.onAction(CheckoutAction.Confirm)

            val idleState =
                state.awaitStateMatching {
                    it is CheckoutUiState.Idle && !it.canSubmit
                } as CheckoutUiState.Idle
            assertTrue(idleState.isCartEmpty)
            coVerify(exactly = 0) { orderRepository.placeOrder() }
            state.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `when email changed then updates form email`() =
        runTurbineTest {
            val state = viewModel.uiState.testIn(this)

            viewModel.onAction(CheckoutAction.ChangeEmail("test@example.com"))

            val idleState =
                state.awaitStateMatching {
                    it is CheckoutUiState.Idle && it.form.email == "test@example.com"
                } as CheckoutUiState.Idle
            assertEquals("test@example.com", idleState.form.email)
            state.cancelAndIgnoreRemainingEvents()
        }

    @Test
    fun `given valid form when onConfirm then emits submitting state`() =
        runTurbineTest {
            val productId = "product-1"
            val orderRepository =
                mockk<OrderRepository> {
                    coEvery { placeOrder() } coAnswers { awaitCancellation() }
                }
            givenCartWithProduct(productId, price = 25.0)
            viewModel = createViewModel(orderRepository = orderRepository)
            val state = viewModel.uiState.testIn(this)
            viewModel.onAction(CheckoutAction.ChangeName("Juan"))
            viewModel.onAction(CheckoutAction.ChangeAddress("Calle Mayor 1"))
            viewModel.onAction(CheckoutAction.ChangeEmail("test@example.com"))

            viewModel.onAction(CheckoutAction.Confirm)

            val idleState =
                state.awaitStateMatching {
                    it is CheckoutUiState.Idle && it.isSubmitting
                } as CheckoutUiState.Idle
            assertTrue(idleState.isSubmitting)
            assertFalse(idleState.canSubmit)
            coVerify(exactly = 1) { orderRepository.placeOrder() }
            state.cancelAndIgnoreRemainingEvents()
        }
}
