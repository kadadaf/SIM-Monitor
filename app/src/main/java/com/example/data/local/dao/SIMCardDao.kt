package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ReminderRecord
import com.example.data.local.entity.RuleTemplate
import com.example.data.local.entity.SIMCard
import com.example.data.local.entity.UsageRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface SIMCardDao {

    // SIM Cards
    @Query("SELECT * FROM sim_cards ORDER BY updatedAt DESC")
    fun getAllSIMCardsFlow(): Flow<List<SIMCard>>

    @Query("SELECT * FROM sim_cards ORDER BY updatedAt DESC")
    suspend fun getAllSIMCards(): List<SIMCard>

    @Query("SELECT * FROM sim_cards WHERE id = :id")
    suspend fun getSIMCardById(id: Int): SIMCard?

    @Query("SELECT * FROM sim_cards WHERE id = :id")
    fun getSIMCardByIdFlow(id: Int): Flow<SIMCard?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSIMCard(sim: SIMCard): Long

    @Update
    suspend fun updateSIMCard(sim: SIMCard)

    @Delete
    suspend fun deleteSIMCard(sim: SIMCard)


    // Rule Templates
    @Query("SELECT * FROM rule_templates ORDER BY id ASC")
    fun getAllRuleTemplatesFlow(): Flow<List<RuleTemplate>>

    @Query("SELECT * FROM rule_templates ORDER BY id ASC")
    suspend fun getAllRuleTemplates(): List<RuleTemplate>

    @Query("SELECT * FROM rule_templates WHERE id = :id")
    suspend fun getRuleTemplateById(id: Int): RuleTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuleTemplate(rule: RuleTemplate): Long

    @Update
    suspend fun updateRuleTemplate(rule: RuleTemplate)

    @Delete
    suspend fun deleteRuleTemplate(rule: RuleTemplate)


    // Usage Records
    @Query("SELECT * FROM usage_records ORDER BY actionDate DESC")
    fun getAllUsageRecordsFlow(): Flow<List<UsageRecord>>

    @Query("SELECT * FROM usage_records ORDER BY actionDate DESC")
    suspend fun getAllUsageRecords(): List<UsageRecord>

    @Query("SELECT * FROM usage_records WHERE simCardId = :simId ORDER BY actionDate DESC")
    fun getUsageRecordsBySIM(simId: Int): Flow<List<UsageRecord>>

    @Query("SELECT * FROM usage_records WHERE simCardId = :simId ORDER BY actionDate DESC")
    suspend fun getUsageRecordsBySIMSync(simId: Int): List<UsageRecord>

    @Query("SELECT * FROM usage_records WHERE simCardId = :simId ORDER BY actionDate DESC LIMIT 1")
    suspend fun getLatestUsageRecordBySIM(simId: Int): UsageRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageRecord(record: UsageRecord): Long

    @Update
    suspend fun updateUsageRecord(record: UsageRecord)

    @Delete
    suspend fun deleteUsageRecord(record: UsageRecord)


    // Reminder Records
    @Query("SELECT * FROM reminder_records ORDER BY remindAt DESC")
    fun getAllRemindersFlow(): Flow<List<ReminderRecord>>

    @Query("SELECT * FROM reminder_records ORDER BY remindAt DESC")
    suspend fun getAllReminders(): List<ReminderRecord>

    @Query("SELECT * FROM reminder_records WHERE simCardId = :simId ORDER BY remindAt DESC")
    fun getRemindersBySIM(simId: Int): Flow<List<ReminderRecord>>

    @Query("SELECT * FROM reminder_records WHERE simCardId = :simId AND ruleId = :ruleId AND reminderType = :type LIMIT 1")
    suspend fun getReminderBySIMRuleAndType(simId: Int, ruleId: Int, type: String): ReminderRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminderRecord(record: ReminderRecord): Long

    @Update
    suspend fun updateReminderRecord(record: ReminderRecord)

    @Delete
    suspend fun deleteReminderRecord(record: ReminderRecord)
}
