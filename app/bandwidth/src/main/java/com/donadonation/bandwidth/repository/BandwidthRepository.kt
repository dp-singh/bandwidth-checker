package com.donadonation.bandwidth.repository

import com.donadonation.bandwidth.local.Report
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import fr.bmartel.speedtest.SpeedTestReport
import kotlinx.coroutines.flow.Flow

interface BandwidthRepository {

    suspend fun startSampling(duration: Long?, interval: Long?): Flow<Pair<Result<Long>,Result<Long>>>

    suspend fun downloadReport(duration: Long?, interval: Long?,startTime:Long): Flow<Result<Long>>

    suspend fun uploadReport(duration: Long?, interval: Long?,startTime:Long): Flow<Result<Long>>

    suspend fun saveReport(report: Report): Long

    suspend fun getReport(): List<Report>

    suspend fun getYAxisValue(report: List<Report>): LineData

    suspend fun getXAxisValue(report: List<Report>): List<String>
}