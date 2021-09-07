package com.donadonation.bandwidth.ui

import android.graphics.Color
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
import com.donadonation.bandwidth.extension.formatToViewTimeDefaults
import com.donadonation.bandwidth.worker.BandwidthWorker
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.formatter.ValueFormatter
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import java.util.*
import java.util.concurrent.TimeUnit

const val BAND_WIDTH_WORKER = "BANDWIDTH_WORKER"
class BandwidthActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityBandwidthBinding
    private val bandwidthViewModel: BandwidthViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject(savedInstanceState)
        initUI(savedInstanceState)
        setUpChart()
        observe()
        startWorker()
    }

    private fun inject(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
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
        viewBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_bandwidth)
        viewBinding.viewModel = bandwidthViewModel
        viewBinding.lifecycleOwner = this
    }

    private fun setUpChart() {
        setXAxis()
        setYAxis()
    }

    private fun setXAxis(){
        val xAxis = viewBinding.lineChartView.xAxis
        xAxis?.let {
            it.position = XAxis.XAxisPosition.BOTTOM
            it.textSize = 10f
            it.textColor = Color.RED
            it.setAvoidFirstLastClipping(true)
            it.setDrawAxisLine(false)
            it.setDrawGridLines(false)
        }
    }

    private fun setYAxis() {
        val yAxis = viewBinding.lineChartView.axisLeft
        yAxis?.let {
            it.textSize = 12f
            it.axisMinimum = 0f
            it.axisMaximum = 20f
            it.textColor = Color.BLACK
            it.granularity = 1f
            it.setDrawGridLines(false)
        }
    }

    private fun startWorker(){
        val sendLogsWorkRequest =
            PeriodicWorkRequestBuilder<BandwidthWorker>(15, TimeUnit.MINUTES)
                .addTag(BAND_WIDTH_WORKER)
                .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            BAND_WIDTH_WORKER,
            ExistingPeriodicWorkPolicy.KEEP,
            sendLogsWorkRequest
        )
    }

    private fun observe(){
        bandwidthViewModel.apply {
            chartLineData.observe(this@BandwidthActivity) {
                populateChart(lineData = it)
            }
            xAxisValue.observe(this@BandwidthActivity) {
                formatXAxis(it)
            }
        }
    }

    private fun populateChart(lineData: LineData) {
        viewBinding.lineChartView.apply {
            data = lineData
            invalidate()
        }
    }

    private fun formatXAxis(timeList: List<String>) {
        val formatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return Date(value.toLong()).formatToViewTimeDefaults()
            }
        }
        val xAxis = viewBinding.lineChartView.xAxis
        xAxis.granularity = 1f
        xAxis.valueFormatter = formatter
    }

}