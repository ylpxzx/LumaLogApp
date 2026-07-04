package com.example.lumalogapp.data

import android.content.Context
import android.net.Uri
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.LocalTime
import java.time.YearMonth
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

    fun archiveItem(data: LumaData, itemId: Long): LumaData {
        val archivedAt = OffsetDateTime.now().toString()
        return data.copy(
            items = data.items.map {
                if (it.id == itemId) it.copy(archivedAt = archivedAt, showOnDashboard = false) else it
            },
        )
    }

    fun restoreItem(data: LumaData, itemId: Long): LumaData {
        return data.copy(
            items = data.items.map {
                if (it.id == itemId) it.copy(archivedAt = "", showOnDashboard = true) else it
            },
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

    fun checkIn(data: LumaData, itemId: Long, note: String = ""): LumaData {
        val nextId = nextId(data.checkins.map { it.id })
        val today = LocalDate.now().toString()
        val now = LocalTime.now().withSecond(0).withNano(0).toString()
        val createdAt = OffsetDateTime.now().toString()
        val trimmedNote = note.trim()
        val checkin = Checkin(
            id = nextId,
            itemId = itemId,
            checkinDate = today,
            checkinTime = now,
            note = trimmedNote,
            createdAt = createdAt,
        )
        return data.copy(
            checkins = data.checkins.map {
                if (it.itemId == itemId && it.checkinDate == today) it.copy(note = trimmedNote) else it
            } + checkin,
        )
    }

    fun makeupCheckins(data: LumaData, itemId: Long, dates: List<String>): LumaData {
        val distinctDates = dates.distinct().sorted()
        if (distinctDates.isEmpty()) return data
        val now = LocalTime.now().withSecond(0).withNano(0).toString()
        val createdAt = OffsetDateTime.now().toString()
        var nextId = nextId(data.checkins.map { it.id })
        val makeups = distinctDates.map { date ->
            Checkin(
                id = nextId++,
                itemId = itemId,
                checkinDate = date,
                checkinTime = now,
                source = "makeup",
                createdAt = createdAt,
            )
        }
        return data.copy(checkins = data.checkins + makeups)
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

fun buildDashboardItems(data: LumaData, dayCount: Long = defaultHeatmapDayCount()): List<DashboardItem> {
    val categories = data.categories.associateBy { it.id }
    return data.items
        .filter { it.deletedAt.isEmpty() && it.archivedAt.isEmpty() && it.showOnDashboard }
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

private fun defaultHeatmapDayCount(): Long =
    154L + LocalDate.now().dayOfWeek.value.toLong()

fun makeupUsedThisMonth(data: LumaData, itemId: Long): Int {
    val currentMonth = YearMonth.now()
    return data.checkins.count { checkin ->
        checkin.itemId == itemId &&
            checkin.source == "makeup" &&
            yearMonthOf(checkin.createdAt, checkin.checkinDate) == currentMonth
    }
}

fun makeupCandidateDates(data: LumaData, item: Item): List<String> {
    if (!item.allowMakeup || item.archivedAt.isNotBlank()) return emptyList()
    val today = LocalDate.now()
    val monthStart = today.withDayOfMonth(1)
    val itemStart = parseDate(item.startDate) ?: today
    val start = if (monthStart.isAfter(itemStart)) monthStart else itemStart
    val yesterday = today.minusDays(1)
    val endLimit = if (item.isUnlimited || item.endDate.isBlank()) {
        yesterday
    } else {
        val itemEnd = parseDate(item.endDate) ?: yesterday
        if (itemEnd.isBefore(yesterday)) itemEnd else yesterday
    }
    if (endLimit.isBefore(start)) return emptyList()

    val itemCheckins = data.checkins.filter { it.itemId == item.id }
    val byDate = itemCheckins.groupBy { it.checkinDate }.mapValues { (_, records) -> records.sumOf { it.count } }
    val makeupDates = itemCheckins.filter { it.source == "makeup" }.map { it.checkinDate }.toSet()
    val days = ChronoUnit.DAYS.between(start, endLimit).toInt()
    return (0..days).map { start.plusDays(it.toLong()).toString() }
        .filter { date -> date !in makeupDates && (byDate[date] ?: 0) < item.dailyTargetCount }
}

fun itemBadges(stats: ItemStats): List<Badge> = listOf(
    Badge("first_light", "初次点亮", "完成第一次签到", "bronze", stats.totalCheckins >= 1),
    Badge("week_streak", "七日连光", "最长连续签到达到 7 天", "silver", stats.longestStreak >= 7),
    Badge("month_streak", "三十日微光", "最长连续签到达到 30 天", "gold", stats.longestStreak >= 30),
    Badge("hundred_lights", "百次记录", "累计签到达到 100 次", "gold", stats.totalCheckins >= 100),
    Badge("steady_flow", "稳定节奏", "完成率达到 80%", "silver", stats.expectedDays >= 7 && stats.completionRate >= 0.8),
)

fun userBadges(data: LumaData): List<Badge> {
    val activeItems = data.items.filter { it.deletedAt.isEmpty() && it.archivedAt.isEmpty() }
    val stats = activeItems.map { item -> itemStats(item, data.checkins.filter { it.itemId == item.id }) }
    val totalCheckins = stats.sumOf { it.totalCheckins }
    val maxLongestStreak = stats.maxOfOrNull { it.longestStreak } ?: 0
    val completedHabits = stats.count { it.completedDays > 0 }
    return listOf(
        Badge("first_habit_light", "第一束光", "任意习惯完成第一次签到", "bronze", totalCheckins >= 1),
        Badge("seven_day_runner", "七日同行", "任意习惯最长连续达到 7 天", "silver", maxLongestStreak >= 7),
        Badge("thirty_day_runner", "一月成线", "任意习惯最长连续达到 30 天", "gold", maxLongestStreak >= 30),
        Badge("three_habits_lit", "三线并进", "至少 3 个习惯有完成记录", "gold", completedHabits >= 3),
        Badge("hundred_total_lights", "百次点亮", "所有习惯累计签到达到 100 次", "gold", totalCheckins >= 100),
    )
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
        currentStreak = streakEndingOn(last, byDate),
        longestStreak = longestStreak(byDate),
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

private fun streakEndingOn(anchorDate: LocalDate, byDate: Map<String, Int>): Int {
    var cursor = anchorDate
    var streak = 0
    while ((byDate[cursor.toString()] ?: 0) > 0) {
        streak += 1
        cursor = cursor.minusDays(1)
    }
    return streak
}

private fun longestStreak(byDate: Map<String, Int>): Int {
    val dates = byDate
        .filterValues { it > 0 }
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

private fun yearMonthOf(createdAt: String, fallbackDate: String): YearMonth? {
    val fromCreated = runCatching { YearMonth.from(OffsetDateTime.parse(createdAt)) }.getOrNull()
    if (fromCreated != null) return fromCreated
    return parseDate(fallbackDate)?.let { YearMonth.of(it.year, it.month) }
}

private fun nextId(ids: List<Long>) = (ids.maxOrNull() ?: 0L) + 1L

fun parseDate(value: String) = runCatching { LocalDate.parse(value) }.getOrNull()

fun parseTime(value: String) = runCatching { LocalTime.parse(value) }.getOrNull()
