package com.jujodevs.cursotestingandroid.cart.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jujodevs.cursotestingandroid.cart.domain.repository.CartRepository
import com.jujodevs.cursotestingandroid.cart.domain.usecase.GetCartItemWithPromotionUseCase
import com.jujodevs.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.jujodevs.cursotestingandroid.cart.domain.usecase.UpdateCartItemUseCase
import com.jujodevs.cursotestingandroid.core.domain.safeRunCatching
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModel
    @Inject
    constructor(
        private val cartRepository: CartRepository,
        private val getCartSummaryUseCase: GetCartSummaryUseCase,
        private val updateCartItemUseCase: UpdateCartItemUseCase,
        private val getCartItemWithPromotionUseCase: GetCartItemWithPromotionUseCase,
    ) : ViewModel() {
        private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val uiState =
            refreshTrigger
                .onStart { emit(Unit) }
                .flatMapLatest { getUiStateFlow() }
                .catch { e ->
                    emit(CartUiState.Error(e.message.orEmpty()))
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = CartUiState.Loading,
                )

        private val _events = MutableSharedFlow<CartEvent>(extraBufferCapacity = 1)
        val events = _events.asSharedFlow()

        private fun getUiStateFlow(): Flow<CartUiState> =
            combine(
                getCartItemWithPromotionUseCase(),
                getCartSummaryUseCase(),
            ) { cartItemWithPromotions, summary ->
                CartUiState.Success(
                    summary = summary,
                    cartItems = cartItemWithPromotions,
                    isLoading = false,
                ) as CartUiState
            }

        fun onAction(action: CartAction) {
            when (action) {
                CartAction.GoBack -> goBack()
                CartAction.LoadCart -> refresh()
                is CartAction.UpdateCartItem ->
                    updateCartItem(
                        productId = action.productId,
                        quantity = action.quantity,
                    )

                is CartAction.RemoveFromCart -> removeFromCart(action.productId)
                is CartAction.IncreaseQuantity ->
                    increaseQuantity(
                        action.productId,
                        action.currentQuantity,
                    )

                is CartAction.DecreaseQuantity ->
                    decreaseQuantity(
                        action.productId,
                        action.currentQuantity,
                    )
            }
        }

        private fun goBack() {
            viewModelScope.launch {
                _events.emit(CartEvent.GoBack)
            }
        }

        private fun refresh() {
            viewModelScope.launch {
                refreshTrigger.emit(Unit)
            }
        }

        private fun updateCartItem(
            productId: String,
            quantity: Int,
        ) {
            viewModelScope.launch {
                safeRunCatching {
                    updateCartItemUseCase(
                        productId = productId,
                        quantity = quantity,
                    )
                }.onFailure { e ->
                    _events.emit(CartEvent.ShowMessage(e.message.orEmpty()))
                }
            }
        }

        private fun removeFromCart(productId: String) {
            viewModelScope.launch {
                safeRunCatching {
                    cartRepository.removeFromCart(
                        productId = productId,
                    )
                }.onFailure { e ->
                    _events.emit(CartEvent.ShowMessage(e.message.orEmpty()))
                }
            }
        }

        private fun increaseQuantity(
            productId: String,
            currentQuantity: Int,
        ) {
            updateCartItem(
                productId,
                currentQuantity + 1,
            )
        }

        private fun decreaseQuantity(
            productId: String,
            currentQuantity: Int,
        ) {
            if (currentQuantity > 1) {
                updateCartItem(
                    productId,
                    currentQuantity - 1,
                )
            } else {
                removeFromCart(productId)
            }
        }
    }
