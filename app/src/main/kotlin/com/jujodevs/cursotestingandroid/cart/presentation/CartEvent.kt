package com.jujodevs.cursotestingandroid.cart.presentation

sealed interface CartEvent {
    data object GoBack : CartEvent

    data class ShowMessage(
        val message: String,
    ) : CartEvent
}
