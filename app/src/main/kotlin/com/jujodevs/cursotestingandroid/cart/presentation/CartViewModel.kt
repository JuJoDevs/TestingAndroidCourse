package com.jujodevs.cursotestingandroid.cart.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jujodevs.cursotestingandroid.cart.domain.repository.CartRepository
import com.jujodevs.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.jujodevs.cursotestingandroid.cart.domain.usecase.UpdateCartItemUseCase
import com.jujodevs.cursotestingandroid.cart.presentation.model.CartItemWithPromotion
import com.jujodevs.cursotestingandroid.core.domain.safeRunCatching
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val getCartSummaryUseCase: GetCartSummaryUseCase,
    private val updateCartItemUseCase: UpdateCartItemUseCase,
    private val getProductsUseCase: GetProductsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CartEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    var cartJob: Job? = null

    init {
        loadCart()
    }

    private fun loadCart() {
        _uiState.value = CartUiState.Loading
        cartJob?.cancel()

        cartJob = cartRepository.getCartItems()
            .flatMapLatest { cartItems ->
                val ids = cartItems.mapTo(mutableSetOf()) { it.productId }
                if (ids.isEmpty()) {
                    getCartSummaryUseCase().map { summary ->
                        _uiState.update {
                            CartUiState.Success(
                                summary = summary,
                                cartItems = emptyList(),
                                isLoading = false
                            )
                        }
                    }
                } else {
                    combine(
                        getProductsUseCase(ids),
                        getCartSummaryUseCase()
                    ) { products, summary ->
                        val productsById = products.associateBy { it.product.id }
                        val cartItemsWithProducts = cartItems.mapNotNull { cartItem ->
                            productsById[cartItem.productId]?.let { product ->
                                CartItemWithPromotion(
                                    productWithPromotion = product,
                                    cartItem = cartItem
                                )
                            }
                        }

                        _uiState.update {
                            CartUiState.Success(
                                summary = summary,
                                cartItems = cartItemsWithProducts,
                                isLoading = false
                            )
                        }
                    }
                }
            }
            .catch { e ->
                _uiState.update { CartUiState.Error(e.message.orEmpty()) }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: CartAction) {
        when (action) {
            CartAction.LoadCart -> loadCart()
            is CartAction.UpdateCartItem -> updateCartItem(
                productId = action.productId,
                quantity = action.quantity
            )

            is CartAction.RemoveFromCart -> removeFromCart(action.productId)
            is CartAction.IncreaseQuantity -> increaseQuantity(
                action.productId,
                action.currentQuantity
            )

            is CartAction.DecreaseQuantity -> decreaseQuantity(
                action.productId,
                action.currentQuantity
            )
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
                    quantity = quantity
                )
            }
                .onFailure { e ->
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
            }
                .onFailure { e ->
                    _events.emit(CartEvent.ShowMessage(e.message.orEmpty()))
                }
        }
    }

    fun increaseQuantity(
        productId: String,
        currentQuantity: Int,
    ) {
        updateCartItem(
            productId,
            currentQuantity + 1
        )
    }

    fun decreaseQuantity(
        productId: String,
        currentQuantity: Int,
    ) {
        if (currentQuantity > 1) {
            updateCartItem(
                productId,
                currentQuantity - 1
            )
        } else {
            removeFromCart(productId)
        }
    }
}