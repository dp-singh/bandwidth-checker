package com.donadonation.bandwidth.ui

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donadonation.bandwidth.repository.BandwidthRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

const val INTERVAL = 15000 // 15 secs

class BandwidthViewModel constructor(private val repository: BandwidthRepository): ViewModel() {

    val bitRateObservableField: ObservableField<String> = ObservableField()


    fun startSampling() {
        viewModelScope.launch {
            repository.startSampling(null, null)
                .collect {  }
        }
    }



}