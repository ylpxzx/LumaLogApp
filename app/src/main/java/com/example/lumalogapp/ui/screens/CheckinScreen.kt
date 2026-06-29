package com.example.lumalogapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumalogapp.data.DashboardItem
import com.example.lumalogapp.data.LumaData
import com.example.lumalogapp.data.buildDashboardItems
import com.example.lumalogapp.data.canCheckIn
import com.example.lumalogapp.ui.components.ContributionHeatmap
import com.example.lumalogapp.ui.i18n.LumaStrings
import com.example.lumalogapp.ui.utils.timeHint

@Composable
fun CheckinScreen(
    data: LumaData,
    itemId: Long,
    strings: LumaStrings,
    onBack: () -> Unit,
    onCheckin: () -> Unit,
) {
    val entry = remember(data, itemId) { buildDashboardItems(data).firstOrNull { it.item.id == itemId } }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = onBack) { Text(strings.t("backHome")) }
            }

            if (entry == null) {
                Spacer(Modifier.height(80.dp))
                Text(strings.t("itemMissing"), color = MaterialTheme.colorScheme.error)
                return@Column
            }

            Spacer(Modifier.height(72.dp))
            Text(
                strings.categoryName(entry.category?.name ?: ""),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                entry.item.name,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                lineHeight = 42.sp,
            )
            Spacer(Modifier.height(36.dp))
            CheckinCircle(entry = entry, strings = strings, onCheckin = onCheckin)
            Spacer(Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                MetaChip(strings.statusText(entry.status))
                MetaChip(timeHint(entry.item, strings))
                MetaChip(strings.t("streakDays", "count" to entry.stats.currentStreak.toString()))
            }
            Spacer(Modifier.height(28.dp))
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
                    showDayDetails = true,
                    modifier = Modifier.padding(14.dp),
                )
            }
        }
    }
}

@Composable
private fun CheckinCircle(entry: DashboardItem, strings: LumaStrings, onCheckin: () -> Unit) {
    val enabled = canCheckIn(entry.status)
    Button(
        onClick = onCheckin,
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = Modifier.size(260.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                "${entry.todayCount.coerceAtMost(entry.item.dailyTargetCount)}/${entry.item.dailyTargetCount}",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.height(10.dp))
            Text(strings.statusText(entry.status), fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            Text(strings.statusHint(entry.status), fontSize = 13.sp)
        }
    }
}

@Composable
private fun MetaChip(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .padding(horizontal = 9.dp, vertical = 6.dp),
    )
}
