package com.jujodevs.cursotestingandroid.core.domain

import kotlinx.coroutines.CancellationException

inline fun <T, R> T.safeRunCatching(block: T.() -> R): Result<R> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
