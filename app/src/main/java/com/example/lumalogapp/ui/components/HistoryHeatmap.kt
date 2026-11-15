package com.example.lumalogapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumalogapp.data.Checkin
import com.example.lumalogapp.data.HeatmapDay
import com.example.lumalogapp.data.Item
import com.example.lumalogapp.data.parseDate
import com.example.lumalogapp.ui.i18n.LumaStrings
import com.example.lumalogapp.ui.utils.heatmapColor
import com.example.lumalogapp.ui.utils.themeColor
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlinx.coroutines.launch

/** Weeks and their cells are materialized only as they enter the lazy viewport. */
@Composable
fun HistoryHeatmap(
    item: Item,
    checkins: List<Checkin>,
    strings: LumaStrings,
    selectedDates: Set<String>,
    onDayClick: (HeatmapDay) -> Unit,
    legend: @Composable () -> Unit,
) {
    val today = LocalDate.now()
    // Size the viewport for this month and the preceding four calendar months.
    val viewportStart = today.withDayOfMonth(1).minusMonths(4)
    val viewportMonday = viewportStart.minusDays(viewportStart.dayOfWeek.value - 1L)
    val visibleWeekCount = (ChronoUnit.DAYS.between(viewportMonday, today) / 7 + 1).toInt()
    val counts = remember(checkins) {
        checkins.groupBy { it.checkinDate }.mapValues { (_, records) -> records.sumOf { it.count } }
    }
    val makeupDates = remember(checkins) {
        checkins.filter { it.source == "makeup" && it.count > 0 }.map { it.checkinDate }.toSet()
    }
    val firstDate = remember(item.startDate, counts, today) {
        minOf(
            parseDate(item.startDate) ?: today,
            counts.keys.mapNotNull(::parseDate).minOrNull() ?: today,
            viewportStart,
        )
    }
    val firstMonday = firstDate.minusDays(firstDate.dayOfWeek.value - 1L)
    val weekCount = (ChronoUnit.DAYS.between(firstMonday, today) / 7 + 1).toInt()
    val state = rememberLazyListState(initialFirstVisibleItemIndex = weekCount - 1)
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme
    val accent = themeColor(item.colorTheme)
    val textMeasurer = rememberTextMeasurer()
    val yearStyle = LocalTextStyle.current.copy(fontSize = 10.sp, lineHeight = 12.sp)
    val firstWeekYear = (0L..6L).map { firstMonday.plusDays(it) }
        .firstOrNull { it.monthValue == 1 && it.dayOfMonth == 1 }
    // Leave room for the half of the first year badge extending before its divider.
    // Padding belongs to the history content, so it scrolls away with the oldest week.
    val historyStartPadding = with(LocalDensity.current) {
        if (firstWeekYear == null) 0.dp else {
            textMeasurer.measure(AnnotatedString(firstWeekYear.year.toString()), yearStyle)
                .size.width.toDp() / 2 + 4.dp + 1.5.dp
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(strings.t("checkinHeatmap"), color = colors.onSurface, fontSize = 15.sp, lineHeight = 19.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f))
            Text(strings.t("heatmapBackToToday"), color = accent, fontSize = 12.sp, lineHeight = 15.sp,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable {
                    scope.launch { state.animateScrollToItem(weekCount - 1) }
                }.padding(horizontal = 2.dp, vertical = 3.dp))
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val cellSize = (maxWidth - 3.dp * (visibleWeekCount - 1)) / visibleWeekCount
            LazyRow(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                contentPadding = PaddingValues(start = historyStartPadding),
            ) {
                items(count = weekCount, key = { firstMonday.plusWeeks(it.toLong()).toEpochDay() }) { index ->
                    val monday = firstMonday.plusWeeks(index.toLong())
                    val january = (0L..6L).map { monday.plusDays(it) }
                        .firstOrNull { it.monthValue == 1 && it.dayOfMonth == 1 }
                    val monthStart = (0L..6L).map { monday.plusDays(it) }
                        .firstOrNull { it.dayOfMonth == 1 && !it.isAfter(today) }
                    // A boundary week is kept intact so the weekday rows never shift.
                    Column(Modifier.width(cellSize)) {
                        Box(Modifier.fillMaxWidth().height(18.dp)) {
                            if (january != null && !january.isAfter(today)) {
                                Text(january.year.toString(), color = accent, fontSize = 10.sp, lineHeight = 12.sp,
                                    modifier = Modifier.align(Alignment.TopCenter)
                                        // The divider sits at the center of the gap before this week.
                                        .offset(x = -cellSize / 2 - 1.5.dp)
                                        .wrapContentWidth(unbounded = true, align = Alignment.CenterHorizontally)
                                        .background(accent.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                        Box(Modifier.height(20.dp)) {
                            val marker = monthStart ?: monday.takeIf { index == 0 }
                            if (marker != null) Text(strings.monthLabel(marker).substringAfter("年").substringBefore(" '"),
                                color = colors.onSurfaceVariant, fontSize = 10.sp, lineHeight = 12.sp,
                                maxLines = 1, modifier = Modifier.wrapContentWidth(unbounded = true))
                        }
                        Box {
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                repeat(7) { weekday ->
                                    val date = monday.plusDays(weekday.toLong())
                                    val key = date.toString()
                                    val count = counts[key] ?: 0
                                    val level = if (count <= 0) 0 else ceil(count.toDouble() /
                                        item.dailyTargetCount.coerceAtLeast(1) * 4).toInt().coerceIn(1, 4)
                                    val day = HeatmapDay(key, count, count >= item.dailyTargetCount, level)
                                    val visible = !date.isBefore(firstDate) && !date.isAfter(today)
                                    val shape = RoundedCornerShape(3.dp)
                                    Box(Modifier.size(cellSize).clip(shape)
                                        .then(if (visible) Modifier.background(heatmapColor(item.colorTheme,
                                            level, colors.surfaceVariant.copy(alpha = 0.62f)))
                                            .then(when {
                                                key in selectedDates -> Modifier
                                                    .border(1.dp, colors.onSurface, shape)
                                                    .padding(1.dp)
                                                    .border(1.dp, colors.surface, RoundedCornerShape(2.dp))
                                                date == today -> Modifier.border(1.dp, accent, shape)
                                                else -> Modifier
                                            })
                                            .clickable { onDayClick(day) } else Modifier)) {
                                        if (visible && key in makeupDates) Box(Modifier.align(Alignment.BottomEnd)
                                            .size(4.dp).background(if (level >= 3) colors.surface else accent, CircleShape))
                                    }
                                }
                            }
                            if (january != null && !january.isAfter(today)) Canvas(
                                Modifier.matchParentSize().offset(y = (-20).dp),
                            ) {
                                drawLine(accent.copy(alpha = 0.6f), Offset(-1.5.dp.toPx(), 0f),
                                    Offset(-1.5.dp.toPx(), size.height + 20.dp.toPx()), strokeWidth = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())))
                            }
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            legend()
            Text(strings.t("heatmapHistoryHint"), color = colors.onSurfaceVariant, fontSize = 9.sp, lineHeight = 12.sp)
        }
        Canvas(Modifier.fillMaxWidth().height(3.dp)) {
            drawRoundRect(colors.outline.copy(alpha = 0.15f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height))
            val layout = state.layoutInfo
            val visible = layout.visibleItemsInfo.size.coerceAtLeast(1)
            val width = (size.width * visible / weekCount).coerceIn(18.dp.toPx().coerceAtMost(size.width), size.width)
            val fraction = if (weekCount <= visible) 1f else
                (state.firstVisibleItemIndex.toFloat() / (weekCount - visible)).coerceIn(0f, 1f)
            drawRoundRect(colors.onSurfaceVariant.copy(alpha = 0.35f),
                topLeft = Offset((size.width - width) * fraction, 0f),
                size = androidx.compose.ui.geometry.Size(width, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height))
        }
    }
}
