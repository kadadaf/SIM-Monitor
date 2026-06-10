package com.example.ui.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.RuleTemplate
import com.example.data.repository.SIMRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RulesViewModel(
    private val repository: SIMRepository,
    private val settingsDataStore: com.example.settings.SettingsDataStore
) : ViewModel() {

    val appSettings: StateFlow<com.example.settings.AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.settings.AppSettings())

    val ruleTemplates: StateFlow<List<RuleTemplate>> = repository.allRuleTemplatesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun duplicateRule(rule: RuleTemplate) {
        viewModelScope.launch {
            val duplicate = RuleTemplate(
                name = "${rule.name} (Copy)",
                carrierName = rule.carrierName,
                country = rule.country,
                activePeriodDays = rule.activePeriodDays,
                firstReminderDaysBefore = rule.firstReminderDaysBefore,
                secondReminderDaysBefore = rule.secondReminderDaysBefore,
                finalReminderDaysBefore = rule.finalReminderDaysBefore,
                requiredActions = rule.requiredActions,
                minSpendAmount = rule.minSpendAmount,
                hasNewCardActivationRule = rule.hasNewCardActivationRule,
                newCardActivationDays = rule.newCardActivationDays,
                allowPaidKeepAliveService = rule.allowPaidKeepAliveService,
                paidKeepAliveDescription = rule.paidKeepAliveDescription,
                isBuiltIn = false, // duplicated items copy over as custom rules
                isEnabled = true
            )
            repository.insertRuleTemplate(duplicate)
        }
    }

    fun deleteRule(rule: RuleTemplate) {
        viewModelScope.launch {
            if (!rule.isBuiltIn) {
                repository.deleteRuleTemplate(rule)
            }
        }
    }

    suspend fun getRuleTemplateById(id: Int): RuleTemplate? {
        return repository.getRuleTemplateById(id)
    }

    fun saveCustomRule(
        name: String,
        carrierName: String,
        country: String,
        activePeriodDays: Int,
        firstReminder: Int,
        secondReminder: Int,
        finalReminder: Int,
        requiredActions: List<String>,
        minSpend: Double?,
        hasActivationRule: Boolean,
        activationDays: Int,
        allowKeepAlive: Boolean,
        keepAliveDesc: String,
        isCustomRuleIdToEdit: Int? = null,
        onCompleted: () -> Unit
    ) {
        viewModelScope.launch {
            val existingIsBuiltIn = if (isCustomRuleIdToEdit != null) {
                repository.getRuleTemplateById(isCustomRuleIdToEdit)?.isBuiltIn ?: false
            } else {
                false
            }
            val actionsCsv = requiredActions.joinToString(",")
            val rule = RuleTemplate(
                id = isCustomRuleIdToEdit ?: 0,
                name = name,
                carrierName = carrierName,
                country = country,
                activePeriodDays = activePeriodDays,
                firstReminderDaysBefore = firstReminder,
                secondReminderDaysBefore = secondReminder,
                finalReminderDaysBefore = finalReminder,
                requiredActions = actionsCsv,
                minSpendAmount = minSpend,
                hasNewCardActivationRule = hasActivationRule,
                newCardActivationDays = activationDays,
                allowPaidKeepAliveService = allowKeepAlive,
                paidKeepAliveDescription = keepAliveDesc,
                isBuiltIn = existingIsBuiltIn,
                isEnabled = true
            )

            if (isCustomRuleIdToEdit == null) {
                repository.insertRuleTemplate(rule)
            } else {
                repository.updateRuleTemplate(rule)
            }
            onCompleted()
        }
    }
}
