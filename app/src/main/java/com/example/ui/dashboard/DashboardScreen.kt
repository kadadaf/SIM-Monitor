package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.settings.Loc
import com.example.ui.components.SimCardItem
import com.example.ui.components.StatCard
import com.example.ui.theme.LightPurple
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.RiskBackground
import com.example.ui.theme.RiskRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onAddSimClick: () -> Unit,
    onCardClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onViewAllClick: (String) -> Unit, // "all", "active", "risk"
    modifier: Modifier = Modifier
) {
    val simCards by viewModel.simCards.collectAsStateWithLifecycle()
    val activeCount by viewModel.activeCount.collectAsStateWithLifecycle()
    val riskCount by viewModel.riskCount.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val rules by viewModel.allRuleTemplates.collectAsStateWithLifecycle()

    var showReminderConfirmDialog by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.statusBarsPadding())
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Top Area Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = Loc.t("connectivity_manager", settings.language),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Loc.t("app_title", settings.language),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Search Button
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search SIMs",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }

                    // Add SIM Button matching exactly the rules add button!
                    IconButton(
                        onClick = onAddSimClick,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add SIM",
                            tint = PrimaryPurple
                        )
                    }

                    // Account Button (Goes directly to Settings tab - Styled with a beautiful premium gradient avatar!)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFEA80FC), PrimaryPurple)
                                )
                            )
                            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), CircleShape)
                            .clickable { onViewAllClick("settings") },
                        contentAlignment = Alignment.Center
                    ) {
                        // Empty inside to show the gorgeous gradient itself, matching the HTML mockup!
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Double Stat Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Active Cards Stat Card
                StatCard(
                    title = Loc.t("active_cards", settings.language),
                    value = String.format("%02d", activeCount),
                    icon = Icons.Filled.PhoneAndroid,
                    backgroundColor = LightPurple,
                    contentColor = PrimaryPurple,
                    valueColor = Color(0xFF4B39C2),
                    onClick = { onViewAllClick("active") },
                    modifier = Modifier.weight(1f)
                )

                // At Risk Stat Card
                StatCard(
                    title = Loc.t("at_risk", settings.language),
                    value = String.format("%02d", riskCount),
                    icon = Icons.Filled.Warning,
                    backgroundColor = RiskBackground,
                    contentColor = RiskRed,
                    valueColor = Color(0xFFB71C1C),
                    onClick = { onViewAllClick("risk") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 3. Recent SIMs Label & 'View All' Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = Loc.t("recent_sims", settings.language),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
                TextButton(
                    onClick = { onViewAllClick("all") },
                    colors = ButtonDefaults.textButtonColors(contentColor = PrimaryPurple)
                ) {
                    Text(
                        text = Loc.t("view_all", settings.language),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. List of SIMs
            if (simCards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhoneAndroid,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = Loc.t("no_sim_recorded", settings.language),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = Loc.t("add_first_card_prompt", settings.language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Show top results
                    items(simCards.take(5), key = { it.id }) { sim ->
                        val rule = rules.find { it.id == sim.ruleId }
                        SimCardItem(
                            simCard = sim,
                            rule = rule,
                            hidePhone = settings.hidePhoneNumberPartially,
                            onCardClick = { onCardClick(sim.id) },
                            language = settings.language,
                            onRemindMeClick = {
                                viewModel.triggerRemindMe(sim)
                                showReminderConfirmDialog = sim.name
                            }
                        )
                    }
                }
            }
        }


    }

    // Remind Me confirmation dialog
    if (showReminderConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = { showReminderConfirmDialog = null },
            confirmButton = {
                TextButton(onClick = { showReminderConfirmDialog = null }) {
                    Text(
                        text = if (settings.language == "zh") "我知道了" else "OK",
                        color = PrimaryPurple,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            title = { Text(Loc.t("reminder_set", settings.language), fontWeight = FontWeight.Bold) },
            text = { Text(String.format(Loc.t("reminder_set_desc", settings.language), showReminderConfirmDialog)) }
        )
    }
}
