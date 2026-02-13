package com.papermill.maintenance.data.repository

import com.papermill.maintenance.data.local.dao.MaintenanceRecordDao
import com.papermill.maintenance.data.local.entity.MaintenanceRecord
import kotlinx.coroutines.flow.Flow

class MaintenanceRepository(private val dao: MaintenanceRecordDao) {
    
    fun getAllRecords(): Flow<List<MaintenanceRecord>> = dao.getAllRecords()
    
    suspend fun getRecordById(id: Long): MaintenanceRecord? = dao.getRecordById(id)
    
    fun searchRecords(query: String): Flow<List<MaintenanceRecord>> = dao.searchRecords(query)
    
    fun getRecordsByEquipment(equipmentName: String): Flow<List<MaintenanceRecord>> = 
        dao.getRecordsByEquipment(equipmentName)
    
    fun getAllEquipmentNames(): Flow<List<String>> = dao.getAllEquipmentNames()
    
    suspend fun insertRecord(record: MaintenanceRecord): Long = dao.insertRecord(record)
    
    suspend fun updateRecord(record: MaintenanceRecord) = dao.updateRecord(record)
    
    suspend fun deleteRecord(record: MaintenanceRecord) = dao.deleteRecord(record)
    
    suspend fun deleteRecordById(id: Long) = dao.deleteRecordById(id)
}
