package com.example

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.local.database.AppDatabase
import com.example.data.repository.SIMRepository
import com.example.settings.SettingsDataStore
import com.example.worker.SimCheckWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SIMMonitorApp : Application() {

    lateinit var database: AppDatabase
    lateinit var repository: SIMRepository
    lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate() {
        super.onCreate()
        
        database = AppDatabase.getDatabase(this)
        repository = SIMRepository(database.simCardDao())
        settingsDataStore = SettingsDataStore(this)

        // Prepopulate rules and schedule background checker in a background thread
        CoroutineScope(Dispatchers.IO).launch {
            repository.prepopulateDefaultRules()
            repository.prepopulateSampleData() // Safe starter mock items as requested by user
            repository.recalculateAllSIMCards()
            setupDailyBackgroundCheck()
        }
    }

    private fun setupDailyBackgroundCheck() {
        val checkRequest = PeriodicWorkRequestBuilder<SimCheckWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(15, TimeUnit.MINUTES) // Soft initial wait to avoid blocking initial load
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SimCheckDailyWork",
            ExistingPeriodicWorkPolicy.KEEP, // Prevent restarting/rescheduling if already queued
            checkRequest
        )
    }
}
