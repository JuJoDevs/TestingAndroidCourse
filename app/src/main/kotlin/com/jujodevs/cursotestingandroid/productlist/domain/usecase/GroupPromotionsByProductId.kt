package com.jujodevs.cursotestingandroid.productlist.domain.usecase

import com.jujodevs.cursotestingandroid.productlist.domain.model.Promotion
import javax.inject.Inject
import kotlin.collections.forEach

class GroupPromotionsByProductId @Inject constructor() {
    operator fun invoke(promotions: List<Promotion>): Map<String, List<Promotion>> {
        val mapPromotion = mutableMapOf<String, List<Promotion>>()
        promotions.forEach { promotion ->
            promotion.productsIds.forEach { productId ->
                mapPromotion[productId] =
                    mapPromotion.getOrDefault(productId, emptyList()) + promotion
            }
        }
        return mapPromotion
    }
}