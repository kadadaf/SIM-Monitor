package com.example.ui.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.settings.Loc
import com.example.ui.theme.BackgroundSoft
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.RiskRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRuleScreen(
    viewModel: RulesViewModel,
    ruleId: Int? = null,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val rules by viewModel.ruleTemplates.collectAsStateWithLifecycle()

    // Input state controls
    var name by remember { mutableStateOf("") }
    var carrierName by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var maxPeriodText by remember { mutableStateOf("180") }

    var firstReminderText by remember { mutableStateOf("30") }
    var secondReminderText by remember { mutableStateOf("14") }
    var finalReminderText by remember { mutableStateOf("5") }

    // Multi-select actions supported
    val actionOptions = listOf("CALL", "SMS", "MMS", "DATA", "TOP_UP", "PLAN_PURCHASE", "BALANCE_CHANGE")
    val selectedActions = remember { mutableStateOf(setOf("CALL", "SMS", "DATA", "TOP_UP")) }

    var minSpendText by remember { mutableStateOf("") }

    var hasActivationRule by remember { mutableStateOf(false) }
    var activationDaysText by remember { mutableStateOf("10") }

    var allowKeepAlive by remember { mutableStateOf(false) }
    var keepAliveDesc by remember { mutableStateOf("") }

    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(ruleId) {
        if (ruleId != null) {
            val rule = viewModel.getRuleTemplateById(ruleId)
            if (rule != null) {
                name = rule.name
                carrierName = rule.carrierName
                country = rule.country
                maxPeriodText = rule.activePeriodDays.toString()
                firstReminderText = rule.firstReminderDaysBefore.toString()
                secondReminderText = rule.secondReminderDaysBefore.toString()
                finalReminderText = rule.finalReminderDaysBefore.toString()
                selectedActions.value = rule.requiredActions.split(",").filter { it.isNotBlank() }.toSet()
                minSpendText = rule.minSpendAmount?.toString() ?: ""
                hasActivationRule = rule.hasNewCardActivationRule
                activationDaysText = rule.newCardActivationDays.toString()
                allowKeepAlive = rule.allowPaidKeepAliveService
                keepAliveDesc = rule.paidKeepAliveDescription
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val titleText = if (ruleId == null) {
                        Loc.t("create_custom_rule", settings.language)
                    } else {
                        val isBuiltIn = rules.find { it.id == ruleId }?.isBuiltIn ?: false
                        if (isBuiltIn) {
                            if (settings.language == "zh") "编辑内置保号规则" else "Edit Built-in Rule"
                        } else {
                            Loc.t("edit_custom_rule", settings.language)
                        }
                    }
                    Text(titleText, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (settings.language == "zh") "返回" else "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundSoft)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp, start = 20.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = if (settings.language == "zh") "在下方自定义保号评估触发条件和预警视窗。" else "Customize keep-alive evaluation triggers and warning windows below.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                // 1. Basics
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = if (settings.language == "zh") "基础保号规则描述" else "RULE BASE DESCRIPTION",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = PrimaryPurple
                            )

                            OutlinedTextField(
                                value = name,
                                onValueChange = {
                                    name = it
                                    isError = false
                                },
                                label = { Text(if (settings.language == "zh") "保号规则模板名称" else "Rule Preset Name") },
                                placeholder = { Text(if (settings.language == "zh") "例如：Giffgaff 英国保号" else "e.g. Giffgaff UK PAYG Keep Alive") },
                                isError = isError && name.isBlank(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = carrierName,
                                    onValueChange = { carrierName = it },
                                    label = { Text(if (settings.language == "zh") "移动提供商 / 运营商" else "Carrier") },
                                    placeholder = { Text(if (settings.language == "zh") "例如：CTExcel" else "e.g. CTExcel") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = country,
                                    onValueChange = { country = it },
                                    label = { Text(if (settings.language == "zh") "国家 / 地区" else "Country") },
                                    placeholder = { Text(if (settings.language == "zh") "例如：英国/UK" else "e.g. UK") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // 2. Timing rules
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = if (settings.language == "zh") "有效期周期与预警视窗" else "ACTIVE PERIODS & TIMEOUT WINDOWS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = PrimaryPurple
                            )

                            OutlinedTextField(
                                value = maxPeriodText,
                                onValueChange = { maxPeriodText = it },
                                label = { Text(if (settings.language == "zh") "单次保号最长有效期 (天)" else "Keep Alive Validity Cycle (Days)") },
                                placeholder = { Text(if (settings.language == "zh") "例如：180" else "e.g. 180") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = if (settings.language == "zh") "设置在到期前多少天发送警报提醒：" else "Configure alert offset days prior to expiration:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = firstReminderText,
                                    onValueChange = { firstReminderText = it },
                                    label = { Text(if (settings.language == "zh") "第1次提醒" else "1st Alert") },
                                    placeholder = { Text("30") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = secondReminderText,
                                    onValueChange = { secondReminderText = it },
                                    label = { Text(if (settings.language == "zh") "第2次提醒" else "2nd Alert") },
                                    placeholder = { Text("14") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = finalReminderText,
                                    onValueChange = { finalReminderText = it },
                                    label = { Text(if (settings.language == "zh") "最后提醒" else "Final Alert") },
                                    placeholder = { Text("5") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // 3. Supported Action Types
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (settings.language == "zh") "支持的官方保号自检动作" else "VALID KEEP ALIVE ACTIONS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = PrimaryPurple
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            actionOptions.forEach { action ->
                                val isChecked = selectedActions.value.contains(action)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                ) {
                                    val actionLabel = when(action) {
                                        "CALL" -> if (settings.language == "zh") "拨打电话" else "CALL"
                                        "SMS" -> if (settings.language == "zh") "发送短信" else "SMS"
                                        "MMS" -> if (settings.language == "zh") "发送彩信" else "MMS"
                                        "DATA" -> if (settings.language == "zh") "行动数据上网" else "DATA"
                                        "TOP_UP" -> if (settings.language == "zh") "充值话费储值" else "TOP_UP"
                                        "PLAN_PURCHASE" -> if (settings.language == "zh") "购买流量/语音套餐" else "PLAN_PURCHASE"
                                        "BALANCE_CHANGE" -> if (settings.language == "zh") "余额变动" else "BALANCE_CHANGE"
                                        else -> action
                                    }
                                    Text(
                                        text = actionLabel,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            val current = selectedActions.value.toMutableSet()
                                            if (checked) current.add(action) else current.remove(action)
                                            selectedActions.value = current
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = PrimaryPurple)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Custom settings (Activation & Keep-alive)
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = if (settings.language == "zh") "高级保号管理策略" else "ADVANCED PRESETS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = PrimaryPurple
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (settings.language == "zh") "是否具有新卡首次激活期限要求吗？" else "New SIM Activation Requirement?",
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = hasActivationRule,
                                    onCheckedChange = { hasActivationRule = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = PrimaryPurple, checkedTrackColor = PrimaryPurple.copy(alpha = 0.5f))
                                )
                            }

                            if (hasActivationRule) {
                                OutlinedTextField(
                                    value = activationDaysText,
                                    onValueChange = { activationDaysText = it },
                                    label = { Text(if (settings.language == "zh") "首次激活宽限期期限 (天)" else "Activation Deadline (Days)") },
                                    placeholder = { Text("10") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (settings.language == "zh") "是否支持官方定时付费保号？" else "Support Paid Keep-Alive Override?",
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = allowKeepAlive,
                                    onCheckedChange = { allowKeepAlive = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = PrimaryPurple, checkedTrackColor = PrimaryPurple.copy(alpha = 0.5f))
                                )
                            }

                            if (allowKeepAlive) {
                                OutlinedTextField(
                                    value = keepAliveDesc,
                                    onValueChange = { keepAliveDesc = it },
                                    label = { Text(if (settings.language == "zh") "付费自动延长机制与详情" else "Keep Alive Service Details") },
                                    placeholder = { Text(if (settings.language == "zh") "例如：每月扣减 1 英镑自动保持卡号活跃" else "e.g. £1/month keep numbers active") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // Central bottom aligned Save Button
            Button(
                onClick = {
                    val period = maxPeriodText.toIntOrNull() ?: 180
                    val first = firstReminderText.toIntOrNull() ?: 30
                    val second = secondReminderText.toIntOrNull() ?: 14
                    val final = finalReminderText.toIntOrNull() ?: 5
                    val actDays = activationDaysText.toIntOrNull() ?: 10

                    if (name.isBlank()) {
                        isError = true
                    } else {
                        viewModel.saveCustomRule(
                            name = name,
                            carrierName = carrierName.ifBlank { name },
                            country = country.ifBlank { "Global" },
                            activePeriodDays = period,
                            firstReminder = first,
                            secondReminder = second,
                            finalReminder = final,
                            requiredActions = selectedActions.value.toList(),
                            minSpend = minSpendText.toDoubleOrNull(),
                            hasActivationRule = hasActivationRule,
                            activationDays = actDays,
                            allowKeepAlive = allowKeepAlive,
                            keepAliveDesc = keepAliveDesc,
                            isCustomRuleIdToEdit = ruleId,
                            onCompleted = onBackClick
                        )
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(8.dp, shape = CircleShape)
            ) {
                Text(
                    text = if (ruleId == null) {
                        if (settings.language == "zh") "创建自定义保号规则" else "Create New Rule Preset"
                    } else {
                        if (settings.language == "zh") "保存修改" else "Save Changes"
                    },
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
