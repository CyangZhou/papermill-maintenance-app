package com.papermill.maintenance.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.papermill.maintenance.data.local.dao.MaintenanceRecordDao
import com.papermill.maintenance.data.local.entity.MaintenanceRecord

@Database(
    entities = [MaintenanceRecord::class],
    version = 1,
    exportSchema = false
)
abstract class MaintenanceDatabase : RoomDatabase() {
    abstract fun maintenanceRecordDao(): MaintenanceRecordDao
}
