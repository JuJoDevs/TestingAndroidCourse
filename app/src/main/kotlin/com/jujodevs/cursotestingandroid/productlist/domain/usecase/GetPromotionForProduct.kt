package com.jujodevs.cursotestingandroid.productlist.domain.usecase

import com.jujodevs.cursotestingandroid.core.domain.ex.roundTo2Decimals
import com.jujodevs.cursotestingandroid.productlist.domain.model.Product
import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.jujodevs.cursotestingandroid.productlist.domain.model.Promotion
import com.jujodevs.cursotestingandroid.productlist.domain.model.PromotionType
import javax.inject.Inject

class GetPromotionForProduct @Inject constructor() {
    operator fun invoke(
        product: Product,
        promotions: Map<String, List<Promotion>>
    ): ProductPromotion? {
        val productPromos = promotions.getOrDefault(product.id, emptyList())

        val percentPromos = productPromos
            .filter { promo -> promo.type == PromotionType.PERCENT }
            .maxByOrNull { promo -> promo.value }

        return if (percentPromos != null) {
            val percent = percentPromos.value.coerceIn(0.0, 100.0)
            val discountedPrice = (product.price * (1 - percent / 100.0)).roundTo2Decimals()
            ProductPromotion.Percent(percent = percent, discountedPrice = discountedPrice)
        } else {

            val buyPayPromo = productPromos
                .filter { promo ->
                    promo.type == PromotionType.BY_X_PAY_Y && (promo.buyQuantity ?: 0) > 0
                }
                .minByOrNull { promo -> promo.value }

            if (buyPayPromo != null) {
                val buy = buyPayPromo.buyQuantity ?: 1
                val pay = buyPayPromo.value.toInt().coerceIn(0, buy)
                ProductPromotion.BuyXPayY(
                    buy = buy,
                    pay = pay,
                    label = "${buy}x$pay"
                )
            } else null
        }
    }
}