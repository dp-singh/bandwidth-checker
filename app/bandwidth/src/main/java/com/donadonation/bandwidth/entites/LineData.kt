package com.donadonation.bandwidth.entites

import com.anychart.chart.common.dataentry.ValueDataEntry

const val VALUE_2 = "value2"

data class LineData(
    val xAxis: String,
    val y1: Number,
    val y2: Number
) : ValueDataEntry(xAxis, y1) {
    init {
        setValue(VALUE_2, y2)
    }
}

data class DisplayData(
    val timestamp: Long,
    val bitRate: Float
)