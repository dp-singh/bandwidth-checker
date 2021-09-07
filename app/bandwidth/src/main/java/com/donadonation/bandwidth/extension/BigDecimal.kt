package com.donadonation.bandwidth.extension

import java.math.BigDecimal

fun BigDecimal.toMbps(): Float = this.divide(BigDecimal(1_000_000)).toFloat()