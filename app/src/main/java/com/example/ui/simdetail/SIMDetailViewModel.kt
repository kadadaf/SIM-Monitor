package com.example.ui.simdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ReminderRecord
import com.example.data.local.entity.RuleTemplate
import com.example.data.local.entity.SIMCard
import com.example.data.local.entity.UsageRecord
import com.example.data.repository.SIMRepository
import com.example.settings.AppSettings
import com.example.settings.SettingsDataStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SIMDetailViewModel(
    private val repository: SIMRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _simCardId = MutableStateFlow<Int?>(null)

    val appSettings: StateFlow<AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val simCard: StateFlow<SIMCard?> = _simCardId
        .filterNotNull()
        .flatMapLatest { id -> repository.getSIMCardByIdFlow(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val rules: StateFlow<List<RuleTemplate>> = repository.allRuleTemplatesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeRule: StateFlow<RuleTemplate?> = combine(simCard, rules) { sim, list ->
        if (sim != null) list.find { it.id == sim.ruleId } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val usageRecords: StateFlow<List<UsageRecord>> = _simCardId
        .filterNotNull()
        .flatMapLatest { id -> repository.getUsageRecordsBySIM(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminderRecords: StateFlow<List<ReminderRecord>> = _simCardId
        .filterNotNull()
        .flatMapLatest { id -> repository.getRemindersBySIM(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSIMCardId(id: Int) {
        _simCardId.value = id
    }

    fun addUsageRecord(actionType: String, amount: Double?, note: String) {
        val currentSim = simCard.value ?: return
        viewModelScope.launch {
            val record = UsageRecord(
                simCardId = currentSim.id,
                actionType = actionType,
                amount = amount,
                currency = currentSim.currency,
                actionDate = System.currentTimeMillis(),
                note = note
            )
            repository.insertUsageRecord(record)
        }
    }

    fun deleteUsageRecord(record: UsageRecord) {
        viewModelScope.launch {
            repository.deleteUsageRecord(record)
        }
    }

    fun deleteSIMCard(onCompleted: () -> Unit) {
        val currentSim = simCard.value ?: return
        viewModelScope.launch {
            repository.deleteSIMCard(currentSim)
            onCompleted()
        }
    }
}
