package com.donadonation.bandwidth.utils

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.donadonation.bandwidth.R
import com.donadonation.bandwidth.entites.enums.NetworkStrength
import java.text.SimpleDateFormat
import java.util.*


@BindingAdapter("app:visibility")
fun visibility(view: View, show: Boolean) {
    if (show) view.visibility = View.VISIBLE else view.visibility = View.GONE
}

@BindingAdapter("app:networkStrength")
fun setNetworkStrength(button: Button, networkStrength: NetworkStrength?) {
    button.visibility = View.VISIBLE
    when(networkStrength){
        NetworkStrength.LOW -> button.text = button.context.getString(R.string.low)
        NetworkStrength.MEDIUM -> button.text = button.context.getString(R.string.medium)
        NetworkStrength.HIGH -> button.text = button.context.getString(R.string.high)
        else -> button.visibility = View.INVISIBLE
    }
}

@BindingAdapter("app:lastUpdated")
fun lastUpdated(button: TextView, timestamp: Long?) {
    timestamp?.let {
        button.visibility = View.VISIBLE
        val simpleDateFormat = SimpleDateFormat("dd MMM yyyy, dd:mm")
        val nowDate = Date(timestamp)
        val date = simpleDateFormat.format(nowDate)
        button.text = button.context.getString(R.string.last_updated_d_ago, date)
    } ?: run {
        button.visibility = View.INVISIBLE
    }
}

