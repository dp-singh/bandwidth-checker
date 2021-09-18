package com.donadonation.bandwidth.repository

import com.anychart.chart.common.dataentry.DataEntry
import com.donadonation.bandwidth.local.Report
import kotlinx.coroutines.flow.Flow
import java.time.Duration

interface BandwidthRepository {

    suspend fun startSampling(
        duration: Long?,
        interval: Long?
    ): Flow<Pair<Result<Report>, Result<Report>>>

    suspend fun downloadReport(
        duration: Long?,
        interval: Long?,
        startTime: Long
    ): Flow<Result<Report>>

    suspend fun uploadReport(duration: Long?, interval: Long?, startTime: Long): Flow<Result<Report>>

    fun getLiveReport(interval: Long): Flow<Pair<Result<Report>, Result<Report>>>

    suspend fun saveReport(report: Report): Long

    suspend fun getReport(): List<Report>

    suspend fun getChartData(report: List<Report>): List<DataEntry>

    suspend fun deleteOldData(currentTimeStamp: Long): Int
}