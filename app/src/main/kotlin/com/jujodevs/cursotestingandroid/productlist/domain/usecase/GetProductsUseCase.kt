package com.jujodevs.cursotestingandroid.productlist.domain.usecase

import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.jujodevs.cursotestingandroid.productlist.domain.model.Promotion
import com.jujodevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.jujodevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.jujodevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val promotionRepository: PromotionRepository,
    private val getPromotionForProduct: GetPromotionForProduct,
    private val groupPromotionsByProductId: GroupPromotionsByProductId,
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(ids: Set<String> = emptySet()): Flow<List<ProductWithPromotion>> {
        return combine(
            if (ids.isEmpty()) productRepository.getProducts()
            else productRepository.getProductsById(ids),
            promotionRepository.getActivePromotions(),
            settingsRepository.inStockOnly,
        ) { products, promotions, inStockOnly ->
            val promotions = groupPromotionsByProductId(promotions.getActivePromotions())

            val filteredProducts = if (inStockOnly) {
                products.filter { it.stock > 0 }
            } else {
                products
            }


            filteredProducts.map { product ->
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
}