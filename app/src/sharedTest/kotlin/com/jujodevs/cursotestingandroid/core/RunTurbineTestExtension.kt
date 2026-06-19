package com.jujodevs.cursotestingandroid.core

import app.cash.turbine.TurbineContext
import app.cash.turbine.turbineScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration

fun runTurbineTest(
    context: CoroutineContext = EmptyCoroutineContext,
    timeout: Duration? = null,
    validate: suspend TurbineContext.() -> Unit,
) {
    runTest(context) {
        turbineScope(timeout, validate)
    }
}
