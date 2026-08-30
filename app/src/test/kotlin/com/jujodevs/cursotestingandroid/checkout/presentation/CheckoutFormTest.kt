package com.jujodevs.cursotestingandroid.checkout.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * EXAMEN — Tests UNITARIOS de la validación del formulario de checkout.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [CheckoutForm.validate], [CheckoutFormErrors.isValid], [FieldError].
 */
class CheckoutFormTest {
    @Test
    fun `given blank name when validate then nameError is REQUIRED`() {
        val form =
            CheckoutForm(
                name = "",
                address = "Calle Mayor 1",
                email = "test@example.com",
            )

        val errors = form.validate()

        assertEquals(FieldError.REQUIRED, errors.nameError)
    }

    @Test
    fun `given blank address when validate then addressError is REQUIRED`() {
        val form =
            CheckoutForm(
                name = "Juan",
                address = "",
                email = "test@example.com",
            )

        val errors = form.validate()

        assertEquals(FieldError.REQUIRED, errors.addressError)
    }

    @Test
    fun `given blank email when validate then emailError is REQUIRED`() {
        val form =
            CheckoutForm(
                name = "Juan",
                address = "Calle Mayor 1",
                email = "",
            )

        val errors = form.validate()

        assertEquals(FieldError.REQUIRED, errors.emailError)
    }

    @Test
    fun `given malformed email when validate then emailError is INVALID_EMAIL`() {
        val form =
            CheckoutForm(
                name = "Juan",
                address = "Calle Mayor 1",
                email = "invalid-email",
            )

        val errors = form.validate()

        assertEquals(FieldError.INVALID_EMAIL, errors.emailError)
    }

    @Test
    fun `given all fields valid when validate then errors isValid is true`() {
        val form =
            CheckoutForm(
                name = "Juan",
                address = "Calle Mayor 1",
                email = "test@example.com",
            )

        val errors = form.validate()

        assertTrue(errors.isValid)
    }
}
