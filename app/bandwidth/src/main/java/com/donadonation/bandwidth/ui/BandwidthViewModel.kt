package com.donadonation.bandwidth.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anychart.chart.common.dataentry.DataEntry
import com.donadonation.bandwidth.local.Report
import com.donadonation.bandwidth.repository.BandwidthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BandwidthViewModel constructor(private val repository: BandwidthRepository) : ViewModel() {

    private val _viewState: MutableLiveData<ViewState> = MutableLiveData()
    val viewState: LiveData<ViewState>
        get() = _viewState

    init {
        getLiveNetworkUpdate()
    }

    private fun getLiveNetworkUpdate() {
        repository.getLiveReport(60 * 1000)
            .onEach { it.updateLiveView() }
            .launchIn(viewModelScope)
    }


    private fun Pair<Result<Report>, Result<Report>>.updateLiveView() {
        if (this.first.isSuccess) {
            this.first.getOrNull()
                ?.takeIf { it.isDownload }
                ?.let {
                    _viewState.value = ViewState.LiveDownloadReport(it)
                } ?: kotlin.run {
                this.first.getOrNull()
                    ?.let {
                        _viewState.value = ViewState.LiveUploadReport(it)
                    }
            }
        }
        if (this.second.isSuccess) {
            this.second.getOrNull()
                ?.takeIf { it.isDownload }
                ?.let {
                    _viewState.value = ViewState.LiveDownloadReport(it)
                } ?: kotlin.run {
                this.second.getOrNull()
                    ?.let {
                        _viewState.value = ViewState.LiveUploadReport(it)
                    }
            }
        }
    }


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

sealed class ViewState {
    object Loading : ViewState()
    object EmptyView : ViewState()
    data class UpdateView(val dataEntryList: List<DataEntry>) : ViewState()
    data class LiveUploadReport(val liveReport: Report) : ViewState()
    data class LiveDownloadReport(val liveReport: Report) : ViewState()
}