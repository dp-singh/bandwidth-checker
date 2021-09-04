package com.donadonation.bandwidth.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.donadonation.bandwidth.repository.BandwidthRepository
import kotlinx.coroutines.flow.last

class BandwidthWorker(
    context: Context,
    private val repository: BandwidthRepository,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val res = repository.startSampling(null, null)
            .last()
        return Result.success()
    }


}