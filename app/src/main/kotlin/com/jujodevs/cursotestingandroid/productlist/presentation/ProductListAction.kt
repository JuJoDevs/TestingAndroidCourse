package com.jujodevs.cursotestingandroid.productlist.presentation

sealed interface ProductListAction {
    data class SetCategory(val category: String?): ProductListAction
}