package com.example.lumalogapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumalogapp.data.Category
import com.example.lumalogapp.data.DashboardMode
import com.example.lumalogapp.data.LumaData
import com.example.lumalogapp.data.buildDashboardItems
import com.example.lumalogapp.ui.components.ItemCard
import com.example.lumalogapp.ui.components.LumaLogo
import com.example.lumalogapp.ui.components.RoundIconButton
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

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreate,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.size(60.dp),
            ) {
                Text("+", fontSize = 30.sp, fontWeight = FontWeight.Light)
            }
        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.Center,
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp, 20.dp, 18.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LumaLogo()
                            RoundIconButton(onClick = onOpenSettings) {
                                Text("⚙", fontSize = 18.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)),
                        )
                    }
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
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 92.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                )
            }
        }
    }
}

@Composable
private fun EmptyState(strings: LumaStrings, onCreate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(strings.t("empty"), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onCreate) {
            Text(strings.t("createFirst"))
        }
    }
}

@Composable
private fun CategoryHeading(category: Category, count: Int, strings: LumaStrings) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(themeColor(category.colorTheme)),
        )
        Text(
            strings.categoryName(category.name),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
        Text(
            strings.t("itemCount", "count" to count.toString()),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
        )
    }
}
