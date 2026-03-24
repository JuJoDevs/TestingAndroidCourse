package com.jujodevs.cursotestingandroid.cart.domain.usecase

import com.jujodevs.cursotestingandroid.cart.domain.model.CartItem
import com.jujodevs.cursotestingandroid.cart.domain.model.CartSummary
import com.jujodevs.cursotestingandroid.cart.domain.repository.CartRepository
import com.jujodevs.cursotestingandroid.productlist.domain.model.Product
import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.jujodevs.cursotestingandroid.productlist.domain.model.Promotion
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

class GetCartSummaryUseCase @Inject constructor(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
    private val promotionRepository: PromotionRepository,
    private val groupPromotionsByProductId: GroupPromotionsByProductId,
    private val getPromotionForProduct: GetPromotionForProduct,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<CartSummary> {
        return cartRepository.getCartItems()
            .flatMapLatest { cartItems ->
                val ids = cartItems.mapTo(mutableSetOf()) { it.productId }
                if (ids.isEmpty()) {
                    flowOf(
                        CartSummary(
                            0.0,
                            0.0,
                            0.0
                        )
                    )
                } else {
                    combine(
                        productRepository.getProductsById(ids),
                        promotionRepository.getActivePromotions()
                    ) { products, promotions ->
                        calculateSummary(
                            cartItems,
                            products,
                            promotions
                        )
                    }
                }
            }
    }

    private fun calculateSummary(
        cartItems: List<CartItem>,
        products: List<Product>,
        promotions: List<Promotion>,
    ): CartSummary {
        val now = Instant.now()
        val activePromotions = groupPromotionsByProductId(promotions.filter {
            it.startTime <= now && it.endTime >= now
        })
        val productsById = products.associateBy { it.id }
        var subtotal = 0.0
        var discountTotal = 0.0

        for (cartItem in cartItems) {
            val product = productsById[cartItem.productId] ?: continue
            val itemTotal = product.price * cartItem.quantity
            subtotal += itemTotal

            discountTotal += calculateDiscountForProduct(
                product = product,
                quantity = cartItem.quantity,
                activePromotions = activePromotions,
            )


        }
        val finalTotal = (subtotal - discountTotal).coerceAtLeast(0.0)

        return CartSummary(
            subtotal = subtotal,
            discountTotal = discountTotal,
            finalTotal = finalTotal
        )
    }

    private fun calculateDiscountForProduct(
        product: Product,
        quantity: Int,
        activePromotions: Map<String, List<Promotion>>,
    ): Double {
        return when (val selectPromotion = getPromotionForProduct(
            product = product,
            promotions = activePromotions
        )) {
            is ProductPromotion.BuyXPayY -> {
                val buy = selectPromotion.buy
                val pay = selectPromotion.pay
                val freePerGroup = (buy - pay).coerceAtLeast(0)
                val groups = quantity / buy
                val freeItems = freePerGroup * groups
                product.price * freeItems
            }

            is ProductPromotion.Percent -> {
                val itemSubTotal = product.price * quantity
                itemSubTotal * (selectPromotion.percent / 100)
            }

            null -> 0.0
        }
    }
}