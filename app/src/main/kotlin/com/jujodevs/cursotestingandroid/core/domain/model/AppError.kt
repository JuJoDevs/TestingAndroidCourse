package com.jujodevs.cursotestingandroid.core.domain.model

@Suppress("ObjectInheritsException",
    "JavaIoSerializableObjectMustHaveReadResolve"
)
sealed class AppError: Exception() {

    data object NetworkError: AppError()
    data object NotFoundError: AppError()
    sealed class Validation: AppError() {
        data object QuantityMustBePositive: Validation()
        data class InsufficientStock(val available: Int): Validation()
    }
    data class ValidationError(override val message: String): AppError()
    data class UnknownError(override val message: String?): AppError()
}