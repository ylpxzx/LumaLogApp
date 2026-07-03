package com.example.lumalogapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.annotation.DrawableRes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumalogapp.R
import com.example.lumalogapp.data.Category
import com.example.lumalogapp.data.Item
import com.example.lumalogapp.data.LumaData
import com.example.lumalogapp.data.TimeMode
import com.example.lumalogapp.ui.components.DefaultLumaIconKey
import com.example.lumalogapp.ui.components.LumaIconBadge
import com.example.lumalogapp.ui.components.lumaIconOptions
import com.example.lumalogapp.ui.components.normalizeLumaIconKey
import com.example.lumalogapp.ui.i18n.LumaStrings
import com.example.lumalogapp.ui.utils.colorThemes
import com.example.lumalogapp.ui.utils.themeColor
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemEditorScreen(
    data: LumaData,
    itemId: Long?,
    strings: LumaStrings,
    onBack: () -> Unit,
    onSave: (Item) -> Unit,
    onDelete: () -> Unit,
    onArchive: () -> Unit,
    onCreateCategory: (String, String) -> Long?,
) {
    val existing = data.items.firstOrNull { it.id == itemId }
    val visibleCategories = data.categories.filterNot { it.isHidden }.sortedBy { it.sortOrder }
    val defaultCategoryId = visibleCategories.firstOrNull()?.id ?: data.categories.first().id
    val colorScheme = MaterialTheme.colorScheme

    var name by remember(itemId) { mutableStateOf(existing?.name ?: "") }
    var description by remember(itemId) { mutableStateOf(existing?.description ?: "") }
    var categoryId by remember(itemId) { mutableStateOf(existing?.categoryId ?: defaultCategoryId) }
    var colorTheme by remember(itemId) { mutableStateOf(existing?.colorTheme ?: visibleCategories.firstOrNull { it.id == categoryId }?.colorTheme ?: "green") }
    var iconKey by remember(itemId) {
        mutableStateOf(existing?.iconKey?.let(::normalizeLumaIconKey) ?: DefaultLumaIconKey)
    }
    var startDate by remember(itemId) { mutableStateOf(existing?.startDate ?: LocalDate.now().toString()) }
    var endDate by remember(itemId) { mutableStateOf(existing?.endDate ?: "") }
    var isUnlimited by remember(itemId) { mutableStateOf(existing?.isUnlimited ?: true) }
    var dailyTarget by remember(itemId) { mutableStateOf((existing?.dailyTargetCount ?: 1).toString()) }
    var timeMode by remember(itemId) { mutableStateOf(existing?.timeMode ?: TimeMode.AllDay) }
    var validStartTime by remember(itemId) { mutableStateOf(existing?.validStartTime ?: "09:00") }
    var validEndTime by remember(itemId) { mutableStateOf(existing?.validEndTime ?: "23:59") }
    var allowMakeup by remember(itemId) { mutableStateOf(existing?.allowMakeup ?: false) }
    var makeupMonthlyLimit by remember(itemId) { mutableStateOf((existing?.makeupMonthlyLimit ?: 3).toString()) }
    var allowExtra by remember(itemId) { mutableStateOf(existing?.allowExtraCheckins ?: false) }
    var showOnDashboard by remember(itemId) { mutableStateOf(existing?.showOnDashboard ?: true) }
    var confirmDelete by remember { mutableStateOf(false) }
    var showAddCategory by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryTheme by remember { mutableStateOf(colorTheme) }
    var datePickerTarget by remember { mutableStateOf<EditorDateTarget?>(null) }

    val dateRangeInvalid = isDateRangeInvalid(
        startDate = startDate.ifBlank { LocalDate.now().toString() },
        endDate = endDate,
        isUnlimited = isUnlimited,
    )
    val timeRangeInvalid = isTimeRangeInvalid(
        startTime = validStartTime,
        endTime = validEndTime,
        timeMode = timeMode,
    )
    val canSave = name.isNotBlank() && !dateRangeInvalid && !timeRangeInvalid

    fun submit() {
        if (!canSave) return
        onSave(
            Item(
                id = existing?.id ?: 0,
                categoryId = categoryId,
                name = name.trim(),
                description = description.trim(),
                colorTheme = colorTheme,
                iconKey = iconKey,
                startDate = startDate.ifBlank { LocalDate.now().toString() },
                endDate = if (isUnlimited) "" else endDate,
                isUnlimited = isUnlimited,
                dailyTargetCount = dailyTarget.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                timeMode = timeMode,
                validStartTime = validStartTime,
                validEndTime = validEndTime,
                allowMakeup = allowMakeup,
                makeupMonthlyLimit = if (allowMakeup) makeupMonthlyLimit.toIntOrNull()?.coerceAtLeast(0) ?: 0 else 0,
                allowExtraCheckins = allowExtra,
                showOnDashboard = showOnDashboard,
                sortOrder = existing?.sortOrder ?: 0,
                archivedAt = existing?.archivedAt ?: "",
                deletedAt = existing?.deletedAt ?: "",
            )
        )
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            EditorTopBar(
                title = if (existing == null) strings.t("newItem") else strings.t("editItem"),
                onBack = onBack,
            )
        },
        bottomBar = {
            EditorBottomBar(
                isEditing = existing != null,
                canSave = canSave,
                strings = strings,
                onArchive = onArchive,
                onDelete = { confirmDelete = true },
                onSave = ::submit,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 12.dp, top = 6.dp, end = 12.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                HabitPreviewCard(
                    name = name,
                    description = description,
                    iconKey = iconKey,
                    colorTheme = colorTheme,
                    strings = strings,
                )
            }

            item {
                EditorGroup {
                    TextInputRow(
                        icon = EditorIcon.Edit,
                        title = strings.t("itemName"),
                        value = name,
                        placeholder = strings.t("itemNamePlaceholder"),
                        counter = "${name.length}/20",
                        singleLine = true,
                        onValueChange = { name = it },
                    )
                    SoftDivider()
                    TextInputRow(
                        icon = EditorIcon.Desc,
                        title = strings.t("description"),
                        value = description,
                        placeholder = strings.t("descriptionPlaceholder"),
                        counter = "${description.length}/100",
                        singleLine = false,
                        onValueChange = { description = it },
                    )
                }
            }

            item {
                EditorGroup {
                    SectionTitleRow(icon = EditorIcon.Photo, title = strings.t("selectIcon"))
                    Spacer(Modifier.height(8.dp))
                    IconStrip(selectedKey = iconKey, onSelect = { iconKey = it })
                }
            }

            item {
                EditorGroup {
                    SectionTitleRow(icon = EditorIcon.Label, title = strings.t("selectCategory"))
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        visibleCategories.forEach { category ->
                            CategoryChoice(
                                category = category,
                                selected = categoryId == category.id,
                                strings = strings,
                                onClick = {
                                    categoryId = category.id
                                    colorTheme = category.colorTheme
                                },
                            )
                        }
                        AddCategoryChip(strings = strings, onClick = {
                            newCategoryTheme = colorTheme
                            showAddCategory = true
                        })
                    }
                    SoftDivider(modifier = Modifier.padding(vertical = 10.dp))
                    SectionTitleRow(icon = EditorIcon.Color, title = strings.t("selectColor"))
                    Spacer(Modifier.height(8.dp))
                    ColorDotRow(selected = colorTheme, onSelect = { colorTheme = it })
                }
            }

            item {
                EditorGroup {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DateCell(
                            icon = EditorIcon.Date,
                            title = strings.t("startDate"),
                            value = startDate,
                            placeholder = LocalDate.now().toString(),
                            enabled = true,
                            onClick = { datePickerTarget = EditorDateTarget.Start },
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(58.dp)
                                .background(colorScheme.outline.copy(alpha = 0.16f)),
                        )
                        DateCell(
                            icon = EditorIcon.Date,
                            title = strings.t("endDate"),
                            value = endDate,
                            placeholder = strings.t("unset"),
                            enabled = !isUnlimited,
                            onClick = {
                                isUnlimited = false
                                datePickerTarget = EditorDateTarget.End
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (dateRangeInvalid) {
                        ValidationMessage(text = strings.t("dateRangeInvalid"))
                    }
                    SoftDivider()
                    SwitchSettingRow(icon = EditorIcon.NoLimit, title = strings.t("unlimited"), checked = isUnlimited, onCheckedChange = { isUnlimited = it })
                }
            }

            item {
                EditorGroup {
                    StepperRow(
                        icon = EditorIcon.Goal,
                        title = strings.t("dailyTarget"),
                        value = dailyTarget.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                        min = 1,
                        onValueChange = { dailyTarget = it.toString() },
                    )
                    SoftDivider()
                    ValidTimeRow(
                        timeMode = timeMode,
                        startTime = validStartTime,
                        endTime = validEndTime,
                        strings = strings,
                        errorMessage = if (timeRangeInvalid) strings.t("timeRangeInvalid") else null,
                        onTimeModeChange = { timeMode = it },
                        onStartTimeChange = { validStartTime = it },
                        onEndTimeChange = { validEndTime = it },
                    )
                }
            }

            item {
                EditorGroup {
                    SwitchSettingRow(icon = EditorIcon.SignIn, title = strings.t("allowMakeup"), checked = allowMakeup, onCheckedChange = { allowMakeup = it })
                    if (allowMakeup) {
                        SoftDivider()
                        StepperRow(
                            icon = EditorIcon.SignIn,
                            title = strings.t("makeupMonthlyLimit"),
                            value = makeupMonthlyLimit.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                            min = 0,
                            onValueChange = { makeupMonthlyLimit = it.toString() },
                            compactIcon = true,
                        )
                    }
                    SoftDivider()
                    SwitchSettingRow(icon = EditorIcon.Add, title = strings.t("allowExtra"), checked = allowExtra, onCheckedChange = { allowExtra = it })
                    SoftDivider()
                    SwitchSettingRow(icon = EditorIcon.Home, title = strings.t("showOnDashboard"), checked = showOnDashboard, onCheckedChange = { showOnDashboard = it })
                }
            }
        }
    }

    if (showAddCategory) {
        AddCategoryDialog(
            name = newCategoryName,
            selectedTheme = newCategoryTheme,
            strings = strings,
            onNameChange = { newCategoryName = it },
            onThemeChange = { newCategoryTheme = it },
            onDismiss = { showAddCategory = false },
            onConfirm = {
                val newId = onCreateCategory(newCategoryName, newCategoryTheme)
                if (newId != null) {
                    categoryId = newId
                    colorTheme = newCategoryTheme
                }
                newCategoryName = ""
                showAddCategory = false
            },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(strings.t("deleteTitle")) },
            text = { Text(strings.t("deleteMessage")) },
            confirmButton = {
                TextButton(onClick = onDelete) { Text(strings.t("delete")) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(strings.t("cancel")) }
            },
        )
    }

    datePickerTarget?.let { target ->
        EditorDatePickerDialog(
            selectedDate = if (target == EditorDateTarget.Start) startDate else endDate,
            strings = strings,
            onDismiss = { datePickerTarget = null },
            onDateSelected = { selected ->
                if (target == EditorDateTarget.Start) {
                    startDate = selected
                } else {
                    endDate = selected
                    isUnlimited = false
                }
                datePickerTarget = null
            },
        )
    }
}

private enum class EditorDateTarget {
    Start,
    End,
}

private enum class EditorIcon(@param:DrawableRes val resId: Int) {
    Add(R.drawable.ic_editor_add),
    Archive(R.drawable.ic_editor_archive),
    Color(R.drawable.ic_editor_color),
    Date(R.drawable.ic_editor_date),
    Delete(R.drawable.ic_editor_delete),
    Desc(R.drawable.ic_editor_desc),
    Edit(R.drawable.ic_editor_edit),
    Goal(R.drawable.ic_editor_goal),
    Home(R.drawable.ic_editor_home),
    Label(R.drawable.ic_editor_label),
    NoLimit(R.drawable.ic_editor_no_limit),
    Photo(R.drawable.ic_editor_photo),
    SignIn(R.drawable.ic_editor_sign_in),
    Time(R.drawable.ic_editor_time),
}

@Composable
private fun EditorTopBar(
    title: String,
    onBack: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(colorScheme.background)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            color = colorScheme.onBackground,
            fontSize = 19.sp,
            lineHeight = 23.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "←",
            color = colorScheme.onBackground,
            fontSize = 27.sp,
            lineHeight = 28.sp,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clip(CircleShape)
                .clickable(onClick = onBack)
                .padding(6.dp),
        )
    }
}

@Composable
private fun HabitPreviewCard(
    name: String,
    description: String,
    iconKey: String,
    colorTheme: String,
    strings: LumaStrings,
) {
    val colorScheme = MaterialTheme.colorScheme
    val title = name.ifBlank { strings.t("exampleHabitName") }
    val subtitle = description.ifBlank { strings.t("exampleHabitDescription") }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface.copy(alpha = 0.98f)),
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.14f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LumaIconBadge(iconKey = iconKey, size = 62.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = title,
                    color = colorScheme.onSurface,
                    fontSize = 17.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                MiniPreviewHeatmap(colorTheme = colorTheme, strings = strings, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun MiniPreviewHeatmap(colorTheme: String, strings: LumaStrings, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val base = themeColor(colorTheme)
    val empty = colorScheme.surfaceVariant.copy(alpha = if (isEditorDark()) 0.64f else 0.78f)
    val today = LocalDate.now()
    val columns = 28
    val gap = 2.dp
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(strings.monthLabel(today.minusMonths(1)), color = colorScheme.onSurfaceVariant, fontSize = 9.sp, lineHeight = 11.sp)
            Text(strings.monthLabel(today), color = colorScheme.onSurfaceVariant, fontSize = 9.sp, lineHeight = 11.sp)
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cell = ((maxWidth - gap * (columns - 1)) / columns).coerceAtLeast(4.dp)
            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                repeat(2) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        repeat(columns) { index ->
                            val lit = index > 20 || (index + row) % 7 == 0
                            Box(
                                modifier = Modifier
                                    .size(cell)
                                    .clip(RoundedCornerShape(1.6.dp))
                                    .background(if (lit) base.copy(alpha = if (index == columns - 1 && row == 0) 1f else 0.24f) else empty),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorGroup(content: @Composable ColumnScope.() -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface.copy(alpha = 0.98f)),
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.13f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), content = content)
    }
}

@Composable
private fun TextInputRow(
    icon: EditorIcon,
    title: String,
    value: String,
    placeholder: String,
    counter: String,
    singleLine: Boolean,
    onValueChange: (String) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        EditorGlyph(icon)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = colorScheme.onSurface, fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
            InlineBasicField(
                value = value,
                placeholder = placeholder,
                singleLine = singleLine,
                onValueChange = onValueChange,
            )
        }
        Text(counter, color = colorScheme.onSurfaceVariant.copy(alpha = 0.64f), fontSize = 11.sp, lineHeight = 13.sp)
    }
}

@Composable
private fun InlineBasicField(
    value: String,
    placeholder: String,
    singleLine: Boolean,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = TextStyle(
            color = if (enabled) colorScheme.onSurfaceVariant else colorScheme.onSurfaceVariant.copy(alpha = 0.44f),
            fontSize = 13.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.Normal,
        ),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box {
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        color = colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 0.62f else 0.36f),
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun SectionTitleRow(icon: EditorIcon, title: String) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        EditorGlyph(icon)
        Text(title, color = colorScheme.onSurface, fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun IconStrip(selectedKey: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        lumaIconOptions.forEach { option ->
            val selected = normalizeLumaIconKey(selectedKey) == option.key
            val color = themeColor(option.theme)
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (selected) 0.18f else 0.34f))
                    .border(
                        width = if (selected) 1.6.dp else 1.dp,
                        color = if (selected) color.copy(alpha = 0.94f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(13.dp),
                    )
                    .clickable { onSelect(option.key) },
                contentAlignment = Alignment.Center,
            ) {
                LumaIconBadge(iconKey = option.key, size = 37.dp, selected = false)
            }
        }
    }
}

@Composable
private fun CategoryChoice(category: Category, selected: Boolean, strings: LumaStrings, onClick: () -> Unit) {
    val color = themeColor(category.colorTheme)
    Text(
        text = strings.categoryName(category.name),
        color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 12.sp,
        lineHeight = 15.sp,
        fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f))
            .border(1.dp, if (selected) color.copy(alpha = 0.34f) else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun AddCategoryChip(strings: LumaStrings, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Text(
        text = "+ ${strings.t("addCategory")}",
        color = colorScheme.primary,
        fontSize = 12.sp,
        lineHeight = 15.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.primary.copy(alpha = 0.08f))
            .border(1.dp, colorScheme.primary.copy(alpha = 0.20f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 6.dp),
    )
}

@Composable
private fun ColorDotRow(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        colorThemes.forEach { theme ->
            val color = themeColor(theme)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .border(if (selected == theme) 1.8.dp else 0.dp, color.copy(alpha = 0.95f), CircleShape)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onSelect(theme) },
            )
        }
    }
}

@Composable
private fun DateCell(
    icon: EditorIcon,
    title: String,
    value: String,
    placeholder: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val alpha = if (enabled) 1f else 0.58f
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        EditorGlyph(icon, size = 31.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = colorScheme.onSurface.copy(alpha = alpha), fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
            Text(
                text = value.ifBlank { placeholder },
                color = if (value.isBlank()) colorScheme.onSurfaceVariant.copy(alpha = 0.58f) else themeColor("green").copy(alpha = alpha),
                fontSize = 13.sp,
                lineHeight = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text("›", color = colorScheme.onSurfaceVariant.copy(alpha = 0.64f), fontSize = 20.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun SwitchSettingRow(icon: EditorIcon, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        EditorGlyph(icon)
        Text(
            title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun StepperRow(
    icon: EditorIcon,
    title: String,
    value: Int,
    min: Int,
    onValueChange: (Int) -> Unit,
    compactIcon: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        if (compactIcon) {
            Spacer(Modifier.width(36.dp))
        } else {
            EditorGlyph(icon)
        }
        Text(
            title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        Stepper(value = value, min = min, onValueChange = onValueChange)
    }
}

@Composable
private fun Stepper(value: Int, min: Int, onValueChange: (Int) -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(11.dp))
            .border(1.dp, colorScheme.outline.copy(alpha = 0.16f), RoundedCornerShape(11.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.26f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton("−") { onValueChange((value - 1).coerceAtLeast(min)) }
        Text(
            value.toString(),
            color = colorScheme.onSurface,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(38.dp),
        )
        StepperButton("+") { onValueChange(value + 1) }
    }
}

@Composable
private fun StepperButton(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 20.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .width(32.dp)
            .height(30.dp)
            .clickable(onClick = onClick)
            .padding(top = 2.dp),
    )
}

@Composable
private fun ValidTimeRow(
    timeMode: TimeMode,
    startTime: String,
    endTime: String,
    strings: LumaStrings,
    errorMessage: String?,
    onTimeModeChange: (TimeMode) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            EditorGlyph(EditorIcon.Time)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(strings.t("validTime"), color = colorScheme.onSurface, fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
                Text(
                    if (timeMode == TimeMode.AllDay) strings.t("allDayCheckin") else "${startTime.ifBlank { "--:--" }} - ${endTime.ifBlank { "--:--" }}",
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    maxLines = 1,
                )
            }
            SegmentedControl(selected = timeMode, strings = strings, onSelect = onTimeModeChange)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            TimeInput(label = strings.t("startTime"), value = startTime, enabled = timeMode == TimeMode.TimeRange, onValueChange = onStartTimeChange, modifier = Modifier.weight(1f))
            Text("—", color = colorScheme.onSurfaceVariant.copy(alpha = 0.56f), fontSize = 18.sp)
            TimeInput(label = strings.t("endTime"), value = endTime, enabled = timeMode == TimeMode.TimeRange, onValueChange = onEndTimeChange, modifier = Modifier.weight(1f))
        }
        if (errorMessage != null) {
            ValidationMessage(text = errorMessage)
        }
    }
}

@Composable
private fun SegmentedControl(selected: TimeMode, strings: LumaStrings, onSelect: (TimeMode) -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.52f))
            .padding(2.dp),
    ) {
        SegmentButton(text = strings.t("allDay"), selected = selected == TimeMode.AllDay) { onSelect(TimeMode.AllDay) }
        SegmentButton(text = strings.t("timeRange"), selected = selected == TimeMode.TimeRange) { onSelect(TimeMode.TimeRange) }
    }
}

@Composable
private fun SegmentButton(text: String, selected: Boolean, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Text(
        text = text,
        color = if (selected) colorScheme.primary else colorScheme.onSurfaceVariant,
        fontSize = 12.sp,
        lineHeight = 15.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) colorScheme.surface else Color.Transparent)
            .border(1.dp, if (selected) colorScheme.primary.copy(alpha = 0.24f) else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    )
}

@Composable
private fun TimeInput(label: String, value: String, enabled: Boolean, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = if (enabled) 0.44f else 0.24f))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Text(label, color = colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 0.78f else 0.38f), fontSize = 11.sp, lineHeight = 13.sp, maxLines = 1)
        InlineBasicField(
            value = value,
            placeholder = "00:00",
            enabled = enabled,
            singleLine = true,
            keyboardType = KeyboardType.Text,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun EditorBottomBar(
    isEditing: Boolean,
    canSave: Boolean,
    strings: LumaStrings,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        color = colorScheme.surface.copy(alpha = 0.98f),
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colorScheme.outline.copy(alpha = 0.12f))
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (isEditing) {
                TextButton(onClick = onArchive) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EditorActionIcon(icon = EditorIcon.Archive, color = colorScheme.onSurface)
                        Text(strings.t("archive"), color = colorScheme.onSurface, fontSize = 13.sp, lineHeight = 16.sp)
                    }
                }
                Box(modifier = Modifier.width(1.dp).height(22.dp).background(colorScheme.outline.copy(alpha = 0.20f)))
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EditorActionIcon(icon = EditorIcon.Delete, color = colorScheme.error)
                        Text(strings.t("delete"), fontSize = 13.sp, lineHeight = 16.sp)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onSave,
                enabled = canSave,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = Color.White,
                    disabledContainerColor = colorScheme.surfaceVariant,
                    disabledContentColor = colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier
                    .widthIn(min = 142.dp)
                    .height(48.dp),
            ) {
                Text(strings.t("save"), fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun AddCategoryDialog(
    name: String,
    selectedTheme: String,
    strings: LumaStrings,
    onNameChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.t("addCategory")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(strings.t("newCategory")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                ColorDotRow(selected = selectedTheme, onSelect = onThemeChange)
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = onConfirm) { Text(strings.t("addCategory")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.t("cancel")) }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorDatePickerDialog(
    selectedDate: String,
    strings: LumaStrings,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit,
) {
    val initialDate = parseEditorDate(selectedDate) ?: LocalDate.now()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate.toPickerMillis())

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selected = datePickerState.selectedDateMillis?.toLocalPickerDate() ?: initialDate
                    onDateSelected(selected.toString())
                },
            ) {
                Text(strings.t("confirm"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.t("cancel"))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun EditorGlyph(icon: EditorIcon, size: Dp = 34.dp) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(colorScheme.primary.copy(alpha = if (isEditorDark()) 0.16f else 0.10f)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(icon.resId),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorScheme.primary.copy(alpha = 0.84f)),
            modifier = Modifier.size(size * 0.52f),
        )
    }
}

@Composable
private fun EditorActionIcon(icon: EditorIcon, color: Color) {
    Image(
        painter = painterResource(icon.resId),
        contentDescription = null,
        colorFilter = ColorFilter.tint(color),
        modifier = Modifier.size(17.dp),
    )
}

@Composable
private fun SoftDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
    )
}

@Composable
private fun ValidationMessage(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.error,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Normal,
        modifier = Modifier.padding(start = 45.dp, top = 4.dp, bottom = 2.dp),
    )
}

@Composable
private fun isEditorDark(): Boolean {
    return MaterialTheme.colorScheme.background == Color(0xFF0C1118)
}

private fun parseEditorDate(value: String): LocalDate? =
    runCatching { LocalDate.parse(value) }.getOrNull()

private fun parseEditorTime(value: String): LocalTime? =
    runCatching { LocalTime.parse(value) }.getOrNull()

private fun isDateRangeInvalid(startDate: String, endDate: String, isUnlimited: Boolean): Boolean {
    if (isUnlimited || endDate.isBlank()) return false
    val start = parseEditorDate(startDate) ?: return false
    val end = parseEditorDate(endDate) ?: return false
    return end.isBefore(start)
}

private fun isTimeRangeInvalid(startTime: String, endTime: String, timeMode: TimeMode): Boolean {
    if (timeMode != TimeMode.TimeRange) return false
    val start = parseEditorTime(startTime) ?: return false
    val end = parseEditorTime(endTime) ?: return false
    return end.isBefore(start)
}

private fun LocalDate.toPickerMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalPickerDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
