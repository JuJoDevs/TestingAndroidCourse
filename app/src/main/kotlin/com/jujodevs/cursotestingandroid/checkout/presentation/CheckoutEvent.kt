package com.jujodevs.cursotestingandroid.checkout.presentation

sealed interface CheckoutEvent {
    data class ShowMessage(
        val message: String,
    ) : CheckoutEvent

    data object GoBack : CheckoutEvent
}
