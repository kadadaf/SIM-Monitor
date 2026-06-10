package com.example.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ReminderRecord
import com.example.data.local.entity.SIMCard
import com.example.data.repository.SIMRepository
import com.example.domain.rule.RuleEngine
import com.example.settings.AppSettings
import com.example.settings.SettingsDataStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: SIMRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val appSettings: StateFlow<AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val simCards: StateFlow<List<SIMCard>> = repository.allSIMCardsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCount: StateFlow<Int> = simCards.map { list ->
        list.count { it.status == "HEALTHY" || it.status == "ATTENTION" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val riskCount: StateFlow<Int> = simCards.map { list ->
        list.count { it.status == "RISK" || it.status == "EXPIRED" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allRuleTemplates = repository.allRuleTemplatesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun triggerRemindMe(simCard: SIMCard) {
        viewModelScope.launch {
            val rule = repository.getRuleTemplateById(simCard.ruleId) ?: return@launch
            val reminder = ReminderRecord(
                simCardId = simCard.id,
                ruleId = simCard.ruleId,
                reminderType = "CUSTOM",
                remindAt = System.currentTimeMillis() + 5000, // custom alert schedule
                message = "Keep alive action required for ${simCard.name}: ${RuleEngine.getDefaultActionForRule(rule.name)}",
                isSent = false
            )
            repository.insertReminderRecord(reminder)
        }
    }
}
