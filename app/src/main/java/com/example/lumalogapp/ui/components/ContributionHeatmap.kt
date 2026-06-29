package com.example.lumalogapp.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumalogapp.data.HeatmapDay
import com.example.lumalogapp.ui.i18n.LumaStrings
import com.example.lumalogapp.ui.utils.heatmapColor
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContributionHeatmap(
    days: List<HeatmapDay>,
    colorTheme: String,
    strings: LumaStrings,
    showDayDetails: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var selectedDay by remember(days) { mutableStateOf<HeatmapDay?>(null) }
    val emptySquareColor =
        if (MaterialTheme.colorScheme.background == Color(0xFF0C1118)) Color(0xFF1C2634) else Color(0xFFE6EBF2)
    val weeks = remember(days) {
        val parsedDays = days.mapNotNull { day ->
            runCatching { LocalDate.parse(day.date) }.getOrNull()?.let { date -> date to day }
        }
        val leadingSlots = parsedDays.firstOrNull()?.first?.dayOfWeek?.value?.minus(1) ?: 0
        val trailingSlots = (7 - ((leadingSlots + parsedDays.size) % 7)) % 7
        (List<HeatmapDay?>(leadingSlots) { null } + parsedDays.map { it.second } + List<HeatmapDay?>(trailingSlots) { null })
            .chunked(7)
    }
    val monthMarkers = remember(weeks, strings) {
        weeks.mapIndexed { index, week ->
            val weekDates = week.mapNotNull { day -> day?.let { runCatching { LocalDate.parse(it.date) }.getOrNull() } }
            val monthStart = weekDates.firstOrNull { it.dayOfMonth == 1 }
            val markerDate = monthStart ?: weekDates.firstOrNull().takeIf { index == 0 }
            markerDate?.let { strings.monthLabel(it) }
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            monthMarkers.forEach { label ->
                Box(modifier = Modifier.weight(1f)) {
                    if (label != null) {
                        Text(
                            text = label,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.wrapContentWidth(unbounded = true),
                        )
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            weeks.forEach { week ->
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    week.forEach { day ->
                        val shape = RoundedCornerShape(3.dp)
                        val isSelected = showDayDetails && day != null && selectedDay?.date == day.date
                        var cellModifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(shape)
                            .background(
                                if (day == null) Color.Transparent else heatmapColor(colorTheme, day.level, emptySquareColor),
                            )
                        if (isSelected) {
                            cellModifier = cellModifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.78f), shape)
                        }
                        if (showDayDetails && day != null) {
                            cellModifier = cellModifier.combinedClickable(onClick = { selectedDay = day })
                        }

                        Box(
                            modifier = cellModifier,
                        )
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
