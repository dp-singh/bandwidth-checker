package com.donadonation.bandwidth.extension

import java.text.SimpleDateFormat
import java.util.*

fun Date.formatToViewTimeDefaults(): String{
    val sdf= SimpleDateFormat("hh:mm aa", Locale.getDefault())
    return sdf.format(this)
}

fun Date.lastXDays(day: Int, zone: TimeZone = TimeZone.getDefault()): Long? {
    val calendar = Calendar.getInstance(zone)
    calendar.timeInMillis = this.time
    calendar.add(Calendar.DATE, -day)
    return calendar.time.time
}