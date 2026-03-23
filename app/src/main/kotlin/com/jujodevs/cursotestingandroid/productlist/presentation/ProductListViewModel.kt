package com.jujodevs.cursotestingandroid.productlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.jujodevs.cursotestingandroid.productlist.domain.model.SortOption
import com.jujodevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProductListUiState>(ProductListUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProductListEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    val filterVisible = settingsRepository.filtersVisible.stateIn(
        scope = viewModelScope,
        initialValue = true,
        started = SharingStarted.WhileSubscribed(5000)
    )

    private var productsJob: Job? = null

    init {
        loadProducts()
    }

    fun loadProducts() {
        productsJob?.cancel()
        productsJob = combine(
            getProductsUseCase(),
            settingsRepository.selectedCategory,
            settingsRepository.sortOption,
        ) { products, category, sortOption ->
            var filteredProducts = products

            if (category != null) {
                filteredProducts = filteredProducts.filter { it.product.category == category }
            }

            val sorted = when (sortOption) {
                SortOption.PRICE_ASC -> filteredProducts.sortedBy { effectivePrice(it) }
                SortOption.PRICE_DESC -> filteredProducts.sortedByDescending { effectivePrice(it) }
                SortOption.DISCOUNT -> filteredProducts.sortedWith(
                    compareByDescending<ProductWithPromotion> {
                        effectiveDiscountPercent(it)
                    }.thenBy { it.promotion == null }
                )
                SortOption.NONE -> filteredProducts
            }
            val categories = products.map { product -> product.product.category }
                .distinct()
                .sorted()

            ProductListUiState.Success(
                products = sorted,
                categories = categories,
                selectedCategory = category,
                sortOption = sortOption,
            )
        }.onEach { state ->
            _uiState.update { state }
        }.catch { e ->
            _uiState.update { ProductListUiState.Error(e.message.orEmpty()) }
        }.launchIn(viewModelScope)
    }

    private fun effectivePrice(item: ProductWithPromotion): Double {
        return when (val promo = item.promotion) {
            is ProductPromotion.Percent -> promo.discountedPrice
            else -> item.product.price
        }
    }

    private fun effectiveDiscountPercent(item: ProductWithPromotion): Double {
        return when (val promo = item.promotion) {
            is ProductPromotion.Percent -> promo.percent
            else -> 0.0
        }
    }

    fun onAction(action: ProductListAction) {
        when (action) {
            is ProductListAction.SetCategory -> setCategory(action.category)
            is ProductListAction.SetOrderSelected -> setSortedOption(action.sortOption)
            is ProductListAction.SetFiltersVisible -> setFiltersVisible(action.showFilters)
            ProductListAction.NavToSettings -> navigateToSettings()
            is ProductListAction.NavToProductDetail -> navigateToProductDetail(action.product)
        }
    }

    private fun setCategory(category: String?) {
        viewModelScope.launch {
            settingsRepository.setSelectedCategory(category)
        }
    }

    private fun setSortedOption(sortOption: SortOption) {
        viewModelScope.launch {
            settingsRepository.setSortOption(sortOption)
        }
    }

    private fun setFiltersVisible(showFilters: Boolean) {
        viewModelScope.launch {
            settingsRepository.setFiltersVisible(showFilters)
        }
    }

    private fun navigateToSettings() {
        viewModelScope.launch {
            _events.tryEmit(ProductListEvent.NavigateToSettings)
        }
    }

    private fun navigateToProductDetail(product: ProductWithPromotion) {
        viewModelScope.launch {
            _events.tryEmit(ProductListEvent.NavigateToProductDetail(product.product.id))
        }
    }
}
