package com.jujodevs.cursotestingandroid.detail.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(

) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProductDetailEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun onAction(action: ProductDetailAction) {
        when(action) {
            is ProductDetailAction.LoadProduct -> loadProduct(action.productId)
            ProductDetailAction.AddToCart -> addToCart()
        }
    }

    private fun loadProduct(productId: String) {
        _uiState.update { it.copy(isLoading = true) }
    }

    private fun addToCart() {

    }
}