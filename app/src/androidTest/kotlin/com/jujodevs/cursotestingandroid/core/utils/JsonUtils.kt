package com.jujodevs.cursotestingandroid.core.utils

import androidx.test.platform.app.InstrumentationRegistry
import com.jujodevs.cursotestingandroid.core.utils.JsonUtils.readJson

object JsonUtils {
    fun readJson(fileName: String): String {
        val context = InstrumentationRegistry.getInstrumentation().context
        return context.assets
            .open(fileName)
            .bufferedReader()
            .use { it.readText() }
    }
}

fun String.asAsset(): String = readJson(this)
