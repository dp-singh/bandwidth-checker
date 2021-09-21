package com.donadonation.bandwidth.extension

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

fun Float?.orZero() = this ?: 0F

fun Float?.round(): String {
    val df = DecimalFormat("#.##", DecimalFormatSymbols(Locale.ENGLISH))
    df.roundingMode = RoundingMode.CEILING
    return df.format(this)
}