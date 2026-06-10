package com.example.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.SIMMonitorApp
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.simdetail.SIMDetailViewModel
import com.example.ui.addsim.AddEditSIMViewModel
import com.example.ui.rules.RulesViewModel
import com.example.ui.history.HistoryViewModel
import com.example.ui.settings.SettingsViewModel
import com.example.ui.search.SearchViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val app: SIMMonitorApp
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(app.repository, app.settingsDataStore) as T
            modelClass.isAssignableFrom(SIMDetailViewModel::class.java) ->
                SIMDetailViewModel(app.repository, app.settingsDataStore) as T
            modelClass.isAssignableFrom(AddEditSIMViewModel::class.java) ->
                AddEditSIMViewModel(app.repository, app.settingsDataStore) as T
            modelClass.isAssignableFrom(RulesViewModel::class.java) ->
                RulesViewModel(app.repository, app.settingsDataStore) as T
            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(app.repository, app.settingsDataStore) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(app, app.settingsDataStore) as T
            modelClass.isAssignableFrom(SearchViewModel::class.java) ->
                SearchViewModel(app.repository, app.settingsDataStore) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
