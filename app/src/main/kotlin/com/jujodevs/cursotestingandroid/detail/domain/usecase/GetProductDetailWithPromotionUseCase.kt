package com.jujodevs.cursotestingandroid.detail.domain.usecase

import com.jujodevs.cursotestingandroid.cart.domain.ex.activeAt
import com.jujodevs.cursotestingandroid.core.domain.time.Clock
import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.jujodevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.jujodevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GroupPromotionsByProductId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import javax.inject.Inject

class GetProductDetailWithPromotionUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val promotionRepository: PromotionRepository,
    private val groupPromotionsByProductId: GroupPromotionsByProductId,
    private val getPromotionForProduct: GetPromotionForProduct,
    private val clock: Clock,
) {

    operator fun invoke(productId: String): Flow<ProductWithPromotion?> {
        return combine(
            productRepository.getProductById(productId),
            promotionRepository.getActivePromotions(),
        ) { product, promotions ->
            val now = clock.now()
            val activePromotions = groupPromotionsByProductId(promotions.activeAt(now))

            product?.let {
                val finalPromotion = getPromotionForProduct(product, activePromotions)
                ProductWithPromotion(product = product, promotion = finalPromotion)
            }
        }
    }
}