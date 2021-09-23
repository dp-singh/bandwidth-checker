package com.donadonation.bandwidth.repository

import com.anychart.chart.common.dataentry.DataEntry
import com.donadonation.bandwidth.entites.Resource
import com.donadonation.bandwidth.local.Report
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import java.time.Duration

interface BandwidthRepository {

    suspend fun downloadReport(
        duration: Long?,
        interval: Long?,
        startTime: Long
    ): Result<Report>

    suspend fun uploadReport(
        duration: Long?,
        interval: Long?,
        startTime: Long
    ): Result<Report>

    suspend fun startSamplingAsFlow(
        duration: Long?,
        interval: Long?
    ): Flow<Pair<Result<Report>, Result<Report>>>

    suspend fun downloadReportAsFlow(
        duration: Long?,
        interval: Long?,
        startTime: Long
    ): Flow<Result<Report>>

    suspend fun uploadReportAsFlow(duration: Long?, interval: Long?, startTime: Long): Flow<Result<Report>>

    fun getLiveReport(interval: Long): Flow<Resource<Pair<Result<Report>, Result<Report>>>>

    suspend fun saveReport(report: Report): Long

    suspend fun getReport(currentTimeStamp: Long): List<Report>

    suspend fun getLastEntryTime(): Long?

    suspend fun shouldSave(lastSavedTimeStamp: Long, latestTimestamp: Long): Boolean

    suspend fun getChartData(report: List<Report>): List<DataEntry>

    suspend fun deleteOldData(currentTimeStamp: Long): Int
}