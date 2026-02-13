package com.papermill.maintenance.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.papermill.maintenance.data.local.entity.MaintenanceRecord
import com.papermill.maintenance.data.repository.MaintenanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecordDetailUiState(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val equipmentName: String = "",
    val imagePaths: List<String> = emptyList(),
    val isNewRecord: Boolean = true,
    val isSaving: Boolean = false,
    val saveComplete: Boolean = false
)

class RecordDetailViewModel(
    private val repository: MaintenanceRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RecordDetailUiState())
    val uiState: StateFlow<RecordDetailUiState> = _uiState.asStateFlow()
    
    fun loadRecord(recordId: Long) {
        if (recordId == 0L) {
            _uiState.update { it.copy(isNewRecord = true) }
            return
        }
        
        viewModelScope.launch {
            repository.getRecordById(recordId)?.let { record ->
                _uiState.update { 
                    it.copy(
                        id = record.id,
                        title = record.title,
                        content = record.content,
                        equipmentName = record.equipmentName,
                        imagePaths = if (record.imagePaths.isNotEmpty()) {
                            record.imagePaths.split(",")
                        } else {
                            emptyList()
                        },
                        isNewRecord = false
                    )
                }
            }
        }
    }
    
    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }
    
    fun onContentChange(content: String) {
        _uiState.update { it.copy(content = content) }
    }
    
    fun onEquipmentNameChange(equipmentName: String) {
        _uiState.update { it.copy(equipmentName = equipmentName) }
    }
    
    fun addImage(path: String) {
        _uiState.update { 
            it.copy(imagePaths = it.imagePaths + path) 
        }
    }
    
    fun removeImage(path: String) {
        _uiState.update { 
            it.copy(imagePaths = it.imagePaths - path) 
        }
    }
    
    fun saveRecord() {
        val state = _uiState.value
        
        if (state.title.isBlank()) {
            return
        }
        
        _uiState.update { it.copy(isSaving = true) }
        
        viewModelScope.launch {
            val record = MaintenanceRecord(
                id = if (state.isNewRecord) 0 else state.id,
                title = state.title,
                content = state.content,
                equipmentName = state.equipmentName,
                imagePaths = state.imagePaths.joinToString(","),
                updatedAt = System.currentTimeMillis()
            )
            
            if (state.isNewRecord) {
                repository.insertRecord(record)
            } else {
                repository.updateRecord(record)
            }
            
            _uiState.update { it.copy(isSaving = false, saveComplete = true) }
        }
    }
    
    class Factory(private val repository: MaintenanceRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecordDetailViewModel::class.java)) {
                return RecordDetailViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
