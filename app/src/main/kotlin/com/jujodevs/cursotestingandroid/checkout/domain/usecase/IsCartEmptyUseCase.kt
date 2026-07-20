package com.jujodevs.cursotestingandroid.checkout.domain.usecase

import com.jujodevs.cursotestingandroid.cart.domain.repository.CartRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class IsCartEmptyUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(): Boolean = cartRepository.getCartItems().firstOrNull()?.isEmpty() ?: false
}
