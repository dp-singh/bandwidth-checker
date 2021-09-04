package com.donadonation.bandwidth.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Report::class],
    version = 1
)
@TypeConverters(Converter::class)
abstract class BandwidthDatabase : RoomDatabase() {
    abstract val bandwidthDao: BandwidthDao
}
