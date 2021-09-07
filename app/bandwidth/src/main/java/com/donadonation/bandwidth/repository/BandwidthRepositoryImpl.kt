package com.donadonation.bandwidth.repository

import android.graphics.Color
import com.donadonation.bandwidth.extension.formatToViewTimeDefaults
import com.donadonation.bandwidth.extension.toMbps
import com.donadonation.bandwidth.local.BandwidthDao
import com.donadonation.bandwidth.local.Report
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.zip
import java.util.*

class BandwidthRepositoryImpl constructor(
    private val bandwidthDao: BandwidthDao,
    private val mapper: Transform
) : BandwidthRepository {

    @ExperimentalCoroutinesApi
    override suspend fun startSampling(
        duration: Long?,
        interval: Long?
    ): Flow<Pair<Result<Long>, Result<Long>>> {
        val startTime = System.currentTimeMillis()
        return downloadReport(duration, interval, startTime).zip(
            uploadReport(
                duration,
                interval,
                startTime
            )
        ) { downloadReport, uploadReport ->
            Pair(downloadReport, uploadReport)
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun downloadReport(
        duration: Long?,
        interval: Long?,
        startTime:Long
    ): Flow<Result<Long>> {
        return callbackFlow<Result<Long>> {
            val speedTestSocket = SpeedTestSocket()
            val listener = object : ISpeedTestListener {
                override fun onCompletion(report: SpeedTestReport?) {
                    report?.apply {
                        val value = bandwidthDao.insertReport(mapper.map(this, startTime))
                        trySend(Result.success(value))
                    } ?: kotlin.run {
                        trySend((Result.failure(Exception(SpeedTestError.CONNECTION_ERROR.name))))
                    }
                }

                override fun onProgress(percent: Float, report: SpeedTestReport?) {

                }

                override fun onError(speedTestError: SpeedTestError?, errorMessage: String?) {
                    trySend((Result.failure(Exception(speedTestError?.name))))
                }
            }
            speedTestSocket.addSpeedTestListener(listener)
            duration?.let {
                speedTestSocket.downloadSetupTime = it
            }
            speedTestSocket.startDownload("http://ipv4.ikoula.testdebit.info/1M.iso")
            awaitClose { speedTestSocket.removeSpeedTestListener(listener) }
        }.flowOn(Dispatchers.IO)

    }

    @ExperimentalCoroutinesApi
    override suspend fun uploadReport(
        duration: Long?,
        interval: Long?,
        startTime:Long
    ): Flow<Result<Long>> {
        return callbackFlow<Result<Long>> {
            val speedTestSocket = SpeedTestSocket()
            val listener = object : ISpeedTestListener {
                override fun onCompletion(report: SpeedTestReport?) {
                    report?.apply {
                        val value = bandwidthDao.insertReport(mapper.map(this,startTime))
                        trySend(Result.success(value))
                    } ?: kotlin.run {
                        trySend((Result.failure(Exception(SpeedTestError.CONNECTION_ERROR.name))))
                    }
                }

                override fun onProgress(percent: Float, report: SpeedTestReport?) {

                }

                override fun onError(speedTestError: SpeedTestError?, errorMessage: String?) {
                    trySend((Result.failure(Exception(speedTestError?.name))))
                }
            }
            speedTestSocket.addSpeedTestListener(listener)
            duration?.let {
                speedTestSocket.uploadSetupTime = it
            }
            speedTestSocket.startUpload("http://ipv4.ikoula.testdebit.info/", 1000000)
            awaitClose { speedTestSocket.removeSpeedTestListener(listener) }
        }.flowOn(Dispatchers.IO)
    }


    override suspend fun saveReport(report: Report): Long {
        return bandwidthDao.insertReport(report)
    }

    override suspend fun getReport(): List<Report> {
        return bandwidthDao.getAllEntries()
    }

    override suspend fun getYAxisValue(report: List<Report>): LineData {
        val downloadReport: List<Entry> = collectList(report, true)
        val uploadReport: List<Entry> = collectList(report, false)
        val downloadLine = LineDataSet(downloadReport, "Download")
        downloadLine.axisDependency = YAxis.AxisDependency.LEFT
        val uploadLine = LineDataSet(uploadReport, "Upload")
        uploadLine.axisDependency = YAxis.AxisDependency.LEFT
        uploadLine.color = Color.RED
        val finalReport = mutableListOf<ILineDataSet>()
        finalReport.add(downloadLine)
        finalReport.add(uploadLine)
        return LineData(finalReport)
    }

    private fun collectList(report: List<Report>, isDownload: Boolean) = report
        .sortedBy { it.startTime }
        .filter { item -> item.isDownload == isDownload }
        .map {
            Entry(
                it.startTime.toFloat(),
                it.bitrate.toMbps()
            )
        }.toList()

    override suspend fun getXAxisValue(report: List<Report>): List<String> {
        return report
            .sortedBy { it.startTime }
            .map {
                Date(it.startTime).formatToViewTimeDefaults()
            }
    }
}