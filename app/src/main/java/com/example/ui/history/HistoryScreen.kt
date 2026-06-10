package com.example.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ReminderItem
import com.example.ui.components.UsageRecordItem
import com.example.ui.theme.BackgroundSoft
import com.example.ui.theme.PrimaryPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val simCards by viewModel.simCards.collectAsStateWithLifecycle()
    val filteredUsages by viewModel.filteredUsages.collectAsStateWithLifecycle()
    val filteredReminders by viewModel.filteredReminders.collectAsStateWithLifecycle()

    val selectedSimId by viewModel.selectedSimId.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Usages, 1 = Alerts
    var showSimDropdown by remember { mutableStateOf(false) }
    var showTypeDropdown by remember { mutableStateOf(false) }

    val actionTypes = listOf("CALL", "SMS", "DATA", "TOP_UP", "KEEP_ALIVE_SERVICE")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.statusBarsPadding())
            Spacer(modifier = Modifier.height(16.dp))

            // Header Section
            Text(
                text = if (settings.language == "zh") "审计日志与历史" else "LOGS & AUDITS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = if (settings.language == "zh") "保号审计日志" else "Audit Logs",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-tabs segment
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentColor = PrimaryPurple
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text(if (settings.language == "zh") "使用活动记录" else "Activity Logs", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text(if (settings.language == "zh") "警报通知历史" else "Alerts Sent", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Pills Row
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // SIM card picker pill
                Box {
                    val activeSimName = if (selectedSimId == null) {
                        if (settings.language == "zh") "全部 SIM 卡" else "All SIMs"
                    } else {
                        simCards.find { it.id == selectedSimId }?.name ?: "All SIMs"
                    }
                    InputChip(
                        selected = selectedSimId != null,
                        onClick = { showSimDropdown = true },
                        label = { Text(activeSimName, fontWeight = FontWeight.Medium) },
                        trailingIcon = { Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = InputChipDefaults.inputChipColors(selectedContainerColor = PrimaryPurple.copy(alpha = 0.15f), selectedLabelColor = PrimaryPurple)
                    )

                    DropdownMenu(
                        expanded = showSimDropdown,
                        onDismissRequest = { showSimDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (settings.language == "zh") "全部 SIM 卡" else "All SIMs") },
                            onClick = {
                                viewModel.setSimFilter(null)
                                showSimDropdown = false
                            }
                        )
                        simCards.forEach { sim ->
                          DropdownMenuItem(
                                text = { Text(sim.name) },
                                onClick = {
                                    viewModel.setSimFilter(sim.id)
                                    showSimDropdown = false
                                }
                            )
                        }
                    }
                }

                // Action Type filter (only relevant for Activity tab)
                if (activeTab == 0) {
                    Box {
                        val selectTypeLabel = if (selectedType == null) {
                            if (settings.language == "zh") "全部行为类型" else "All Action Types"
                        } else {
                            when (selectedType) {
                                "CALL" -> if (settings.language == "zh") "拨打电话" else "CALL"
                                "SMS" -> if (settings.language == "zh") "发送短信" else "SMS"
                                "DATA" -> if (settings.language == "zh") "使用流量" else "DATA"
                                "TOP_UP" -> if (settings.language == "zh") "充值储值" else "TOP_UP"
                                "KEEP_ALIVE_SERVICE" -> if (settings.language == "zh") "官方保号扣费" else "KEEP_ALIVE_SERVICE"
                                else -> selectedType ?: ""
                            }
                        }
                        InputChip(
                            selected = selectedType != null,
                            onClick = { showTypeDropdown = true },
                            label = { Text(selectTypeLabel, fontWeight = FontWeight.Medium) },
                            trailingIcon = { Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = InputChipDefaults.inputChipColors(selectedContainerColor = PrimaryPurple.copy(alpha = 0.15f), selectedLabelColor = PrimaryPurple)
                        )

                        DropdownMenu(
                            expanded = showTypeDropdown,
                            onDismissRequest = { showTypeDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (settings.language == "zh") "全部行为" else "All Types") },
                                onClick = {
                                    viewModel.setTypeFilter(null)
                                    showTypeDropdown = false
                                }
                            )
                            actionTypes.forEach { type ->
                                val itemLabel = when (type) {
                                    "CALL" -> if (settings.language == "zh") "拨打电话" else "CALL"
                                    "SMS" -> if (settings.language == "zh") "发送短信" else "SMS"
                                    "DATA" -> if (settings.language == "zh") "使用流量" else "DATA"
                                    "TOP_UP" -> if (settings.language == "zh") "充值储值" else "TOP_UP"
                                    "KEEP_ALIVE_SERVICE" -> if (settings.language == "zh") "官方保号扣费" else "KEEP_ALIVE_SERVICE"
                                    else -> type
                                }
                                DropdownMenuItem(
                                    text = { Text(itemLabel) },
                                    onClick = {
                                        viewModel.setTypeFilter(type)
                                        showTypeDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main items list
            if (activeTab == 0) {
                if (filteredUsages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (settings.language == "zh") "没有找到符合当前过滤条件的使用记录。" else "No usage records match the current filters.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(filteredUsages, key = { it.id }) { record ->
                            val simName = simCards.find { it.id == record.simCardId }?.name ?: "Unknown SIM"
                            UsageRecordItem(
                                record = record,
                                simCardName = simName,
                                onDeleteClick = { viewModel.deleteUsageRecord(record) }
                            )
                        }
                    }
                }
            } else {
                if (filteredReminders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (settings.language == "zh") "暂未发送过任何到期保号的警报通知。" else "No notification reminders have been sent yet.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(filteredReminders, key = { it.id }) { reminder ->
                            val simName = simCards.find { it.id == reminder.simCardId }?.name ?: "Unknown SIM"
                            ReminderItem(
                                reminder = reminder,
                                simCardName = simName
                            )
                        }
                    }
                }
            }
        }
    }
}
