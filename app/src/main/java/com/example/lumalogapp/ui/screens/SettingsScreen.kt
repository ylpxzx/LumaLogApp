package com.example.lumalogapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumalogapp.data.AppPreferences
import com.example.lumalogapp.data.DashboardMode
import com.example.lumalogapp.data.LanguagePreference
import com.example.lumalogapp.data.LumaData
import com.example.lumalogapp.data.ThemePreference
import com.example.lumalogapp.data.userBadges
import com.example.lumalogapp.ui.components.AchievementBadge
import com.example.lumalogapp.ui.components.CategoryPill
import com.example.lumalogapp.ui.components.ColorThemePicker
import com.example.lumalogapp.ui.components.FormPanel
import com.example.lumalogapp.ui.components.PreferenceChips
import com.example.lumalogapp.ui.i18n.LumaStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    data: LumaData,
    strings: LumaStrings,
    onBack: () -> Unit,
    onUpdatePreferences: (AppPreferences) -> Unit,
    onCreateCategory: (String, String) -> Unit,
    onToggleCategory: (Long) -> Unit,
    onDeleteCategory: (Long) -> Unit,
    onRestoreItem: (Long) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
) {
    var newCategory by remember { mutableStateOf("") }
    var newCategoryColor by remember { mutableStateOf("green") }
    val earnedBadges = remember(data) { userBadges(data).filter { it.earned } }
    val archivedItems = remember(data) { data.items.filter { it.archivedAt.isNotBlank() && it.deletedAt.isEmpty() } }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .navigationBarsPadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp, 20.dp, 18.dp, 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                SettingsHeader(
                    title = strings.t("settings"),
                    subtitle = strings.t("settingsSub"),
                    trailing = { OutlinedButton(onClick = onBack) { Text(strings.t("backHome")) } },
                )
            }

            item {
                CollapsibleSettingsPanel(title = strings.t("display")) {
                    PreferenceChips(
                        title = strings.t("language"),
                        options = listOf(LanguagePreference.Zh to "中文", LanguagePreference.En to "English"),
                        selected = data.preferences.language,
                        onSelect = { onUpdatePreferences(data.preferences.copy(language = it)) },
                    )
                    PreferenceChips(
                        title = strings.t("theme"),
                        options = listOf(
                            ThemePreference.System to strings.t("system"),
                            ThemePreference.Light to strings.t("light"),
                            ThemePreference.Dark to strings.t("dark"),
                        ),
                        selected = data.preferences.theme,
                        onSelect = { onUpdatePreferences(data.preferences.copy(theme = it)) },
                    )
                    PreferenceChips(
                        title = strings.t("dashboardMode"),
                        options = listOf(DashboardMode.All to strings.t("all"), DashboardMode.Category to strings.t("category")),
                        selected = data.preferences.dashboardMode,
                        onSelect = { onUpdatePreferences(data.preferences.copy(dashboardMode = it)) },
                    )
                }
            }

            item {
                CollapsibleSettingsPanel(title = strings.t("dashboardDisplay")) {
                    CompactSwitchRow(strings.t("todayStatus"), data.preferences.showTodayStatus) {
                        onUpdatePreferences(data.preferences.copy(showTodayStatus = it))
                    }
                    CompactSwitchRow(strings.t("currentStreak"), data.preferences.showCurrentStreak) {
                        onUpdatePreferences(data.preferences.copy(showCurrentStreak = it))
                    }
                    CompactSwitchRow(strings.t("longestStreak"), data.preferences.showLongestStreak) {
                        onUpdatePreferences(data.preferences.copy(showLongestStreak = it))
                    }
                    CompactSwitchRow(strings.t("completionRate"), data.preferences.showCompletionRate) {
                        onUpdatePreferences(data.preferences.copy(showCompletionRate = it))
                    }
                    CompactSwitchRow(strings.t("totalCheckins"), data.preferences.showTotalCheckins) {
                        onUpdatePreferences(data.preferences.copy(showTotalCheckins = it))
                    }
                }
            }

            item {
                FormPanel {
                    Text(strings.t("earnedBadges"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (earnedBadges.isEmpty()) {
                        Text(
                            strings.t("noEarnedBadges"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                        )
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            earnedBadges.forEach { badge ->
                                AchievementBadge(badge = badge)
                            }
                        }
                    }
                }
            }

            item {
                CollapsibleSettingsPanel(title = strings.t("categories"), count = data.categories.size.toString()) {
                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        label = { Text(strings.t("newCategory")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    ColorThemePicker(selected = newCategoryColor, strings = strings, onSelect = { newCategoryColor = it })
                    Button(
                        onClick = {
                            onCreateCategory(newCategory, newCategoryColor)
                            newCategory = ""
                        },
                        enabled = newCategory.isNotBlank(),
                    ) {
                        Text(strings.t("addCategory"))
                    }
                    data.categories.sortedBy { it.sortOrder }.forEach { category ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CategoryPill(category = category, fallbackTheme = category.colorTheme, strings = strings)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextButton(onClick = { onToggleCategory(category.id) }) {
                                    Text(if (category.isHidden) strings.t("show") else strings.t("hide"))
                                }
                                TextButton(
                                    enabled = !category.isDefault && data.items.none { it.categoryId == category.id },
                                    onClick = { onDeleteCategory(category.id) },
                                ) {
                                    Text(strings.t("delete"))
                                }
                            }
                        }
                    }
                }
            }

            item {
                CollapsibleSettingsPanel(title = strings.t("archivedItems"), count = archivedItems.size.toString()) {
                    if (archivedItems.isEmpty()) {
                        Text(
                            strings.t("archivedEmpty"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                        )
                    } else {
                        archivedItems.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                TextButton(onClick = { onRestoreItem(item.id) }) {
                                    Text(strings.t("unarchive"))
                                }
                            }
                        }
                    }
                }
            }

            item {
                FormPanel {
                    Text(strings.t("data"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = onExport, modifier = Modifier.weight(1f)) { Text(strings.t("export")) }
                        OutlinedButton(onClick = onImport, modifier = Modifier.weight(1f)) { Text(strings.t("import")) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactSwitchRow(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 18.sp,
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsHeader(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }
        Spacer(Modifier.width(12.dp))
        trailing()
    }
}

@Composable
private fun CollapsibleSettingsPanel(
    title: String,
    count: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    FormPanel {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                if (count != null) {
                    Text(count, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
                Text(if (expanded) "-" else "+", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
            }
        }
        if (expanded) {
            content()
        }
    }
}
