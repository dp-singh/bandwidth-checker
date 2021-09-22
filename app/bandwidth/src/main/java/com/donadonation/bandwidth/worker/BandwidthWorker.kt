package com.donadonation.bandwidth.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.donadonation.bandwidth.repository.BandwidthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.withContext
import java.util.*

class BandwidthWorker(
    context: Context,
    private val repository: BandwidthRepository,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            repository.deleteOldData(getCurrentTime())
            val startTime = System.currentTimeMillis()
            val downloadReport = repository.downloadReport(0, 0, startTime)
            val uploadReport = repository.uploadReport(0, 0, startTime)
            if (downloadReport.isSuccess) {
                downloadReport.getOrNull()?.let { repository.saveReport(it) }
            }
            if (uploadReport.isSuccess) {
                uploadReport.getOrNull()?.let { repository.saveReport(it) }
            }
            Result.success()
        }
    }

    private fun getCurrentTime(timeZone: TimeZone = TimeZone.getDefault()): Long {
        return Calendar.getInstance(timeZone).timeInMillis
    }


}