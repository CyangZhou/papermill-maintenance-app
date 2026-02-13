package com.papermill.maintenance.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.papermill.maintenance.data.local.entity.MaintenanceRecord
import com.papermill.maintenance.data.repository.MaintenanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RecordListUiState(
    val records: List<MaintenanceRecord> = emptyList(),
    val searchQuery: String = "",
    val selectedEquipment: String? = null,
    val equipmentNames: List<String> = emptyList(),
    val isLoading: Boolean = false
)

class RecordListViewModel(
    private val repository: MaintenanceRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    private val _selectedEquipment = MutableStateFlow<String?>(null)
    
    val uiState: StateFlow<RecordListUiState> = combine(
        _searchQuery,
        _selectedEquipment,
        repository.getAllRecords(),
        repository.getAllEquipmentNames()
    ) { query, equipment, records, equipmentNames ->
        val filteredRecords = when {
            query.isNotEmpty() -> records.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true) ||
                it.equipmentName.contains(query, ignoreCase = true)
            }
            equipment != null -> records.filter { it.equipmentName == equipment }
            else -> records
        }
        RecordListUiState(
            records = filteredRecords,
            searchQuery = query,
            selectedEquipment = equipment,
            equipmentNames = equipmentNames,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecordListUiState(isLoading = true)
    )
    
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
    
    fun onEquipmentSelected(equipment: String?) {
        _selectedEquipment.value = equipment
    }
    
    fun deleteRecord(record: MaintenanceRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }
    
    class Factory(private val repository: MaintenanceRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecordListViewModel::class.java)) {
                return RecordListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
