package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.ReminderRecord
import com.example.data.repository.SIMRepository
import com.example.domain.rule.RuleEngine
import com.example.notification.NotificationHelper
import com.example.settings.SettingsDataStore
import kotlinx.coroutines.flow.first

class SimCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = SIMRepository(database.simCardDao())
        val settingsDataStore = SettingsDataStore(applicationContext)

        val settings = settingsDataStore.settingsFlow.first()
        if (!settings.enableNotifications || !settings.enableDailyCheck) {
            return Result.success()
        }

        // Fetch all cards and evaluate
        val simCards = repository.getAllSIMCards()
        val notificationHelper = NotificationHelper(applicationContext)

        for (sim in simCards) {
            val rule = sim.ruleId.let { repository.getRuleTemplateById(it) } ?: continue
            val usages = database.simCardDao().getUsageRecordsBySIMSync(sim.id)
            val result = RuleEngine.evaluate(sim, rule, usages)

            // Dynamic sync of DB status just in case
            if (sim.status != result.status) {
                repository.updateSIMCard(sim.copy(status = result.status, updatedAt = System.currentTimeMillis()))
            }

            // Check if reminder is needed
            // Determine type of reminder based on daysRemaining
            val reminderType = when (result.daysRemaining) {
                rule.firstReminderDaysBefore -> "FIRST_REMINDER"
                rule.secondReminderDaysBefore -> "SECOND_REMINDER"
                rule.finalReminderDaysBefore -> "FINAL_REMINDER"
                0 -> "EXPIRED_ALERT"
                else -> {
                    // Urgent alerts for risk items
                    if (result.daysRemaining in 1..rule.finalReminderDaysBefore) {
                        "RISK_REMINDER"
                    } else {
                        null
                    }
                }
            }

            if (reminderType != null) {
                // Check if this specific alert has already been logged and sent
                // (Or check if we sent a notification for this sim card in the last 24 hours to prevent duplicate spamming)
                val existing = repository.getReminderBySIMRuleAndType(sim.id, rule.id, reminderType)
                if (existing == null) {
                    // Save reminder record
                    val reminder = ReminderRecord(
                        simCardId = sim.id,
                        ruleId = rule.id,
                        reminderType = reminderType,
                        remindAt = System.currentTimeMillis(),
                        message = result.reminderMessage,
                        isSent = true
                    )
                    repository.insertReminderRecord(reminder)

                    // Trigger notification
                    notificationHelper.sendNotification(
                        title = "SIM Monitor Alert",
                        message = result.reminderMessage,
                        simId = sim.id
                    )
                }
            }
        }

        return Result.success()
    }
}
