package com.example.data.repository

import com.example.data.local.dao.SIMCardDao
import com.example.data.local.entity.ReminderRecord
import com.example.data.local.entity.RuleTemplate
import com.example.data.local.entity.SIMCard
import com.example.data.local.entity.UsageRecord
import com.example.domain.rule.RuleEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.max

class SIMRepository(private val dao: SIMCardDao) {

    val allSIMCardsFlow: Flow<List<SIMCard>> = dao.getAllSIMCardsFlow()
    val allRuleTemplatesFlow: Flow<List<RuleTemplate>> = dao.getAllRuleTemplatesFlow()
    val allUsageRecordsFlow: Flow<List<UsageRecord>> = dao.getAllUsageRecordsFlow()
    val allRemindersFlow: Flow<List<ReminderRecord>> = dao.getAllRemindersFlow()

    suspend fun getAllSIMCards(): List<SIMCard> = dao.getAllSIMCards()
    suspend fun getSIMCardById(id: Int): SIMCard? = dao.getSIMCardById(id)
    fun getSIMCardByIdFlow(id: Int): Flow<SIMCard?> = dao.getSIMCardByIdFlow(id)

    suspend fun insertSIMCard(sim: SIMCard): Long {
        val simId = dao.insertSIMCard(sim)
        val insertedSim = dao.getSIMCardById(simId.toInt())
        if (insertedSim != null) {
            recalculateSIMStatus(insertedSim)
        }
        return simId
    }

    suspend fun updateSIMCard(sim: SIMCard) {
        dao.updateSIMCard(sim)
        recalculateSIMStatus(sim)
    }

    suspend fun deleteSIMCard(sim: SIMCard) {
        dao.deleteSIMCard(sim)
    }

    // Rules
    suspend fun getAllRuleTemplates(): List<RuleTemplate> = dao.getAllRuleTemplates()
    suspend fun getRuleTemplateById(id: Int): RuleTemplate? = dao.getRuleTemplateById(id)
    suspend fun insertRuleTemplate(rule: RuleTemplate): Long = dao.insertRuleTemplate(rule)
    suspend fun updateRuleTemplate(rule: RuleTemplate) = dao.updateRuleTemplate(rule)
    suspend fun deleteRuleTemplate(rule: RuleTemplate) = dao.deleteRuleTemplate(rule)

    // Usage Records
    fun getUsageRecordsBySIM(simId: Int): Flow<List<UsageRecord>> = dao.getUsageRecordsBySIM(simId)

    suspend fun insertUsageRecord(record: UsageRecord): Long {
        val id = dao.insertUsageRecord(record)
        val sim = dao.getSIMCardById(record.simCardId)
        if (sim != null) {
            val usages = dao.getUsageRecordsBySIMSync(sim.id)
            val latestActiveDate = usages.firstOrNull { it.actionType != "TOP_UP" && it.actionType != "BALANCE_CHANGE" }?.actionDate ?: sim.lastActiveDate
            val latestTopUpDate = usages.firstOrNull { it.actionType == "TOP_UP" }?.actionDate ?: sim.lastTopUpDate
            
            var newBalance = sim.balance
            if (record.actionType.uppercase() == "TOP_UP" && record.amount != null) {
                newBalance = (sim.balance ?: 0.0) + record.amount
            } else if (record.actionType.uppercase() == "BALANCE_CHANGE" && record.amount != null) {
                newBalance = record.amount
            }

            val updatedSim = sim.copy(
                lastActiveDate = max(sim.lastActiveDate, latestActiveDate),
                lastTopUpDate = max(sim.lastTopUpDate, latestTopUpDate),
                balance = newBalance,
                updatedAt = System.currentTimeMillis()
            )
            dao.updateSIMCard(updatedSim)
            recalculateSIMStatus(updatedSim)
        }
        return id
    }

    suspend fun deleteUsageRecord(record: UsageRecord) {
        dao.deleteUsageRecord(record)
        val sim = dao.getSIMCardById(record.simCardId)
        if (sim != null) {
            recalculateSIMStatus(sim)
        }
    }

    // Reminders
    fun getRemindersBySIM(simId: Int): Flow<List<ReminderRecord>> = dao.getRemindersBySIM(simId)
    suspend fun insertReminderRecord(record: ReminderRecord): Long = dao.insertReminderRecord(record)
    suspend fun updateReminderRecord(record: ReminderRecord) = dao.updateReminderRecord(record)

    suspend fun getReminderBySIMRuleAndType(simId: Int, ruleId: Int, type: String): ReminderRecord? {
        return dao.getReminderBySIMRuleAndType(simId, ruleId, type)
    }

    // Recalculates the status of a specific SIM Card based on the linked rules and recent usage logs
    suspend fun recalculateSIMStatus(sim: SIMCard) {
        val rule = dao.getRuleTemplateById(sim.ruleId)
        val usages = dao.getUsageRecordsBySIMSync(sim.id)
        val result = RuleEngine.evaluate(sim, rule, usages)

        if (sim.status != result.status) {
            val updatedSim = sim.copy(
                status = result.status,
                updatedAt = System.currentTimeMillis()
            )
            dao.updateSIMCard(updatedSim)
        }
    }

    // Automatically recalculates all active cards
    suspend fun recalculateAllSIMCards() {
        val sims = dao.getAllSIMCards()
        for (sim in sims) {
            recalculateSIMStatus(sim)
        }
    }

    // Prepulate default rules on startup
    suspend fun prepopulateDefaultRules() {
        val existing = dao.getAllRuleTemplates()
        if (existing.isEmpty()) {
            val rules = listOf(
                RuleTemplate(
                    id = 1,
                    name = "Giffgaff UK PAYG Keep Alive Rule",
                    carrierName = "Giffgaff",
                    country = "UK",
                    activePeriodDays = 180,
                    firstReminderDaysBefore = 30,
                    secondReminderDaysBefore = 14,
                    finalReminderDaysBefore = 5,
                    requiredActions = "CALL,SMS,MMS,DATA,TOP_UP,PLAN_PURCHASE",
                    minSpendAmount = null,
                    hasNewCardActivationRule = false,
                    newCardActivationDays = 10,
                    allowPaidKeepAliveService = false,
                    paidKeepAliveDescription = "",
                    isBuiltIn = true,
                    isEnabled = true
                ),
                RuleTemplate(
                    id = 2,
                    name = "CTExcel UK Keep Alive Rule",
                    carrierName = "CTExcel",
                    country = "UK",
                    activePeriodDays = 180,
                    firstReminderDaysBefore = 30,
                    secondReminderDaysBefore = 14,
                    finalReminderDaysBefore = 5,
                    requiredActions = "CALL,SMS,DATA,TOP_UP,PLAN_PURCHASE,BALANCE_CHANGE",
                    minSpendAmount = null,
                    hasNewCardActivationRule = true,
                    newCardActivationDays = 10,
                    allowPaidKeepAliveService = true,
                    paidKeepAliveDescription = "£1/month Keep Alive Service",
                    isBuiltIn = true,
                    isEnabled = true
                ),
                RuleTemplate(
                    id = 3,
                    name = "Generic 180 Days Keep Alive Rule",
                    carrierName = "Generic",
                    country = "Global",
                    activePeriodDays = 180,
                    firstReminderDaysBefore = 30,
                    secondReminderDaysBefore = 14,
                    finalReminderDaysBefore = 5,
                    requiredActions = "CALL,SMS,DATA,TOP_UP,PLAN_PURCHASE",
                    minSpendAmount = null,
                    hasNewCardActivationRule = false,
                    newCardActivationDays = 10,
                    allowPaidKeepAliveService = false,
                    paidKeepAliveDescription = "",
                    isBuiltIn = true,
                    isEnabled = true
                )
            )
            for (rule in rules) {
                dao.insertRuleTemplate(rule)
            }
        }
    }

    suspend fun prepopulateSampleData() {
        // Only run if no sim cards exist
        val existingSIMs = dao.getAllSIMCards()
        if (existingSIMs.isEmpty()) {
            // Inserts 4 starting cards
            // 1. Giffgaff healthy
            val giffgaffId = insertSIMCard(
                SIMCard(
                    name = "Giffgaff UK",
                    phoneNumber = "+44 7520 123456",
                    country = "UK",
                    carrier = "Giffgaff",
                    networkProvider = "O2 Network",
                    cardType = "Pay As You Go",
                    activationDate = System.currentTimeMillis() - 138 * 24 * 3600 * 1000L, // 138 days ago
                    lastActiveDate = System.currentTimeMillis() - 138 * 24 * 3600 * 1000L,
                    lastTopUpDate = System.currentTimeMillis() - 138 * 24 * 3600 * 1000L,
                    balance = 10.0,
                    currency = "GBP",
                    ruleId = 1,
                    status = "HEALTHY"
                )
            )

            // Usages for Giffgaff
            insertUsageRecord(
                UsageRecord(
                    simCardId = giffgaffId.toInt(),
                    actionType = "TOP_UP",
                    amount = 10.0,
                    currency = "GBP",
                    actionDate = System.currentTimeMillis() - 138 * 24 * 3600 * 1000L,
                    note = "Initial Top-up"
                )
            )

            // 2. CTExcel UK (Risky)
            val ctexcelId = insertSIMCard(
                SIMCard(
                    name = "CTExcel UK",
                    phoneNumber = "+44 7721 987654",
                    country = "UK",
                    carrier = "CTExcel",
                    networkProvider = "EE Network",
                    cardType = "Pay As You Go",
                    activationDate = System.currentTimeMillis() - 175 * 24 * 3600 * 1000L, // 175 days ago (meaning 5 days left)
                    lastActiveDate = System.currentTimeMillis() - 175 * 24 * 3600 * 1000L,
                    lastTopUpDate = System.currentTimeMillis() - 175 * 24 * 3600 * 1000L,
                    balance = 5.5,
                    currency = "GBP",
                    ruleId = 2,
                    status = "RISK"
                )
            )

            insertUsageRecord(
                UsageRecord(
                    simCardId = ctexcelId.toInt(),
                    actionType = "TOP_UP",
                    amount = 5.0,
                    currency = "GBP",
                    actionDate = System.currentTimeMillis() - 175 * 24 * 3600 * 1000L,
                    note = "Activation and Top-up"
                )
            )

            // 3. Ultra Mobile (Attention)
            val ultraId = insertSIMCard(
                SIMCard(
                    name = "Ultra Mobile US",
                    phoneNumber = "+1 212 5550199",
                    country = "US",
                    carrier = "Ultra Mobile",
                    networkProvider = "T-Mobile",
                    cardType = "Pay As You Go",
                    activationDate = System.currentTimeMillis() - 160 * 24 * 3600 * 1000L, // 20 days left
                    lastActiveDate = System.currentTimeMillis() - 160 * 24 * 3600 * 1000L,
                    lastTopUpDate = System.currentTimeMillis() - 160 * 24 * 3600 * 1000L,
                    balance = 3.00,
                    currency = "USD",
                    ruleId = 3, // Generic 180 Days rule
                    status = "ATTENTION"
                )
            )

            // 4. Lycamobile UK (Healthy, recently texted)
            val lycaId = insertSIMCard(
                SIMCard(
                    name = "Lycamobile UK",
                    phoneNumber = "+44 7522 654321",
                    country = "UK",
                    carrier = "Lycamobile",
                    networkProvider = "O2 Network",
                    cardType = "Pay As You Go",
                    activationDate = System.currentTimeMillis() - 120 * 24 * 3600 * 1000L,
                    lastActiveDate = System.currentTimeMillis() - 10 * 24 * 3600 * 1000L, // 10 days ago (text sent)
                    lastTopUpDate = System.currentTimeMillis() - 120 * 24 * 3600 * 1000L,
                    balance = 12.50,
                    currency = "GBP",
                    ruleId = 3,
                    status = "HEALTHY"
                )
            )
            
            insertUsageRecord(
                UsageRecord(
                    simCardId = lycaId.toInt(),
                    actionType = "SMS",
                    amount = null,
                    actionDate = System.currentTimeMillis() - 10 * 24 * 3600 * 1000L,
                    note = "Sent text to bank to keep alive"
                )
            )
        }
    }

    suspend fun clearAllData() {
        // Safely wipe out.
        // We'll recalculate. We can drop items by using raw SQL, or we can just delete SIM cards and keep templates, but wait: we can implement resetting database inside ViewModel/Repository.
    }
}
