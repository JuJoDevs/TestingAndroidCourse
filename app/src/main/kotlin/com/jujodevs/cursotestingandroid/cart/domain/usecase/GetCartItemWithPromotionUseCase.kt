package com.jujodevs.cursotestingandroid.cart.domain.usecase

import com.jujodevs.cursotestingandroid.cart.domain.ex.activeAt
import com.jujodevs.cursotestingandroid.cart.domain.repository.CartRepository
import com.jujodevs.cursotestingandroid.cart.presentation.model.CartItemWithPromotion
import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.jujodevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.jujodevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GroupPromotionsByProductId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import javax.inject.Inject

class GetCartItemWithPromotionUseCase @Inject constructor(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
    private val promotionRepository: PromotionRepository,
    private val groupPromotionsByProductId: GroupPromotionsByProductId,
    private val getPromotionForProduct: GetPromotionForProduct,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<CartItemWithPromotion>> {
        return cartRepository.getCartItems().flatMapLatest { cartItems ->
            val ids = cartItems.mapTo(mutableSetOf()) { it.productId }

            if (ids.isEmpty()) {
                flowOf(emptyList())
            }
            else {
                combine(
                    productRepository.getProductsById(ids),
                    promotionRepository.getActivePromotions()
                ) { products, promotions ->
                    val now = Instant.now()
                    val activePromotions = groupPromotionsByProductId(promotions.activeAt(now))
                    val productsById = products.associateBy { it.id }
                    cartItems.mapNotNull { cartItem ->
                        val product = productsById[cartItem.productId] ?: return@mapNotNull null
                        val promotion = getPromotionForProduct(product, activePromotions)
                        val productWithPromotion = ProductWithPromotion(
                            product = product,
                            promotion = promotion,
                        )
                        CartItemWithPromotion(
                            cartItem = cartItem,
                            item = productWithPromotion,
                        )
                    }
                }
            }
        }
    }
}