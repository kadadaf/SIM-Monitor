package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sim_cards")
data class SIMCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val country: String,
    val carrier: String,
    val networkProvider: String,
    val cardType: String, // e.g. "Pay As You Go", "Monthly Plan", "Custom"
    val activationDate: Long,
    val lastActiveDate: Long,
    val lastTopUpDate: Long,
    val balance: Double?,
    val currency: String, // e.g. "GBP", "USD", "CNY"
    val ruleId: Int,
    val status: String, // HEALTHY, ATTENTION, RISK, EXPIRED, UNKNOWN
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "rule_templates")
data class RuleTemplate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val carrierName: String,
    val country: String,
    val activePeriodDays: Int,
    val firstReminderDaysBefore: Int, // e.g. 30
    val secondReminderDaysBefore: Int, // e.g. 14
    val finalReminderDaysBefore: Int, // e.g. 5
    val requiredActions: String, // Comma separated actions, e.g. "CALL,SMS,DATA,TOP_UP"
    val minSpendAmount: Double? = null,
    val hasNewCardActivationRule: Boolean = false,
    val newCardActivationDays: Int = 10,
    val allowPaidKeepAliveService: Boolean = false,
    val paidKeepAliveDescription: String = "",
    val isBuiltIn: Boolean = false,
    val isEnabled: Boolean = true
) {
    // Helper to check if an action is supported under this rule
    fun supportsAction(action: String): Boolean {
        if (requiredActions.isBlank()) return true
        val list = requiredActions.split(",").map { it.trim().uppercase() }
        return list.contains(action.trim().uppercase())
    }
}

@Entity(tableName = "usage_records")
data class UsageRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val simCardId: Int,
    val actionType: String, // CALL, SMS, MMS, DATA, TOP_UP, PLAN_PURCHASE, BALANCE_CHANGE, KEEP_ALIVE_SERVICE, OTHER
    val amount: Double? = null,
    val currency: String? = null,
    val actionDate: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "reminder_records")
data class ReminderRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val simCardId: Int,
    val ruleId: Int,
    val reminderType: String, // FIRST_REMINDER, SECOND_REMINDER, FINAL_REMINDER, EXPIRED_ALERT, CUSTOM
    val remindAt: Long,
    val message: String,
    val isSent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
