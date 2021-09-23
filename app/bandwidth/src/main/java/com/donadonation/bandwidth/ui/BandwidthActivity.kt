package com.donadonation.bandwidth.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Line
import com.anychart.data.Mapping
import com.anychart.data.Set
import com.anychart.enums.Align
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



class BandwidthActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityBandwidthBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI(savedInstanceState)

    }

    private fun initUI(savedInstanceState: Bundle?) {
        viewBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_bandwidth)
        viewBinding.lifecycleOwner = this
        if(savedInstanceState==null){
            transact()
        }
    }

    private fun transact() {
        val fragmentTransaction = supportFragmentManager
            .beginTransaction()
            .replace(
                viewBinding.container.id,
                BandwidthFragment.newInstance(),
                BandwidthFragment::class.java.simpleName
            )
            .addToBackStack(null)
            .commit()
    }



}