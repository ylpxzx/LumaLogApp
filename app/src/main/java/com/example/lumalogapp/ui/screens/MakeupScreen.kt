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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumalogapp.data.Item
import com.example.lumalogapp.data.LumaData
import com.example.lumalogapp.data.makeupCandidateDates
import com.example.lumalogapp.data.makeupUsedThisMonth
import com.example.lumalogapp.ui.components.lumaIconOptions
import com.example.lumalogapp.ui.components.normalizeLumaIconKey
import com.example.lumalogapp.ui.i18n.LumaStrings
import com.example.lumalogapp.ui.utils.themeColor
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.max

@Composable
fun MakeupScreen(
    data: LumaData,
    itemId: Long,
    strings: LumaStrings,
    onBack: () -> Unit,
    onConfirm: (List<String>) -> Unit,
) {
    val item = remember(data, itemId) { data.items.firstOrNull { it.id == itemId } }
    var selectedDates by remember(itemId) { mutableStateOf(emptySet<String>()) }
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isMakeupDark()
    val background = if (isDark) colorScheme.background else Color(0xFFF8FBFD)

    Scaffold(
        containerColor = background,
        topBar = {
            MakeupTopBar(
                title = strings.t("makeupEntry"),
                background = background,
                onBack = onBack,
            )
        },
        bottomBar = {
            if (item != null) {
                MakeupBottomBar(
                    enabled = selectedDates.isNotEmpty(),
                    label = strings.t("confirmMakeup"),
                    onClick = { onConfirm(selectedDates.sorted()) },
                )
            }
        },
    ) { padding ->
        if (item == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = strings.t("itemMissing"),
                    color = colorScheme.error,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                )
            }
            return@Scaffold
        }

        val currentItem = item
        val itemCheckins = remember(data, itemId) { data.checkins.filter { it.itemId == itemId } }
        val candidates = remember(data, currentItem) { makeupCandidateDates(data, currentItem) }
        val usedThisMonth = remember(data, itemId) { makeupUsedThisMonth(data, itemId) }
        val remainingSlots = if (currentItem.makeupMonthlyLimit <= 0) {
            Int.MAX_VALUE
        } else {
            max(0, currentItem.makeupMonthlyLimit - usedThisMonth - selectedDates.size)
        }
        val clickableDates = if (!currentItem.allowMakeup) {
            emptySet()
        } else if (currentItem.makeupMonthlyLimit <= 0 || remainingSlots > 0) {
            candidates.toSet() + selectedDates
        } else {
            selectedDates
        }
        val completedDates = remember(itemCheckins, currentItem.dailyTargetCount) {
            itemCheckins
                .groupBy { it.checkinDate }
                .filterValues { records -> records.sumOf { it.count } >= currentItem.dailyTargetCount }
                .keys
        }
        val categoryName = remember(data, currentItem) {
            data.categories.firstOrNull { it.id == currentItem.categoryId }?.name
        }
        val month = remember { YearMonth.now() }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentPadding = PaddingValues(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                MakeupHabitCard(
                    item = currentItem,
                    categoryName = categoryName?.let(strings::categoryName) ?: strings.t("uncategorized"),
                    remainingSlots = remainingSlots,
                    strings = strings,
                )
            }
            item {
                MakeupCalendarCard(
                    month = month,
                    item = currentItem,
                    strings = strings,
                    completedDates = completedDates,
                    clickableDates = clickableDates,
                    selectedDates = selectedDates,
                    onToggleDate = { date ->
                        selectedDates = if (selectedDates.contains(date)) {
                            selectedDates - date
                        } else {
                            selectedDates + date
                        }
                    },
                )
            }
            item {
                MakeupSelectedCard(selectedDates = selectedDates, strings = strings)
            }
        }
    }
}

@Composable
private fun MakeupTopBar(
    title: String,
    background: Color,
    onBack: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .statusBarsPadding()
            .padding(start = 12.dp, top = 6.dp, end = 12.dp, bottom = 6.dp)
            .height(42.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            color = colorScheme.onBackground,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
        Text(
            text = "←",
            color = colorScheme.onBackground,
            fontSize = 25.sp,
            lineHeight = 25.sp,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clip(CircleShape)
                .clickable(onClick = onBack)
                .padding(horizontal = 5.dp, vertical = 3.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(28.dp)
                .clip(CircleShape)
                .border(1.4.dp, colorScheme.onSurfaceVariant.copy(alpha = 0.72f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "?",
                color = colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun MakeupHabitCard(
    item: Item,
    categoryName: String,
    remainingSlots: Int,
    strings: LumaStrings,
) {
    val colorScheme = MaterialTheme.colorScheme
    val accent = themeColor(item.colorTheme)
    MakeupCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(accent.copy(alpha = if (isMakeupDark()) 0.17f else 0.09f)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(iconDrawableFor(item.iconKey)),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(accent),
                    modifier = Modifier.size(45.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = item.name,
                    color = colorScheme.onSurface,
                    fontSize = 21.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = categoryName,
                    color = accent,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(accent.copy(alpha = if (isMakeupDark()) 0.18f else 0.10f))
                        .border(1.dp, accent.copy(alpha = if (isMakeupDark()) 0.26f else 0.15f), RoundedCornerShape(9.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
                Text(
                    text = if (item.makeupMonthlyLimit <= 0) {
                        strings.t("makeupUnlimitedShort")
                    } else {
                        strings.t("makeupRemainingShort", "count" to remainingSlots.toString())
                    },
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MakeupCalendarCard(
    month: YearMonth,
    item: Item,
    strings: LumaStrings,
    completedDates: Set<String>,
    clickableDates: Set<String>,
    selectedDates: Set<String>,
    onToggleDate: (String) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val accent = themeColor(item.colorTheme)
    val cells = remember(month) { makeupCalendarCells(month) }
    MakeupCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "‹",
                color = colorScheme.onSurfaceVariant,
                fontSize = 31.sp,
                lineHeight = 31.sp,
                modifier = Modifier.align(Alignment.CenterStart),
            )
            Text(
                text = strings.fullMonthLabel(month),
                color = colorScheme.onSurface,
                fontSize = 21.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
            Text(
                text = "›",
                color = colorScheme.onSurfaceVariant,
                fontSize = 31.sp,
                lineHeight = 31.sp,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            strings.weekdayLabels().forEach { label ->
                Text(
                    text = label,
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
            }
        }
        Spacer(Modifier.height(9.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            cells.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    week.forEach { cell ->
                        val dateText = cell.date.toString()
                        val status = when {
                            !cell.inCurrentMonth -> MakeupDayStatus.Outside
                            selectedDates.contains(dateText) -> MakeupDayStatus.Selected
                            clickableDates.contains(dateText) -> MakeupDayStatus.Available
                            completedDates.contains(dateText) -> MakeupDayStatus.Completed
                            else -> MakeupDayStatus.Unavailable
                        }
                        MakeupCalendarDay(
                            date = cell.date,
                            status = status,
                            accent = accent,
                            onClick = { onToggleDate(dateText) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                        )
                    }
                }
            }
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MakeupLegendItem(type = MakeupLegendType.Completed, label = strings.t("makeupCompletedLegend"), accent = accent)
            MakeupLegendItem(type = MakeupLegendType.Available, label = strings.t("makeupAvailableLegend"), accent = accent)
            MakeupLegendItem(type = MakeupLegendType.Selected, label = strings.t("makeupSelectedLegend"), accent = accent)
            MakeupLegendItem(type = MakeupLegendType.Unavailable, label = strings.t("makeupUnavailableLegend"), accent = accent)
        }
    }
}

@Composable
private fun MakeupCalendarDay(
    date: LocalDate,
    status: MakeupDayStatus,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(9.dp)
    val enabled = status == MakeupDayStatus.Selected || status == MakeupDayStatus.Available
    val isDark = isMakeupDark()
    val background = when (status) {
        MakeupDayStatus.Selected -> accent
        MakeupDayStatus.Available -> colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.22f else 0.20f)
        MakeupDayStatus.Completed -> colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.26f else 0.28f)
        MakeupDayStatus.Unavailable -> colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.14f else 0.18f)
        MakeupDayStatus.Outside -> colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.08f else 0.10f)
    }
    val borderColor = when (status) {
        MakeupDayStatus.Available -> accent
        MakeupDayStatus.Selected -> accent
        else -> colorScheme.outline.copy(alpha = if (isDark) 0.14f else 0.10f)
    }
    val textColor = when (status) {
        MakeupDayStatus.Selected -> Color.White
        MakeupDayStatus.Available -> colorScheme.onSurface
        MakeupDayStatus.Completed -> colorScheme.onSurface
        MakeupDayStatus.Unavailable -> colorScheme.onSurfaceVariant.copy(alpha = 0.68f)
        MakeupDayStatus.Outside -> colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(background)
            .border(if (status == MakeupDayStatus.Available) 1.4.dp else 1.dp, borderColor, shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 16.sp,
                lineHeight = 18.sp,
                fontWeight = if (status == MakeupDayStatus.Unavailable || status == MakeupDayStatus.Outside) {
                    FontWeight.Normal
                } else {
                    FontWeight.Medium
                },
            )
            when (status) {
                MakeupDayStatus.Selected -> Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
                MakeupDayStatus.Completed -> Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = if (isDark) 0.86f else 0.78f)),
                )
                else -> Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun MakeupLegendItem(type: MakeupLegendType, label: String, accent: Color) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        when (type) {
            MakeupLegendType.Completed -> Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            MakeupLegendType.Available -> Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .border(1.5.dp, accent, RoundedCornerShape(3.dp)),
            )
            MakeupLegendType.Selected -> Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(accent),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            MakeupLegendType.Unavailable -> Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colorScheme.surfaceVariant.copy(alpha = if (isMakeupDark()) 0.28f else 0.40f)),
            )
        }
        Text(
            text = label,
            color = colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun MakeupSelectedCard(selectedDates: Set<String>, strings: LumaStrings) {
    val colorScheme = MaterialTheme.colorScheme
    val selectedLocalDates = remember(selectedDates) {
        selectedDates
            .mapNotNull { date -> runCatching { LocalDate.parse(date) }.getOrNull() }
            .sorted()
    }
    MakeupCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primary.copy(alpha = if (isMakeupDark()) 0.14f else 0.09f))
                    .border(1.5.dp, colorScheme.primary.copy(alpha = 0.76f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "✓",
                    color = colorScheme.primary,
                    fontSize = 25.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = strings.t("makeupSelected", "count" to selectedDates.size.toString()),
                    color = colorScheme.onSurface,
                    fontSize = 17.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = selectedLocalDates.takeIf { it.isNotEmpty() }?.let(strings::compactDateList)
                        ?: strings.t("makeupNoDatesSelected"),
                    color = if (selectedLocalDates.isEmpty()) colorScheme.onSurfaceVariant else colorScheme.primary,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun MakeupBottomBar(
    enabled: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.surface.copy(alpha = if (isMakeupDark()) 0.96f else 0.98f))
            .border(1.dp, colorScheme.outline.copy(alpha = if (isMakeupDark()) 0.16f else 0.10f))
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = Color.White,
                disabledContainerColor = colorScheme.surfaceVariant,
                disabledContentColor = colorScheme.onSurfaceVariant,
            ),
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun MakeupCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colorScheme.surface.copy(alpha = if (isMakeupDark()) 0.88f else 0.98f))
            .border(1.dp, colorScheme.outline.copy(alpha = if (isMakeupDark()) 0.18f else 0.12f), shape)
            .padding(contentPadding),
        content = content,
    )
}

private fun makeupCalendarCells(month: YearMonth): List<MakeupCalendarCell> {
    val firstDay = month.atDay(1)
    val leadingDays = firstDay.dayOfWeek.value % 7
    val startDate = firstDay.minusDays(leadingDays.toLong())
    return List(42) { index ->
        val date = startDate.plusDays(index.toLong())
        MakeupCalendarCell(date = date, inCurrentMonth = YearMonth.from(date) == month)
    }
}

@DrawableRes
private fun iconDrawableFor(key: String): Int {
    val normalized = normalizeLumaIconKey(key)
    return lumaIconOptions.firstOrNull { it.key == normalized }?.drawableRes ?: lumaIconOptions.first().drawableRes
}

@Composable
private fun isMakeupDark(): Boolean =
    MaterialTheme.colorScheme.background == Color(0xFF0C1118)

private data class MakeupCalendarCell(
    val date: LocalDate,
    val inCurrentMonth: Boolean,
)

private enum class MakeupDayStatus {
    Outside,
    Unavailable,
    Completed,
    Available,
    Selected,
}

private enum class MakeupLegendType {
    Completed,
    Available,
    Selected,
    Unavailable,
}
