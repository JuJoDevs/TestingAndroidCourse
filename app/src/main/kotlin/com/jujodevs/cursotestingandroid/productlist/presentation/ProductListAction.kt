package com.jujodevs.cursotestingandroid.productlist.presentation

import com.jujodevs.cursotestingandroid.productlist.domain.model.SortOption

sealed interface ProductListAction {
    data class SetCategory(val category: String?): ProductListAction
    data class SetOrderSelected(val sortOption: SortOption): ProductListAction
}