package com.donadonation.bandwidth.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.donadonation.bandwidth.repository.BandwidthRepository
import kotlinx.coroutines.flow.last
import java.util.*

class BandwidthWorker(
    context: Context,
    private val repository: BandwidthRepository,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val deleteCount = repository.deleteOldData(getCurrentTime())
        Log.d("WORKER", "delete count " + deleteCount)
        val res = repository.startSampling(null, null)
            .last()
        return Result.success()
    }

    private fun getCurrentTime(timeZone: TimeZone = TimeZone.getDefault()): Long {
        return Calendar.getInstance(timeZone).timeInMillis
    }


}