package com.donadonation.bandwidth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.donadonation.bandwidth.databinding.ActivityBandwidthBinding
import com.donadonation.bandwidth.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.startKoin

class BandwidthActivity : AppCompatActivity() {

    private val bandwidthViewModel: BandwidthViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject(savedInstanceState)
        initUI(savedInstanceState)
    }

    private fun inject(savedInstanceState: Bundle?) {
        savedInstanceState
            .takeIf { savedInstanceState == null }
            .let {
                startKoin {
                    androidLogger()
                    androidContext(this@BandwidthActivity.applicationContext)
                    modules(appModule)
                }
            }
    }

    private fun initUI(savedInstanceState: Bundle?) {
        val binding: ActivityBandwidthBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_bandwidth)
        binding.viewModel = bandwidthViewModel
        binding.lifecycleOwner = this
    }

}