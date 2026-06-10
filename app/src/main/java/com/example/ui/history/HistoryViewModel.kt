package com.example.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ReminderRecord
import com.example.data.local.entity.SIMCard
import com.example.data.local.entity.UsageRecord
import com.example.data.repository.SIMRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: SIMRepository,
    private val settingsDataStore: com.example.settings.SettingsDataStore
) : ViewModel() {

    val appSettings: StateFlow<com.example.settings.AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.settings.AppSettings())

    val simCards: StateFlow<List<SIMCard>> = repository.allSIMCardsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsageRecords: StateFlow<List<UsageRecord>> = repository.allUsageRecordsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReminders: StateFlow<List<ReminderRecord>> = repository.allRemindersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filters UI state
    private val _selectedSimId = MutableStateFlow<Int?>(null)
    val selectedSimId: StateFlow<Int?> = _selectedSimId.asStateFlow()

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType.asStateFlow()

    // Filtered lists
    val filteredUsages: StateFlow<List<UsageRecord>> = combine(
        allUsageRecords,
        _selectedSimId,
        _selectedType
    ) { usages, simId, type ->
        usages.filter { 
            (simId == null || it.simCardId == simId) &&
            (type == null || it.actionType.uppercase() == type.uppercase())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredReminders: StateFlow<List<ReminderRecord>> = combine(
        allReminders,
        _selectedSimId
    ) { reminders, simId ->
        reminders.filter {
            (simId == null || it.simCardId == simId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSimFilter(id: Int?) {
        _selectedSimId.value = id
    }

    fun setTypeFilter(type: String?) {
        _selectedType.value = type
    }

    fun deleteUsageRecord(record: UsageRecord) {
        viewModelScope.launch {
            repository.deleteUsageRecord(record)
        }
    }
}
