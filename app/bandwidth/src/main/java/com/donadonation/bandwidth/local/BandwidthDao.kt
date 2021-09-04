package com.donadonation.bandwidth.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

const val METRIC_TABLE : String = "metric"
@Dao
interface BandwidthDao {

    @Query("SELECT * FROM $METRIC_TABLE")
    fun getAllEntries(): List<Report>

    @Insert(onConflict = REPLACE)
    fun insertReport(report: Report): Long
}