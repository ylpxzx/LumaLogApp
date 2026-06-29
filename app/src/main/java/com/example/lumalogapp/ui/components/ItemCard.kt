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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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
    val visibleStats = listOf(
        preferences.showCurrentStreak,
        preferences.showLongestStreak,
        preferences.showCompletionRate,
        preferences.showTotalCheckins,
    ).any { it }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onOpenCheckin(entry.item.id) },
                onLongClick = { onOpenEdit(entry.item.id) },
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    entry.item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                if (preferences.showTodayStatus) {
                    StatusChip(status = entry.status, strings = strings)
                }
            }
            CategoryPill(entry.category, entry.item.colorTheme, strings)
        }

        if (visibleStats) {
            StatsRow(entry = entry, preferences = preferences, strings = strings)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            ContributionHeatmap(
                days = entry.heatmap,
                colorTheme = entry.item.colorTheme,
                strings = strings,
                showDayDetails = false,
                modifier = Modifier.padding(14.dp),
            )
        }
    }
}

@Composable
private fun StatusChip(status: CheckinStatus, strings: LumaStrings) {
    Text(
        text = strings.statusText(status),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.72f), RoundedCornerShape(7.dp))
            .clip(RoundedCornerShape(7.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 6.dp, vertical = 3.dp),
    )
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
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (preferences.showCurrentStreak) {
            StatBox(entry.stats.currentStreak.toString(), strings.t("currentStreak"), Modifier.weight(1f))
        }
        if (preferences.showLongestStreak) {
            StatBox(entry.stats.longestStreak.toString(), strings.t("longestStreak"), Modifier.weight(1f))
        }
        if (preferences.showCompletionRate) {
            StatBox("${(entry.stats.completionRate * 100).toInt()}%", strings.t("completionRate"), Modifier.weight(1f))
        }
        if (preferences.showTotalCheckins) {
            StatBox(entry.stats.totalCheckins.toString(), strings.t("totalCheckins"), Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatBox(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(value, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, maxLines = 1)
    }
}
