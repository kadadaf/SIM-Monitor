package com.example.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.RuleTemplate
import com.example.data.local.entity.SIMCard
import com.example.data.repository.SIMRepository
import com.example.settings.AppSettings
import com.example.settings.SettingsDataStore
import kotlinx.coroutines.flow.*

class SearchViewModel(
    private val repository: SIMRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val appSettings: StateFlow<AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val simCards: StateFlow<List<SIMCard>> = repository.allSIMCardsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rules: StateFlow<List<RuleTemplate>> = repository.allRuleTemplatesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredSIMs: StateFlow<List<SIMCard>> = combine(
        simCards,
        _searchQuery
    ) { cards, query ->
        if (query.isBlank()) {
            cards
        } else {
            val cleanQuery = query.trim().lowercase()
            cards.filter { card ->
                card.name.lowercase().contains(cleanQuery) ||
                card.phoneNumber.lowercase().contains(cleanQuery) ||
                card.carrier.lowercase().contains(cleanQuery) ||
                card.networkProvider.lowercase().contains(cleanQuery) ||
                card.status.lowercase().contains(cleanQuery) ||
                card.note.lowercase().contains(cleanQuery)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
