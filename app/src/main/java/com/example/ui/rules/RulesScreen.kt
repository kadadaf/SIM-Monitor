package com.example.ui.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.RuleCard
import com.example.settings.Loc
import com.example.ui.theme.BackgroundSoft
import com.example.ui.theme.PrimaryPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    viewModel: RulesViewModel,
    onAddRuleClick: () -> Unit,
    onEditRuleClick: (ruleId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val rules by viewModel.ruleTemplates.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

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

            // Topic Name
            Text(
                text = Loc.t("rules_subtitle", settings.language),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Loc.t("rules_title", settings.language),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(
                    onClick = onAddRuleClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = Loc.t("create_custom_rule", settings.language),
                        tint = PrimaryPurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (rules.isEmpty()) {
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
                            imageVector = Icons.Filled.Rule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = if (settings.language == "zh") "暂无保号规则设定" else "No Rules defined",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Medium
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
                    // Segment: Predefined built-in rules
                    item {
                        Text(
                            text = if (settings.language == "zh") "系统内置保号模板" else "BUILT-IN TEMPLATES",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(rules.filter { it.isBuiltIn }, key = { it.id }) { rule ->
                        RuleCard(
                            rule = rule,
                            onDuplicateClick = { viewModel.duplicateRule(rule) },
                            onEditClick = { onEditRuleClick(rule.id) },
                            language = settings.language
                        )
                    }

                    // Segment: Custom Rules
                    val customRules = rules.filter { !it.isBuiltIn }
                    item {
                        Text(
                            text = if (settings.language == "zh") "自定义规章模板" else "CUSTOM PRESETS",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    if (customRules.isEmpty()) {
                        item {
                            Text(
                                text = if (settings.language == "zh") "在下方创建您自定义的运营商保号规则以安排通知自检机制。" else "Build personalized rules below to outline bespoke carrier validity schedules.",
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )
                        }
                    } else {
                        items(customRules, key = { it.id }) { rule ->
                            RuleCard(
                                rule = rule,
                                onDuplicateClick = { viewModel.duplicateRule(rule) },
                                onDeleteClick = { viewModel.deleteRule(rule) },
                                onEditClick = { onEditRuleClick(rule.id) },
                                language = settings.language
                            )
                        }
                    }
                }
            }
        }

        // Add Custom Rule Floating bottom action button
        Button(
            onClick = onAddRuleClick,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .fillMaxWidth()
                .height(56.dp)
                .shadow(8.dp, shape = CircleShape)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = Loc.t("create_custom_rule", settings.language),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}
