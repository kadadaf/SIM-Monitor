package com.example.ui.addsim

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.RuleTemplate
import com.example.domain.rule.RuleEngine
import com.example.ui.theme.LightPurple
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.RiskRed
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSIMScreen(
    viewModel: AddEditSIMViewModel,
    simId: Int?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(simId) {
        viewModel.loadSIM(simId)
    }

    val existingSim by viewModel.existingSIM.collectAsStateWithLifecycle()
    val ruleTemplates by viewModel.ruleTemplates.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Fields state variables
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("UK") }
    var carrier by remember { mutableStateOf("") }
    var networkProvider by remember { mutableStateOf("") }
    var cardType by remember { mutableStateOf("Pay As You Go") }
    var balanceText by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("GBP") }
    var selectedRuleId by remember { mutableIntStateOf(-1) }
    var note by remember { mutableStateOf("") }

    var activationDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var lastActiveDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var lastTopUpDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var isError by remember { mutableStateOf(false) }

    // Dropdown state controls
    var showCardTypeDropdown by remember { mutableStateOf(false) }
    var showRuleDropdown by remember { mutableStateOf(false) }
    var showCurrencyDropdown by remember { mutableStateOf(false) }

    // Watch for existing card data to fill fields
    LaunchedEffect(existingSim) {
        existingSim?.let {
            name = it.name
            phoneNumber = it.phoneNumber
            country = it.country
            carrier = it.carrier
            networkProvider = it.networkProvider
            cardType = it.cardType
            balanceText = it.balance?.toString() ?: ""
            currency = it.currency
            selectedRuleId = it.ruleId
            note = it.note
            activationDate = it.activationDate
            lastActiveDate = it.lastActiveDate
            lastTopUpDate = it.lastTopUpDate
        }
    }

    // Default rule selector if ruleTemplates exist and no rule is selected
    LaunchedEffect(ruleTemplates, selectedRuleId) {
        if (ruleTemplates.isNotEmpty() && selectedRuleId == -1) {
            // Pick Giffgaff template or first one
            selectedRuleId = ruleTemplates.first().id
        }
    }

    // Helper to launch standard date picker dialog
    fun launchDatePicker(initialTime: Long, onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance().apply { timeInMillis = initialTime }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onDateSelected(selectedCal.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    val titleText = if (simId == null) {
                        if (settings.language == "zh") "添加新 SIM 卡" else "Add New SIM"
                    } else {
                        if (settings.language == "zh") "编辑卡片档案" else "Edit SIM Info"
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp, start = 20.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Intro text
                item {
                    Text(
                        text = if (settings.language == "zh") "在下方输入详细信息，以追踪并维护您 SIM 卡的保号生命周期。" else "Enter details below to trace keep-alive status of your subscriber identity module.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                // 1. Core Profile Details
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // SIM Card custom Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                isError = false
                            },
                            label = { Text(if (settings.language == "zh") "SIM 卡友好别名" else "SIM Friendly Name") },
                            placeholder = { Text(if (settings.language == "zh") "例如：Giffgaff 英国主卡" else "e.g. Giffgaff UK Main") },
                            isError = isError && name.isBlank(),
                            supportingText = {
                                if (isError && name.isBlank()) Text(if (settings.language == "zh") "未填写 SIM 卡别名，此字段必填" else "SIM Name is required", color = RiskRed)
                            },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Phone digits
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = {
                                phoneNumber = it
                                isError = false
                            },
                            label = { Text(if (settings.language == "zh") "移动电话号码" else "Phone Number") },
                            placeholder = { Text(if (settings.language == "zh") "例如：+44 7520 000000" else "e.g. +44 7520 000000") },
                            isError = isError && phoneNumber.isBlank(),
                            supportingText = {
                                if (isError && phoneNumber.isBlank()) Text(if (settings.language == "zh") "手机号为必填项" else "Phone number is required", color = RiskRed)
                            },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 2. Carrier profile attributes
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = carrier,
                            onValueChange = { carrier = it },
                            label = { Text(if (settings.language == "zh") "基础运营商" else "Carrier") },
                            placeholder = { Text(if (settings.language == "zh") "例如：Giffgaff" else "e.g. Giffgaff") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = networkProvider,
                            onValueChange = { networkProvider = it },
                            label = { Text(if (settings.language == "zh") "物理网络托管方" else "Network Host") },
                            placeholder = { Text(if (settings.language == "zh") "例如：O2 Network" else "e.g. O2 Network") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = { Text(if (settings.language == "zh") "国家/地区" else "Country") },
                            placeholder = { Text(if (settings.language == "zh") "例如：英国/UK" else "e.g. UK") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        // Card type Dropdown Selection
                        Box(modifier = Modifier.weight(1f)) {
                            val cardTypeDisplay = if (settings.language == "zh") {
                                when(cardType) {
                                    "Pay As You Go" -> "预付卡 (PAYG 储值)"
                                    "Monthly Plan" -> "月度订阅套餐卡"
                                    "Custom" -> "自定义特殊方案"
                                    else -> cardType
                                }
                            } else cardType
                            OutlinedTextField(
                                value = cardTypeDisplay,
                                onValueChange = {},
                                label = { Text(if (settings.language == "zh") "卡片付费类型" else "Card Type") },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { showCardTypeDropdown = true }) {
                                        Icon(Icons.Default.Info, contentDescription = null)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showCardTypeDropdown = true }
                            )

                            DropdownMenu(
                                expanded = showCardTypeDropdown,
                                onDismissRequest = { showCardTypeDropdown = false }
                            ) {
                                listOf("Pay As You Go", "Monthly Plan", "Custom").forEach { typeOption ->
                                    val optionLabel = when(typeOption) {
                                        "Pay As You Go" -> if (settings.language == "zh") "预付卡 (PAYG 储值)" else "Pay As You Go"
                                        "Monthly Plan" -> if (settings.language == "zh") "月度订阅套餐卡" else "Monthly Plan"
                                        "Custom" -> if (settings.language == "zh") "自定义特殊方案" else "Custom"
                                        else -> typeOption
                                    }
                                    DropdownMenuItem(
                                        text = { Text(optionLabel) },
                                        onClick = {
                                            cardType = typeOption
                                            showCardTypeDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Balance details
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = balanceText,
                            onValueChange = { balanceText = it },
                            label = { Text(if (settings.language == "zh") "当前卡内余额" else "Initial Balance") },
                            placeholder = { Text("e.g. 10.0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                            singleLine = true,
                            modifier = Modifier.weight(1.5f)
                        )

                        // Currency drop
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = currency,
                                onValueChange = {},
                                label = { Text(if (settings.language == "zh") "结算币种" else "Currency") },
                                readOnly = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showCurrencyDropdown = true }
                            )

                            DropdownMenu(
                                expanded = showCurrencyDropdown,
                                onDismissRequest = { showCurrencyDropdown = false }
                            ) {
                                listOf("GBP", "USD", "EUR", "CNY", "HKD", "SGD").forEach { cur ->
                                    DropdownMenuItem(
                                        text = { Text(cur) },
                                        onClick = {
                                            currency = cur
                                            showCurrencyDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Rule Template Dropdown selection
                item {
                    Column {
                        Text(
                            text = if (settings.language == "zh") "绑定的保号评估评估规则" else "LINKED EVALUATION RULE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val activeTemplate = ruleTemplates.find { it.id == selectedRuleId }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = activeTemplate?.name ?: (if (settings.language == "zh") "暂未绑定保号核准基准规则" else "No rule Template selected"),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { showRuleDropdown = true }) {
                                        Icon(Icons.Filled.Info, contentDescription = null)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showRuleDropdown = true }
                            )

                            DropdownMenu(
                                expanded = showRuleDropdown,
                                onDismissRequest = { showRuleDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                ruleTemplates.forEach { ruleOption ->
                                    DropdownMenuItem(
                                        text = { Text("${ruleOption.name} " + if (settings.language == "zh") "(${ruleOption.activePeriodDays}天有效期巡回)" else "(${ruleOption.activePeriodDays} days)") },
                                        onClick = {
                                            selectedRuleId = ruleOption.id
                                            showRuleDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Date Selectors
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = if (settings.language == "zh") "激活周期与重点历史时间" else "ACTIVATION & DATES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )

                        // Activation Date Picker Button
                        Button(
                            onClick = { launchDatePicker(activationDate) { activationDate = it } },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LightPurple,
                                contentColor = PrimaryPurple
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text((if (settings.language == "zh") "卡片激活日期: " else "SIM Activation Date: ") + RuleEngine.formatDate(activationDate), fontWeight = FontWeight.Bold)
                        }

                        // Last Active Date Picker Button
                        Button(
                            onClick = { launchDatePicker(lastActiveDate) { lastActiveDate = it } },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LightPurple,
                                contentColor = PrimaryPurple
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text((if (settings.language == "zh") "最近一次活跃保号动作: " else "Last Active Keep-alive Date: ") + RuleEngine.formatDate(lastActiveDate), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 6. Note details
                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(if (settings.language == "zh") "高级个性化备注与说明" else "Custom Notes") },
                        placeholder = { Text(if (settings.language == "zh") "例如：放在双卡卡槽，每隔180天发一条自检短信检查信号。" else "e.g. Kept inside auxiliary phone, needs text every six months.") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                }
            }

            // Central bottom aligned Save Button
            Button(
                onClick = {
                    if (name.isBlank() || phoneNumber.isBlank()) {
                        isError = true
                    } else {
                        viewModel.saveSIMCard(
                            name = name,
                            phoneNumber = phoneNumber,
                            country = country,
                            carrier = carrier.ifBlank { name },
                            networkProvider = networkProvider.ifBlank { "Unknown" },
                            cardType = cardType,
                            activationDate = activationDate,
                            lastActiveDate = lastActiveDate,
                            lastTopUpDate = lastTopUpDate,
                            balance = balanceText.toDoubleOrNull(),
                            currency = currency,
                            ruleId = selectedRuleId,
                            note = note,
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
                    text = if (simId == null) {
                        if (settings.language == "zh") "添加并创建卡片档案" else "Add SIM Card"
                    } else {
                        if (settings.language == "zh") "保存所有修改选项" else "Save Changes"
                    },
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
