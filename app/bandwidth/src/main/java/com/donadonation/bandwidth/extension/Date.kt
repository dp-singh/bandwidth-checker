package com.donadonation.bandwidth.extension

import java.text.SimpleDateFormat
import java.util.*

fun Date.formatToViewTimeDefaults(): String{
    val sdf= SimpleDateFormat("hh:mm aa", Locale.getDefault())
    return sdf.format(this)
}