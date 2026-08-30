package com.jujodevs.cursotestingandroid.checkout.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.jujodevs.cursotestingandroid.R
import com.jujodevs.cursotestingandroid.cart.domain.model.CartSummary
import com.jujodevs.cursotestingandroid.checkout.domain.model.OrderConfirmation
import com.jujodevs.cursotestingandroid.core.presentation.ComposeTest
import com.jujodevs.cursotestingandroid.core.utils.getString
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * EXAMEN — Tests de UI (Compose) de la pantalla de checkout.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: composables de [CheckoutScreen] / CheckoutContent renderizando cada [CheckoutUiState].
 * Pistas: usa `composeRule.setContent { ... }` pasando el estado deseado y callbacks de prueba;
 * localiza nodos por texto (la pantalla aún no expone testTags) y verifica habilitación del botón.
 */
class CheckoutScreenTest : ComposeTest() {
    private fun createCheckoutScreen(
        uiState: CheckoutUiState,
        onAction: (CheckoutAction) -> Unit = {},
    ) {
        composeRule.setContent {
            CheckoutContent(
                uiState = uiState,
                snackbarHostState = remember { SnackbarHostState() },
                onAction = onAction,
            )
        }
    }

    @Test
    fun givenLoadingState_whenRendered_thenShowsProgress() =
        withComposeRule {
            val uiState = CheckoutUiState.Loading

            createCheckoutScreen(uiState)

            onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
                .assertIsDisplayed()
        }

    @Test
    fun givenIdleStateWithEmptyCart_whenRendered_thenConfirmButtonDisabled() =
        withComposeRule {
            val uiState =
                idleState(
                    form =
                        CheckoutForm(
                            name = "Juan",
                            address = "Calle Mayor 1",
                            email = "test@example.com",
                        ),
                    isCartEmpty = true,
                )

            createCheckoutScreen(uiState)

            onNodeWithText(getString(R.string.checkout_confirm_order))
                .assertIsNotEnabled()
        }

    @Test
    fun givenIdleStateWithValidForm_whenRendered_thenConfirmButtonEnabled() =
        withComposeRule {
            val uiState =
                idleState(
                    form =
                        CheckoutForm(
                            name = "Juan",
                            address = "Calle Mayor 1",
                            email = "test@example.com",
                        ),
                )

            createCheckoutScreen(uiState)

            onNodeWithText(getString(R.string.checkout_confirm_order))
                .assertIsEnabled()
        }

    @Test
    fun givenIdleState_whenTypingInvalidEmail_thenConfirmButtonDisabled() =
        withComposeRule {
            val initialState =
                idleState(
                    form =
                        CheckoutForm(
                            name = "Juan",
                            address = "Calle Mayor 1",
                            email = "test@example.com",
                        ),
                )

            createEditableCheckoutScreen(initialState)
            onNode(hasSetTextAction() and hasText("test@example.com"))
                .performTextClearance()
            onNode(hasSetTextAction() and hasText(""))
                .performTextInput("invalid-email")

            onNodeWithText(getString(R.string.checkout_confirm_order))
                .assertIsNotEnabled()
        }

    @Test
    fun givenSuccessState_whenRendered_thenShowsOrderConfirmation() =
        withComposeRule {
            val confirmation =
                OrderConfirmation(
                    orderId = "order-1",
                    etaMinutes = 30,
                    total = 25.0,
                )
            val uiState = CheckoutUiState.Success(confirmation)

            createCheckoutScreen(uiState)

            onNodeWithText(getString(R.string.checkout_order_confirmed, confirmation.orderId))
                .assertIsDisplayed()
            onNodeWithText(getString(R.string.checkout_estimated_time, confirmation.etaMinutes))
                .assertIsDisplayed()
            onNodeWithText(getString(R.string.checkout_price, confirmation.total))
                .assertIsDisplayed()
        }

    @Test
    fun givenErrorState_whenRetryClicked_thenInvokesRetryCallback() =
        withComposeRule {
            var retryClicked = false
            val uiState = CheckoutUiState.Error("Error test")

            createCheckoutScreen(
                uiState = uiState,
                onAction = { action ->
                    if (action is CheckoutAction.Retry) {
                        retryClicked = true
                    }
                },
            )
            onNodeWithText(getString(R.string.checkout_retry))
                .performClick()

            assertTrue(retryClicked)
        }

    private fun createEditableCheckoutScreen(initialState: CheckoutUiState.Idle) {
        composeRule.setContent {
            var uiState: CheckoutUiState by remember { mutableStateOf(initialState) }
            CheckoutContent(
                uiState = uiState,
                snackbarHostState = remember { SnackbarHostState() },
                onAction = { action ->
                    val currentState = uiState
                    if (currentState is CheckoutUiState.Idle) {
                        val form =
                            when (action) {
                                is CheckoutAction.ChangeName -> currentState.form.copy(name = action.name)
                                is CheckoutAction.ChangeAddress -> currentState.form.copy(address = action.address)
                                is CheckoutAction.ChangeEmail -> currentState.form.copy(email = action.email)
                                else -> currentState.form
                            }
                        val errors = form.validate()
                        uiState =
                            currentState.copy(
                                form = form,
                                errors = errors,
                                canSubmit = !currentState.isCartEmpty && !currentState.isSubmitting && errors.isValid,
                            )
                    }
                },
            )
        }
    }
}

private fun idleState(
    form: CheckoutForm = CheckoutForm(),
    isCartEmpty: Boolean = false,
): CheckoutUiState.Idle {
    val errors = form.validate()
    return CheckoutUiState.Idle(
        summary =
            CartSummary(
                subtotal = 25.0,
                discountTotal = 0.0,
                finalTotal = 25.0,
            ),
        form = form,
        errors = errors,
        isCartEmpty = isCartEmpty,
        isSubmitting = false,
        canSubmit = !isCartEmpty && errors.isValid,
    )
}
