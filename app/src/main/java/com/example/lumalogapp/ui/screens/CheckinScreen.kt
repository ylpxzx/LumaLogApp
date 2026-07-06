package com.example.lumalogapp.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.lumalogapp.R
import com.example.lumalogapp.data.Checkin
import com.example.lumalogapp.data.DashboardItem
import com.example.lumalogapp.data.HeatmapDay
import com.example.lumalogapp.data.LumaData
import com.example.lumalogapp.data.buildDashboardItems
import com.example.lumalogapp.data.canCheckIn
import com.example.lumalogapp.data.itemBadges
import com.example.lumalogapp.ui.components.AchievementBadge
import com.example.lumalogapp.ui.components.ContributionHeatmap
import com.example.lumalogapp.ui.components.FoldIndicator
import com.example.lumalogapp.ui.components.lumaIconOptions
import com.example.lumalogapp.ui.components.normalizeLumaIconKey
import com.example.lumalogapp.ui.i18n.LumaStrings
import com.example.lumalogapp.ui.share.ShareTemplate
import com.example.lumalogapp.ui.utils.heatmapColor
import com.example.lumalogapp.ui.utils.themeColor
import java.time.LocalDate
import kotlin.math.roundToInt

private const val CheckinNoteMaxLength = 200

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckinScreen(
    data: LumaData,
    itemId: Long,
    strings: LumaStrings,
    onBack: () -> Unit,
    onOpenMakeup: () -> Unit,
    onSaveShareImage: (DashboardItem, ShareTemplate) -> Unit,
    onCheckin: () -> Unit,
    onUpdateNote: (String, String) -> Unit,
) {
    val entry = remember(data, itemId) { buildDashboardItems(data).firstOrNull { it.item.id == itemId } }
    val itemCheckins = remember(data, itemId) {
        data.checkins.filter { it.itemId == itemId }.sortedByDescending { it.checkinDate }
    }
    var achievementsExpanded by remember(itemId) { mutableStateOf(false) }
    var shareEntry by remember(itemId) { mutableStateOf<DashboardItem?>(null) }

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
                onShare = { shareEntry = it },
            )
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
                    CheckinGoalCard(
                        entry = entry,
                        strings = strings,
                        onOpenMakeup = onOpenMakeup,
                        onCheckin = onCheckin,
                    )
                }

                item {
                    CheckinStatsCard(entry = entry, strings = strings)
                }

                item {
                    CheckinHeatmapCard(
                        entry = entry,
                        checkins = itemCheckins,
                        strings = strings,
                        onUpdateNote = onUpdateNote,
                    )
                }

                item {
                    CheckinAchievementsCard(
                        entry = entry,
                        expanded = achievementsExpanded,
                        strings = strings,
                        onToggle = { achievementsExpanded = !achievementsExpanded },
                    )
                }

                item {
                    CheckinTipCard(entry = entry, strings = strings)
                }
            }
        }
    }

    shareEntry?.let { currentEntry ->
        ShareTemplatePickerDialog(
            strings = strings,
            colorTheme = currentEntry.item.colorTheme,
            onDismiss = { shareEntry = null },
            onSelect = { template ->
                shareEntry = null
                onSaveShareImage(currentEntry, template)
            },
        )
    }
}

@Composable
private fun CheckinTopBar(
    title: String,
    entry: DashboardItem?,
    strings: LumaStrings,
    onBack: () -> Unit,
    onShare: (DashboardItem) -> Unit,
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
                    onClick = { onShare(currentEntry) },
                )
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
private fun ShareTemplatePickerDialog(
    strings: LumaStrings,
    colorTheme: String,
    onDismiss: () -> Unit,
    onSelect: (ShareTemplate) -> Unit,
) {
    val templates = ShareTemplate.entries
    var selectedTemplate by remember { mutableStateOf(templates.getOrNull(1) ?: templates.first()) }
    val colorScheme = MaterialTheme.colorScheme
    val dark = isCheckinDark()
    val habitAccent = themeColor(colorTheme)
    val accent = if (dark) lerp(habitAccent, Color.White, 0.18f) else habitAccent
    val scrim = Color.Black.copy(alpha = if (dark) 0.52f else 0.24f)
    val panelBackground = if (dark) Color(0xFF11131A) else Color(0xFFFFFBFF)
    val handleColor = if (dark) Color(0xFF5E6370) else Color(0xFFB7B8C1)
    val titleColor = if (dark) colorScheme.onSurface else Color(0xFF151827)
    val subtitleColor = if (dark) colorScheme.onSurfaceVariant else Color(0xFF676D80)
    val optionBackground = if (dark) Color(0xFF181A23) else Color(0xFFFFFCFF)
    val cancelColor = if (dark) Color(0xFFADB3C2) else Color(0xFF777B8F)
    val silentInteraction = remember { MutableInteractionSource() }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrim)
                .clickable(interactionSource = silentInteraction, indication = null, onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(383.dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(panelBackground)
                    .clickable(
                        interactionSource = silentInteraction,
                        indication = null,
                        onClick = {},
                    )
                    .padding(horizontal = 14.dp),
            ) {
                Spacer(Modifier.height(9.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(35.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(handleColor),
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = strings.t("shareTemplateTitle"),
                    color = titleColor,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = strings.t("shareTemplateSub"),
                    color = subtitleColor,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Normal,
                )
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    templates.take(2).forEach { template ->
                        ShareTemplateOption(
                            template = template,
                            label = strings.shareTemplateName(template),
                            accent = accent,
                            background = optionBackground,
                            contentColor = titleColor,
                            selected = selectedTemplate == template,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTemplate = template },
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    templates.drop(2).forEach { template ->
                        ShareTemplateOption(
                            template = template,
                            label = strings.shareTemplateName(template),
                            accent = accent,
                            background = optionBackground,
                            contentColor = titleColor,
                            selected = selectedTemplate == template,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTemplate = template },
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ShareTemplateDialogAction(
                        text = strings.t("cancel"),
                        color = cancelColor,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                    ShareTemplateDialogAction(
                        text = strings.t("confirm"),
                        color = accent,
                        onClick = { onSelect(selectedTemplate) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareTemplateOption(
    template: ShareTemplate,
    label: String,
    accent: Color,
    background: Color,
    contentColor: Color,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val optionShape = RoundedCornerShape(12.dp)
    val borderWidth = if (selected) 2.dp else 1.dp
    val optionBorder = accent.copy(alpha = if (selected) 0.92f else 0.72f)
    Column(
        modifier = modifier
            .height(109.dp)
            .clip(optionShape)
            .background(background)
            .border(borderWidth, optionBorder, optionShape)
            .clickable(onClick = onClick)
            .padding(start = 8.dp, top = 7.dp, end = 8.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ShareTemplatePreview(
            template = template,
            accent = accent,
            modifier = Modifier
                .fillMaxWidth()
                .height(63.dp),
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = label,
            color = contentColor,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ShareTemplateDialogAction(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun ShareTemplatePreview(
    template: ShareTemplate,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
    ) {
        when (template) {
            ShareTemplate.Classic -> ClassicTemplatePreview(accent)
            ShareTemplate.Poster -> PosterTemplatePreview(accent)
            ShareTemplate.Zen -> ZenTemplatePreview(accent)
            ShareTemplate.Dashboard -> DashboardTemplatePreview(accent)
        }
    }
}

@Composable
private fun ClassicTemplatePreview(accent: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.Top) {
            PreviewSoftSquare(accent, 14.dp)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.padding(top = 2.dp)) {
                PreviewLine(width = 49.dp, height = 4.dp, color = accent)
                PreviewLine(width = 16.dp, height = 3.dp, color = accent.copy(alpha = 0.48f))
            }
        }
        PreviewMiniHeatmap(accent, rows = 4, columns = 16)
        PreviewFooterLines(accent)
    }
}

@Composable
private fun PosterTemplatePreview(accent: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.Top) {
                PreviewSoftSquare(accent, 14.dp)
                PreviewLine(width = 56.dp, height = 5.dp, color = accent, modifier = Modifier.padding(top = 2.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(3) {
                    PreviewLine(width = 9.dp, height = 3.dp, color = accent.copy(alpha = 0.52f))
                }
            }
        }
        PreviewMiniHeatmap(accent, rows = 4, columns = 18)
        PreviewFooterLines(accent)
    }
}

@Composable
private fun ZenTemplatePreview(accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        PreviewSoftSquare(accent, 12.dp)
        PreviewLine(width = 62.dp, height = 4.dp, color = accent)
        PreviewMiniHeatmap(accent, rows = 5, columns = 18)
        PreviewFooterLines(accent)
    }
}

@Composable
private fun DashboardTemplatePreview(accent: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.Top) {
                PreviewSoftSquare(accent, 14.dp)
                PreviewLine(width = 38.dp, height = 5.dp, color = accent, modifier = Modifier.padding(top = 2.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .width(19.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(accent.copy(alpha = 0.10f))
                            .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(4.dp)),
                    )
                }
            }
        }
        PreviewMiniHeatmap(accent, rows = 4, columns = 14)
        PreviewFooterLines(accent)
    }
}

@Composable
private fun PreviewSoftSquare(accent: Color, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(4.dp))
            .background(accent.copy(alpha = 0.10f))
            .border(1.dp, accent.copy(alpha = 0.20f), RoundedCornerShape(4.dp)),
    )
}

@Composable
private fun PreviewLine(width: Dp, height: Dp, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(2.dp))
            .background(color),
    )
}

@Composable
private fun PreviewMiniHeatmap(accent: Color, rows: Int, columns: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(rows) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(columns) { column ->
                    val strong = (row + column) % 3 == 0 || (row * column) % 7 == 0
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (strong) accent.copy(alpha = 0.74f) else accent.copy(alpha = 0.30f)),
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewFooterLines(accent: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        repeat(3) {
            PreviewLine(width = 24.dp, height = 3.dp, color = accent.copy(alpha = 0.16f))
        }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = entry.item.name,
                        color = colorScheme.onSurface,
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
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
private fun CheckinGoalCard(
    entry: DashboardItem,
    strings: LumaStrings,
    onOpenMakeup: () -> Unit,
    onCheckin: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val accent = themeColor(entry.item.colorTheme)
    val enabled = canCheckIn(entry.status)
    val target = entry.item.dailyTargetCount.coerceAtLeast(1)
    val count = entry.todayCount.coerceIn(0, target)
    val segmentCount = target.coerceIn(1, 5)
    val progressUnits = (count.toFloat() / target.toFloat()).coerceIn(0f, 1f) * segmentCount
    val inactiveSegment = if (isCheckinDark()) {
        colorScheme.surfaceVariant.copy(alpha = 0.48f)
    } else {
        Color(0xFFF1EFEA)
    }
    CheckinCard(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = strings.t("todayGoal"),
                color = colorScheme.onSurface,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "$count/$target",
                    color = accent,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = strings.t("completedLabel"),
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Normal,
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(segmentCount) { index ->
                val segmentProgress = (progressUnits - index).coerceIn(0f, 1f)
                CheckinGoalSegment(
                    progress = segmentProgress,
                    accent = accent,
                    inactiveColor = inactiveSegment,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (entry.item.allowMakeup) {
                CheckinGoalActionButton(
                    label = strings.t("makeupEntry"),
                    iconRes = R.drawable.ic_checkin_supplement,
                    accent = accent,
                    filled = false,
                    enabled = true,
                    onClick = onOpenMakeup,
                    modifier = Modifier.weight(1f),
                )
            }
            CheckinGoalActionButton(
                label = strings.t("checkinAction"),
                iconRes = R.drawable.ic_checkin_ok,
                accent = accent,
                filled = true,
                enabled = enabled,
                onClick = onCheckin,
                modifier = Modifier.weight(if (entry.item.allowMakeup) 1.35f else 1f),
            )
        }
    }
}

@Composable
private fun CheckinGoalSegment(
    progress: Float,
    accent: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(20.dp)
    val clampedProgress = progress.coerceIn(0f, 1f)
    val isComplete = clampedProgress >= 1f
    val iconRes = if (isComplete) R.drawable.ic_checkin_signed else R.drawable.ic_checkin_unsigned
    val iconColor = when {
        isComplete -> Color.White
        clampedProgress > 0f -> Color.White.copy(alpha = 0.92f)
        isCheckinDark() -> colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
        else -> Color.White.copy(alpha = 0.74f)
    }
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(inactiveColor),
        contentAlignment = Alignment.Center,
    ) {
        if (clampedProgress > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(clampedProgress)
                    .height(40.dp)
                    .background(accent),
            )
        }
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(iconColor),
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun CheckinGoalActionButton(
    label: String,
    @DrawableRes iconRes: Int,
    accent: Color,
    filled: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(9.dp)
    val background = when {
        filled && enabled -> accent
        filled -> colorScheme.surfaceVariant.copy(alpha = if (isCheckinDark()) 0.52f else 0.68f)
        else -> Color.Transparent
    }
    val contentColor = when {
        filled && enabled -> Color.White
        filled -> colorScheme.onSurfaceVariant
        else -> accent
    }
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(shape)
            .background(background)
            .border(
                width = 1.dp,
                color = if (filled) Color.Transparent else accent.copy(alpha = if (isCheckinDark()) 0.42f else 0.34f),
                shape = shape,
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(contentColor),
            modifier = Modifier.size(15.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = contentColor,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CheckinStatsCard(entry: DashboardItem, strings: LumaStrings) {
    val accent = themeColor(entry.item.colorTheme)
    CheckinCard(contentPadding = PaddingValues(0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CheckinStat(
                iconRes = R.drawable.ic_stat_flame,
                label = strings.t("currentStreak"),
                value = entry.stats.currentStreak.toString(),
                accent = accent,
                modifier = Modifier.weight(1f),
            )
            CheckinVerticalDivider()
            CheckinStat(
                iconRes = R.drawable.ic_stat_rise,
                label = strings.t("longestStreak"),
                value = entry.stats.longestStreak.toString(),
                accent = accent,
                modifier = Modifier.weight(1f),
            )
            CheckinVerticalDivider()
            CheckinStat(
                iconRes = R.drawable.ic_stat_progress,
                label = strings.t("completionRate"),
                value = "${(entry.stats.completionRate * 100).roundToInt()}%",
                accent = accent,
                modifier = Modifier.weight(1f),
            )
            CheckinVerticalDivider()
            CheckinStat(
                iconRes = R.drawable.ic_stat_star,
                label = strings.t("totalCheckins"),
                value = entry.stats.totalCheckins.toString(),
                accent = accent,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CheckinStat(
    @DrawableRes iconRes: Int,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(accent),
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = value,
                color = accent,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }
        Text(
            text = label,
            color = colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CheckinHeatmapCard(
    entry: DashboardItem,
    checkins: List<Checkin>,
    strings: LumaStrings,
    onUpdateNote: (String, String) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val today = remember { LocalDate.now().toString() }
    val heatmapDays = remember(entry.heatmap) { entry.heatmap }
    val checkinsByDate = remember(checkins) { checkins.groupBy { it.checkinDate } }
    val makeupDates = remember(checkinsByDate) {
        checkinsByDate.filterValues { records -> records.any { it.source == "makeup" && it.count > 0 } }.keys
    }
    val clickableDates = remember(heatmapDays) { heatmapDays.map { it.date }.toSet() }
    var selectedDate by remember(entry.item.id) { mutableStateOf<String?>(null) }
    var editingDate by remember(entry.item.id) { mutableStateOf<String?>(null) }
    val selectedDay = remember(heatmapDays, selectedDate) { heatmapDays.firstOrNull { it.date == selectedDate } }
    val editingDay = remember(heatmapDays, editingDate) { heatmapDays.firstOrNull { it.date == editingDate } }
    val highlightedDates = selectedDate?.let { setOf(today, it) } ?: setOf(today)
    val accent = themeColor(entry.item.colorTheme)

    CheckinCard(contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = strings.t("checkinHeatmap"),
                color = colorScheme.onSurface,
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            HeatmapIntensityLegend(colorTheme = entry.item.colorTheme, strings = strings)
        }
        Spacer(Modifier.height(7.dp))
        ContributionHeatmap(
            days = heatmapDays,
            colorTheme = entry.item.colorTheme,
            strings = strings,
            selectedDates = highlightedDates,
            makeupDates = makeupDates,
            clickableDates = clickableDates,
            onDayClick = { selectedDate = it.date },
            showContainer = false,
            modifier = Modifier.fillMaxWidth(),
        )
        selectedDay?.let { day ->
            Spacer(Modifier.height(7.dp))
            HeatmapDayDetail(
                day = day,
                records = checkinsByDate[day.date].orEmpty(),
                strings = strings,
                accent = accent,
                onEditNote = { editingDate = day.date },
            )
        }
    }

    editingDay?.let { day ->
        val records = checkinsByDate[day.date].orEmpty()
        CheckinNoteEditorDialog(
            day = day,
            initialNote = latestNote(records),
            strings = strings,
            accent = accent,
            onDismiss = { editingDate = null },
            onSave = { note ->
                editingDate = null
                onUpdateNote(day.date, note)
            },
        )
    }
}

@Composable
private fun HeatmapDayDetail(
    day: HeatmapDay,
    records: List<Checkin>,
    strings: LumaStrings,
    accent: Color,
    onEditNote: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val dark = isCheckinDark()
    val note = latestNote(records)
    val sourceLabel = checkinSourceLabel(records, strings) ?: strings.t("noCheckinRecords")
    val actionLabel = strings.t(if (note.isEmpty()) "addCheckinNote" else "editCheckinNote")
    val detailShape = RoundedCornerShape(9.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(detailShape)
            .background(colorScheme.surfaceVariant.copy(alpha = if (dark) 0.42f else 0.58f))
            .padding(horizontal = 9.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            text = strings.heatmapDayLabel(day),
            color = colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = sourceLabel,
            color = colorScheme.onSurfaceVariant.copy(alpha = if (dark) 0.82f else 0.76f),
            fontSize = 11.sp,
            lineHeight = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = strings.t("checkinNote"),
                    color = colorScheme.onSurfaceVariant.copy(alpha = if (dark) 0.78f else 0.72f),
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    maxLines = 1,
                )
                Text(
                    text = note.ifEmpty { strings.t("checkinNoteEmpty") },
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = actionLabel,
                color = accent,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, accent.copy(alpha = if (dark) 0.44f else 0.32f), RoundedCornerShape(8.dp))
                    .clickable(onClick = onEditNote)
                    .padding(horizontal = 9.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun CheckinNoteEditorDialog(
    day: HeatmapDay,
    initialNote: String,
    strings: LumaStrings,
    accent: Color,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var note by remember(day.date, initialNote) { mutableStateOf(initialNote) }
    val colorScheme = MaterialTheme.colorScheme
    val dark = isCheckinDark()
    val shape = RoundedCornerShape(20.dp)
    val title = strings.t(if (initialNote.isBlank()) "addCheckinNote" else "editCheckinNote")

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colorScheme.surface)
                .border(1.dp, colorScheme.outline.copy(alpha = if (dark) 0.22f else 0.14f), shape)
                .imePadding()
                .padding(18.dp),
        ) {
            Text(
                text = title,
                color = colorScheme.onSurface,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = strings.heatmapDayLabel(day),
                color = colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(CheckinNoteMaxLength) },
                label = { Text(strings.t("checkinNote")) },
                placeholder = { Text(strings.t("checkinNotePlaceholder")) },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShareTemplateDialogAction(
                    text = strings.t("cancel"),
                    color = colorScheme.onSurfaceVariant,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                ShareTemplateDialogAction(
                    text = strings.t("save"),
                    color = accent,
                    onClick = { onSave(note) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private fun checkinSourceLabel(records: List<Checkin>, strings: LumaStrings): String? {
    val countedRecords = records.filter { it.count > 0 }
    val hasMakeup = countedRecords.any { it.source == "makeup" }
    val hasNormal = countedRecords.any { it.source != "makeup" }
    return when {
        hasNormal && hasMakeup -> "${strings.t("normalCheckin")} + ${strings.t("makeupCheckin")}"
        hasMakeup -> strings.t("makeupCheckin")
        hasNormal -> strings.t("normalCheckin")
        else -> null
    }
}

@Composable
private fun HeatmapIntensityLegend(colorTheme: String, strings: LumaStrings) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isCheckinDark()
    val emptySquareColor = if (isDark) {
        colorScheme.surfaceVariant.copy(alpha = 0.86f)
    } else {
        colorScheme.surfaceVariant.copy(alpha = 0.62f)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = strings.t("heatmapLess"),
            color = colorScheme.onSurfaceVariant,
            fontSize = 9.sp,
            lineHeight = 11.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            (0..4).forEach { level ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(heatmapColor(colorTheme, level, emptySquareColor)),
                )
            }
        }
        Text(
            text = strings.t("heatmapMore"),
            color = colorScheme.onSurfaceVariant,
            fontSize = 9.sp,
            lineHeight = 11.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CheckinAchievementsCard(
    entry: DashboardItem,
    expanded: Boolean,
    strings: LumaStrings,
    onToggle: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val accent = themeColor(entry.item.colorTheme)
    val earnedBadges = remember(entry.stats) { itemBadges(entry.stats).filter { it.earned } }
    CheckinCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = if (isCheckinDark()) 0.18f else 0.10f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_achievement),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(accent),
                        modifier = Modifier.size(15.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = strings.t("earnedAchievements"),
                        color = colorScheme.onSurface,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = strings.t("achievementCount", "count" to earnedBadges.size.toString()),
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            FoldIndicator(
                expanded = expanded,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
        }
        if (expanded) {
            Spacer(Modifier.height(10.dp))
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
                        AchievementBadge(badge = badge, strings = strings, accentColor = accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckinTipCard(entry: DashboardItem, strings: LumaStrings) {
    val colorScheme = MaterialTheme.colorScheme
    val accent = themeColor(entry.item.colorTheme)
    CheckinCard(contentPadding = PaddingValues(horizontal = 11.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = if (isCheckinDark()) 0.18f else 0.09f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "⊙",
                    color = accent,
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
            .height(32.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = if (isCheckinDark()) 0.13f else 0.08f)),
    )
}

private fun iconDrawableFor(key: String): Int {
    val normalized = normalizeLumaIconKey(key)
    return lumaIconOptions.firstOrNull { it.key == normalized }?.drawableRes ?: lumaIconOptions.first().drawableRes
}

private fun latestNote(checkins: List<Checkin>): String =
    checkins
        .sortedByDescending { it.id }
        .firstNotNullOfOrNull { checkin -> checkin.note.trim().takeIf { it.isNotEmpty() } }
        .orEmpty()

@Composable
private fun isCheckinDark(): Boolean =
    MaterialTheme.colorScheme.background == Color(0xFF0C1118)
