package com.example.lumalogapp.ui.i18n

import com.example.lumalogapp.data.CheckinStatus
import com.example.lumalogapp.data.HeatmapDay
import com.example.lumalogapp.data.LanguagePreference
import java.time.LocalDate

class LumaStrings(private val language: LanguagePreference) {
    fun t(key: String, vararg params: Pair<String, String>): String {
        val template = (if (language == LanguagePreference.En) en[key] else zh[key]) ?: zh[key] ?: key
        return params.fold(template) { text, (name, value) -> text.replace("{$name}", value) }
    }

    fun categoryName(name: String): String {
        if (language != LanguagePreference.En) return name
        return categoryNames[name] ?: name
    }

    fun monthLabel(date: LocalDate): String {
        val month = date.monthValue
        val year = date.year % 100
        if (language == LanguagePreference.En) {
            val label = englishMonths.getOrElse(month - 1) { month.toString() }
            return if (month == 1) "$label '$year" else label
        }
        return if (month == 1) "${year}\u5e74${month}\u6708" else "$month\u6708"
    }

    fun heatmapDayLabel(day: HeatmapDay): String {
        val date = runCatching { LocalDate.parse(day.date) }.getOrNull()
        val dateText = if (date == null) {
            day.date
        } else if (language == LanguagePreference.En) {
            "${englishMonths.getOrElse(date.monthValue - 1) { date.monthValue.toString() }} ${date.dayOfMonth}, ${date.year}"
        } else {
            "${date.year}\u5e74${date.monthValue}\u6708${date.dayOfMonth}\u65e5"
        }
        val countText = if (language == LanguagePreference.En) {
            "${day.count} time(s)"
        } else {
            "${day.count} \u6b21"
        }
        val completedText = if (!day.completed) {
            ""
        } else if (language == LanguagePreference.En) {
            " \u00b7 completed"
        } else {
            " \u00b7 \u5df2\u5b8c\u6210"
        }
        return "$dateText \u00b7 $countText$completedText"
    }

    fun colorName(theme: String): String = t("color_$theme")

    fun statusText(status: CheckinStatus): String = when (status) {
        CheckinStatus.Available -> t("statusAvailable")
        CheckinStatus.NotStarted -> t("statusNotStarted")
        CheckinStatus.Ended -> t("statusEnded")
        CheckinStatus.BeforeTimeWindow -> t("statusBefore")
        CheckinStatus.AfterTimeWindow -> t("statusAfter")
        CheckinStatus.Completed -> t("statusCompleted")
        CheckinStatus.CompletedCanContinue -> t("statusContinue")
    }

    fun statusHint(status: CheckinStatus): String = when (status) {
        CheckinStatus.Available -> t("hintAvailable")
        CheckinStatus.NotStarted -> t("hintNotStarted")
        CheckinStatus.Ended -> t("hintEnded")
        CheckinStatus.BeforeTimeWindow -> t("hintBefore")
        CheckinStatus.AfterTimeWindow -> t("hintAfter")
        CheckinStatus.Completed -> t("hintCompleted")
        CheckinStatus.CompletedCanContinue -> t("hintContinue")
    }
}

private val categoryNames = mapOf(
    "戒断" to "Quit",
    "健康" to "Health",
    "健身" to "Fitness",
    "学习" to "Study",
    "阅读" to "Reading",
    "工作" to "Work",
    "创作" to "Creative",
    "生活" to "Life",
)

private val englishMonths = listOf(
    "Jan",
    "Feb",
    "Mar",
    "Apr",
    "May",
    "Jun",
    "Jul",
    "Aug",
    "Sep",
    "Oct",
    "Nov",
    "Dec",
)

private val zh = mapOf(
    "settings" to "设置",
    "settingsSub" to "配置主题、语言、首页显示和数据迁移。",
    "backHome" to "返回首页",
    "empty" to "还没有 habit。",
    "createFirst" to "创建第一个",
    "itemCount" to "{count} 个 habit",
    "uncategorized" to "未分类",
    "currentStreak" to "连续天数",
    "longestStreak" to "最长连续",
    "completionRate" to "完成率",
    "totalCheckins" to "总次数",
    "newItem" to "新建 habit",
    "editItem" to "编辑 habit",
    "newItemSub" to "给一个长期目标一张属于它的热力图。",
    "editItemSub" to "调整分类、时间、颜色和签到规则。",
    "itemName" to "habit 名称",
    "description" to "描述",
    "category" to "分类",
    "color" to "颜色",
    "startDate" to "开始日期",
    "endDate" to "结束日期",
    "unlimited" to "不限结束日期",
    "dailyTarget" to "每日目标次数",
    "validTime" to "有效时间",
    "allDay" to "全天",
    "timeRange" to "指定时间段",
    "startTime" to "开始时间",
    "endTime" to "结束时间",
    "allowExtra" to "达标后允许继续签到",
    "showOnDashboard" to "显示在首页",
    "createItem" to "创建 habit",
    "save" to "保存修改",
    "deleteItem" to "删除 habit",
    "deleteTitle" to "删除这个 habit？",
    "deleteMessage" to "删除后，该 habit 的所有签到记录也会被删除，无法恢复。",
    "delete" to "删除",
    "cancel" to "取消",
    "display" to "显示",
    "language" to "语言",
    "theme" to "主题",
    "system" to "系统",
    "light" to "亮色",
    "dark" to "暗色",
    "dashboardMode" to "首页默认模式",
    "all" to "全部",
    "dashboardDisplay" to "Dashboard 显示项",
    "todayStatus" to "今日状态",
    "categories" to "分类",
    "newCategory" to "新分类名称",
    "addCategory" to "新增分类",
    "show" to "显示",
    "hide" to "隐藏",
    "data" to "数据",
    "export" to "导出",
    "import" to "导入",
    "exported" to "已导出",
    "imported" to "已导入",
    "checked" to "已点亮",
    "itemMissing" to "habit 不存在",
    "allDayCheckin" to "全天可签到",
    "streakDays" to "连续 {count} 天",
    "statusAvailable" to "今天可签到",
    "statusNotStarted" to "尚未开始",
    "statusEnded" to "目标已结束",
    "statusBefore" to "还不到时间",
    "statusAfter" to "今日已结束",
    "statusCompleted" to "今日已点亮",
    "statusContinue" to "已完成，可继续",
    "hintAvailable" to "点击点亮今天",
    "hintNotStarted" to "开始日期还没到",
    "hintEnded" to "这个目标已经结束",
    "hintBefore" to "稍后再回来点亮",
    "hintAfter" to "明天继续",
    "hintCompleted" to "今天已经完成",
    "hintContinue" to "已达标，还能继续记录",
    "color_green" to "绿色",
    "color_blue" to "蓝色",
    "color_purple" to "紫色",
    "color_orange" to "橙色",
    "color_red" to "红色",
    "color_teal" to "青色",
    "color_pink" to "粉色",
    "color_gray" to "灰色",
)

private val en = mapOf(
    "settings" to "Settings",
    "settingsSub" to "Theme, language, dashboard, and data migration.",
    "backHome" to "Home",
    "empty" to "No habits yet.",
    "createFirst" to "Create first",
    "itemCount" to "{count} habits",
    "uncategorized" to "Uncategorized",
    "currentStreak" to "Current streak",
    "longestStreak" to "Longest streak",
    "completionRate" to "Completion rate",
    "totalCheckins" to "Total check-ins",
    "newItem" to "New habit",
    "editItem" to "Edit habit",
    "newItemSub" to "Give a long-term goal its own heatmap.",
    "editItemSub" to "Adjust category, time, color, and check-in rules.",
    "itemName" to "Habit name",
    "description" to "Description",
    "category" to "Category",
    "color" to "Color",
    "startDate" to "Start date",
    "endDate" to "End date",
    "unlimited" to "No end date",
    "dailyTarget" to "Daily target",
    "validTime" to "Valid time",
    "allDay" to "All day",
    "timeRange" to "Time range",
    "startTime" to "Start time",
    "endTime" to "End time",
    "allowExtra" to "Allow extra check-ins after target",
    "showOnDashboard" to "Show on dashboard",
    "createItem" to "Create habit",
    "save" to "Save changes",
    "deleteItem" to "Delete habit",
    "deleteTitle" to "Delete this habit?",
    "deleteMessage" to "All check-in records for this habit will be deleted.",
    "delete" to "Delete",
    "cancel" to "Cancel",
    "display" to "Display",
    "language" to "Language",
    "theme" to "Theme",
    "system" to "System",
    "light" to "Light",
    "dark" to "Dark",
    "dashboardMode" to "Default dashboard view",
    "all" to "All",
    "category" to "Categories",
    "dashboardDisplay" to "Dashboard Display",
    "todayStatus" to "Today status",
    "categories" to "Categories",
    "newCategory" to "New category name",
    "addCategory" to "Add category",
    "show" to "Show",
    "hide" to "Hide",
    "data" to "Data",
    "export" to "Export",
    "import" to "Import",
    "exported" to "Exported",
    "imported" to "Imported",
    "checked" to "Lit up",
    "itemMissing" to "Habit not found",
    "allDayCheckin" to "Available all day",
    "streakDays" to "{count}-day streak",
    "statusAvailable" to "Available today",
    "statusNotStarted" to "Not started",
    "statusEnded" to "Goal ended",
    "statusBefore" to "Not time yet",
    "statusAfter" to "Closed today",
    "statusCompleted" to "Completed today",
    "statusContinue" to "Done, can continue",
    "hintAvailable" to "Click to light up today",
    "hintNotStarted" to "Start date has not arrived",
    "hintEnded" to "This goal has ended",
    "hintBefore" to "Come back later",
    "hintAfter" to "Continue tomorrow",
    "hintCompleted" to "Today is complete",
    "hintContinue" to "Target met, keep recording",
    "color_green" to "Green",
    "color_blue" to "Blue",
    "color_purple" to "Purple",
    "color_orange" to "Orange",
    "color_red" to "Red",
    "color_teal" to "Teal",
    "color_pink" to "Pink",
    "color_gray" to "Gray",
)
