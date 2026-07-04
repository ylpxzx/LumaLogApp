package com.example.lumalogapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumalogapp.data.Category
import com.example.lumalogapp.data.DashboardMode
import com.example.lumalogapp.data.LumaData
import com.example.lumalogapp.data.buildDashboardItems
import com.example.lumalogapp.ui.components.ItemCard
import com.example.lumalogapp.ui.components.LumaLogo
import com.example.lumalogapp.ui.i18n.LumaStrings
import com.example.lumalogapp.ui.utils.themeColor

@Composable
fun DashboardScreen(
    data: LumaData,
    strings: LumaStrings,
    message: String?,
    onMessageShown: () -> Unit,
    onOpenSettings: () -> Unit,
    onCreate: () -> Unit,
    onOpenCheckin: (Long) -> Unit,
    onOpenEdit: (Long) -> Unit,
) {
    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(1800)
            onMessageShown()
        }
    }

    val dashboardItems = remember(data) { buildDashboardItems(data) }
    val visibleCategories = data.categories.filterNot { it.isHidden }.sortedBy { it.sortOrder }
    val grouped = visibleCategories.map { category ->
        category to dashboardItems.filter { it.item.categoryId == category.id }
    }.filter { it.second.isNotEmpty() }
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isLumaDashboardDark()

    Scaffold(
        floatingActionButton = { DashboardAddButton(onClick = onCreate) },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.Center,
        containerColor = Color.Transparent,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        if (isDark) {
                            listOf(
                                colorScheme.background,
                                colorScheme.surface,
                                colorScheme.surfaceVariant,
                            )
                        } else {
                            listOf(
                                Color(0xFFF9FCFF),
                                Color(0xFFF4FAFE),
                                Color(0xFFEFF9FB),
                            )
                        },
                    ),
                )
                .padding(padding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 34.dp, end = 16.dp, bottom = 108.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    DashboardHeader(strings = strings, onOpenSettings = onOpenSettings)
                }
                item {
                    DashboardIntroCard(strings = strings)
                }

                if (dashboardItems.isEmpty()) {
                    item {
                        EmptyState(strings = strings, onCreate = onCreate)
                    }
                } else if (data.preferences.dashboardMode == DashboardMode.All) {
                    items(dashboardItems, key = { it.item.id }) {
                        ItemCard(
                            entry = it,
                            preferences = data.preferences,
                            strings = strings,
                            onOpenCheckin = onOpenCheckin,
                            onOpenEdit = onOpenEdit,
                        )
                    }
                } else {
                    grouped.forEach { (category, entries) ->
                        item(key = "category-${category.id}") {
                            CategoryHeading(category = category, count = entries.size, strings = strings)
                        }
                        items(entries, key = { it.item.id }) {
                            ItemCard(
                                entry = it,
                                preferences = data.preferences,
                                strings = strings,
                                onOpenCheckin = onOpenCheckin,
                                onOpenEdit = onOpenEdit,
                            )
                        }
                    }
                }
            }

            if (message != null) {
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 92.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF18A75F))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun DashboardHeader(strings: LumaStrings, onOpenSettings: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LumaLogo(strings)
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(colorScheme.surface.copy(alpha = 0.94f))
                .border(1.dp, colorScheme.outline.copy(alpha = 0.28f), CircleShape)
                .clickable(onClick = onOpenSettings),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "\u2699",
                color = colorScheme.onSurfaceVariant,
                fontSize = 18.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun DashboardIntroCard(strings: LumaStrings) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isLumaDashboardDark()
    val accent = colorScheme.primary
    val mutedCell = if (isDark) {
        colorScheme.surfaceVariant.copy(alpha = 0.82f)
    } else {
        Color(0xFFD9E4F3)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    if (isDark) {
                        listOf(
                            colorScheme.surface.copy(alpha = 0.96f),
                            colorScheme.surfaceVariant.copy(alpha = 0.72f),
                        )
                    } else {
                        listOf(
                            Color(0xFFEFFBF7),
                            Color(0xFFF7FEFB),
                        )
                    },
                ),
            )
            .border(1.dp, colorScheme.outline.copy(alpha = if (isDark) 0.22f else 0.14f), RoundedCornerShape(22.dp))
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        IntroHeatmapMark(accent = accent, mutedCell = mutedCell, isDark = isDark)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = strings.t("brandTagline"),
                color = colorScheme.onSurface,
                fontSize = 12.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = strings.t("dashboardIntroSubtitle"),
                color = colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            IntroLegend(color = accent, text = strings.t("checked"))
            IntroLegend(color = mutedCell, text = strings.t("unchecked"))
            IntroLegend(color = colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.48f else 0.72f), text = strings.t("noData"))
        }
    }
}

@Composable
private fun IntroHeatmapMark(accent: Color, mutedCell: Color, isDark: Boolean) {
    Box(
        modifier = Modifier
            .size(width = 74.dp, height = 58.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(accent.copy(alpha = if (isDark) 0.15f else 0.08f))
            .padding(11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            repeat(4) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    repeat(4) { column ->
                        val lit = (row == 0 && column < 2) || (row == 1 && column in 1..2) || (row == 2 && column == 2) || (row == 3 && column == 0)
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (lit) accent else mutedCell),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IntroLegend(color: Color, text: String) {
    val colorScheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .width(18.dp)
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color),
        )
        Text(
            text = text,
            color = colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun DashboardAddButton(onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(74.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF41D487), Color(0xFF0DA45B), Color(0xFF078849)),
                ),
            )
            .border(4.dp, colorScheme.surface.copy(alpha = 0.92f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+",
            color = Color.White,
            fontSize = 38.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Composable
private fun EmptyState(strings: LumaStrings, onCreate: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(colorScheme.surface.copy(alpha = 0.92f))
            .border(1.dp, colorScheme.outline.copy(alpha = 0.56f), RoundedCornerShape(24.dp))
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            strings.t("empty"),
            color = colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(14.dp))
        Button(onClick = onCreate) {
            Text(strings.t("createFirst"))
        }
    }
}

@Composable
private fun CategoryHeading(category: Category, count: Int, strings: LumaStrings) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.padding(top = 4.dp, start = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(themeColor(category.colorTheme)),
        )
        Text(
            strings.categoryName(category.name),
            color = colorScheme.onBackground,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            strings.t("itemCount", "count" to count.toString()),
            color = colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Composable
private fun isLumaDashboardDark(): Boolean {
    return MaterialTheme.colorScheme.background == Color(0xFF0C1118)
}
