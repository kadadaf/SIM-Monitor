package com.example.ui.addsim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.RuleTemplate
import com.example.data.local.entity.SIMCard
import com.example.data.repository.SIMRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AddEditSIMViewModel(
    private val repository: SIMRepository,
    private val settingsDataStore: com.example.settings.SettingsDataStore
) : ViewModel() {

    val appSettings: StateFlow<com.example.settings.AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.settings.AppSettings())

    private val _simId = MutableStateFlow<Int?>(null)
    
    val existingSIM: StateFlow<SIMCard?> = _simId
        .filterNotNull()
        .map { id -> repository.getSIMCardById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val ruleTemplates: StateFlow<List<RuleTemplate>> = repository.allRuleTemplatesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadSIM(id: Int?) {
        _simId.value = id
    }

    fun saveSIMCard(
        name: String,
        phoneNumber: String,
        country: String,
        carrier: String,
        networkProvider: String,
        cardType: String,
        activationDate: Long,
        lastActiveDate: Long,
        lastTopUpDate: Long,
        balance: Double?,
        currency: String,
        ruleId: Int,
        note: String,
        onCompleted: () -> Unit
    ) {
        viewModelScope.launch {
            val current = existingSIM.value
            val status = current?.status ?: "UNKNOWN" // Repository recalculateSIMStatus will immediately override this safely based on rules

            if (current == null) {
                // Insert new card
                val newCard = SIMCard(
                    name = name,
                    phoneNumber = phoneNumber,
                    country = country,
                    carrier = carrier,
                    networkProvider = networkProvider,
                    cardType = cardType,
                    activationDate = activationDate,
                    lastActiveDate = lastActiveDate,
                    lastTopUpDate = lastTopUpDate,
                    balance = balance,
                    currency = currency,
                    ruleId = ruleId,
                    status = status,
                    note = note
                )
                repository.insertSIMCard(newCard)
            } else {
                // Edit existing
                val updatedCard = current.copy(
                    name = name,
                    phoneNumber = phoneNumber,
                    country = country,
                    carrier = carrier,
                    networkProvider = networkProvider,
                    cardType = cardType,
                    activationDate = activationDate,
                    lastActiveDate = lastActiveDate,
                    lastTopUpDate = lastTopUpDate,
                    balance = balance,
                    currency = currency,
                    ruleId = ruleId,
                    note = note,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateSIMCard(updatedCard)
            }
            onCompleted()
        }
    }
}
