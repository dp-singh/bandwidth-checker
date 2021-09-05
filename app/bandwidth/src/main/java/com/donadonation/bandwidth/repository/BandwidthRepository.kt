package com.donadonation.bandwidth.repository

import com.donadonation.bandwidth.local.Report
import fr.bmartel.speedtest.SpeedTestReport
import kotlinx.coroutines.flow.Flow

interface BandwidthRepository {

    suspend fun startSampling(duration: Long?, interval: Long?): Flow<Pair<Result<Long>,Result<Long>>>

    suspend fun downloadReport(duration: Long?, interval: Long?,startTime:Long): Flow<Result<Long>>

    suspend fun uploadReport(duration: Long?, interval: Long?,startTime:Long): Flow<Result<Long>>

    suspend fun saveReport(report: Report): Long

    suspend fun getReport(): List<Report>
}