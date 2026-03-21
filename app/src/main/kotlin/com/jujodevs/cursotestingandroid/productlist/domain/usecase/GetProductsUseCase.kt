package com.jujodevs.cursotestingandroid.productlist.domain.usecase

import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.jujodevs.cursotestingandroid.productlist.domain.model.Promotion
import com.jujodevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.jujodevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val promotionRepository: PromotionRepository,
    private val getPromotionForProduct: GetPromotionForProduct,
) {
    operator fun invoke(): Flow<List<ProductWithPromotion>> {
        return combine(
            productRepository.getProducts(),
            promotionRepository.getActivePromotions(),
        ) { products, promotions ->
            val promotions = promotions.getActivePromotions().getPromotionsByProductId()

            products.map { product ->
                val promotion = getPromotionForProduct(product, promotions)
                ProductWithPromotion(product = product, promotion = promotion)
            }
        }
    }

    private fun List<Promotion>.getActivePromotions(): List<Promotion> {
        val now = Instant.now()
        return this.filter { promotion ->
            promotion.startTime <= now && promotion.endTime >= now
        }
    }

    private fun List<Promotion>.getPromotionsByProductId(): Map<String, List<Promotion>> {
        val mapPromotion = mutableMapOf<String, List<Promotion>>()
        forEach { promotion ->
            promotion.productsIds.forEach { productId ->
                mapPromotion[productId] =
                    mapPromotion.getOrDefault(productId, emptyList()) + promotion
            }
        }
        return mapPromotion
    }
}