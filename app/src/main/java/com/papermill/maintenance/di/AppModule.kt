package com.papermill.maintenance.di

import android.content.Context
import androidx.room.Room
import com.papermill.maintenance.data.local.database.MaintenanceDatabase
import com.papermill.maintenance.data.local.dao.MaintenanceRecordDao
import com.papermill.maintenance.data.repository.MaintenanceRepository

object AppModule {
    
    @Volatile
    private var database: MaintenanceDatabase? = null
    
    private var repository: MaintenanceRepository? = null
    
    fun getDatabase(context: Context): MaintenanceDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                MaintenanceDatabase::class.java,
                "maintenance_database"
            ).build().also { database = it }
        }
    }
    
    fun getDao(context: Context): MaintenanceRecordDao {
        return getDatabase(context).maintenanceRecordDao()
    }
    
    fun getRepository(context: Context): MaintenanceRepository {
        return repository ?: synchronized(this) {
            repository ?: MaintenanceRepository(getDao(context)).also { repository = it }
        }
    }
}
