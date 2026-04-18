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
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ProductDetailViewModel.Factory::class)
class ProductDetailViewModel @AssistedInject constructor(
    private val getProductDetailWithPromotion: GetProductDetailWithPromotionUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    @Assisted private val productId: String,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(productId: String): ProductDetailViewModel
    }

    val uiState = getUiStateFlow(productId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProductDetailUiState(),
        )

    private val _events = MutableSharedFlow<ProductDetailEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun onAction(action: ProductDetailAction) {
        when (action) {
            ProductDetailAction.AddToCart -> addToCart()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getUiStateFlow(productId: String) =
        getProductDetailWithPromotion(productId)
            .mapLatest { product ->
                ProductDetailUiState(
                    isLoading = false,
                    item = product,
                )
            }
            .catch { e ->
                if (e is AppError) handleError(e)
                else handleError(UnknownError(e.message))
                emit(ProductDetailUiState(isLoading = false,))
            }

    private fun addToCart() {
        val product = uiState.value.item?.product?.id ?: return
        viewModelScope.launch {
            safeRunCatching {
                addToCartUseCase(product)
                _events.emit(ProductDetailEvent.SuccessAddToCart)
            }
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