package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ReminderRecord
import com.example.data.local.entity.RuleTemplate
import com.example.data.local.entity.SIMCard
import com.example.data.local.entity.UsageRecord
import com.example.domain.rule.RuleEngine
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// Formats phone numbers partially as +44 75****20
fun formatPhoneNumber(number: String, partiallyHide: Boolean): String {
    if (number.isBlank()) return ""
    if (!partiallyHide || number.length < 6) return number
    
    // Hide central digits
    val clean = number.trim()
    val prefix = clean.take(4)
    val suffix = clean.takeLast(2)
    return "$prefix **** $suffix"
}

// 1. StatCard
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    valueColor: Color? = null
) {
    val finalValueColor = valueColor ?: contentColor

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(28.dp),
        modifier = modifier
            .height(115.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(28.dp), clip = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopStart),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                    color = finalValueColor,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.18f),
                modifier = Modifier
                    .size(72.dp)
                    .offset(x = 16.dp, y = 16.dp)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

// 2. StatusBadge
@Composable
fun StatusBadge(
    status: String,
    language: String = "en",
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, label) = when (status.uppercase()) {
        "HEALTHY" -> Triple(HealthyBackground, HealthyGreen, if (language == "zh") "正常在网" else "HEALTHY")
        "ATTENTION" -> Triple(WarningBackground, WarningOrange, if (language == "zh") "需要关注" else "ATTENTION")
        "RISK" -> Triple(RiskBackground, RiskRed, if (language == "zh") "到期预警" else "RISK")
        "EXPIRED" -> Triple(RiskBackground, RiskRed, if (language == "zh") "已失效" else "EXPIRED")
        else -> Triple(UnknownBackground, UnknownGray, if (language == "zh") "未知卡片" else "UNKNOWN")
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = textColor.copy(alpha = 0.25f),
                shape = CircleShape
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}

// 3. SimCardItem
@Composable
fun SimCardItem(
    simCard: SIMCard,
    rule: RuleTemplate?,
    hidePhone: Boolean,
    onCardClick: () -> Unit,
    language: String = "en",
    onRemindMeClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val usages = emptyList<UsageRecord>() // evaluation logic already cached in simCard.status
    val ruleEngineResult = RuleEngine.evaluate(simCard, rule, usages)

    val iconColor = when {
        simCard.carrier.lowercase().contains("giffgaff") -> Color(0xFF7C69EF) // Clean violet
        simCard.carrier.lowercase().contains("ctexcel") -> Color(0xFFF27D26) // Orange
        simCard.carrier.lowercase().contains("o2") -> Color(0xFF0019A8) // Blue
        simCard.carrier.lowercase().contains("ee") -> Color(0xFF007A87) // Teal
        simCard.carrier.lowercase().contains("vodafone") -> Color(0xFFE60000) // Red
        else -> Color(0xFF4B5563) // Slate
    }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(28.dp),
                clip = true
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                shape = RoundedCornerShape(28.dp)
            )
            .clickable { onCardClick() }
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Carrier Icon Bubble (Solid background like HTML!)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(iconColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = simCard.carrier.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    }

                    // Carrier and Phone
                    Column {
                        Text(
                            text = simCard.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = formatPhoneNumber(simCard.phoneNumber, hidePhone),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Status Badge
                StatusBadge(status = simCard.status, language = language)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle text (Network Carrier Model)
            Text(
                text = (if (language == "zh") "所属物理网络托管商: " else "Carrier: ") + simCard.networkProvider,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f)
            )

            // Bottom Requirement and Period Labels
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Next action requirement
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    val labelText = if (language == "zh") {
                        when (ruleEngineResult.labelText.uppercase()) {
                            "REQUIREMENT" -> "保号到期要求"
                            "NEXT ACTION" -> "下个保号动作"
                            "SERVICE ACTIVE" -> "定期自动保护生效中"
                            else -> ruleEngineResult.labelText.uppercase()
                        }
                    } else {
                        ruleEngineResult.labelText.uppercase()
                    }
                    Text(
                        text = labelText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (language == "zh") RuleEngine.getLocalizedNextAction(ruleEngineResult.nextAction, "zh") else ruleEngineResult.nextAction,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (simCard.status == "RISK") RiskRed else MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Remind Me Button
                    if (onRemindMeClick != null && (simCard.status == "RISK" || simCard.status == "ATTENTION")) {
                        Button(
                            onClick = onRemindMeClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LightPurple,
                                contentColor = PrimaryPurple
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.heightIn(min = 34.dp)
                        ) {
                            Text(if (language == "zh") "提醒我" else "Remind Me", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Rule duration badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${rule?.activePeriodDays ?: 180}" + if (language == "zh") "天周期" else "d cycle",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }
    }
}

// 4. BottomNavigationBar
@Composable
fun BottomNavigationBar(
    currentDestination: String,
    language: String = "en",
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple("dashboard", if (language == "zh") "保号仪表盘" else "Dashboard", Icons.Filled.Dashboard),
            Triple("rules", if (language == "zh") "巡回规则" else "Rules", Icons.Filled.Rule),
            Triple("history", if (language == "zh") "事件日志" else "History", Icons.Filled.History),
            Triple("settings", if (language == "zh") "更多设置" else "Settings", Icons.Filled.Settings)
        )

        items.forEach { (route, label, icon) ->
            val selected = currentDestination == route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(route) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label
                    )
                },
                label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryPurple,
                    unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    selectedTextColor = PrimaryPurple,
                    unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    indicatorColor = LightPurple
                )
            )
        }
    }
}

// 5. AddSimButton
@Composable
fun AddSimButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryPurple,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(8.dp, shape = CircleShape)
            .testTag("add_new_sim_button")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add New SIM Card",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// 6. RuleCard
@Composable
fun RuleCard(
    rule: RuleTemplate,
    onDuplicateClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null, // Custom rules can be deleted
    onEditClick: (() -> Unit)? = null,   // Custom rules can be edited
    language: String = "en",
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rule.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${rule.carrierName} • ${rule.country}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                // Built-in Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (rule.isBuiltIn) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            else HealthyBackground
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (rule.isBuiltIn) {
                            if (language == "zh") "系统预设" else "Built-in"
                        } else {
                            if (language == "zh") "自定义" else "Custom"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rule.isBuiltIn) PrimaryPurple else HealthyGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = if (language == "zh") "保号有效周期" else "ACTIVE TIMEFRAME",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Text(
                        text = if (language == "zh") "${rule.activePeriodDays} 天" else "${rule.activePeriodDays} Days",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = if (language == "zh") "警报提前通知天数" else "REMINDERS SENT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Text(
                        text = if (language == "zh") "前 -${rule.finalReminderDaysBefore}, -${rule.secondReminderDaysBefore}, -${rule.firstReminderDaysBefore} 天" else "-${rule.finalReminderDaysBefore}, -${rule.secondReminderDaysBefore}, -${rule.firstReminderDaysBefore} Days",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val mappedActions = rule.requiredActions.split(",")
                .map { act ->
                    when (act.trim().uppercase()) {
                        "SMS" -> if (language == "zh") "发送出站短信" else "SMS"
                        "CALL" -> if (language == "zh") "拨打出站电话" else "CALL"
                        "DATA" -> if (language == "zh") "触发蜂窝网流量" else "DATA"
                        "TOP_UP" -> if (language == "zh") "话费余额充值" else "TOP_UP"
                        "KEEP_ALIVE" -> if (language == "zh") "开启官方保号套餐自动代扣" else "KEEP_ALIVE"
                        else -> act
                    }
                }
                .joinToString(", ")

            Text(
                text = if (language == "zh") "评估支持动作: $mappedActions" else "Allowed Actions: $mappedActions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            if (rule.allowPaidKeepAliveService) {
                Spacer(modifier = Modifier.height(4.dp))
                val paidDesc = if (language == "zh") {
                    when (rule.paidKeepAliveDescription) {
                        "O2 Monthly contract support" -> "支持英国 O2 漫游合约保号套餐"
                        "Giffgaff supports automatic credit use" -> "支持英国 Giffgaff 定期余额保号扣费"
                        "CTExcel monthly AutoPay service support" -> "支持中国电信中英 CTExcel 托管代扣"
                        "EE 180-day keepalive automated premium" -> "支持英国 EE 180天定期自动保号抵扣"
                        "Vodafone 180-day automated paygo protection" -> "支持沃达丰 Vodafone 180天极简代扣保护"
                        else -> rule.paidKeepAliveDescription
                    }
                } else rule.paidKeepAliveDescription
                Text(
                    text = if (language == "zh") "支持自动付费保号服务: $paidDesc" else "Supports Keep Alive Service: $paidDesc",
                    style = MaterialTheme.typography.bodySmall,
                    color = HealthyGreen,
                    fontWeight = FontWeight.Medium
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
            )

            // Actions block
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onDuplicateClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = PrimaryPurple)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (language == "zh") "克隆" else "Duplicate")
                }

                if (onEditClick != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = onEditClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryPurple)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (language == "zh") "编辑" else "Edit")
                    }
                }

                if (!rule.isBuiltIn && onDeleteClick != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = RiskRed)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (language == "zh") "删除" else "Delete")
                    }
                }
            }
        }
    }
}

// 7. UsageRecordItem
@Composable
fun UsageRecordItem(
    record: UsageRecord,
    simCardName: String,
    onDeleteClick: () -> Unit,
    language: String = "en",
    modifier: Modifier = Modifier
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val formattedDate = sdf.format(Date(record.actionDate))

    val (icon, color) = when (record.actionType.uppercase()) {
        "CALL" -> Icons.Filled.Call to PrimaryPurple
        "SMS" -> Icons.Filled.Sms to PrimaryPurple
        "DATA" -> Icons.Filled.SettingsInputAntenna to PrimaryPurple
        "TOP_UP" -> Icons.Filled.DoubleArrow to HealthyGreen
        "KEEP_ALIVE_SERVICE", "KEEP_ALIVE" -> Icons.Filled.VerifiedUser to HealthyGreen
        else -> Icons.Filled.Receipt to UnknownGray
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 0.5.dp, shape = RoundedCornerShape(16.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }

                Column {
                    val actionLabel = when(record.actionType.uppercase()) {
                        "CALL" -> if (language == "zh") "拨通语音呼叫" else "CALL"
                        "SMS" -> if (language == "zh") "发送自检短信" else "SMS"
                        "MMS" -> if (language == "zh") "发送彩信" else "MMS"
                        "DATA" -> if (language == "zh") "产生蜂窝流量" else "DATA"
                        "TOP_UP" -> if (language == "zh") "话费余额充值" else "TOP_UP"
                        "KEEP_ALIVE" -> if (language == "zh") "官方代扣保号生命周期自检" else "KEEP_ALIVE"
                        "KEEP_ALIVE_SERVICE" -> if (language == "zh") "定期代扣服务保障" else "KEEP_ALIVE_SERVICE"
                        else -> record.actionType
                    }
                    Text(
                        text = "$simCardName - $actionLabel",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (record.amount != null) {
                        Text(
                            text = (if (language == "zh") "发生金额: " else "Amount: ") + "${record.amount} ${record.currency ?: ""}",
                            fontSize = 12.sp,
                            color = color,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (record.note.isNotBlank()) {
                        Text(
                            text = record.note,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = if (language == "zh") "删除此活动日志" else "Delete record",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// 8. ReminderItem
@Composable
fun ReminderItem(
    reminder: ReminderRecord,
    simCardName: String,
    language: String = "en",
    modifier: Modifier = Modifier
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val formattedDate = sdf.format(Date(reminder.remindAt))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 0.5.dp, shape = RoundedCornerShape(16.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(RiskRed.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = RiskRed,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = (if (language == "zh") "已发出到期报警: " else "Notification Sent: ") + simCardName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (language == "zh") RuleEngine.getLocalizedReminderMessage(reminder.message, "zh") else reminder.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
        }
    }
}
