package com.donadonation.bandwidth.repository

import com.anychart.chart.common.dataentry.DataEntry
import com.donadonation.bandwidth.entites.DisplayData
import com.donadonation.bandwidth.entites.LineData
import com.donadonation.bandwidth.entites.Resource
import com.donadonation.bandwidth.extension.*
import com.donadonation.bandwidth.local.BandwidthDao
import com.donadonation.bandwidth.local.Report
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.*

class BandwidthRepositoryImpl constructor(
    private val bandwidthDao: BandwidthDao,
    private val mapper: Transform
) : BandwidthRepository {

    @ExperimentalCoroutinesApi
    override suspend fun downloadReport(
        duration: Long?,
        interval: Long?,
        startTime: Long
    ): Result<Report> {
       return withContext(Dispatchers.IO){
            suspendCancellableCoroutine {cont->
                val speedTestSocket = SpeedTestSocket()
                val listener = object : ISpeedTestListener {
                    override fun onCompletion(report: SpeedTestReport?) {
                        report?.apply {
                            cont.resume(Result.success(mapper.map(this, startTime))){

                            }
                        } ?: kotlin.run {
                            cont.resume(Result.failure(Exception(SpeedTestError.CONNECTION_ERROR.name))){}
                        }
                    }

                    override fun onProgress(percent: Float, report: SpeedTestReport?) {

                    }

                    override fun onError(speedTestError: SpeedTestError?, errorMessage: String?) {
                        cont.resume(Result.failure(Exception(speedTestError?.name))){}
                    }
                }
                speedTestSocket.addSpeedTestListener(listener)
                duration?.let {
                    speedTestSocket.downloadSetupTime = it
                }
                speedTestSocket.startDownload("http://ipv4.ikoula.testdebit.info/1M.iso")
            }
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun uploadReport(
        duration: Long?,
        interval: Long?,
        startTime: Long
    ): Result<Report> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { cont ->
                val speedTestSocket = SpeedTestSocket()
                val listener = object : ISpeedTestListener {
                    override fun onCompletion(report: SpeedTestReport?) {
                        report?.apply {
                            cont.resume(Result.success(mapper.map(this, startTime))){}
                        } ?: kotlin.run {
                            cont.resume((Result.failure(Exception(SpeedTestError.CONNECTION_ERROR.name)))){}
                        }
                    }

                    override fun onProgress(percent: Float, report: SpeedTestReport?) {

                    }

                    override fun onError(speedTestError: SpeedTestError?, errorMessage: String?) {
                        cont.resume((Result.failure(Exception(speedTestError?.name)))) {}
                    }
                }
                speedTestSocket.addSpeedTestListener(listener)
                duration?.let {
                    speedTestSocket.uploadSetupTime = it
                }
                speedTestSocket.startUpload("http://ipv4.ikoula.testdebit.info/", 1000000)
            }
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun startSamplingAsFlow(
        duration: Long?,
        interval: Long?
    ): Flow<Pair<Result<Report>, Result<Report>>> {
        val startTime = System.currentTimeMillis()
        return downloadReportAsFlow(duration, interval, startTime).zip(
            uploadReportAsFlow(
                duration,
                interval,
                startTime
            )
        ) { downloadReport, uploadReport ->
            Pair(downloadReport, uploadReport)
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun downloadReportAsFlow(
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
    override suspend fun uploadReportAsFlow(
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
    override fun getLiveReport(interval: Long): Flow<Resource<Pair<Result<Report>, Result<Report>>>> {
        return interval
            .tickerFlow(10000)
            .flatMapLatest { res ->
                flow {
                    emit(Resource.Loading<Pair<Result<Report>, Result<Report>>>())
                    emitAll(
                        startSamplingAsFlow(0, 0)
                        .map { Resource.Success(it) })
                }
            }
            .flowOn(Dispatchers.IO)
    }


    override suspend fun saveReport(report: Report): Long {
        return bandwidthDao.insertReport(report)
    }

    override suspend fun getReport(currentTimeStamp: Long): List<Report> {
        return bandwidthDao.getAllEntries()
            .filter { greaterThanDay(it.startTime, currentTimeStamp).not() }
    }


    override suspend fun getLastEntryTime(): Long? {
        return bandwidthDao.getLastEntryInTable()
    }

    override suspend fun shouldSave(lastSavedTimeStamp: Long, latestTimestamp: Long): Boolean {
        val diff = latestTimestamp - lastSavedTimeStamp
        val diffMinutes: Long = diff / (60 * 1000)
        return (diffMinutes >= 30)
    }

    override suspend fun getChartData(report: List<Report>): List<DataEntry> {
        return withContext(Dispatchers.IO) {
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
            dataEntryList
        }
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

    private fun greaterThanDay(lastSavedTimeStamp: Long, latestTimestamp: Long): Boolean {
        val diff = latestTimestamp - lastSavedTimeStamp
        val diffDays: Long = diff / (24 * 60 * 60 * 1000)
        return diffDays > 0
    }


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