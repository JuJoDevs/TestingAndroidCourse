package com.jujodevs.cursotestingandroid.core.domain.model

@Suppress("ObjectInheritsException",
    "JavaIoSerializableObjectMustHaveReadResolve"
)
sealed class AppError: Exception() {

    data object NetworkError: AppError()
    data object NotFoundError: AppError()
    data class ValidationError(override val message: String): AppError()
    data class UnknownError(override val message: String?): AppError()
}