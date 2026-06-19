package com.jujodevs.cursotestingandroid.core

fun List<String>.toListString(): String =
    """[${
        this.joinToString(
            separator = ", ",
            prefix = """"""",
            postfix = """"""",
        )
    }]"""
