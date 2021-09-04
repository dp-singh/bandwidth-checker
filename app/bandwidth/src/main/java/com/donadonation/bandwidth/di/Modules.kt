package com.donadonation.bandwidth.di

import android.app.Application
import androidx.room.Room

import com.donadonation.bandwidth.local.BandwidthDao
import com.donadonation.bandwidth.local.BandwidthDatabase
import com.donadonation.bandwidth.local.METRIC_TABLE
import com.donadonation.bandwidth.repository.BandwidthRepository
import com.donadonation.bandwidth.repository.BandwidthRepositoryImpl
import com.donadonation.bandwidth.repository.Transform
import com.donadonation.bandwidth.ui.BandwidthViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module{

    viewModel{
        BandwidthViewModel(get())
    }
}

val repositoryModule = module{

    fun providesBandwidthRepository(dao: BandwidthDao, mapper: Transform):BandwidthRepository{
        return BandwidthRepositoryImpl(dao, mapper)
    }
    single { providesBandwidthRepository(get(), Transform) }

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