package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.settings.Loc

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    var showWipeConfirmDialog by remember { mutableStateOf(false) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var showExportInfoDialog by remember { mutableStateOf(false) }
    var showSuccessToastMsg by remember { mutableStateOf<String?>(null) }

    var showReminderTimeMenu by remember { mutableStateOf(false) }
    var showCountryMenu by remember { mutableStateOf(false) }
    var showCurrencyMenu by remember { mutableStateOf(false) }

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

            // Heading
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (settings.language == "zh") "返回" else "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                Column {
                    Text(
                        text = Loc.t("user_preferences", settings.language),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Loc.t("settings", settings.language),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Alert Preferences
                item {
                    SettingsGroupCard(title = Loc.t("notifications_automation", settings.language)) {
                        SettingsToggleRow(
                            label = Loc.t("enable_local_alerts", settings.language),
                            subtitle = Loc.t("enable_local_alerts_desc", settings.language),
                            checked = settings.enableNotifications,
                            icon = Icons.Default.Notifications,
                            onCheckedChange = { viewModel.toggleNotifications(it) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))

                        SettingsToggleRow(
                            label = Loc.t("daily_expiry_inspector", settings.language),
                            subtitle = Loc.t("daily_expiry_inspector_desc", settings.language),
                            checked = settings.enableDailyCheck,
                            icon = Icons.Default.Autorenew,
                            onCheckedChange = { viewModel.toggleDailyCheck(it) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))

                        // Reminder Daily schedule selection
                        Box {
                            SettingsDropdownRow(
                                label = Loc.t("inspect_start_time", settings.language),
                                value = settings.defaultReminderTime,
                                icon = Icons.Default.Schedule,
                                onClick = { showReminderTimeMenu = true }
                            )

                            DropdownMenu(
                                expanded = showReminderTimeMenu,
                                onDismissRequest = { showReminderTimeMenu = false }
                            ) {
                                listOf("08:00", "09:00", "10:00", "12:00", "18:00", "20:00").forEach { time ->
                                    DropdownMenuItem(
                                        text = { Text(time) },
                                        onClick = {
                                            viewModel.updateReminderTime(time)
                                            showReminderTimeMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Display security
                item {
                    SettingsGroupCard(title = Loc.t("visuals_safety", settings.language)) {
                        SettingsToggleRow(
                            label = Loc.t("dynamic_dark_mode", settings.language),
                            subtitle = Loc.t("dynamic_dark_mode_desc", settings.language),
                            checked = settings.enableDarkMode,
                            icon = Icons.Default.BrightnessMedium,
                            onCheckedChange = { viewModel.toggleDarkMode(it) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))

                        SettingsToggleRow(
                            label = Loc.t("obscure_phone", settings.language),
                            subtitle = Loc.t("obscure_phone_desc", settings.language),
                            checked = settings.hidePhoneNumberPartially,
                            icon = Icons.Default.VisibilityOff,
                            onCheckedChange = { viewModel.toggleHidePhoneNumber(it) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))

                        var showLanguageMenu by remember { mutableStateOf(false) }
                        Box {
                            SettingsDropdownRow(
                                label = Loc.t("language_setting", settings.language),
                                value = if (settings.language == "zh") "中文 (Chinese)" else "English",
                                icon = Icons.Default.Language,
                                onClick = { showLanguageMenu = true }
                            )

                            DropdownMenu(
                                expanded = showLanguageMenu,
                                onDismissRequest = { showLanguageMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("中文 (Chinese)") },
                                    onClick = {
                                        viewModel.updateLanguage("zh")
                                        showLanguageMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("English") },
                                    onClick = {
                                        viewModel.updateLanguage("en")
                                        showLanguageMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 3. App Default Presets
                item {
                    SettingsGroupCard(title = Loc.t("default_protocols", settings.language)) {
                        // Default Country Selector
                        Box {
                            SettingsDropdownRow(
                                label = Loc.t("default_country", settings.language),
                                value = settings.defaultCountry,
                                icon = Icons.Default.Public,
                                onClick = { showCountryMenu = true }
                            )

                            DropdownMenu(
                                expanded = showCountryMenu,
                                onDismissRequest = { showCountryMenu = false }
                            ) {
                                listOf("UK", "US", "GLOBAL", "HK", "SG").forEach { count ->
                                    DropdownMenuItem(
                                        text = { Text(count) },
                                        onClick = {
                                            viewModel.updateDefaultCountry(count)
                                            showCountryMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))

                        // Default Currency
                        Box {
                            SettingsDropdownRow(
                                label = Loc.t("default_currency", settings.language),
                                value = settings.defaultCurrency,
                                icon = Icons.Default.Paid,
                                onClick = { showCurrencyMenu = true }
                            )

                            DropdownMenu(
                                expanded = showCurrencyMenu,
                                onDismissRequest = { showCurrencyMenu = false }
                            ) {
                                listOf("GBP", "USD", "EUR", "CNY", "HKD").forEach { coin ->
                                    DropdownMenuItem(
                                        text = { Text(coin) },
                                        onClick = {
                                            viewModel.updateDefaultCurrency(coin)
                                            showCurrencyMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Backups clearances
                item {
                    SettingsGroupCard(title = Loc.t("backups_resets", settings.language)) {
                        SettingsClickableRow(
                            label = Loc.t("export_backup", settings.language),
                            subtitle = Loc.t("export_backup_desc", settings.language),
                            icon = Icons.Default.Backup,
                            onClick = { showExportInfoDialog = true }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))

                        SettingsClickableRow(
                            label = Loc.t("import_backup", settings.language),
                            subtitle = Loc.t("import_backup_desc", settings.language),
                            icon = Icons.Default.Restore,
                            onClick = { showImportConfirmDialog = true }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))

                        SettingsClickableRow(
                            label = Loc.t("reset_database", settings.language),
                            subtitle = Loc.t("reset_database_desc", settings.language),
                            icon = Icons.Default.DeleteForever,
                            iconColor = RiskRed,
                            textColor = RiskRed,
                            onClick = { showWipeConfirmDialog = true }
                        )
                    }
                }

                // 5. App Info block
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = if (settings.language == "zh") "SIM 卡保号管家" else "SIM Monitor",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (settings.language == "zh") "出境电话卡保号及漫游消费自校验管家" else "Keep-Alive and Consumption Manager",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (settings.language == "zh") "版本 1.0.0 (运行环境 API 36)" else "Version 1.0.0 (API 36)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Settings actions popup dialog managers
    // 1. Wipe Confirm
    if (showWipeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showWipeConfirmDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWipeConfirmDialog = false
                        viewModel.wipeAllData {
                            showSuccessToastMsg = Loc.t("db_wiped_msg", settings.language)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = RiskRed)
                ) {
                    Text(Loc.t("reset_everything", settings.language), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirmDialog = false }) { Text(Loc.t("cancel", settings.language)) }
            },
            title = { Text(Loc.t("factory_reset_title", settings.language), fontWeight = FontWeight.Bold) },
            text = { Text(Loc.t("factory_reset_desc", settings.language)) }
        )
    }

    // 2. Import Confirm
    if (showImportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showImportConfirmDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showImportConfirmDialog = false
                        viewModel.triggerDatabaseImport {
                            showSuccessToastMsg = Loc.t("backup_imported_msg", settings.language)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text(Loc.t("import_backup_data_btn", settings.language), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmDialog = false }) { Text(Loc.t("cancel", settings.language)) }
            },
            title = { Text(Loc.t("import_profile_title", settings.language), fontWeight = FontWeight.Bold) },
            text = { Text(Loc.t("import_profile_desc", settings.language)) }
        )
    }

    // 3. Export Dialog Detail
    if (showExportInfoDialog) {
        AlertDialog(
            onDismissRequest = { showExportInfoDialog = false },
            confirmButton = {
                TextButton(onClick = { showExportInfoDialog = false }) {
                    Text(if (settings.language == "zh") "我知道了" else "OK", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
            },
            title = { Text(Loc.t("json_exported_title", settings.language), fontWeight = FontWeight.Bold) },
            text = { Text(Loc.t("json_exported_desc", settings.language)) }
        )
    }

    // 4. Action Completion message toast dialog helper
    if (showSuccessToastMsg != null) {
        AlertDialog(
            onDismissRequest = { showSuccessToastMsg = null },
            confirmButton = {
                TextButton(onClick = { showSuccessToastMsg = null }) {
                    Text(if (settings.language == "zh") "我知道了" else "OK", color = PrimaryPurple)
                }
            },
            title = { Text(Loc.t("success", settings.language), fontWeight = FontWeight.Bold) },
            text = { Text(showSuccessToastMsg!!) }
        )
    }
}

// Sub components for Settings layout
@Composable
fun SettingsGroupCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(0.5.dp, shape = RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsToggleRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    icon: ImageVector,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(LightPurple, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryPurple, checkedTrackColor = LightPurple)
        )
    }
}

@Composable
fun SettingsDropdownRow(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(LightPurple, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, fontWeight = FontWeight.Bold, color = PrimaryPurple, fontSize = 14.sp)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = PrimaryPurple)
        }
    }
}

@Composable
fun SettingsClickableRow(
    label: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: androidx.compose.ui.graphics.Color = PrimaryPurple,
    textColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(LightPurple, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        }
    }
}
