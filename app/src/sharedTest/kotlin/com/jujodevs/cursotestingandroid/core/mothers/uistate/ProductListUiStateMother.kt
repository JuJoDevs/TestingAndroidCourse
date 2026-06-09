package com.jujodevs.cursotestingandroid.core.mothers.uistate

import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.apple
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.bread
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.chicken
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.coffee
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.milk
import com.jujodevs.cursotestingandroid.core.mothers.ProductMother.yogurt
import com.jujodevs.cursotestingandroid.core.mothers.PromotionMother
import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.jujodevs.cursotestingandroid.productlist.domain.model.SortOption
import com.jujodevs.cursotestingandroid.productlist.presentation.ProductListUiState
import kotlin.collections.emptyList

object ProductListUiStateMother {
    val success = ProductListUiState.Success(
        products = listOf(
            ProductWithPromotion(bread),
            ProductWithPromotion(milk),
            ProductWithPromotion(coffee, PromotionMother.percent()),
            ProductWithPromotion(apple),
            ProductWithPromotion(yogurt, PromotionMother.buyXGetY()),
            ProductWithPromotion(chicken),
        ),
        categories = setOf(bread.category, milk.category, coffee.category, apple.category, yogurt.category, chicken.category).toList(),
        selectedCategory = null,
        sortOption = SortOption.NONE,
    )
}