package com.papermill.maintenance.data.local.dao

import androidx.room.*
import com.papermill.maintenance.data.local.entity.MaintenanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceRecordDao {
    
    @Query("SELECT * FROM maintenance_records ORDER BY updatedAt DESC")
    fun getAllRecords(): Flow<List<MaintenanceRecord>>
    
    @Query("SELECT * FROM maintenance_records WHERE id = :id")
    suspend fun getRecordById(id: Long): MaintenanceRecord?
    
    @Query("SELECT * FROM maintenance_records WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR equipmentName LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchRecords(query: String): Flow<List<MaintenanceRecord>>
    
    @Query("SELECT * FROM maintenance_records WHERE equipmentName = :equipmentName ORDER BY updatedAt DESC")
    fun getRecordsByEquipment(equipmentName: String): Flow<List<MaintenanceRecord>>
    
    @Query("SELECT DISTINCT equipmentName FROM maintenance_records ORDER BY equipmentName ASC")
    fun getAllEquipmentNames(): Flow<List<String>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MaintenanceRecord): Long
    
    @Update
    suspend fun updateRecord(record: MaintenanceRecord)
    
    @Delete
    suspend fun deleteRecord(record: MaintenanceRecord)
    
    @Query("DELETE FROM maintenance_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)
}
