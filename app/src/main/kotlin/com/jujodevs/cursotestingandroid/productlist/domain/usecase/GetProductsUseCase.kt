package com.jujodevs.cursotestingandroid.productlist.domain.usecase

import com.jujodevs.cursotestingandroid.cart.domain.ex.activeAt
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
    operator fun invoke(): Flow<List<ProductWithPromotion>> {
        return combine(
            productRepository.getProducts(),
            promotionRepository.getActivePromotions(),
            settingsRepository.inStockOnly,
        ) { products, promotions, inStockOnly ->
            val now = Instant.now()
            val promotions = groupPromotionsByProductId(promotions.activeAt(now))

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
}