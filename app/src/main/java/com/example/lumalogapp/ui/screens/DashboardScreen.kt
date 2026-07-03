package com.example.lumalogapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumalogapp.R
import com.example.lumalogapp.data.Category
import com.example.lumalogapp.data.DashboardMode
import com.example.lumalogapp.data.LanguagePreference
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
                    DashboardHeader(onOpenSettings = onOpenSettings)
                }
                item {
                    DashboardIntroImage(language = data.preferences.language)
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
private fun DashboardHeader(onOpenSettings: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LumaLogo()
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(colorScheme.surface.copy(alpha = 0.94f))
                .border(1.dp, colorScheme.outline.copy(alpha = 0.56f), CircleShape)
                .clickable(onClick = onOpenSettings),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "⚙",
                color = colorScheme.onSurfaceVariant,
                fontSize = 22.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun DashboardIntroImage(language: LanguagePreference) {
    val imageRes = if (language == LanguagePreference.En) {
        R.drawable.dashboard_intro_en
    } else {
        R.drawable.dashboard_intro_zh
    }
    Image(
        painter = painterResource(imageRes),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4.5f),
        contentScale = ContentScale.FillBounds,
    )
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
