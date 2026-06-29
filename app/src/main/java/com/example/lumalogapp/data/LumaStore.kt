package com.example.lumalogapp.data

import android.content.Context
import android.net.Uri
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

class LumaStore(private val context: Context) {
    private val dataFile = context.filesDir.resolve("lumalog-data.json")

    fun load(): LumaData {
        if (!dataFile.exists()) {
            val data = LumaData()
            save(data)
            return data
        }

        return runCatching { lumaDataFromJson(dataFile.readText()) }.getOrElse {
            LumaData()
        }
    }

    fun save(data: LumaData) {
        dataFile.writeText(data.toJsonString())
    }

    fun createItem(data: LumaData, item: Item): LumaData {
        val nextId = nextId(data.items.map { it.id })
        return data.copy(items = data.items + item.copy(id = nextId, sortOrder = data.items.size * 10))
    }

    fun updateItem(data: LumaData, item: Item): LumaData {
        return data.copy(items = data.items.map { if (it.id == item.id) item else it })
    }

    fun deleteItem(data: LumaData, itemId: Long): LumaData {
        return data.copy(
            items = data.items.filterNot { it.id == itemId },
            checkins = data.checkins.filterNot { it.itemId == itemId },
        )
    }

    fun createCategory(data: LumaData, name: String, colorTheme: String): LumaData {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return data
        val nextId = nextId(data.categories.map { it.id })
        val category = Category(
            id = nextId,
            name = trimmed,
            slug = "custom-$nextId",
            colorTheme = colorTheme,
            sortOrder = data.categories.size * 10 + 100,
        )
        return data.copy(categories = data.categories + category)
    }

    fun toggleCategoryHidden(data: LumaData, categoryId: Long): LumaData {
        return data.copy(
            categories = data.categories.map {
                if (it.id == categoryId) it.copy(isHidden = !it.isHidden) else it
            },
        )
    }

    fun deleteCategory(data: LumaData, categoryId: Long): LumaData {
        val category = data.categories.firstOrNull { it.id == categoryId } ?: return data
        if (category.isDefault || data.items.any { it.categoryId == categoryId }) return data
        return data.copy(categories = data.categories.filterNot { it.id == categoryId })
    }

    fun checkIn(data: LumaData, itemId: Long): LumaData {
        val nextId = nextId(data.checkins.map { it.id })
        val now = LocalTime.now().withSecond(0).withNano(0).toString()
        val checkin = Checkin(
            id = nextId,
            itemId = itemId,
            checkinDate = LocalDate.now().toString(),
            checkinTime = now,
        )
        return data.copy(checkins = data.checkins + checkin)
    }

    fun exportTo(uri: Uri, data: LumaData) {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            output.write(data.toJsonString().toByteArray())
        }
    }

    fun importFrom(uri: Uri): LumaData {
        val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: return LumaData()
        return lumaDataFromJson(json)
    }
}

fun buildDashboardItems(data: LumaData, dayCount: Long = 153): List<DashboardItem> {
    val categories = data.categories.associateBy { it.id }
    return data.items
        .filter { it.deletedAt.isEmpty() && it.showOnDashboard }
        .sortedWith(compareBy<Item> { it.sortOrder }.thenBy { it.id })
        .map { item ->
            val itemCheckins = data.checkins.filter { it.itemId == item.id }
            val todayCount = itemCheckins
                .filter { it.checkinDate == LocalDate.now().toString() }
                .sumOf { it.count }
            DashboardItem(
                item = item,
                category = categories[item.categoryId],
                todayCount = todayCount,
                status = checkinStatus(item, todayCount),
                stats = itemStats(item, itemCheckins),
                heatmap = buildHeatmap(item, itemCheckins, dayCount),
            )
        }
}

fun checkinStatus(item: Item, todayCount: Int): CheckinStatus {
    val today = LocalDate.now()
    val start = parseDate(item.startDate) ?: today
    val end = item.endDate.takeIf { it.isNotBlank() }?.let { parseDate(it) }

    if (today.isBefore(start)) return CheckinStatus.NotStarted
    if (!item.isUnlimited && end != null && today.isAfter(end)) return CheckinStatus.Ended

    if (item.timeMode == TimeMode.TimeRange) {
        val now = LocalTime.now()
        val startTime = parseTime(item.validStartTime) ?: LocalTime.MIN
        val endTime = parseTime(item.validEndTime) ?: LocalTime.MAX
        if (now.isBefore(startTime)) return CheckinStatus.BeforeTimeWindow
        if (now.isAfter(endTime)) return CheckinStatus.AfterTimeWindow
    }

    val completed = todayCount >= item.dailyTargetCount
    return when {
        !completed -> CheckinStatus.Available
        item.allowExtraCheckins -> CheckinStatus.CompletedCanContinue
        else -> CheckinStatus.Completed
    }
}

fun itemStats(item: Item, checkins: List<Checkin>): ItemStats {
    val byDate = checkins.groupBy { it.checkinDate }.mapValues { (_, records) -> records.sumOf { it.count } }
    val start = parseDate(item.startDate) ?: LocalDate.now()
    val end = if (item.isUnlimited || item.endDate.isBlank()) LocalDate.now() else parseDate(item.endDate) ?: LocalDate.now()
    val last = minOf(end, LocalDate.now())
    val expectedDays = if (last.isBefore(start)) 0 else ChronoUnit.DAYS.between(start, last).toInt() + 1
    val completedDays = byDate.count { (_, count) -> count >= item.dailyTargetCount }
    val completionRate = if (expectedDays == 0) 0.0 else completedDays.toDouble() / expectedDays.toDouble()
    return ItemStats(
        currentStreak = streakEndingToday(item, byDate),
        longestStreak = longestStreak(item, byDate),
        totalCheckins = checkins.sumOf { it.count },
        completedDays = completedDays,
        expectedDays = expectedDays,
        completionRate = completionRate,
    )
}

fun buildHeatmap(item: Item, checkins: List<Checkin>, dayCount: Long): List<HeatmapDay> {
    val byDate = checkins.groupBy { it.checkinDate }.mapValues { (_, records) -> records.sumOf { it.count } }
    val today = LocalDate.now()
    return (dayCount - 1 downTo 0).map { offset ->
        val date = today.minusDays(offset)
        val count = byDate[date.toString()] ?: 0
        val completed = count >= item.dailyTargetCount
        HeatmapDay(
            date = date.toString(),
            count = count,
            completed = completed,
            level = heatmapLevel(count, item.dailyTargetCount),
        )
    }
}

fun canCheckIn(status: CheckinStatus) = status == CheckinStatus.Available || status == CheckinStatus.CompletedCanContinue

private fun heatmapLevel(count: Int, target: Int): Int {
    if (count <= 0) return 0
    return ceil((count.toDouble() / target.coerceAtLeast(1).toDouble()) * 4.0).toInt().coerceIn(1, 4)
}

private fun streakEndingToday(item: Item, byDate: Map<String, Int>): Int {
    var cursor = LocalDate.now()
    var streak = 0
    while ((byDate[cursor.toString()] ?: 0) >= item.dailyTargetCount) {
        streak += 1
        cursor = cursor.minusDays(1)
    }
    return streak
}

private fun longestStreak(item: Item, byDate: Map<String, Int>): Int {
    val dates = byDate
        .filterValues { it >= item.dailyTargetCount }
        .keys
        .mapNotNull { parseDate(it) }
        .sorted()
    var longest = 0
    var current = 0
    var previous: LocalDate? = null
    dates.forEach { date ->
        current = if (previous?.plusDays(1) == date) current + 1 else 1
        longest = maxOf(longest, current)
        previous = date
    }
    return longest
}

private fun nextId(ids: List<Long>) = (ids.maxOrNull() ?: 0L) + 1L

fun parseDate(value: String) = runCatching { LocalDate.parse(value) }.getOrNull()

fun parseTime(value: String) = runCatching { LocalTime.parse(value) }.getOrNull()
