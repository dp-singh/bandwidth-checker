package com.donadonation.bandwidth.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.donadonation.bandwidth.R
import com.donadonation.bandwidth.databinding.ActivityBandwidthBinding
import com.donadonation.bandwidth.di.appModule
import com.donadonation.bandwidth.di.dbModule
import com.donadonation.bandwidth.di.repositoryModule
import com.donadonation.bandwidth.di.workerModule
import com.donadonation.bandwidth.worker.BandwidthWorker
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class BandwidthActivity : AppCompatActivity() {

    private val bandwidthViewModel: BandwidthViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject(savedInstanceState)
        initUI(savedInstanceState)
        startWorker()
    }

    private fun inject(savedInstanceState: Bundle?) {
        savedInstanceState
            .takeIf { savedInstanceState == null }
            .let {
                startKoin {
                    androidLogger()
                    androidContext(this@BandwidthActivity.applicationContext)
                    workManagerFactory()
                    modules(
                        appModule,
                        dbModule,
                        repositoryModule,
                        workerModule
                    )
                }
            }
    }

    private fun initUI(savedInstanceState: Bundle?) {
        val binding: ActivityBandwidthBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_bandwidth)
        binding.viewModel = bandwidthViewModel
        binding.lifecycleOwner = this
    }

    private fun startWorker(){
        val sendLogsWorkRequest =
            PeriodicWorkRequestBuilder<BandwidthWorker>(15, TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "bandwidth_metrics ${System.currentTimeMillis()}",
            ExistingPeriodicWorkPolicy.KEEP,
            sendLogsWorkRequest
        )
    }

}