package com.jujodevs.cursotestingandroid.detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jujodevs.cursotestingandroid.cart.domain.usecase.AddToCartUseCase
import com.jujodevs.cursotestingandroid.core.domain.model.AppError
import com.jujodevs.cursotestingandroid.core.domain.model.AppError.DatabaseError
import com.jujodevs.cursotestingandroid.core.domain.model.AppError.NetworkError
import com.jujodevs.cursotestingandroid.core.domain.model.AppError.NotFoundError
import com.jujodevs.cursotestingandroid.core.domain.model.AppError.UnknownError
import com.jujodevs.cursotestingandroid.core.domain.model.AppError.Validation
import com.jujodevs.cursotestingandroid.core.domain.safeRunCatching
import com.jujodevs.cursotestingandroid.detail.domain.usecase.GetProductDetailWithPromotionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductDetailWithPromotion: GetProductDetailWithPromotionUseCase,
    private val addToCartUseCase: AddToCartUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProductDetailEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private var productJob: Job? = null

    fun onAction(action: ProductDetailAction) {
        when (action) {
            is ProductDetailAction.LoadProduct -> loadProduct(action.productId)
            ProductDetailAction.AddToCart -> addToCart()
        }
    }

    private fun loadProduct(productId: String) {
        _uiState.update { it.copy(isLoading = true) }
        productJob?.cancel()
        productJob = getProductDetailWithPromotion(productId)
            .onEach { product ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        item = product
                    )
                }
            }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false) }
                if (e is AppError) handleError(e)
                else handleError(UnknownError(e.message))
            }
            .launchIn(viewModelScope)
    }

    private fun addToCart() {
        val product = _uiState.value.item?.product?.id ?: return
        viewModelScope.launch {
            safeRunCatching { addToCartUseCase(product) }
                .onFailure { e ->
                    if (e is AppError) handleError(e)
                    else handleError(UnknownError(e.message))
                }
        }
    }

    private suspend fun handleError(e: AppError) {
        val newEvent = when (e) {
            NetworkError -> ProductDetailEvent.NetworkError
            is Validation.InsufficientStock -> ProductDetailEvent.InsufficientStockError
            is UnknownError, DatabaseError, NotFoundError, Validation.QuantityMustBePositive -> ProductDetailEvent.UnknownError
        }
        _events.emit(newEvent)
    }
}