package com.example.lumalogapp.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumalogapp.R
import com.example.lumalogapp.data.Checkin
import com.example.lumalogapp.data.DashboardItem
import com.example.lumalogapp.data.LumaData
import com.example.lumalogapp.data.buildDashboardItems
import com.example.lumalogapp.data.canCheckIn
import com.example.lumalogapp.data.itemBadges
import com.example.lumalogapp.ui.components.AchievementBadge
import com.example.lumalogapp.ui.components.ContributionHeatmap
import com.example.lumalogapp.ui.components.lumaIconOptions
import com.example.lumalogapp.ui.components.normalizeLumaIconKey
import com.example.lumalogapp.ui.i18n.LumaStrings
import com.example.lumalogapp.ui.utils.themeColor
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckinScreen(
    data: LumaData,
    itemId: Long,
    strings: LumaStrings,
    onBack: () -> Unit,
    onOpenMakeup: () -> Unit,
    onSaveShareImage: (DashboardItem) -> Unit,
    onCheckin: () -> Unit,
) {
    val entry = remember(data, itemId) { buildDashboardItems(data).firstOrNull { it.item.id == itemId } }
    val itemCheckins = remember(data, itemId) {
        data.checkins.filter { it.itemId == itemId }.sortedByDescending { it.checkinDate }
    }
    var recordsExpanded by remember(itemId) { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme
    val isDark = isCheckinDark()
    val background = if (isDark) colorScheme.background else Color(0xFFF8FBFD)

    Scaffold(
        containerColor = background,
        topBar = {
            CheckinTopBar(
                title = strings.t("checkin"),
                entry = entry,
                strings = strings,
                onBack = onBack,
                onShare = onSaveShareImage,
                onOpenMakeup = onOpenMakeup,
            )
        },
        bottomBar = {
            entry?.let { currentEntry ->
                CheckinBottomBar(
                    entry = currentEntry,
                    strings = strings,
                    onCheckin = onCheckin,
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentPadding = PaddingValues(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            if (entry == null) {
                item {
                    Spacer(Modifier.height(72.dp))
                    Text(
                        text = strings.t("itemMissing"),
                        color = colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                item {
                    HabitIdentityCard(entry = entry, strings = strings)
                }

                item {
                    CheckinProgressCircle(
                        entry = entry,
                        strings = strings,
                        onCheckin = onCheckin,
                    )
                }

                item {
                    CheckinStatsCard(entry = entry, strings = strings)
                }

                item {
                    CheckinHeatmapCard(entry = entry, strings = strings)
                }

                item {
                    CheckinAchievementsCard(entry = entry, strings = strings)
                }

                item {
                    CheckinTipCard(strings = strings)
                }

                item {
                    CheckinRecordsCard(
                        records = itemCheckins,
                        expanded = recordsExpanded,
                        strings = strings,
                        onToggle = { recordsExpanded = !recordsExpanded },
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckinTopBar(
    title: String,
    entry: DashboardItem?,
    strings: LumaStrings,
    onBack: () -> Unit,
    onShare: (DashboardItem) -> Unit,
    onOpenMakeup: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.background)
            .statusBarsPadding()
            .padding(start = 12.dp, top = 6.dp, end = 12.dp, bottom = 6.dp)
            .height(40.dp),
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
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            entry?.let { currentEntry ->
                CheckinTopAction(
                    label = strings.t("share"),
                    iconRes = R.drawable.ic_checkin_save,
                    onClick = { onShare(currentEntry) },
                )
                if (currentEntry.item.allowMakeup) {
                    CheckinTopAction(
                        label = strings.t("makeupEntry"),
                        iconRes = R.drawable.ic_checkin_supplement,
                        onClick = onOpenMakeup,
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckinTopAction(
    label: String,
    onClick: () -> Unit,
    symbol: String? = null,
    @DrawableRes iconRes: Int? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 3.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (iconRes != null) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorScheme.onSurface),
                modifier = Modifier.size(16.dp),
            )
        } else if (symbol != null) {
            Text(
                text = symbol,
                color = colorScheme.onSurface,
                fontSize = 16.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        Text(
            text = label,
            color = colorScheme.onSurface,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
        )
    }
}

@Composable
private fun HabitIdentityCard(entry: DashboardItem, strings: LumaStrings) {
    val colorScheme = MaterialTheme.colorScheme
    val accent = themeColor(entry.item.colorTheme)
    CheckinCard(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HabitIconTile(entry = entry)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = strings.categoryName(entry.category?.name ?: ""),
                        color = accent,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(accent.copy(alpha = if (isCheckinDark()) 0.17f else 0.10f))
                            .border(1.dp, accent.copy(alpha = if (isCheckinDark()) 0.24f else 0.14f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    )
                }
                Text(
                    text = entry.item.name,
                    color = colorScheme.onSurface,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (entry.item.description.isNotBlank()) {
                    Text(
                        text = entry.item.description,
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitIconTile(entry: DashboardItem) {
    val accent = themeColor(entry.item.colorTheme)
    val iconRes = iconDrawableFor(entry.item.iconKey)
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(accent.copy(alpha = if (isCheckinDark()) 0.17f else 0.09f)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(accent),
            modifier = Modifier.size(40.dp),
        )
    }
}

@Composable
private fun CheckinProgressCircle(entry: DashboardItem, strings: LumaStrings, onCheckin: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val enabled = canCheckIn(entry.status)
    val target = entry.item.dailyTargetCount.coerceAtLeast(1)
    val count = entry.todayCount.coerceAtMost(target)
    val progress = (count.toFloat() / target).coerceIn(0f, 1f)
    val visualProgress = if (progress == 0f && enabled) 0.18f else progress
    val accent = if (enabled) colorScheme.primary else colorScheme.onSurfaceVariant
    val track = if (isCheckinDark()) colorScheme.surfaceVariant.copy(alpha = 0.58f) else colorScheme.primary.copy(alpha = 0.14f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(198.dp)
                .clip(CircleShape)
                .clickable(enabled = enabled, onClick = onCheckin),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(190.dp)) {
                drawCircle(
                    color = track,
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round),
                )
                if (visualProgress > 0f) {
                    drawArc(
                        color = accent,
                        startAngle = -92f,
                        sweepAngle = 360f * visualProgress,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round),
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "$count/$target",
                    color = accent,
                    fontSize = 35.sp,
                    lineHeight = 39.sp,
                    fontWeight = FontWeight.Normal,
                )
                Text(
                    text = strings.statusText(entry.status),
                    color = accent,
                    fontSize = 15.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = strings.statusHint(entry.status),
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun CheckinStatsCard(entry: DashboardItem, strings: LumaStrings) {
    CheckinCard(contentPadding = PaddingValues(vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CheckinStat(
                label = strings.t("streakShort"),
                value = entry.stats.currentStreak.toString(),
                unit = strings.t("dayUnit"),
                modifier = Modifier.weight(1f),
            )
            CheckinVerticalDivider()
            CheckinStat(
                label = strings.t("longestShort"),
                value = entry.stats.longestStreak.toString(),
                unit = strings.t("dayUnit"),
                modifier = Modifier.weight(1f),
            )
            CheckinVerticalDivider()
            CheckinStat(
                label = strings.t("totalShort"),
                value = entry.stats.totalCheckins.toString(),
                unit = strings.t("timesUnit"),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CheckinStat(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = label,
            color = colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
        )
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = value,
                color = colorScheme.primary,
                fontSize = 21.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = unit,
                color = colorScheme.onSurface,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun CheckinHeatmapCard(entry: DashboardItem, strings: LumaStrings) {
    val colorScheme = MaterialTheme.colorScheme
    val today = remember { LocalDate.now().toString() }
    val heatmapDays = remember(entry.heatmap) { entry.heatmap }
    CheckinCard(contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)) {
        Text(
            text = strings.t("checkinHeatmap"),
            color = colorScheme.onSurface,
            fontSize = 15.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(6.dp))
        ContributionHeatmap(
            days = heatmapDays,
            colorTheme = entry.item.colorTheme,
            strings = strings,
            showDayDetails = true,
            selectedDates = setOf(today),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CheckinAchievementsCard(entry: DashboardItem, strings: LumaStrings) {
    val colorScheme = MaterialTheme.colorScheme
    val earnedBadges = remember(entry.stats) { itemBadges(entry.stats).filter { it.earned } }
    CheckinCard(contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = strings.t("achievements"),
                color = colorScheme.onSurface,
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = strings.t("settingsBadgeCount", "count" to earnedBadges.size.toString()),
                color = colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
        }
        Spacer(Modifier.height(8.dp))
        if (earnedBadges.isEmpty()) {
            Text(
                text = strings.t("noAchievements"),
                color = colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                earnedBadges.forEach { badge ->
                    AchievementBadge(badge = badge, strings = strings)
                }
            }
        }
    }
}

@Composable
private fun CheckinTipCard(strings: LumaStrings) {
    val colorScheme = MaterialTheme.colorScheme
    CheckinCard(contentPadding = PaddingValues(horizontal = 11.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primary.copy(alpha = if (isCheckinDark()) 0.18f else 0.09f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "⊙",
                    color = colorScheme.primary,
                    fontSize = 13.sp,
                    lineHeight = 13.sp,
                )
            }
            Text(
                text = strings.t("checkinTip"),
                color = colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CheckinRecordsCard(
    records: List<Checkin>,
    expanded: Boolean,
    strings: LumaStrings,
    onToggle: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    CheckinCard(contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = strings.t("checkinRecords"),
                color = colorScheme.onSurface,
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "${records.size} ${if (expanded) "⌃" else "⌄"}",
                color = colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
        }
        if (expanded) {
            Spacer(Modifier.height(8.dp))
            if (records.isEmpty()) {
                Text(
                    text = strings.t("noCheckinRecords"),
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            } else {
                records.take(8).forEachIndexed { index, record ->
                    if (index > 0) CheckinDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = record.checkinDate,
                            color = colorScheme.onSurface,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = if (record.source == "makeup") strings.t("makeupCheckin") else strings.t("normalCheckin"),
                            color = colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                        )
                    }
                }
            }
        } else if (records.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                records.take(3).forEach { record ->
                    RecentRecordChip(record = record, strings = strings)
                }
            }
        }
    }
}

@Composable
private fun RecentRecordChip(record: Checkin, strings: LumaStrings) {
    val colorScheme = MaterialTheme.colorScheme
    val isMakeup = record.source == "makeup"
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = if (isCheckinDark()) 0.30f else 0.42f))
            .border(1.dp, colorScheme.outline.copy(alpha = if (isCheckinDark()) 0.16f else 0.10f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (isMakeup) colorScheme.tertiary else colorScheme.primary),
        )
        Text(
            text = record.checkinDate.substringAfter("-").replace("-", "/"),
            color = colorScheme.onSurface,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = if (isMakeup) strings.t("makeupCheckin") else strings.t("normalCheckin"),
            color = colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            lineHeight = 13.sp,
        )
    }
}

@Composable
private fun CheckinBottomBar(entry: DashboardItem, strings: LumaStrings, onCheckin: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val enabled = canCheckIn(entry.status)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.surface.copy(alpha = if (isCheckinDark()) 0.95f else 0.98f))
            .border(1.dp, colorScheme.outline.copy(alpha = if (isCheckinDark()) 0.16f else 0.10f))
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Button(
            onClick = onCheckin,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = Color.White,
                disabledContainerColor = colorScheme.surfaceVariant,
                disabledContentColor = colorScheme.onSurfaceVariant,
            ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, if (enabled) Color.White else colorScheme.onSurfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "✓",
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Text(
                    text = strings.t("checkinTodayCta"),
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun CheckinCard(
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
            .background(colorScheme.surface.copy(alpha = if (isCheckinDark()) 0.88f else 0.98f))
            .border(1.dp, colorScheme.outline.copy(alpha = if (isCheckinDark()) 0.18f else 0.12f), shape)
            .padding(contentPadding),
        content = content,
    )
}

@Composable
private fun CheckinVerticalDivider() {
    Box(
        modifier = Modifier
            .height(42.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = if (isCheckinDark()) 0.16f else 0.10f)),
    )
}

@Composable
private fun CheckinDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = if (isCheckinDark()) 0.14f else 0.10f)),
    )
}

private fun iconDrawableFor(key: String): Int {
    val normalized = normalizeLumaIconKey(key)
    return lumaIconOptions.firstOrNull { it.key == normalized }?.drawableRes ?: lumaIconOptions.first().drawableRes
}

@Composable
private fun isCheckinDark(): Boolean =
    MaterialTheme.colorScheme.background == Color(0xFF0C1118)
