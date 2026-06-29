package com.example.lumalogapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lumalogapp.data.HeatmapDay
import com.example.lumalogapp.data.LumaData
import com.example.lumalogapp.data.buildHeatmap
import com.example.lumalogapp.data.makeupCandidateDates
import com.example.lumalogapp.data.makeupUsedThisMonth
import com.example.lumalogapp.ui.components.ContributionHeatmap
import com.example.lumalogapp.ui.i18n.LumaStrings
import kotlin.math.max

@Composable
fun MakeupScreen(
    data: LumaData,
    itemId: Long,
    strings: LumaStrings,
    onBack: () -> Unit,
    onConfirm: (List<String>) -> Unit,
) {
    val item = remember(data, itemId) { data.items.firstOrNull { it.id == itemId } }
    var selectedDates by remember(itemId) { mutableStateOf(emptySet<String>()) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(18.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = onBack) { Text(strings.t("backCheckin")) }
                Button(
                    onClick = { onConfirm(selectedDates.sorted()) },
                    enabled = selectedDates.isNotEmpty(),
                ) {
                    Text(strings.t("confirmMakeup"))
                }
            }

            if (item == null) {
                Spacer(Modifier.height(80.dp))
                Text(strings.t("itemMissing"), color = MaterialTheme.colorScheme.error)
                return@Column
            }

            val itemCheckins = remember(data, itemId) { data.checkins.filter { it.itemId == itemId } }
            val candidates = remember(data, item) { makeupCandidateDates(data, item) }
            val usedThisMonth = remember(data, itemId) { makeupUsedThisMonth(data, itemId) }
            val remainingSlots = if (item.makeupMonthlyLimit <= 0) {
                Int.MAX_VALUE
            } else {
                max(0, item.makeupMonthlyLimit - usedThisMonth - selectedDates.size)
            }
            val clickableDates = if (!item.allowMakeup) {
                emptySet()
            } else if (item.makeupMonthlyLimit <= 0 || remainingSlots > 0) {
                candidates.toSet() + selectedDates
            } else {
                selectedDates
            }
            val selectedDateSet = selectedDates
            val heatmap = remember(item, itemCheckins, selectedDateSet) {
                buildHeatmap(item, itemCheckins, 153).map { day ->
                    if (selectedDateSet.contains(day.date)) {
                        HeatmapDay(
                            date = day.date,
                            count = item.dailyTargetCount,
                            completed = true,
                            level = 4,
                        )
                    } else {
                        day
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(item.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text(
                if (item.makeupMonthlyLimit <= 0) strings.t("makeupUnlimited")
                else strings.t("makeupRemaining", "count" to remainingSlots.toString()),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(18.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                ContributionHeatmap(
                    days = heatmap,
                    colorTheme = item.colorTheme,
                    strings = strings,
                    clickableDates = clickableDates,
                    selectedDates = selectedDates,
                    onDayClick = { day ->
                        selectedDates = if (selectedDates.contains(day.date)) {
                            selectedDates - day.date
                        } else {
                            selectedDates + day.date
                        }
                    },
                    modifier = Modifier.padding(14.dp),
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                strings.t("makeupSelected", "count" to selectedDates.size.toString()),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
