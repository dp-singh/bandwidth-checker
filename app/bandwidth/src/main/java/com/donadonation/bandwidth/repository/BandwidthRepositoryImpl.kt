package com.donadonation.bandwidth.repository

import android.util.Log
import com.anychart.chart.common.dataentry.DataEntry
import com.donadonation.bandwidth.entites.DisplayData
import com.donadonation.bandwidth.entites.LineData
import com.donadonation.bandwidth.extension.*
import com.donadonation.bandwidth.local.BandwidthDao
import com.donadonation.bandwidth.local.Report
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class BandwidthRepositoryImpl constructor(
    private val bandwidthDao: BandwidthDao,
    private val mapper: Transform
) : BandwidthRepository {

    @ExperimentalCoroutinesApi
    override suspend fun startSampling(
        duration: Long?,
        interval: Long?
    ): Flow<Pair<Result<Report>, Result<Report>>> {
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
    ): Flow<Result<Report>> {
        return callbackFlow<Result<Report>> {
            val speedTestSocket = SpeedTestSocket()
            val listener = object : ISpeedTestListener {
                override fun onCompletion(report: SpeedTestReport?) {
                    report?.apply {
                        trySend(Result.success(mapper.map(this, startTime)))
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
    ): Flow<Result<Report>> {
        return callbackFlow<Result<Report>> {
            val speedTestSocket = SpeedTestSocket()
            val listener = object : ISpeedTestListener {
                override fun onCompletion(report: SpeedTestReport?) {
                    report?.apply {
                        trySend(Result.success(mapper.map(this,startTime)))
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


    @ExperimentalCoroutinesApi
    override fun getLiveReport(interval: Long): Flow<Pair<Result<Report>, Result<Report>>> {
        return interval
            .tickerFlow(10000)
            .flatMapLatest { startSampling(0, 0) }
            .onStart { Log.d("TAG", "Live on start") }
            .flowOn(Dispatchers.IO)
    }


    override suspend fun saveReport(report: Report): Long {
        return bandwidthDao.insertReport(report)
    }

    override suspend fun getReport(): List<Report> {
        return bandwidthDao.getAllEntries()
    }

    override suspend fun getChartData(report: List<Report>): List<DataEntry> {
        val downloadData = collectList(report, true)
        val uploadData = collectList(report, false)
        val dataEntryList: List<DataEntry> = xAxisEntries(report)
            .map {
                val downloadBitRate =
                    downloadData.find { entry -> entry.timestamp == it }?.bitRate.orZero()
                val uploadBitRate =
                    uploadData.find { entry -> entry.timestamp == it }?.bitRate.orZero()
                LineData(
                    Date(it).formatToViewTimeDefaults(),
                    downloadBitRate,
                    uploadBitRate
                )
            }
        return dataEntryList
    }

    override suspend fun deleteOldData(currentTimeStamp: Long): Int {
        val lastWeekTimeStamp = Date(currentTimeStamp).lastXDays(7)
        return lastWeekTimeStamp?.let {
            bandwidthDao.deleteByTimeStamp(it)
        } ?: 0
    }

    private fun xAxisEntries(report: List<Report>): List<Long> =
        report.distinctBy { it.startTime }
            .toList()
            .sortedBy { it.startTime }
            .map { it.startTime }


    private fun collectList(report: List<Report>, isDownload: Boolean): List<DisplayData> =
        report
            .filter { item -> item.isDownload == isDownload }
            .map {
                DisplayData(
                    it.startTime,
                    it.bitrate.toMbps()
                )
            }.toList()

}