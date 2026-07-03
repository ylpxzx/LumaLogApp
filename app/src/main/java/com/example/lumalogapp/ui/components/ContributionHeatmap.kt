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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumalogapp.data.HeatmapDay
import com.example.lumalogapp.ui.i18n.LumaStrings
import com.example.lumalogapp.ui.utils.heatmapColor
import java.time.LocalDate

private const val HeatmapRowCount = 7

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContributionHeatmap(
    days: List<HeatmapDay>,
    colorTheme: String,
    strings: LumaStrings,
    showDayDetails: Boolean = false,
    clickableDates: Set<String> = emptySet(),
    selectedDates: Set<String> = emptySet(),
    onDayClick: ((HeatmapDay) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var selectedDay by remember(days) { mutableStateOf<HeatmapDay?>(null) }
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background == Color(0xFF0C1118)
    val emptySquareColor = if (isDark) colorScheme.surfaceVariant.copy(alpha = 0.82f) else Color(0xFFE8EDF4)
    val labelColor = colorScheme.onSurfaceVariant
    val weeks = remember(days) {
        val parsedDays = days.mapNotNull { day ->
            runCatching { LocalDate.parse(day.date) }.getOrNull()?.let { date -> date to day }
        }
        val leadingSlots = parsedDays.firstOrNull()?.first?.dayOfWeek?.value?.minus(1) ?: 0
        val trailingSlots = (HeatmapRowCount - ((leadingSlots + parsedDays.size) % HeatmapRowCount)) % HeatmapRowCount
        (List<HeatmapDay?>(leadingSlots) { null } + parsedDays.map { it.second } + List<HeatmapDay?>(trailingSlots) { null })
            .chunked(HeatmapRowCount)
    }
    val monthMarkers = remember(weeks, strings) {
        weeks.mapIndexed { index, week ->
            val weekDates = week.mapNotNull { day -> day?.let { runCatching { LocalDate.parse(it.date) }.getOrNull() } }
            val monthStart = weekDates.firstOrNull { it.dayOfMonth == 1 }
            val markerDate = monthStart ?: weekDates.firstOrNull().takeIf { index == 0 }
            markerDate?.let { strings.monthLabel(it) }
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val weekCount = weeks.size.coerceAtLeast(1)
            val horizontalGap = 3.dp
            val verticalGap = 4.dp
            val cellSize = ((maxWidth - horizontalGap * (weekCount - 1).toFloat()) / weekCount.toFloat()).coerceAtLeast(1.dp)

            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(horizontalGap),
                ) {
                    monthMarkers.forEach { label ->
                        Box(modifier = Modifier.width(cellSize)) {
                            if (label != null) {
                                Text(
                                    text = label,
                                    color = labelColor,
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    modifier = Modifier.wrapContentWidth(unbounded = true),
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(horizontalGap)) {
                    weeks.forEach { week ->
                        Column(modifier = Modifier.width(cellSize), verticalArrangement = Arrangement.spacedBy(verticalGap)) {
                            week.forEach { day ->
                                val shape = RoundedCornerShape(3.dp)
                                val canClick = day != null && onDayClick != null && clickableDates.contains(day.date)
                                val isSelected = day != null && (selectedDates.contains(day.date) || (showDayDetails && selectedDay?.date == day.date))
                                var cellModifier = Modifier
                                    .size(cellSize)
                                    .clip(shape)
                                    .background(
                                        if (day == null) Color.Transparent else heatmapColor(colorTheme, day.level, emptySquareColor),
                                    )
                                    .alpha(if (onDayClick != null && day != null && !canClick) 0.36f else 1f)
                                if (isSelected) {
                                    cellModifier = cellModifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.78f), shape)
                                }
                                if (day != null) {
                                    when {
                                        canClick -> cellModifier = cellModifier.combinedClickable(onClick = { onDayClick?.invoke(day) })
                                        showDayDetails && onDayClick == null -> cellModifier = cellModifier.combinedClickable(onClick = { selectedDay = day })
                                    }
                                }

                                Box(
                                    modifier = cellModifier,
                                )
                            }
                        }
                    }
                }
            }
        }
        if (showDayDetails) {
            selectedDay?.let { day ->
                Text(
                    text = strings.heatmapDayLabel(day),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                )
            }
        }
    }
}
