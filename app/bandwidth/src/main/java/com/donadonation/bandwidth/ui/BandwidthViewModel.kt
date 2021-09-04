package com.donadonation.bandwidth.ui

import androidx.lifecycle.ViewModel
import com.donadonation.bandwidth.repository.BandwidthRepository

const val INTERVAL = 15000 // 15 secs

class BandwidthViewModel constructor(private val repository: BandwidthRepository): ViewModel() {


}