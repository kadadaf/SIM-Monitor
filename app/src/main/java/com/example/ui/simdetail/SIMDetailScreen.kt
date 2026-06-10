package com.example.ui.simdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.UsageRecord
import com.example.domain.rule.RuleEngine
import com.example.ui.components.ReminderItem
import com.example.ui.components.StatusBadge
import com.example.ui.components.UsageRecordItem
import com.example.ui.components.formatPhoneNumber
import com.example.ui.theme.LightPurple
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.RiskRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SIMDetailScreen(
    viewModel: SIMDetailViewModel,
    simId: Int,
    onBackClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(simId) {
        viewModel.setSIMCardId(simId)
    }

    val sim by viewModel.simCard.collectAsStateWithLifecycle()
    val rule by viewModel.activeRule.collectAsStateWithLifecycle()
    val usages by viewModel.usageRecords.collectAsStateWithLifecycle()
    val reminders by viewModel.reminderRecords.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showAddUsageDialog by remember { mutableStateOf<String?>(null) } // "SMS", "CALL", "DATA", "TOP_UP", "KEEP_ALIVE"
    var currentTab by remember { mutableIntStateOf(0) } // 0 = Usages, 1 = Reminders

    val focusManager = LocalFocusManager.current

    if (sim == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryPurple)
        }
        return
    }

    val activeSim = sim!!
    val evalResult = RuleEngine.evaluate(activeSim, rule, usages)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(activeSim.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(activeSim.id) }) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = if (settings.language == "zh") "编辑卡片信息" else "Edit SIM")
                    }
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = if (settings.language == "zh") "删除此卡片档案" else "Delete SIM", tint = RiskRed)
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Status overview card
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(0.5.dp, shape = RoundedCornerShape(24.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            text = if (settings.language == "zh") "移动电话号码" else "PHONE NUMBER",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = formatPhoneNumber(activeSim.phoneNumber, settings.hidePhoneNumberPartially),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    StatusBadge(status = activeSim.status, language = settings.language)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (settings.language == "zh") "当前卡内余额" else "CURRENT BALANCE",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = if (activeSim.balance != null) "${activeSim.currency} ${String.format("%.2f", activeSim.balance)}" else "—",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryPurple
                                        )
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (settings.language == "zh") "保号失效日期" else "NEXT EVALUATION",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                            textAlign = TextAlign.End
                                        )
                                        Text(
                                            text = RuleEngine.formatDate(evalResult.expiryDate),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (activeSim.status == "RISK") RiskRed.copy(alpha = 0.08f) else LightPurple.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (activeSim.status == "HEALTHY") Icons.Filled.CheckCircle else Icons.Filled.Info,
                                            contentDescription = null,
                                            tint = if (activeSim.status == "RISK") RiskRed else PrimaryPurple,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (settings.language == "zh") RuleEngine.getLocalizedReminderMessage(evalResult.reminderMessage, "zh") else evalResult.reminderMessage,
                                            fontSize = 13.sp,
                                            color = if (activeSim.status == "RISK") RiskRed else MaterialTheme.colorScheme.onBackground,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Carrier Metadata Card
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(0.5.dp, shape = RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (settings.language == "zh") "SIM 卡运营商等档案元数据" else "CARRIER INFORMATION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                val metaItems = if (settings.language == "zh") {
                                    listOf(
                                        Triple("归属运营商品牌", activeSim.carrier, Icons.Filled.Business),
                                        Triple("物理底层信号托管商", activeSim.networkProvider, Icons.Filled.SettingsInputAntenna),
                                        Triple("所属国家/地区", activeSim.country, Icons.Filled.Public),
                                        Triple("卡片付费计费协议", when (activeSim.cardType) {
                                            "Pay As You Go" -> "预付储值方案 (PAYG)"
                                            "Monthly Plan" -> "月度结算套餐"
                                            "Custom" -> "自定义个性化方案"
                                            else -> activeSim.cardType
                                        }, Icons.Filled.SimCard),
                                        Triple("关联保号检查巡回机制", rule?.name ?: "默认全局策略", Icons.Filled.Rule)
                                    )
                                } else {
                                    listOf(
                                        Triple("Carrier Name", activeSim.carrier, Icons.Filled.Business),
                                        Triple("Network Host", activeSim.networkProvider, Icons.Filled.SettingsInputAntenna),
                                        Triple("Country Code", activeSim.country, Icons.Filled.Public),
                                        Triple("Card Protocol", activeSim.cardType, Icons.Filled.SimCard),
                                        Triple("Linked Rule", rule?.name ?: "Generic", Icons.Filled.Rule)
                                    )
                                }

                                metaItems.forEach { (label, value, icon) ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = label,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = value,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.weight(1f, fill = false),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Quick Action log area
                    item {
                        Column {
                            Text(
                                text = if (settings.language == "zh") "极速手动保号活动登账" else "QUICK RECORD LOG",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val quickActions = if (settings.language == "zh") {
                                    listOf(
                                        Triple("发短信", Icons.Filled.Sms, "SMS"),
                                        Triple("打电话", Icons.Filled.Call, "CALL"),
                                        Triple("用流量", Icons.Filled.SettingsInputAntenna, "DATA"),
                                        Triple("储值充值", Icons.Filled.AddCard, "TOP_UP")
                                    )
                                } else {
                                    listOf(
                                        Triple("SMS", Icons.Filled.Sms, "SMS"),
                                        Triple("Call", Icons.Filled.Call, "CALL"),
                                        Triple("Data", Icons.Filled.SettingsInputAntenna, "DATA"),
                                        Triple("Top Up", Icons.Filled.AddCard, "TOP_UP")
                                    )
                                }

                                quickActions.forEach { (label, symbol, type) ->
                                    Button(
                                        onClick = { showAddUsageDialog = type },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = LightPurple,
                                            contentColor = PrimaryPurple
                                        ),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(symbol, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }

                            if (rule?.allowPaidKeepAliveService == true) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { showAddUsageDialog = "KEEP_ALIVE" },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                        contentColor = PrimaryPurple
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 40.dp)
                                ) {
                                    Icon(Icons.Filled.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (settings.language == "zh") "登记官方付费长效保号维护状态" else "Record Paid Keep-Alive Service", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 4. History Logs Tabs
                    item {
                        Column {
                            TabRow(
                                selectedTabIndex = currentTab,
                                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                contentColor = PrimaryPurple
                            ) {
                                Tab(
                                    selected = currentTab == 0,
                                    onClick = { currentTab = 0 },
                                    text = { Text(if (settings.language == "zh") "卡片维护动作历史 (${usages.size})" else "Usage Logs (${usages.size})", fontWeight = FontWeight.Bold) }
                                )
                                Tab(
                                    selected = currentTab == 1,
                                    onClick = { currentTab = 1 },
                                    text = { Text(if (settings.language == "zh") "已发送预警推送 (${reminders.size})" else "Alert Sent (${reminders.size})", fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                    }

                    // Display respective records based on tab
                    if (currentTab == 0) {
                        if (usages.isEmpty()) {
                            item {
                                Text(
                                    text = if (settings.language == "zh") "该 SIM 卡片当前暂无登账历史操作。" else "No activities recorded yet.",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp)
                                )
                            }
                        } else {
                            items(usages, key = { it.id }) { record ->
                                UsageRecordItem(
                                    record = record,
                                    simCardName = activeSim.name,
                                    onDeleteClick = { viewModel.deleteUsageRecord(record) },
                                    language = settings.language
                                )
                            }
                        }
                    } else {
                        if (reminders.isEmpty()) {
                            item {
                                Text(
                                    text = if (settings.language == "zh") "暂物发送过的报警通知日志记录。" else "No alerts sent yet.",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp)
                                )
                            }
                        } else {
                            items(reminders, key = { it.id }) { reminder ->
                                ReminderItem(
                                    reminder = reminder,
                                    simCardName = activeSim.name,
                                    language = settings.language
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 1. Delete Confirm Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteSIMCard(onBackClick)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = RiskRed)
                ) {
                    Text(if (settings.language == "zh") "确认删除" else "Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(if (settings.language == "zh") "取消" else "Cancel")
                }
            },
            title = { Text(if (settings.language == "zh") "删除 SIM 卡备份" else "Delete SIM Card", fontWeight = FontWeight.Bold) },
            text = { Text(if (settings.language == "zh") "您确定要将 SIM 卡 ${activeSim.name} 的全部数据永久删除吗？此操作包含的所有历史轨迹和预警信息均会无法恢复。" else "Are you absolutely sure you want to delete ${activeSim.name}? All history records and logs will be permanently erased.") }
        )
    }

    // 2. Add Activity dialog popup
    if (showAddUsageDialog != null) {
        val type = showAddUsageDialog!!
        var note by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }

        // Prefills notes
        LaunchedEffect(type) {
            note = when (type) {
                "SMS" -> if (settings.language == "zh") "发送了保号短信（自检）" else "Sent SMS to check validity"
                "CALL" -> if (settings.language == "zh") "拨打语音电话一次（核验）" else "Made a quick call"
                "DATA" -> if (settings.language == "zh") "触发蜂窝网数据微量流量" else "Used brief internet cellular data"
                "TOP_UP" -> if (settings.language == "zh") "对电话卡账户充值了话费余额" else "Topped up standard credits"
                "KEEP_ALIVE" -> if (settings.language == "zh") "扣除了官方套餐保号服务费/固定费用" else "Paid keep-alive monthly fee"
                else -> ""
            }
        }

        AlertDialog(
            onDismissRequest = { showAddUsageDialog = null },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount == null && (type == "TOP_UP")) {
                            isError = true
                        } else {
                            viewModel.addUsageRecord(
                                actionType = type,
                                amount = amount,
                                note = note
                            )
                            showAddUsageDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text(if (settings.language == "zh") "确认登记" else "Save Activity", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUsageDialog = null }) {
                    Text(if (settings.language == "zh") "取消" else "Cancel")
                }
            },
            title = {
                val typeLabel = when(type) {
                    "SMS" -> if (settings.language == "zh") "发自检短信" else "SMS"
                    "CALL" -> if (settings.language == "zh") "拨通电话自检" else "CALL"
                    "DATA" -> if (settings.language == "zh") "产生流量自检" else "DATA"
                    "TOP_UP" -> if (settings.language == "zh") "余额账单充值保号" else "TOP_UP"
                    "KEEP_ALIVE" -> if (settings.language == "zh") "定期代扣保障操作" else "KEEP_ALIVE"
                    else -> type
                }
                Text(if (settings.language == "zh") "极速手动登记: $typeLabel" else "Log Activity: $type", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (type == "TOP_UP") {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = {
                                amountText = it
                                isError = false
                            },
                            label = { Text((if (settings.language == "zh") "充值发生金额" else "Amount") + " (${activeSim.currency})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = isError,
                            supportingText = {
                                if (isError) Text(if (settings.language == "zh") "请确保输入有效的数额数值" else "Please enter a valid amount")
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(if (settings.language == "zh") "任何补充备注 / 场景说明" else "Notes / Descriptions") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}
