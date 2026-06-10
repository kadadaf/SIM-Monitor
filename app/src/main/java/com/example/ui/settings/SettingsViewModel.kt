package com.example.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SIMMonitorApp
import com.example.data.local.database.AppDatabase
import com.example.data.repository.SIMRepository
import com.example.settings.AppSettings
import com.example.settings.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val settingsDataStore: SettingsDataStore
) : AndroidViewModel(application) {

    private val app = application as SIMMonitorApp
    private val repository: SIMRepository = app.repository
    private val database: AppDatabase = app.database

    val appSettings: StateFlow<AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateEnableNotifications(enabled)
        }
    }

    fun toggleDailyCheck(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateEnableDailyCheck(enabled)
        }
    }

    fun toggleHidePhoneNumber(hide: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateHidePhoneNumberPartially(hide)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateEnableDarkMode(enabled)
        }
    }

    fun updateDefaultCountry(country: String) {
        viewModelScope.launch {
            settingsDataStore.updateDefaultCountry(country)
        }
    }

    fun updateDefaultCurrency(currency: String) {
        viewModelScope.launch {
            settingsDataStore.updateDefaultCurrency(currency)
        }
    }

    fun updateReminderTime(time: String) {
        viewModelScope.launch {
            settingsDataStore.updateReminderTime(time)
        }
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            settingsDataStore.updateLanguage(lang)
        }
    }

    fun wipeAllData(onCompleted: () -> Unit) {
        viewModelScope.launch {
            // Drop variables
            database.clearAllTables()
            // Reset built-in templates
            repository.prepopulateDefaultRules()
            onCompleted()
        }
    }

    fun triggerDatabaseImport(onCompleted: () -> Unit) {
        viewModelScope.launch {
            // Simulated backup rebuild of standard sample data
            repository.prepopulateDefaultRules()
            repository.prepopulateSampleData()
            repository.recalculateAllSIMCards()
            onCompleted()
        }
    }
}
