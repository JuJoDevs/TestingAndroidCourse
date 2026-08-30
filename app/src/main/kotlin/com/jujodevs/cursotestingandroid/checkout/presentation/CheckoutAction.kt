package com.jujodevs.cursotestingandroid.checkout.presentation

sealed interface CheckoutAction {
    data object GoBack : CheckoutAction
    data object Retry : CheckoutAction
    data class ChangeName(val name: String) : CheckoutAction
    data class ChangeEmail(val email: String) : CheckoutAction
    data class ChangeAddress(val address: String) : CheckoutAction
    data object Confirm : CheckoutAction
}
