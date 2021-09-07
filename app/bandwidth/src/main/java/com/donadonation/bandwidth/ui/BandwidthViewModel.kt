package com.donadonation.bandwidth.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donadonation.bandwidth.local.Report
import com.donadonation.bandwidth.repository.BandwidthRepository
import com.github.mikephil.charting.data.LineData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BandwidthViewModel constructor(private val repository: BandwidthRepository): ViewModel() {

    private val _chartLineData: MutableLiveData<LineData> = MutableLiveData()
    val chartLineData: LiveData<LineData>
        get() = _chartLineData

    private val _xAxisValue: MutableLiveData<List<String>> = MutableLiveData()
    val xAxisValue: LiveData<List<String>>
        get() = _xAxisValue

    fun prepareChartData(){
        viewModelScope.launch {
            val metricReport: List<Report> = getReport()
            val xAxisStrings = repository.getXAxisValue(metricReport)
            _xAxisValue.postValue(xAxisStrings)
            val lineValues = repository.getYAxisValue(metricReport)
            _chartLineData.postValue(lineValues)
        }
    }


    private suspend fun getReport(): List<Report> {
        return withContext(Dispatchers.IO) {
            repository.getReport()
        }
    }

}