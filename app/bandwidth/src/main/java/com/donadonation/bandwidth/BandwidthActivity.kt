package com.donadonation.bandwidth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.donadonation.bandwidth.databinding.ActivityBandwidthBinding
import fr.bmartel.speedtest.SpeedTestReport

class BandwidthActivity : AppCompatActivity() {

    private val viewModel: BandwidthViewModel by lazy {
        ViewModelProvider(this)[BandwidthViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityBandwidthBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_bandwidth)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

}