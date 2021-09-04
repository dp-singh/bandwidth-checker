package com.donadonation.bandwidth.di

import com.donadonation.bandwidth.BandwidthViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module{

    viewModel{
        BandwidthViewModel()
    }
}