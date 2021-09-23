package com.donadonation.bandwidth.ui

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anychart.chart.common.dataentry.DataEntry
import com.donadonation.bandwidth.entites.Resource
import com.donadonation.bandwidth.entites.enums.NetworkStrength
import com.donadonation.bandwidth.extension.orZero
import com.donadonation.bandwidth.extension.round
import com.donadonation.bandwidth.extension.toMbps
import com.donadonation.bandwidth.local.Report
import com.donadonation.bandwidth.repository.BandwidthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class BandwidthViewModel constructor(private val repository: BandwidthRepository) : ViewModel() {

    private val _viewState: MutableLiveData<ViewState> = MutableLiveData()
    val viewState: LiveData<ViewState>
        get() = _viewState

    val refreshState: ObservableBoolean = ObservableBoolean(false)
    val uploadRateText: ObservableField<String> = ObservableField("0")
    val downloadRateText: ObservableField<String> = ObservableField("0")
    val recommendedUploadSpeed: ObservableField<String> = ObservableField("1.00")
    val recommendedUDownloadSpeed: ObservableField<String> = ObservableField("3.00")
    val lastUpdatedText: ObservableField<Long> = ObservableField()
    val networkStrength: ObservableField<NetworkStrength> = ObservableField()


    init {
        getLiveNetworkUpdate()
        prepareChartData()
    }

    private fun getLiveNetworkUpdate() {
        repository.getLiveReport(60 * 1000L)
            .onEach {
                if(it is Resource.Loading){
                    refreshState.set(true)
                }else{
                    refreshState.set(false)
                    it.data?.updateLiveView()
                }

            }
            .launchIn(viewModelScope)
    }


    private fun Pair<Result<Report>, Result<Report>>.updateLiveView() {
        setLastUpdatedTimestamp()
        if (this.first.isSuccess) {
            this.first.getOrNull()
                ?.takeIf { it.isDownload }
                ?.let {
                    downloadRateText.set(it.bitrate.toMbps().round())
                } ?: kotlin.run {
                this.first.getOrNull()
                    ?.let {
                        uploadRateText.set(it.bitrate.toMbps().round())
                    }
            }
        }
        if (this.second.isSuccess) {
            this.second.getOrNull()
                ?.takeIf { it.isDownload }
                ?.let {
                    downloadRateText.set(it.bitrate.toMbps().round())
                } ?: kotlin.run {
                this.second.getOrNull()
                    ?.let {
                        uploadRateText.set(it.bitrate.toMbps().round())
                    }
            }
        }
        setNetworkStrength()
    }

    private fun setNetworkStrength() {
        if (uploadRateText.get().isNullOrEmpty().not() &&
            downloadRateText.get().isNullOrEmpty().not()
        ) {
            val uploadRate = uploadRateText.get()?.toFloat().orZero()
            val downloadRate = downloadRateText.get()?.toFloat().orZero()
            if (uploadRate > 3 && downloadRate > 5) {
                networkStrength.set(NetworkStrength.HIGH)
            } else if (uploadRate > 1 && downloadRate > 3) {
                networkStrength.set(NetworkStrength.MEDIUM)
            } else {
                networkStrength.set(NetworkStrength.LOW)
            }
        } else {
            networkStrength.set(null)
        }
    }

    private fun setLastUpdatedTimestamp(){
        val calender = Calendar.getInstance()
        lastUpdatedText.set(calender.time.time)
    }


    private fun prepareChartData() {
        viewModelScope.launch {
            _viewState.value = ViewState.Loading
            delay(1000L)
            val metricReport: List<Report> = getReport()
            val dataEntryList: List<DataEntry> = repository.getChartData(metricReport)
            if (dataEntryList.isNotEmpty()) {
                _viewState.value = ViewState.UpdateView(dataEntryList)
            } else {
                _viewState.value = ViewState.EmptyView
            }
        }
    }

    private suspend fun getReport(): List<Report> {
        return withContext(Dispatchers.IO) {
            val current = Calendar.getInstance()
            val currentTimestamp = current.time.time
            repository.getReport(currentTimestamp)
        }
    }
}

sealed class ViewState {
    object Loading : ViewState()
    object EmptyView : ViewState()
    data class UpdateView(val dataEntryList: List<DataEntry>) : ViewState()
}