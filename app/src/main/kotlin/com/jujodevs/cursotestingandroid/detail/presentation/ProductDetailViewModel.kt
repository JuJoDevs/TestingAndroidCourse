package com.jujodevs.cursotestingandroid.detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductDetailWithPromotion: GetProductDetailWithPromotionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProductDetailEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private var productJob: Job? = null

    fun onAction(action: ProductDetailAction) {
        when(action) {
            is ProductDetailAction.LoadProduct -> loadProduct(action.productId)
            ProductDetailAction.AddToCart -> addToCart()
        }
    }

    private fun loadProduct(productId: String) {
        _uiState.update { it.copy(isLoading = true) }
        productJob?.cancel()
        productJob = getProductDetailWithPromotion(productId)
            .onEach { product ->
                _uiState.update { it.copy(isLoading = false, item = product) }
            }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(ProductDetailEvent.ShowError(e.message.orEmpty()))
            }
            .launchIn(viewModelScope)
    }

    private fun addToCart() {

    }
}