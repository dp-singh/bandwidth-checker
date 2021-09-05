package com.donadonation.bandwidth.repository

import com.donadonation.bandwidth.local.BandwidthDao
import com.donadonation.bandwidth.local.Report
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
}