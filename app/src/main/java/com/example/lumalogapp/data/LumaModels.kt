package com.example.lumalogapp.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

enum class ThemePreference(val value: String) {
    System("system"),
    Light("light"),
    Dark("dark");

    companion object {
        fun from(value: String?) = entries.firstOrNull { it.value == value } ?: System
    }
}

enum class LanguagePreference(val value: String) {
    Zh("zh"),
    En("en");

    companion object {
        fun from(value: String?) = entries.firstOrNull { it.value == value } ?: Zh
    }
}

enum class DashboardMode(val value: String) {
    All("all"),
    Category("category");

    companion object {
        fun from(value: String?) = entries.firstOrNull { it.value == value } ?: All
    }
}

enum class TimeMode(val value: String) {
    AllDay("all_day"),
    TimeRange("time_range");

    companion object {
        fun from(value: String?) = entries.firstOrNull { it.value == value } ?: AllDay
    }
}

data class AppPreferences(
    val theme: ThemePreference = ThemePreference.System,
    val language: LanguagePreference = LanguagePreference.Zh,
    val dashboardMode: DashboardMode = DashboardMode.All,
    val showTodayStatus: Boolean = false,
    val showCurrentStreak: Boolean = false,
    val showLongestStreak: Boolean = false,
    val showCompletionRate: Boolean = false,
    val showTotalCheckins: Boolean = false,
)

data class Category(
    val id: Long,
    val name: String,
    val slug: String,
    val colorTheme: String,
    val sortOrder: Int,
    val isDefault: Boolean = false,
    val isHidden: Boolean = false,
)

data class Item(
    val id: Long,
    val categoryId: Long,
    val name: String,
    val description: String = "",
    val colorTheme: String = "green",
    val iconKey: String = "briefcase",
    val startDate: String = LocalDate.now().toString(),
    val endDate: String = "",
    val isUnlimited: Boolean = true,
    val dailyTargetCount: Int = 1,
    val timeMode: TimeMode = TimeMode.AllDay,
    val validStartTime: String = "09:00",
    val validEndTime: String = "23:59",
    val allowMakeup: Boolean = false,
    val makeupMonthlyLimit: Int = 3,
    val allowExtraCheckins: Boolean = false,
    val showOnDashboard: Boolean = true,
    val sortOrder: Int = 0,
    val archivedAt: String = "",
    val deletedAt: String = "",
)

data class Checkin(
    val id: Long,
    val itemId: Long,
    val checkinDate: String,
    val checkinTime: String,
    val count: Int = 1,
    val note: String = "",
    val source: String = "normal",
    val createdAt: String = java.time.OffsetDateTime.now().toString(),
)

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val level: String,
    val earned: Boolean,
)

data class LumaData(
    val preferences: AppPreferences = AppPreferences(),
    val categories: List<Category> = defaultCategories(),
    val items: List<Item> = emptyList(),
    val checkins: List<Checkin> = emptyList(),
)

data class ItemStats(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCheckins: Int,
    val completedDays: Int,
    val expectedDays: Int,
    val completionRate: Double,
)

data class DashboardItem(
    val item: Item,
    val category: Category?,
    val todayCount: Int,
    val status: CheckinStatus,
    val stats: ItemStats,
    val heatmap: List<HeatmapDay>,
)

data class HeatmapDay(
    val date: String,
    val count: Int,
    val completed: Boolean,
    val level: Int,
)

enum class CheckinStatus {
    Available,
    NotStarted,
    Ended,
    BeforeTimeWindow,
    AfterTimeWindow,
    Completed,
    CompletedCanContinue,
}

fun defaultCategories() = listOf(
    Category(1, "戒断", "quit", "red", 10, isDefault = true),
    Category(2, "健康", "health", "green", 20, isDefault = true),
    Category(3, "健身", "fitness", "orange", 30, isDefault = true),
    Category(4, "学习", "study", "blue", 40, isDefault = true),
    Category(5, "阅读", "reading", "teal", 50, isDefault = true),
    Category(6, "工作", "work", "gray", 60, isDefault = true),
    Category(7, "创作", "creative", "purple", 70, isDefault = true),
    Category(8, "生活", "life", "pink", 80, isDefault = true),
)

fun LumaData.toJsonString(): String {
    val root = JSONObject()
    root.put("app", "LumaLog")
    root.put("version", 1)
    root.put("exported_at", java.time.OffsetDateTime.now().toString())
    root.put("preferences", preferences.toJson())
    root.put("categories", JSONArray(categories.map { it.toJson() }))
    root.put("items", JSONArray(items.map { it.toJson() }))
    root.put("checkins", JSONArray(checkins.map { it.toJson() }))
    return root.toString(2)
}

fun lumaDataFromJson(json: String): LumaData {
    val root = JSONObject(json)
    return LumaData(
        preferences = root.optJSONObject("preferences")?.toPreferences() ?: AppPreferences(),
        categories = root.optJSONArray("categories").toList { it.toCategory() }.ifEmpty { defaultCategories() },
        items = root.optJSONArray("items").toList { it.toItem() },
        checkins = root.optJSONArray("checkins").toList { it.toCheckin() },
    )
}

private fun AppPreferences.toJson() = JSONObject()
    .put("theme", theme.value)
    .put("language", language.value)
    .put("dashboard_mode", dashboardMode.value)
    .put("show_today_status", showTodayStatus)
    .put("show_current_streak", showCurrentStreak)
    .put("show_longest_streak", showLongestStreak)
    .put("show_completion_rate", showCompletionRate)
    .put("show_total_checkins", showTotalCheckins)

private fun JSONObject.toPreferences() = AppPreferences(
    theme = ThemePreference.from(optString("theme")),
    language = LanguagePreference.from(optString("language")),
    dashboardMode = DashboardMode.from(optString("dashboard_mode")),
    showTodayStatus = optBoolean("show_today_status", false),
    showCurrentStreak = optBoolean("show_current_streak", false),
    showLongestStreak = optBoolean("show_longest_streak", false),
    showCompletionRate = optBoolean("show_completion_rate", false),
    showTotalCheckins = optBoolean("show_total_checkins", false),
)

private fun Category.toJson() = JSONObject()
    .put("id", id)
    .put("name", name)
    .put("slug", slug)
    .put("color_theme", colorTheme)
    .put("sort_order", sortOrder)
    .put("is_default", isDefault)
    .put("is_hidden", isHidden)

private fun JSONObject.toCategory() = Category(
    id = optLong("id"),
    name = optString("name"),
    slug = optString("slug"),
    colorTheme = optString("color_theme", "green"),
    sortOrder = optInt("sort_order", 0),
    isDefault = optBoolean("is_default", false),
    isHidden = optBoolean("is_hidden", false),
)

private fun Item.toJson() = JSONObject()
    .put("id", id)
    .put("category_id", categoryId)
    .put("name", name)
    .put("description", description)
    .put("color_theme", colorTheme)
    .put("icon_key", iconKey)
    .put("start_date", startDate)
    .put("end_date", endDate)
    .put("is_unlimited", isUnlimited)
    .put("daily_target_count", dailyTargetCount)
    .put("time_mode", timeMode.value)
    .put("valid_start_time", validStartTime)
    .put("valid_end_time", validEndTime)
    .put("allow_makeup", allowMakeup)
    .put("makeup_monthly_limit", makeupMonthlyLimit)
    .put("allow_extra_checkins", allowExtraCheckins)
    .put("show_on_dashboard", showOnDashboard)
    .put("sort_order", sortOrder)
    .put("archived_at", archivedAt)
    .put("deleted_at", deletedAt)

private fun JSONObject.toItem() = Item(
    id = optLong("id"),
    categoryId = optLong("category_id"),
    name = optString("name"),
    description = optString("description"),
    colorTheme = optString("color_theme", "green"),
    iconKey = optString("icon_key", "briefcase"),
    startDate = optString("start_date", LocalDate.now().toString()),
    endDate = optString("end_date"),
    isUnlimited = optBoolean("is_unlimited", true),
    dailyTargetCount = optInt("daily_target_count", 1).coerceAtLeast(1),
    timeMode = TimeMode.from(optString("time_mode")),
    validStartTime = optString("valid_start_time", "09:00"),
    validEndTime = optString("valid_end_time", "23:59"),
    allowMakeup = optBoolean("allow_makeup", false),
    makeupMonthlyLimit = optInt("makeup_monthly_limit", 3).coerceAtLeast(0),
    allowExtraCheckins = optBoolean("allow_extra_checkins", false),
    showOnDashboard = optBoolean("show_on_dashboard", true),
    sortOrder = optInt("sort_order", 0),
    archivedAt = optString("archived_at"),
    deletedAt = optString("deleted_at"),
)

private fun Checkin.toJson() = JSONObject()
    .put("id", id)
    .put("item_id", itemId)
    .put("checkin_date", checkinDate)
    .put("checkin_time", checkinTime)
    .put("count", count)
    .put("note", note)
    .put("source", source)
    .put("created_at", createdAt)

private fun JSONObject.toCheckin() = Checkin(
    id = optLong("id"),
    itemId = optLong("item_id"),
    checkinDate = optString("checkin_date"),
    checkinTime = optString("checkin_time"),
    count = optInt("count", 1).coerceAtLeast(1),
    note = optString("note"),
    source = optString("source", "normal"),
    createdAt = optString("created_at").ifBlank {
        "${optString("checkin_date")}T${optString("checkin_time", "00:00")}"
    },
)

private fun <T> JSONArray?.toList(mapper: (JSONObject) -> T): List<T> {
    if (this == null) return emptyList()
    return List(length()) { index -> mapper(getJSONObject(index)) }
}
