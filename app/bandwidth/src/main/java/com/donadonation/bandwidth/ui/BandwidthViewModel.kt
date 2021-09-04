package com.donadonation.bandwidth.ui

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.RoundingMode

const val INTERVAL = 15000 // 15 secs

class BandwidthViewModel : ViewModel() {

    val bitRateObservableField: ObservableField<String> = ObservableField()

    private var speedTestSocket: SpeedTestSocket? = null

    fun startSampling() {
        viewModelScope.launch {
            calculateBandwidth()
        }
    }

    private suspend fun calculateBandwidth() {
        withContext(Dispatchers.IO) {
            speedTestSocket = SpeedTestSocket()
            speedTestSocket?.addSpeedTestListener(object : ISpeedTestListener {
                override fun onCompletion(report: SpeedTestReport?) {
                    bitRateObservableField.set("rate in bit/s =${report?.transferRateBit}")
                }

                override fun onProgress(percent: Float, report: SpeedTestReport?) {
                    bitRateObservableField.set("Progress%= $percent rate in bit/s = ${report?.transferRateBit}")
                }

                override fun onError(speedTestError: SpeedTestError?, errorMessage: String?) {
                    bitRateObservableField.set("Error!! $errorMessage")
                }
            })
            speedTestSocket?.defaultRoundingMode = RoundingMode.HALF_EVEN;
            speedTestSocket?.startDownload("http://ipv4.ikoula.testdebit.info/1M.iso", INTERVAL)
        }
    }

}