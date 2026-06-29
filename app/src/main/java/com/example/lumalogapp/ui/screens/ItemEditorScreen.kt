package com.example.lumalogapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lumalogapp.data.Item
import com.example.lumalogapp.data.LumaData
import com.example.lumalogapp.data.TimeMode
import com.example.lumalogapp.ui.components.ColorThemePicker
import com.example.lumalogapp.ui.components.FormPanel
import com.example.lumalogapp.ui.components.SwitchRow
import com.example.lumalogapp.ui.components.TopTitle
import com.example.lumalogapp.ui.i18n.LumaStrings
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemEditorScreen(
    data: LumaData,
    itemId: Long?,
    strings: LumaStrings,
    onBack: () -> Unit,
    onSave: (Item) -> Unit,
    onDelete: () -> Unit,
) {
    val existing = data.items.firstOrNull { it.id == itemId }
    val visibleCategories = data.categories.filterNot { it.isHidden }.sortedBy { it.sortOrder }
    val defaultCategoryId = visibleCategories.firstOrNull()?.id ?: data.categories.first().id

    var name by remember(itemId) { mutableStateOf(existing?.name ?: "") }
    var description by remember(itemId) { mutableStateOf(existing?.description ?: "") }
    var categoryId by remember(itemId) { mutableStateOf(existing?.categoryId ?: defaultCategoryId) }
    var colorTheme by remember(itemId) { mutableStateOf(existing?.colorTheme ?: visibleCategories.firstOrNull { it.id == categoryId }?.colorTheme ?: "green") }
    var startDate by remember(itemId) { mutableStateOf(existing?.startDate ?: LocalDate.now().toString()) }
    var endDate by remember(itemId) { mutableStateOf(existing?.endDate ?: "") }
    var isUnlimited by remember(itemId) { mutableStateOf(existing?.isUnlimited ?: true) }
    var dailyTarget by remember(itemId) { mutableStateOf((existing?.dailyTargetCount ?: 1).toString()) }
    var timeMode by remember(itemId) { mutableStateOf(existing?.timeMode ?: TimeMode.AllDay) }
    var validStartTime by remember(itemId) { mutableStateOf(existing?.validStartTime ?: "09:00") }
    var validEndTime by remember(itemId) { mutableStateOf(existing?.validEndTime ?: "23:59") }
    var allowExtra by remember(itemId) { mutableStateOf(existing?.allowExtraCheckins ?: false) }
    var showOnDashboard by remember(itemId) { mutableStateOf(existing?.showOnDashboard ?: true) }
    var confirmDelete by remember { mutableStateOf(false) }

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
                TopTitle(
                    title = if (existing == null) strings.t("newItem") else strings.t("editItem"),
                    subtitle = if (existing == null) strings.t("newItemSub") else strings.t("editItemSub"),
                    trailing = { OutlinedButton(onClick = onBack) { Text(strings.t("backHome")) } },
                )
            }

            item {
                FormPanel {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(strings.t("itemName")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(strings.t("description")) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                    )
                    Text(strings.t("category"), fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        visibleCategories.forEach { category ->
                            FilterChip(
                                selected = categoryId == category.id,
                                onClick = {
                                    categoryId = category.id
                                    colorTheme = category.colorTheme
                                },
                                label = { Text(strings.categoryName(category.name)) },
                            )
                        }
                    }
                    Text(strings.t("color"), fontWeight = FontWeight.Bold)
                    ColorThemePicker(selected = colorTheme, strings = strings, onSelect = { colorTheme = it })
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text(strings.t("startDate")) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            enabled = !isUnlimited,
                            label = { Text(strings.t("endDate")) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                    }
                    SwitchRow(strings.t("unlimited"), isUnlimited) { isUnlimited = it }
                    OutlinedTextField(
                        value = dailyTarget,
                        onValueChange = { dailyTarget = it.filter(Char::isDigit).ifBlank { "1" } },
                        label = { Text(strings.t("dailyTarget")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Text(strings.t("validTime"), fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = timeMode == TimeMode.AllDay,
                            onClick = { timeMode = TimeMode.AllDay },
                            label = { Text(strings.t("allDay")) },
                        )
                        FilterChip(
                            selected = timeMode == TimeMode.TimeRange,
                            onClick = { timeMode = TimeMode.TimeRange },
                            label = { Text(strings.t("timeRange")) },
                        )
                    }
                    if (timeMode == TimeMode.TimeRange) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = validStartTime,
                                onValueChange = { validStartTime = it },
                                label = { Text(strings.t("startTime")) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = validEndTime,
                                onValueChange = { validEndTime = it },
                                label = { Text(strings.t("endTime")) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                        }
                    }
                    SwitchRow(strings.t("allowExtra"), allowExtra) { allowExtra = it }
                    SwitchRow(strings.t("showOnDashboard"), showOnDashboard) { showOnDashboard = it }
                    Button(
                        onClick = {
                            onSave(
                                Item(
                                    id = existing?.id ?: 0,
                                    categoryId = categoryId,
                                    name = name.trim(),
                                    description = description.trim(),
                                    colorTheme = colorTheme,
                                    startDate = startDate.ifBlank { LocalDate.now().toString() },
                                    endDate = if (isUnlimited) "" else endDate,
                                    isUnlimited = isUnlimited,
                                    dailyTargetCount = dailyTarget.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                                    timeMode = timeMode,
                                    validStartTime = validStartTime,
                                    validEndTime = validEndTime,
                                    allowExtraCheckins = allowExtra,
                                    showOnDashboard = showOnDashboard,
                                    sortOrder = existing?.sortOrder ?: 0,
                                )
                            )
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (existing == null) strings.t("createItem") else strings.t("save"))
                    }
                    if (existing != null) {
                        OutlinedButton(
                            onClick = { confirmDelete = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(strings.t("deleteItem"))
                        }
                    }
                }
            }
        }
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
}
