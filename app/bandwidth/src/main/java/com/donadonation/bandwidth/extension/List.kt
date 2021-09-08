package com.donadonation.bandwidth.extension

fun <T> List<T>?.second(): T? {
    return this?.let {
        if (it.size > 1) {
            return it[1]
        } else {
            null
        }
    }
}