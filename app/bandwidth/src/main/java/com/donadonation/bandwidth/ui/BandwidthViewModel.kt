package com.donadonation.bandwidth.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.data.Mapping
import com.donadonation.bandwidth.local.Report
import com.donadonation.bandwidth.repository.BandwidthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BandwidthViewModel constructor(private val repository: BandwidthRepository): ViewModel() {

    private val _viewState: MutableLiveData<ViewState> = MutableLiveData()
    val viewState: LiveData<ViewState>
        get() = _viewState

    fun prepareChartData() {
        viewModelScope.launch {
            _viewState.value = ViewState.Loading
            val metricReport: List<Report> = getReport()
            val dataEntryList: List<DataEntry> = repository.getChartData(metricReport)
            if (dataEntryList.isNotEmpty()) {
                _viewState.postValue(ViewState.UpdateView(dataEntryList))
            } else {
                _viewState.postValue(ViewState.EmptyView)
            }
        }
    }

    private suspend fun getReport(): List<Report> {
        return withContext(Dispatchers.IO) {
            repository.getReport()
        }
    }
}

sealed class ViewState() {
    object Loading : ViewState()
    object EmptyView : ViewState()
    class UpdateView(val dataEntryList: List<DataEntry>) : ViewState()
}