package com.jujodevs.cursotestingandroid.productlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jujodevs.cursotestingandroid.productlist.domain.model.SortOption
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProductListUiState>(ProductListUiState.Loading)
    val uiState = _uiState.asStateFlow()


    private val _events = MutableSharedFlow<ProductListEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        getProductsUseCase()
            .onStart {
                _uiState.update { ProductListUiState.Loading }
            }
            .onEach { products ->
                val categories = products.map { product -> product.category }.distinct().sorted()
                _uiState.update {
                    ProductListUiState.Success(
                        products = products,
                        categories = categories,
                        selectedCategory = null,
                        sortOption = SortOption.NONE,
                    )
                }
            }
            .catch { e ->
                _uiState.update { ProductListUiState.Error(e.message.orEmpty()) }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: ProductListAction) {
        when(action) {
            is ProductListAction.SetCategory -> setCategory(action.category)
            is ProductListAction.SetOrderSelected -> setSortedOption(action.sortOption)
        }
    }

    private fun setCategory(category: String?) {
        viewModelScope.launch {
            // Llamar a setting repository
        }
    }

    private fun setSortedOption(sortOption: SortOption) {
        viewModelScope.launch {
            // Llamar a setting repository
        }
    }
}