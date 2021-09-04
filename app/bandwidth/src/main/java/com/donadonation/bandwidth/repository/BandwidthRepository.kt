package com.donadonation.bandwidth.repository

import com.donadonation.bandwidth.local.Report
import fr.bmartel.speedtest.SpeedTestReport
import kotlinx.coroutines.flow.Flow

interface BandwidthRepository {

    suspend fun startSampling(duration: Long?, interval: Long?): Flow<Result<Long>>

    suspend fun saveReport(report: Report): Long

    suspend fun getReport(): List<Report>
}