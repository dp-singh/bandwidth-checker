package com.donadonation.bandwidth.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Line
import com.anychart.data.Mapping
import com.anychart.data.Set
import com.anychart.enums.Anchor
import com.anychart.enums.MarkerType
import com.anychart.enums.TooltipPositionMode
import com.donadonation.bandwidth.R
import com.donadonation.bandwidth.databinding.ActivityBandwidthBinding
import com.donadonation.bandwidth.di.appModule
import com.donadonation.bandwidth.di.dbModule
import com.donadonation.bandwidth.di.repositoryModule
import com.donadonation.bandwidth.di.workerModule
import com.donadonation.bandwidth.extension.hide
import com.donadonation.bandwidth.extension.second
import com.donadonation.bandwidth.extension.visible
import com.donadonation.bandwidth.worker.BandwidthWorker
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import java.util.*
import java.util.concurrent.TimeUnit


const val BAND_WIDTH_WORKER = "BANDWIDTH_WORKER"
const val TAG = "BANDWIDTH_ACTIVITY"

class BandwidthActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityBandwidthBinding
    private val bandwidthViewModel: BandwidthViewModel by viewModel()
    private val cartesian: Cartesian by lazy { AnyChart.line() }

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
        initChart()
    }

    private fun initChart() {
        cartesian.apply {
            animation(true)
            padding(10, 20, 5, 20)
            crosshair().enabled(true)
            crosshair().yLabel(true)
            tooltip().positionMode(TooltipPositionMode.POINT)
            title("Bandwidth metric for last 24 hours")
            yAxis(0).title("Data in MB/s")
            xAxis(0).labels().padding(5, 5, 5, 5)
        }
    }

    private fun startWorker() {
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

    private fun observe() {
        bandwidthViewModel.apply {
            viewState.observe(this@BandwidthActivity) {
                updateView(it)
            }
        }
    }

    private fun updateView(viewState: ViewState) {
        when(viewState){
            is ViewState.Loading -> {
                viewBinding.tvEmptyView.hide()
                viewBinding.loader.visible()
            }
            is ViewState.EmptyView -> {
                viewBinding.loader.hide()
                viewBinding.tvEmptyView.visible()
            }
            is ViewState.UpdateView -> {
                viewBinding.loader.hide()
                populateChart(viewState.dataEntryList)
            }
            is ViewState.LiveDownloadReport -> {
                Log.d(TAG, "Live Download Report  ---> ${viewState.liveReport.bitrate}")
            }
            is ViewState.LiveUploadReport -> {
                Log.d(TAG, "Live Upload Report ${viewState.liveReport.bitrate}")
            }
        }
    }

    private fun populateChart(list: List<DataEntry>) {
        val set = Set.instantiate()
        set.data(list)
        val series1Mapping = set.mapAs("{ x: 'x', value: 'value' }")
        val series2Mapping = set.mapAs("{ x: 'x', value: 'value2' }")
        val mappings = listOf<Mapping>(series1Mapping, series2Mapping)
        viewBinding.lineChartView.apply {
            setDownloadLine(mappings.first())
            mappings.second()?.let {
                setUploadLine(it)
            }
            cartesian.legend().enabled(true)
            cartesian.legend().fontSize(13.0)
            cartesian.legend().padding(0.0, 0.0, 10.0, 0.0)
            setChart(cartesian)
        }
    }

    private fun setDownloadLine(series1Mapping: Mapping) {
        val series1: Line = cartesian.line(series1Mapping)
        series1.name("Download")
        series1.color("#228B22")
        series1.hovered().markers().enabled(true)
        series1.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series1.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)
    }

    private fun setUploadLine(series2Mapping: Mapping) {
        val series2 = cartesian.line(series2Mapping)
        series2.name("Upload")
        series2.hovered().markers().enabled(true)
        series2.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series2.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)
    }

}