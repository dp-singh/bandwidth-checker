package com.donadonation.bandwidth.di

import android.app.Application
import androidx.room.Room
import com.donadonation.bandwidth.BandwidthViewModel
import com.donadonation.bandwidth.local.BandwidthDao
import com.donadonation.bandwidth.local.BandwidthDatabase
import com.donadonation.bandwidth.local.METRIC_TABLE
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module{

    viewModel{
        BandwidthViewModel()

    }
}

val dbModule = module {

    fun provideDatabase(application: Application): BandwidthDatabase {
        return Room.databaseBuilder(application, BandwidthDatabase::class.java, METRIC_TABLE)
            .fallbackToDestructiveMigration()
            .build()
    }

    fun providesBandwidthDao(database: BandwidthDatabase): BandwidthDao {
        return database.bandwidthDao
    }

    single { provideDatabase(androidApplication()) }
    single { providesBandwidthDao(get()) }
}