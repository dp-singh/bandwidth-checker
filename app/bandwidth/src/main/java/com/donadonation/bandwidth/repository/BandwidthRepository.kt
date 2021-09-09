package com.donadonation.bandwidth.repository

import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.data.Mapping
import com.donadonation.bandwidth.local.Report
import kotlinx.coroutines.flow.Flow

interface BandwidthRepository {

    suspend fun startSampling(
        duration: Long?,
        interval: Long?
    ): Flow<Pair<Result<Long>, Result<Long>>>

    suspend fun downloadReport(
        duration: Long?,
        interval: Long?,
        startTime: Long
    ): Flow<Result<Long>>

    suspend fun uploadReport(duration: Long?, interval: Long?, startTime: Long): Flow<Result<Long>>

    suspend fun saveReport(report: Report): Long

    suspend fun getReport(): List<Report>

    suspend fun getChartData(report: List<Report>): List<DataEntry>

    suspend fun deleteOldData(currentTimeStamp: Long): Int
}