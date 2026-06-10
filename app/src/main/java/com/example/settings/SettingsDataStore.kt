package com.example.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AppSettings(
    val defaultReminderTime: String = "09:00",
    val enableNotifications: Boolean = true,
    val enableDailyCheck: Boolean = true,
    val hidePhoneNumberPartially: Boolean = true,
    val enableDarkMode: Boolean = false,
    val defaultCurrency: String = "GBP",
    val defaultCountry: String = "UK",
    val language: String = "zh"
)

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sim_monitor_settings")

class SettingsDataStore(private val context: Context) {

    private object PreferencesKeys {
        val DEFAULT_REMINDER_TIME = stringPreferencesKey("default_reminder_time")
        val ENABLE_NOTIFICATIONS = booleanPreferencesKey("enable_notifications")
        val ENABLE_DAILY_CHECK = booleanPreferencesKey("enable_daily_check")
        val HIDE_PHONE_NUMBER_PARTIALLY = booleanPreferencesKey("hide_phone_number_partially")
        val ENABLE_DARK_MODE = booleanPreferencesKey("enable_dark_mode")
        val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
        val DEFAULT_COUNTRY = stringPreferencesKey("default_country")
        val LANGUAGE = stringPreferencesKey("language")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .map { preferences ->
            AppSettings(
                defaultReminderTime = preferences[PreferencesKeys.DEFAULT_REMINDER_TIME] ?: "09:00",
                enableNotifications = preferences[PreferencesKeys.ENABLE_NOTIFICATIONS] ?: true,
                enableDailyCheck = preferences[PreferencesKeys.ENABLE_DAILY_CHECK] ?: true,
                hidePhoneNumberPartially = preferences[PreferencesKeys.HIDE_PHONE_NUMBER_PARTIALLY] ?: true,
                enableDarkMode = preferences[PreferencesKeys.ENABLE_DARK_MODE] ?: false,
                defaultCurrency = preferences[PreferencesKeys.DEFAULT_CURRENCY] ?: "GBP",
                defaultCountry = preferences[PreferencesKeys.DEFAULT_COUNTRY] ?: "UK",
                language = preferences[PreferencesKeys.LANGUAGE] ?: "zh"
            )
        }

    suspend fun updateReminderTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_REMINDER_TIME] = time
        }
    }

    suspend fun updateEnableNotifications(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_NOTIFICATIONS] = enabled
        }
    }

    suspend fun updateEnableDailyCheck(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_DAILY_CHECK] = enabled
        }
    }

    suspend fun updateHidePhoneNumberPartially(hide: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDE_PHONE_NUMBER_PARTIALLY] = hide
        }
    }

    suspend fun updateEnableDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_DARK_MODE] = enabled
        }
    }

    suspend fun updateDefaultCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_CURRENCY] = currency
        }
    }

    suspend fun updateDefaultCountry(country: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_COUNTRY] = country
        }
    }

    suspend fun updateLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = lang
        }
    }
}
