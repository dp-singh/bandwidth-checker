package com.donadonation.bandwidth.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anychart.data.Mapping
import com.donadonation.bandwidth.local.Report
import com.donadonation.bandwidth.repository.BandwidthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BandwidthViewModel constructor(private val repository: BandwidthRepository): ViewModel() {

    private val _chartLineData: MutableLiveData<List<Mapping>> = MutableLiveData()
    val chartLineData: LiveData<List<Mapping>>
        get() = _chartLineData

    private val _xAxisValue: MutableLiveData<List<String>> = MutableLiveData()
    val xAxisValue: LiveData<List<String>>
        get() = _xAxisValue

    fun prepareChartData() {
        viewModelScope.launch {
            val metricReport: List<Report> = getReport()
            val mappings: List<Mapping> = repository.getChartData(metricReport)
            _chartLineData.postValue(mappings)
        }
    }


    private suspend fun getReport(): List<Report> {
        return withContext(Dispatchers.IO) {
            repository.getReport()
        }
    }

}