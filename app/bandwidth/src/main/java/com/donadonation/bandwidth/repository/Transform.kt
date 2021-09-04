package com.donadonation.bandwidth.repository

import com.donadonation.bandwidth.local.Report
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.model.SpeedTestMode

object Transform {

    fun map(speedTestReport: SpeedTestReport): Report {
        return Report(
            0,
            speedTestReport.startTime,
            0,
            speedTestReport.totalPacketSize,
            speedTestReport.transferRateBit,
            speedTestReport.speedTestMode == SpeedTestMode.DOWNLOAD

        )
    }
}