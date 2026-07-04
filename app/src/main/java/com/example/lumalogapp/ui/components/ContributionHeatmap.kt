package com.example.lumalogapp.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumalogapp.data.HeatmapDay
import com.example.lumalogapp.ui.i18n.LumaStrings
import com.example.lumalogapp.ui.utils.heatmapColor
import com.example.lumalogapp.ui.utils.themeColor
import java.time.LocalDate
import kotlin.math.floor

private const val HeatmapRowCount = 7

private data class HeatmapCell(
    val day: HeatmapDay?,
    val visible: Boolean,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContributionHeatmap(
    days: List<HeatmapDay>,
    colorTheme: String,
    strings: LumaStrings,
    showDayDetails: Boolean = false,
    clickableDates: Set<String> = emptySet(),
    selectedDates: Set<String> = emptySet(),
    makeupDates: Set<String> = emptySet(),
    dayDetailLabels: Map<String, String> = emptyMap(),
    onDayClick: ((HeatmapDay) -> Unit)? = null,
    maxCellSize: Dp? = null,
    showContainer: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var selectedDay by remember(days) { mutableStateOf<HeatmapDay?>(null) }
    val colorScheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val isDark = colorScheme.background == Color(0xFF0C1118)
    val accent = themeColor(colorTheme)
    val emptySquareColor = if (isDark) {
        colorScheme.surfaceVariant.copy(alpha = 0.86f)
    } else {
        colorScheme.surfaceVariant.copy(alpha = 0.62f)
    }
    val labelColor = colorScheme.onSurfaceVariant
    val weeks = remember(days) {
        val parsedDays = days.mapNotNull { day ->
            runCatching { LocalDate.parse(day.date) }.getOrNull()?.let { date -> date to day }
        }
        val leadingSlots = parsedDays.firstOrNull()?.first?.dayOfWeek?.value?.minus(1) ?: 0
        val trailingSlots = (HeatmapRowCount - ((leadingSlots + parsedDays.size) % HeatmapRowCount)) % HeatmapRowCount
        val leadingCells = parsedDays.firstOrNull()?.first?.let { firstDate ->
            List(leadingSlots) { index ->
                val date = firstDate.minusDays((leadingSlots - index).toLong())
                HeatmapCell(
                    day = HeatmapDay(date = date.toString(), count = 0, completed = false, level = 0),
                    visible = true,
                )
            }
        } ?: emptyList()
        (leadingCells +
            parsedDays.map { HeatmapCell(day = it.second, visible = true) } +
            List(trailingSlots) { HeatmapCell(day = null, visible = false) })
            .chunked(HeatmapRowCount)
    }
    val monthMarkers = remember(weeks, strings) {
        weeks.mapIndexed { index, week ->
            val weekDates = week.mapNotNull { cell -> cell.day?.let { runCatching { LocalDate.parse(it.date) }.getOrNull() } }
            val monthStart = weekDates.firstOrNull { it.dayOfMonth == 1 }
            val markerDate = monthStart ?: weekDates.firstOrNull().takeIf { index == 0 }
            markerDate?.let { strings.monthLabel(it) }
        }
    }

    val panelShape = RoundedCornerShape(13.dp)
    val panelBackground = if (isDark) {
        colorScheme.surfaceVariant.copy(alpha = 0.34f)
    } else {
        colorScheme.surface.copy(alpha = 0.78f)
    }
    val panelBorder = colorScheme.outline.copy(alpha = if (isDark) 0.22f else 0.16f)
    val compact = maxCellSize != null
    val heatmapBoxModifier = Modifier
        .fillMaxWidth()
        .let { base ->
            if (showContainer) {
                base
                    .clip(panelShape)
                    .background(panelBackground)
                    .border(1.dp, panelBorder, panelShape)
                    .padding(horizontal = if (compact) 8.dp else 10.dp, vertical = if (compact) 7.dp else 9.dp)
            } else {
                base
            }
        }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Box(
            modifier = heatmapBoxModifier,
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = if (showContainer) Alignment.Center else Alignment.CenterStart,
            ) {
                val weekCount = weeks.size.coerceAtLeast(1)
                val horizontalGap = if (maxCellSize == null) 3.dp else 2.dp
                val verticalGap = if (maxCellSize == null) 4.dp else 3.dp
                val cellSize = with(density) {
                    val availablePx = maxWidth.toPx()
                    val gapPx = horizontalGap.toPx()
                    floor((availablePx - gapPx * (weekCount - 1)) / weekCount)
                        .coerceAtLeast(1f)
                        .toDp()
                }.let { size -> maxCellSize?.let { size.coerceAtMost(it) } ?: size }
                val contentWidth = cellSize * weekCount.toFloat() + horizontalGap * (weekCount - 1).toFloat()

                Column(
                    modifier = Modifier.width(contentWidth),
                    verticalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 7.dp),
                ) {
                    Row(
                        modifier = Modifier.width(contentWidth),
                        horizontalArrangement = Arrangement.spacedBy(horizontalGap),
                    ) {
                        monthMarkers.forEach { label ->
                            Box(modifier = Modifier.width(cellSize)) {
                                if (label != null) {
                                    Text(
                                        text = label,
                                        color = labelColor,
                                        fontSize = if (compact) 9.sp else 10.sp,
                                        lineHeight = if (compact) 11.sp else 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        modifier = Modifier.wrapContentWidth(unbounded = true),
                                    )
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.width(contentWidth), horizontalArrangement = Arrangement.spacedBy(horizontalGap)) {
                        weeks.forEach { week ->
                            Column(modifier = Modifier.width(cellSize), verticalArrangement = Arrangement.spacedBy(verticalGap)) {
                                week.forEach { cell ->
                                    val day = cell.day
                                    val shape = RoundedCornerShape(3.dp)
                                    val canClick = day != null && onDayClick != null && clickableDates.contains(day.date)
                                    val isSelected = day != null && (selectedDates.contains(day.date) || (showDayDetails && selectedDay?.date == day.date))
                                    val backgroundColor = when {
                                        !cell.visible -> Color.Transparent
                                        day != null -> heatmapColor(colorTheme, day.level, emptySquareColor)
                                        else -> Color.Transparent
                                    }
                                    var cellModifier = Modifier
                                        .size(cellSize)
                                        .clip(shape)
                                        .background(backgroundColor)
                                        .alpha(if (onDayClick != null && day != null && !canClick) 0.36f else 1f)
                                    if (isSelected) {
                                        cellModifier = cellModifier.border(1.dp, accent.copy(alpha = 0.78f), shape)
                                    }
                                    if (day != null) {
                                        when {
                                            canClick -> cellModifier = cellModifier.combinedClickable(onClick = { onDayClick?.invoke(day) })
                                            showDayDetails && onDayClick == null -> cellModifier = cellModifier.combinedClickable(onClick = { selectedDay = day })
                                        }
                                    }

                                    Box(
                                        modifier = cellModifier,
                                    ) {
                                        if (day != null && makeupDates.contains(day.date) && cell.visible) {
                                            val markerSize = if (compact) 3.dp else 4.dp
                                            val markerColor = if (day.level >= 3) Color.White else accent
                                            val markerBorder = if (day.level >= 3) {
                                                accent.copy(alpha = 0.72f)
                                            } else {
                                                Color.White.copy(alpha = if (isDark) 0.72f else 0.90f)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .size(markerSize)
                                                    .clip(CircleShape)
                                                    .background(markerColor)
                                                    .border(1.dp, markerBorder, CircleShape),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (showDayDetails) {
            selectedDay?.let { day ->
                Text(
                    text = dayDetailLabels[day.date] ?: strings.heatmapDayLabel(day),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.42f else 0.58f))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                )
            }
        }
    }
}
