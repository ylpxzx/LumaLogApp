package com.example.lumalogapp.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumalogapp.R
import com.example.lumalogapp.data.AppPreferences
import com.example.lumalogapp.data.Category
import com.example.lumalogapp.data.DashboardMode
import com.example.lumalogapp.data.LanguagePreference
import com.example.lumalogapp.data.LumaData
import com.example.lumalogapp.data.ThemePreference
import com.example.lumalogapp.data.userBadges
import com.example.lumalogapp.ui.components.AchievementBadge
import com.example.lumalogapp.ui.components.FoldIndicator
import com.example.lumalogapp.ui.i18n.LumaStrings
import com.example.lumalogapp.ui.utils.colorThemes
import com.example.lumalogapp.ui.utils.themeColor

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
    var expandedPanel by remember { mutableStateOf<SettingsPanelKey?>(SettingsPanelKey.Display) }

    val earnedBadges = remember(data) { userBadges(data).filter { it.earned } }
    val archivedItems = remember(data) { data.items.filter { it.archivedAt.isNotBlank() && it.deletedAt.isEmpty() } }
    val usedCategoryIds = remember(data.items) { data.items.filter { it.deletedAt.isEmpty() }.map { it.categoryId }.toSet() }
    val dashboardEnabledCount = listOf(
        data.preferences.showTodayStatus,
        data.preferences.showCurrentStreak,
        data.preferences.showLongestStreak,
        data.preferences.showCompletionRate,
        data.preferences.showTotalCheckins,
    ).count { it }
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSettingsDark()
    val background = if (isDark) colorScheme.background else Color(0xFFF8FBFD)

    Scaffold(
        containerColor = background,
        topBar = {
            SettingsTopBar(title = strings.t("settings"), onBack = onBack)
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 12.dp, top = 14.dp, end = 12.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SettingsBrandBlock(strings = strings)
            }

            item {
                SettingsPanel(
                    icon = SettingsIcon.Display,
                    title = strings.t("display"),
                    expanded = expandedPanel == SettingsPanelKey.Display,
                    onToggle = { expandedPanel = expandedPanel.toggle(SettingsPanelKey.Display) },
                ) {
                    SettingsFieldLabel(strings.t("language"))
                    SegmentedPicker(
                        options = listOf(
                            LanguagePreference.Zh to "中文",
                            LanguagePreference.En to "English",
                        ),
                        selected = data.preferences.language,
                        onSelect = { onUpdatePreferences(data.preferences.copy(language = it)) },
                    )
                    Spacer(Modifier.height(9.dp))
                    SettingsFieldLabel(strings.t("theme"))
                    SegmentedPicker(
                        options = listOf(
                            ThemePreference.System to strings.t("system"),
                            ThemePreference.Light to strings.t("light"),
                            ThemePreference.Dark to strings.t("dark"),
                        ),
                        selected = data.preferences.theme,
                        onSelect = { onUpdatePreferences(data.preferences.copy(theme = it)) },
                    )
                    Spacer(Modifier.height(9.dp))
                    SettingsFieldLabel(strings.t("dashboardMode"))
                    SegmentedPicker(
                        options = listOf(
                            DashboardMode.All to strings.t("all"),
                            DashboardMode.Category to strings.t("category"),
                        ),
                        selected = data.preferences.dashboardMode,
                        onSelect = { onUpdatePreferences(data.preferences.copy(dashboardMode = it)) },
                    )
                }
            }

            item {
                SettingsPanel(
                    icon = SettingsIcon.Home,
                    title = strings.t("dashboardDisplay"),
                    summary = strings.t("settingsEnabledCount", "count" to dashboardEnabledCount.toString()),
                    expanded = expandedPanel == SettingsPanelKey.Dashboard,
                    onToggle = { expandedPanel = expandedPanel.toggle(SettingsPanelKey.Dashboard) },
                ) {
                    SettingsSwitchRow(strings.t("todayStatus"), data.preferences.showTodayStatus) {
                        onUpdatePreferences(data.preferences.copy(showTodayStatus = it))
                    }
                    SettingsDivider()
                    SettingsSwitchRow(strings.t("currentStreak"), data.preferences.showCurrentStreak) {
                        onUpdatePreferences(data.preferences.copy(showCurrentStreak = it))
                    }
                    SettingsDivider()
                    SettingsSwitchRow(strings.t("longestStreak"), data.preferences.showLongestStreak) {
                        onUpdatePreferences(data.preferences.copy(showLongestStreak = it))
                    }
                    SettingsDivider()
                    SettingsSwitchRow(strings.t("completionRate"), data.preferences.showCompletionRate) {
                        onUpdatePreferences(data.preferences.copy(showCompletionRate = it))
                    }
                    SettingsDivider()
                    SettingsSwitchRow(strings.t("totalCheckins"), data.preferences.showTotalCheckins) {
                        onUpdatePreferences(data.preferences.copy(showTotalCheckins = it))
                    }
                }
            }

            item {
                SettingsPanel(
                    icon = SettingsIcon.Category,
                    title = strings.t("categories"),
                    summary = strings.t("settingsCategoryCount", "count" to data.categories.size.toString()),
                    expanded = expandedPanel == SettingsPanelKey.Categories,
                    onToggle = { expandedPanel = expandedPanel.toggle(SettingsPanelKey.Categories) },
                ) {
                    CategoryEditor(
                        categories = data.categories.sortedBy { it.sortOrder },
                        usedCategoryIds = usedCategoryIds,
                        newCategory = newCategory,
                        newCategoryColor = newCategoryColor,
                        strings = strings,
                        onNewCategoryChange = { newCategory = it },
                        onNewCategoryColorChange = { newCategoryColor = it },
                        onCreateCategory = {
                            onCreateCategory(newCategory, newCategoryColor)
                            newCategory = ""
                        },
                        onToggleCategory = onToggleCategory,
                        onDeleteCategory = onDeleteCategory,
                    )
                }
            }

            item {
                SettingsPanel(
                    icon = SettingsIcon.Badge,
                    title = strings.t("earnedBadges"),
                    summary = strings.t("settingsBadgeCount", "count" to earnedBadges.size.toString()),
                    expanded = expandedPanel == SettingsPanelKey.Badges,
                    onToggle = { expandedPanel = expandedPanel.toggle(SettingsPanelKey.Badges) },
                ) {
                    if (earnedBadges.isEmpty()) {
                        EmptySettingsText(strings.t("noEarnedBadges"))
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            earnedBadges.forEach { badge ->
                                AchievementBadge(badge = badge, strings = strings)
                            }
                        }
                    }
                }
            }

            item {
                SettingsPanel(
                    icon = SettingsIcon.Archive,
                    title = strings.t("archivedItems"),
                    summary = strings.t("settingsArchivedCount", "count" to archivedItems.size.toString()),
                    expanded = expandedPanel == SettingsPanelKey.Archived,
                    onToggle = { expandedPanel = expandedPanel.toggle(SettingsPanelKey.Archived) },
                ) {
                    if (archivedItems.isEmpty()) {
                        EmptySettingsText(strings.t("archivedEmpty"))
                    } else {
                        archivedItems.forEachIndexed { index, item ->
                            if (index > 0) SettingsDivider()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = item.name,
                                    color = colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    lineHeight = 17.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(onClick = { onRestoreItem(item.id) }) {
                                    Text(strings.t("unarchive"), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                SettingsPanel(
                    icon = SettingsIcon.Data,
                    title = strings.t("data"),
                    summary = strings.t("settingsDataSummary"),
                    expanded = expandedPanel == SettingsPanelKey.Data,
                    onToggle = { expandedPanel = expandedPanel.toggle(SettingsPanelKey.Data) },
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onExport,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(strings.t("export"), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        OutlinedButton(
                            onClick = onImport,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(strings.t("import"), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

private enum class SettingsPanelKey {
    Display,
    Dashboard,
    Categories,
    Badges,
    Archived,
    Data,
}

private enum class SettingsIcon(@param:DrawableRes val resId: Int) {
    Display(R.drawable.ic_editor_photo),
    Home(R.drawable.ic_editor_home),
    Category(R.drawable.ic_editor_label),
    Badge(R.drawable.ic_editor_goal),
    Archive(R.drawable.ic_editor_archive),
    Data(R.drawable.ic_editor_no_limit),
}

private fun SettingsPanelKey?.toggle(target: SettingsPanelKey): SettingsPanelKey? =
    if (this == target) null else target

@Composable
private fun SettingsTopBar(title: String, onBack: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.background)
            .statusBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                color = colorScheme.onBackground,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
            Text(
                text = "←",
                color = colorScheme.onBackground,
                fontSize = 22.sp,
                lineHeight = 23.sp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clip(CircleShape)
                    .clickable(onClick = onBack)
                    .padding(4.dp),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colorScheme.outline.copy(alpha = if (isSettingsDark()) 0.18f else 0.12f)),
        )
    }
}

@Composable
private fun SettingsBrandBlock(strings: LumaStrings) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsHeatmapLogo(size = 58.dp)
        Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = "LumaLog",
                color = colorScheme.onBackground,
                fontSize = 17.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
            Text(
                text = strings.t("brandTagline"),
                color = colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(CircleShape)
                        .border(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.68f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "✓",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 9.sp,
                        lineHeight = 9.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Text(
                    text = strings.t("settingsLocalAutoSave"),
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SettingsHeatmapLogo(size: Dp) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSettingsDark()
    val accent = colorScheme.primary
    val unlit = colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.72f else 0.88f)
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface.copy(alpha = if (isDark) 0.78f else 0.96f))
            .border(1.dp, colorScheme.outline.copy(alpha = if (isDark) 0.18f else 0.12f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(4) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(4) { column ->
                        val lit = (row == 0 && column <= 1) ||
                            (row == 1 && column in 1..2) ||
                            (row == 2 && column == 2) ||
                            (row == 3 && (column == 0 || column == 3))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (lit) accent.copy(alpha = if (column == 3) 0.72f else 1f) else unlit),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsPanel(
    icon: SettingsIcon,
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colorScheme.surface.copy(alpha = if (isSettingsDark()) 0.88f else 0.98f))
            .border(1.dp, colorScheme.outline.copy(alpha = if (isSettingsDark()) 0.18f else 0.12f), shape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SettingsIconBadge(icon = icon)
            Text(
                text = title,
                color = colorScheme.onSurface,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!expanded && summary != null) {
                Text(
                    text = summary,
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                modifier = Modifier.width(18.dp),
                contentAlignment = Alignment.Center,
            ) {
                FoldIndicator(
                    expanded = expanded,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        if (expanded) {
            SettingsDivider(modifier = Modifier.padding(top = 8.dp, bottom = 9.dp))
            content()
        }
    }
}

@Composable
private fun SettingsIconBadge(icon: SettingsIcon) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(colorScheme.primary.copy(alpha = if (isSettingsDark()) 0.18f else 0.10f)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(icon.resId),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorScheme.primary),
            modifier = Modifier.size(17.dp),
        )
    }
}

@Composable
private fun SettingsFieldLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

@Composable
private fun <T> SegmentedPicker(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(11.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(35.dp)
            .clip(shape)
            .background(colorScheme.surfaceVariant.copy(alpha = if (isSettingsDark()) 0.34f else 0.24f))
            .border(1.dp, colorScheme.outline.copy(alpha = if (isSettingsDark()) 0.16f else 0.12f), shape)
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option.first == selected
            Text(
                text = option.second,
                color = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(29.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) colorScheme.surface.copy(alpha = if (isSettingsDark()) 0.72f else 0.96f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (isSelected) colorScheme.primary.copy(alpha = 0.28f) else Color.Transparent,
                        RoundedCornerShape(9.dp),
                    )
                    .clickable { onSelect(option.first) }
                    .padding(top = 7.dp),
            )
            if (index < options.lastIndex) {
                Box(
                    modifier = Modifier
                        .height(18.dp)
                        .width(1.dp)
                        .background(colorScheme.outline.copy(alpha = if (isSettingsDark()) 0.14f else 0.10f)),
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun CategoryEditor(
    categories: List<Category>,
    usedCategoryIds: Set<Long>,
    newCategory: String,
    newCategoryColor: String,
    strings: LumaStrings,
    onNewCategoryChange: (String) -> Unit,
    onNewCategoryColorChange: (String) -> Unit,
    onCreateCategory: () -> Unit,
    onToggleCategory: (Long) -> Unit,
    onDeleteCategory: (Long) -> Unit,
) {
    OutlinedTextField(
        value = newCategory,
        onValueChange = onNewCategoryChange,
        label = { Text(strings.t("newCategory")) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(7.dp))
    SettingsColorDots(selected = newCategoryColor, onSelect = onNewCategoryColorChange)
    Spacer(Modifier.height(8.dp))
    Button(
        onClick = onCreateCategory,
        enabled = newCategory.isNotBlank(),
        shape = RoundedCornerShape(11.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp),
    ) {
        Text(strings.t("addCategory"), fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
    Spacer(Modifier.height(9.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        categories.forEach { category ->
            CategoryManageChip(
                category = category,
                used = category.id in usedCategoryIds,
                strings = strings,
                onToggleCategory = onToggleCategory,
                onDeleteCategory = onDeleteCategory,
            )
        }
    }
}

@Composable
private fun CategoryManageChip(
    category: Category,
    used: Boolean,
    strings: LumaStrings,
    onToggleCategory: (Long) -> Unit,
    onDeleteCategory: (Long) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val color = themeColor(category.colorTheme)
    val canDelete = !category.isDefault && !used
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(13.dp))
            .background(color.copy(alpha = if (isSettingsDark()) 0.14f else 0.08f))
            .border(1.dp, color.copy(alpha = if (isSettingsDark()) 0.22f else 0.13f), RoundedCornerShape(13.dp))
            .padding(start = 8.dp, top = 6.dp, end = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = strings.categoryName(category.name),
            color = colorScheme.onSurface,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = if (category.isHidden) strings.t("show") else strings.t("hide"),
            color = colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .clickable { onToggleCategory(category.id) }
                .padding(horizontal = 3.dp, vertical = 1.dp),
        )
        if (canDelete) {
            Text(
                text = "×",
                color = colorScheme.error,
                fontSize = 15.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onDeleteCategory(category.id) }
                    .padding(horizontal = 3.dp, vertical = 1.dp),
            )
        }
    }
}

@Composable
private fun SettingsColorDots(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        colorThemes.forEach { theme ->
            val color = themeColor(theme)
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(if (selected == theme) 1.8.dp else 0.dp, color.copy(alpha = 0.95f), CircleShape)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onSelect(theme) },
            )
        }
    }
}

@Composable
private fun EmptySettingsText(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        modifier = Modifier.padding(vertical = 2.dp),
    )
}

@Composable
private fun SettingsDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = if (isSettingsDark()) 0.16f else 0.10f)),
    )
}

@Composable
private fun isSettingsDark(): Boolean =
    MaterialTheme.colorScheme.background == Color(0xFF0C1118)
