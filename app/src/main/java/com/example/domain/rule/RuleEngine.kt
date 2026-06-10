package com.example.domain.rule

import com.example.data.local.entity.RuleTemplate
import com.example.data.local.entity.SIMCard
import com.example.data.local.entity.UsageRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

data class RuleResult(
    val status: String, // HEALTHY, ATTENTION, RISK, EXPIRED, UNKNOWN
    val expiryDate: Long, // timestamp
    val daysRemaining: Int,
    val nextAction: String, // e.g., "SMS in 42 days" or "Top-up within 5 days"
    val isActionRequired: Boolean,
    val reminderMessage: String,
    val labelText: String // "NEXT ACTION" or "REQUIREMENT"
)

object RuleEngine {

    fun getDefaultActionForRule(ruleName: String): String {
        val lower = ruleName.lowercase()
        return when {
            lower.contains("giffgaff") -> "SMS"
            lower.contains("ctexcel") -> "Top-up"
            else -> "SMS or Call"
        }
    }

    fun evaluate(
        simCard: SIMCard,
        rule: RuleTemplate?,
        usages: List<UsageRecord>,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): RuleResult {
        if (rule == null) {
            return RuleResult(
                status = "UNKNOWN",
                expiryDate = simCard.activationDate,
                daysRemaining = 0,
                nextAction = "No active rule template",
                isActionRequired = false,
                reminderMessage = "${simCard.name} has no associated rule.",
                labelText = "REQUIREMENT"
            )
        }

        // 1. Check if there is an active keep-alive paid service that overrides standard rule.
        // Let's check for "KEEP_ALIVE_SERVICE" usage record.
        val lastPaidKeepAlive = usages.firstOrNull { it.actionType.uppercase() == "KEEP_ALIVE_SERVICE" }
        if (rule.allowPaidKeepAliveService && lastPaidKeepAlive != null) {
            // Assume the service provides safety for 30 days after the action
            val serviceDuration = 30 * 24 * 3600 * 1000L
            val serviceExpiry = lastPaidKeepAlive.actionDate + serviceDuration
            if (serviceExpiry > currentTimeMillis) {
                val serviceDaysRemaining = ((serviceExpiry - currentTimeMillis) / (24 * 1000 * 60 * 60L)).toInt()
                return RuleResult(
                    status = "HEALTHY",
                    expiryDate = serviceExpiry,
                    daysRemaining = serviceDaysRemaining,
                    nextAction = "Service active for $serviceDaysRemaining days",
                    isActionRequired = false,
                    reminderMessage = "Keep Alive service is active for ${simCard.name}",
                    labelText = "SERVICE ACTIVE"
                )
            }
        }

        // 2. Check for new card activation rule:
        // If has new card activation rule, and NO usage records exist yet, count from activationDate
        val validUsages = if (rule.requiredActions.isBlank()) {
            usages
        } else {
            val requiredList = rule.requiredActions.split(",").map { it.trim().uppercase() }
            usages.filter { requiredList.contains(it.actionType.uppercase()) }
        }

        if (rule.hasNewCardActivationRule && validUsages.isEmpty()) {
            val activationDeadline = simCard.activationDate + (rule.newCardActivationDays * 24 * 3600 * 1000L)
            val activationDaysRemaining = ((activationDeadline - currentTimeMillis) / (24 * 1000 * 60 * 60L)).toInt()

            return when {
                activationDaysRemaining > 7 -> {
                    RuleResult(
                        status = "ATTENTION",
                        expiryDate = activationDeadline,
                        daysRemaining = activationDaysRemaining,
                        nextAction = "Activate SIM within $activationDaysRemaining days",
                        isActionRequired = true,
                        reminderMessage = "Please activate ${simCard.name} within $activationDaysRemaining days.",
                        labelText = "REQUIREMENT"
                    )
                }
                activationDaysRemaining in 1..7 -> {
                    RuleResult(
                        status = "RISK",
                        expiryDate = activationDeadline,
                        daysRemaining = activationDaysRemaining,
                        nextAction = "Activate SIM within $activationDaysRemaining days",
                        isActionRequired = true,
                        reminderMessage = "URGENT: Activate ${simCard.name} within $activationDaysRemaining days!",
                        labelText = "REQUIREMENT"
                    )
                }
                else -> {
                    RuleResult(
                        status = "EXPIRED",
                        expiryDate = activationDeadline,
                        daysRemaining = activationDaysRemaining,
                        nextAction = "Activation expired",
                        isActionRequired = true,
                        reminderMessage = "${simCard.name} activation period has expired.",
                        labelText = "REQUIREMENT"
                    )
                }
            }
        }

        // 3. Standard rule evaluation logic:
        val calculationStart = if (validUsages.isNotEmpty()) {
            max(simCard.activationDate, validUsages.first().actionDate)
        } else {
            simCard.activationDate
        }

        val expiryDate = calculationStart + (rule.activePeriodDays * 24 * 3600 * 1000L)
        val daysRemaining = ((expiryDate - currentTimeMillis) / (24 * 3600 * 1000L)).toInt()

        val status = when {
            daysRemaining > 30 -> "HEALTHY"
            daysRemaining in 8..30 -> "ATTENTION"
            daysRemaining in 1..7 -> "RISK"
            else -> "EXPIRED"
        }

        val defaultAction = getDefaultActionForRule(rule.name)
        val term = if (status == "RISK") "within" else "in"
        val nextAction = if (daysRemaining > 0) {
            "$defaultAction $term ${if (daysRemaining == 1) "1 day" else "$daysRemaining days"}"
        } else {
            "SIM card expired"
        }

        val labelText = if (status == "RISK") "REQUIREMENT" else "NEXT ACTION"

        val actionTypeHint = if (defaultAction == "SMS") "sending an SMS or calling" else "making a top-up"
        val reminderMessage = when (status) {
            "HEALTHY" -> "${simCard.name} is in healthy state. Next action: $nextAction."
            "ATTENTION" -> "${simCard.name} should perform $actionTypeHint in $daysRemaining days."
            "RISK" -> "Urgent: ${simCard.name} needs a $actionTypeHint within $daysRemaining days to stay active."
            else -> "Warning: ${simCard.name} has expired. Please check immediately!"
        }

        return RuleResult(
            status = status,
            expiryDate = expiryDate,
            daysRemaining = daysRemaining,
            nextAction = nextAction,
            isActionRequired = status == "RISK" || status == "EXPIRED" || status == "ATTENTION",
            reminderMessage = reminderMessage,
            labelText = labelText
        )
    }

    fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun getLocalizedNextAction(nextAction: String, language: String): String {
        if (language != "zh") return nextAction
        if (nextAction == "No active rule template") return "未关联保号评估模板"
        if (nextAction.startsWith("Service active for")) {
            val days = nextAction.filter { it.isDigit() }
            return "代扣保号服务保护中（剩余 $days 天）"
        }
        if (nextAction.startsWith("Activate SIM within")) {
            val days = nextAction.filter { it.isDigit() }
            return "请在 $days 天内充值激活SIM卡"
        }
        if (nextAction == "Activation expired") return "首期激活期已期满超限"
        if (nextAction == "SIM card expired") return "卡片已到期失效"

        val lowercase = nextAction.lowercase()
        val days = nextAction.filter { it.isDigit() }
        val actionType = when {
            lowercase.contains("sms") -> "发送短信/呼出"
            lowercase.contains("top-up") || lowercase.contains("top up") -> "充值余额"
            lowercase.contains("call") -> "拨通电话"
            else -> "保号动作巡检"
        }
        val term = if (lowercase.contains("within")) "以内" else "内"
        return "在 $days 天${term}完成 $actionType"
    }

    fun getLocalizedReminderMessage(msg: String, language: String): String {
        if (language != "zh") return msg
        if (msg.contains("no associated rule")) return "该SIM卡大区暂未与之绑定任何保号巡检机制。"
        if (msg.contains("Keep Alive service is active for")) {
            val simName = msg.substringAfter("active for ").removeSuffix(".")
            return "${simName} 绑定的官方自动代扣保号已经生效，安全运行中。"
        }
        if (msg.contains("Please activate") && msg.contains("within")) {
            val parts = msg.split(" ")
            val days = parts.find { it.all { char -> char.isDigit() } } ?: "0"
            return "新卡导入提示：请及时在首次激活期限 ${days} 天之内完成充值激活。"
        }
        if (msg.contains("URGENT: Activate") && msg.contains("within")) {
            val parts = msg.split(" ")
            val days = parts.find { it.all { char -> char.isDigit() } } ?: "0"
            return "极其紧急：首次激活宽限期仅剩最后的 ${days} 天，请立即操作！"
        }
        if (msg.contains("activation period has expired")) {
            return "卡片由于未能在首次规定的激活期限内完成动作核验，判定已被封号或无法使用。"
        }
        if (msg.contains("healthy state")) {
            val parts = msg.split("is in healthy state. Next action: ")
            val simName = parts.getOrNull(0)?.trim() ?: "SIM 卡"
            val rawAction = parts.getOrNull(1)?.removeSuffix(".") ?: ""
            return "${simName} 当前处于合规健康状态。下期动作提醒：${getLocalizedNextAction(rawAction, "zh")}。"
        }
        if (msg.contains("should perform") && msg.contains("in")) {
            val parts = msg.split(" ")
            val days = parts.find { it.all { char -> char.isDigit() } } ?: "0"
            val action = if (msg.contains("sending an") || msg.contains("SMS")) "发送短信/呼出" else "充值话费余额"
            return "建议提示：您应该最迟在 $days 天后完成一次 $action 自检进行存续保号。"
        }
        if (msg.contains("needs a") && msg.contains("within")) {
            val parts = msg.split(" ")
            val days = parts.find { it.all { char -> char.isDigit() } } ?: "0"
            val action = if (msg.contains("sending an") || msg.contains("SMS")) "发送自检短信或通话" else "给卡片进行余额充值"
            return "严重警告：您的卡片即将到期失效，务必在 ${days} 天以内完成 [$action]！"
        }
        if (msg.contains("has expired. Please check immediately")) {
            return "极度危险：该 SIM 卡由于长时间缺少活跃动作已被判定为失效过期。请立刻核实物理卡状态！"
        }
        return msg
    }
}
