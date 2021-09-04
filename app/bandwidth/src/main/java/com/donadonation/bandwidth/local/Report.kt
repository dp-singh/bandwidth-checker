package com.donadonation.bandwidth.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = METRIC_TABLE)
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val startTime: Long,
    val endTime: Long,
    val packetSize: Long,
    val bitrate: BigDecimal,
    val isDownload: Boolean
)