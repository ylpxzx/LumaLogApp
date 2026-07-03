package com.example.lumalogapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumalogapp.data.AppPreferences
import com.example.lumalogapp.data.Category
import com.example.lumalogapp.data.CheckinStatus
import com.example.lumalogapp.data.DashboardItem
import com.example.lumalogapp.ui.i18n.LumaStrings
import com.example.lumalogapp.ui.utils.themeColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemCard(
    entry: DashboardItem,
    preferences: AppPreferences,
    strings: LumaStrings,
    onOpenCheckin: (Long) -> Unit,
    onOpenEdit: (Long) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val visibleStats = listOf(
        preferences.showCurrentStreak,
        preferences.showLongestStreak,
        preferences.showCompletionRate,
        preferences.showTotalCheckins,
    ).any { it }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onOpenCheckin(entry.item.id) },
                onLongClick = { onOpenEdit(entry.item.id) },
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface.copy(alpha = 0.97f)),
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.58f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 13.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LumaIconBadge(
                    iconKey = entry.item.iconKey,
                    size = 38.dp,
                )
                Spacer(Modifier.width(11.dp))
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Text(
                        text = entry.item.name,
                        color = colorScheme.onSurface,
                        fontSize = 18.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (preferences.showTodayStatus) {
                        StatusChip(status = entry.status, strings = strings)
                    }
                }
                Spacer(Modifier.width(7.dp))
                HabitCategoryPill(entry.category, entry.item.colorTheme, strings)
            }

            if (visibleStats) {
                StatsRow(entry = entry, preferences = preferences, strings = strings)
            }

            ContributionHeatmap(
                days = entry.heatmap,
                colorTheme = entry.item.colorTheme,
                strings = strings,
                showDayDetails = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            )
        }
    }
}

@Composable
private fun HabitCategoryPill(category: Category?, fallbackTheme: String, strings: LumaStrings) {
    val color = themeColor(category?.colorTheme ?: fallbackTheme)
    val isDark = isLumaCardDark()
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = if (isDark) 0.16f else 0.08f))
            .border(1.dp, color.copy(alpha = if (isDark) 0.26f else 0.14f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color.copy(alpha = 0.82f)),
        )
        Text(
            text = strings.categoryName(category?.name ?: strings.t("uncategorized")),
            color = color.copy(alpha = 0.90f),
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatusChip(status: CheckinStatus, strings: LumaStrings) {
    val color = statusColor(status, isLumaCardDark())
    Text(
        text = strings.statusText(status),
        color = color,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.11f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

private fun statusColor(status: CheckinStatus, isDark: Boolean): Color = when (status) {
    CheckinStatus.Completed,
    CheckinStatus.CompletedCanContinue -> if (isDark) Color(0xFF4ADE80) else Color(0xFF129A58)
    CheckinStatus.Available -> if (isDark) Color(0xFF5EEAD4) else Color(0xFF0C8EA0)
    CheckinStatus.BeforeTimeWindow,
    CheckinStatus.AfterTimeWindow -> if (isDark) Color(0xFFFBBF24) else Color(0xFFE58B22)
    CheckinStatus.NotStarted,
    CheckinStatus.Ended -> if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
}

@Composable
fun CategoryPill(category: Category?, fallbackTheme: String, strings: LumaStrings) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .size(9.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(themeColor(category?.colorTheme ?: fallbackTheme)),
        )
        Text(
            strings.categoryName(category?.name ?: strings.t("uncategorized")),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun StatsRow(entry: DashboardItem, preferences: AppPreferences, strings: LumaStrings) {
    val colorScheme = MaterialTheme.colorScheme
    val stats = buildList {
        if (preferences.showCurrentStreak) add(entry.stats.currentStreak.toString() to strings.t("currentStreak"))
        if (preferences.showLongestStreak) add(entry.stats.longestStreak.toString() to strings.t("longestStreak"))
        if (preferences.showCompletionRate) add("${(entry.stats.completionRate * 100).toInt()}%" to strings.t("completionRate"))
        if (preferences.showTotalCheckins) add(entry.stats.totalCheckins.toString() to strings.t("totalCheckins"))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = if (isLumaCardDark()) 0.54f else 0.42f))
            .border(1.dp, colorScheme.outline.copy(alpha = 0.42f), RoundedCornerShape(13.dp))
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        stats.forEachIndexed { index, stat ->
            StatBox(stat.first, stat.second, Modifier.weight(1f))
            if (index != stats.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(colorScheme.outline.copy(alpha = 0.38f)),
                )
            }
        }
    }
}

@Composable
private fun StatBox(value: String, label: String, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            color = colorScheme.onSurface,
            fontSize = 18.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        Text(
            text = label,
            color = colorScheme.onSurfaceVariant,
            fontSize = 9.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun isLumaCardDark(): Boolean {
    return MaterialTheme.colorScheme.background == Color(0xFF0C1118)
}
