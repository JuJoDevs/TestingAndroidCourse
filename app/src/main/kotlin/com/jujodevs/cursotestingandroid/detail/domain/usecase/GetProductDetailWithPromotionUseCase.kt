package com.jujodevs.cursotestingandroid.detail.domain.usecase

import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.jujodevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.jujodevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import javax.inject.Inject

class GetProductDetailWithPromotionUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val promotionRepository: PromotionRepository,
    private val getPromotionForProduct: GetPromotionForProduct,
) {

    operator fun invoke(productId: String): Flow<ProductWithPromotion?> {
        return combine(
            productRepository.getProductById(productId),
            promotionRepository.getActivePromotions(),
        ) { product, promotions ->
            val now = Instant.now()
            val activePromotions = promotions.filter {
                it.startTime <= now && it.endTime >= now && it.productsIds.contains(productId)
            }

            product?.let {
                val finalPromotion = getPromotionForProduct(product, mapOf(productId to activePromotions))
                ProductWithPromotion(product = product, promotion = finalPromotion)
            }
        }
    }
}