package com.example.lumalogapp.ui.i18n

import com.example.lumalogapp.data.Badge
import com.example.lumalogapp.data.CheckinStatus
import com.example.lumalogapp.data.HeatmapDay
import com.example.lumalogapp.data.LanguagePreference
import com.example.lumalogapp.ui.share.ShareTemplate
import java.time.LocalDate
import java.time.YearMonth

class LumaStrings(private val language: LanguagePreference) {
    fun t(key: String, vararg params: Pair<String, String>): String {
        val template = (if (language == LanguagePreference.En) en[key] else zh[key]) ?: zh[key] ?: key
        return params.fold(template) { text, (name, value) -> text.replace("{$name}", value) }
    }

    fun categoryName(name: String): String {
        if (language != LanguagePreference.En) return name
        return categoryNames[name] ?: name
    }

    fun badgeTitle(badge: Badge): String = t("badge_${badge.id}_title").takeUnless { it == "badge_${badge.id}_title" } ?: badge.title

    fun shareTemplateName(template: ShareTemplate): String = t("shareTemplate${template.number}")

    fun monthLabel(date: LocalDate): String {
        val month = date.monthValue
        val year = date.year % 100
        if (language == LanguagePreference.En) {
            val label = englishMonths.getOrElse(month - 1) { month.toString() }
            return if (month == 1) "$label '$year" else label
        }
        return if (month == 1) "${year}年${month}月" else "${month}月"
    }

    fun fullMonthLabel(month: YearMonth): String {
        if (language == LanguagePreference.En) {
            val label = englishMonths.getOrElse(month.monthValue - 1) { month.monthValue.toString() }
            return "$label ${month.year}"
        }
        return "${month.year}年${month.monthValue}月"
    }

    fun weekdayLabels(): List<String> =
        if (language == LanguagePreference.En) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        } else {
            listOf("日", "一", "二", "三", "四", "五", "六")
        }

    fun compactDateLabel(date: LocalDate): String =
        if (language == LanguagePreference.En) {
            "${englishMonths.getOrElse(date.monthValue - 1) { date.monthValue.toString() }} ${date.dayOfMonth}"
        } else {
            "${date.monthValue}月${date.dayOfMonth}日"
        }

    fun compactDateList(dates: List<LocalDate>): String =
        dates.joinToString(if (language == LanguagePreference.En) ", " else "、") { compactDateLabel(it) }

    fun heatmapDayLabel(day: HeatmapDay): String {
        val date = runCatching { LocalDate.parse(day.date) }.getOrNull()
        val dateText = if (date == null) {
            day.date
        } else if (language == LanguagePreference.En) {
            "${englishMonths.getOrElse(date.monthValue - 1) { date.monthValue.toString() }} ${date.dayOfMonth}, ${date.year}"
        } else {
            "${date.year}年${date.monthValue}月${date.dayOfMonth}日"
        }
        val countText = if (language == LanguagePreference.En) "${day.count} time(s)" else "${day.count} 次"
        val completedText = if (!day.completed) "" else if (language == LanguagePreference.En) " / completed" else " / 已完成"
        return "$dateText / $countText$completedText"
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
    "backCheckin" to "返回签到",
    "checkin" to "签到",
    "checkinAction" to "签到",
    "share" to "生成分享图",
    "brandTagline" to "习惯热力图",
    "settingsLocalAutoSave" to "本地数据 · 自动保存",
    "dashboardIntroSubtitle" to "坚持每一天，见证改变的力量",
    "empty" to "还没有习惯。",
    "createFirst" to "创建第一个",
    "itemCount" to "{count} 个习惯",
    "uncategorized" to "未分类",
    "currentStreak" to "连续天数",
    "longestStreak" to "最长连续",
    "completionRate" to "完成率",
    "totalCheckins" to "总次数",
    "streakShort" to "连续",
    "longestShort" to "最长",
    "totalShort" to "总次数",
    "dayUnit" to "天",
    "timesUnit" to "次",
    "newItem" to "新增习惯",
    "editItem" to "编辑习惯",
    "newItemSub" to "给一个长期目标一张属于它的热力图。",
    "editItemSub" to "调整分类、时间、颜色和签到规则。",
    "itemName" to "习惯名称",
    "itemNamePlaceholder" to "请输入习惯名称",
    "description" to "描述",
    "descriptionPlaceholder" to "请输入描述（可选）",
    "exampleHabitName" to "习惯名称",
    "exampleHabitDescription" to "这是习惯描述的示例",
    "selectIcon" to "选择图标",
    "allIcons" to "全部图标",
    "selectCategory" to "选择分类",
    "selectColor" to "选择颜色",
    "unset" to "未设置",
    "category" to "分类",
    "icon" to "图标",
    "color" to "颜色",
    "startDate" to "开始日期",
    "endDate" to "结束日期",
    "dateRangeInvalid" to "结束日期不能早于开始日期",
    "unlimited" to "不限结束日期",
    "dailyTarget" to "每日目标次数",
    "todayGoal" to "今日目标",
    "completedLabel" to "已完成",
    "validTime" to "有效时间",
    "allDay" to "全天",
    "timeRange" to "指定时间段",
    "startTime" to "开始时间",
    "endTime" to "结束时间",
    "timeRangeInvalid" to "结束时间不能早于开始时间",
    "allowExtra" to "达标后允许继续签到",
    "allowMakeup" to "允许补签",
    "makeupMonthlyLimit" to "每月最多补签次数",
    "showOnDashboard" to "显示在首页",
    "createItem" to "创建习惯",
    "save" to "保存",
    "deleteItem" to "删除习惯",
    "archive" to "归档",
    "deleteTitle" to "删除这个习惯？",
    "deleteMessage" to "删除后，该习惯的所有签到记录也会被删除，无法恢复。",
    "delete" to "删除",
    "cancel" to "取消",
    "confirm" to "确认",
    "display" to "显示",
    "language" to "语言",
    "theme" to "主题",
    "system" to "系统",
    "light" to "亮色",
    "dark" to "暗色",
    "dashboardMode" to "首页默认模式",
    "all" to "全部",
    "dashboardDisplay" to "首页显示",
    "settingsEnabledCount" to "{count} 项已开启",
    "todayStatus" to "今日状态",
    "categories" to "分类",
    "settingsCategoryCount" to "{count} 个分类",
    "newCategory" to "新分类名称",
    "addCategory" to "新增分类",
    "show" to "显示",
    "hide" to "隐藏",
    "data" to "数据",
    "export" to "导出",
    "import" to "导入",
    "exported" to "已导出",
    "imported" to "已导入",
    "settingsDataSummary" to "导出 / 导入",
    "earnedBadges" to "已获得徽章",
    "settingsBadgeCount" to "{count} 枚已获得",
    "achievements" to "成就",
    "earnedAchievements" to "已获得成就",
    "achievementCount" to "{count} 个成就",
    "noAchievements" to "还没有获得成就",
    "noEarnedBadges" to "还没有获得徽章",
    "archivedItems" to "归档习惯",
    "settingsArchivedCount" to "{count} 个",
    "archivedEmpty" to "还没有归档的习惯。",
    "unarchive" to "恢复",
    "checked" to "已点亮",
    "unchecked" to "未点亮",
    "noData" to "暂无数据",
    "makeupEntry" to "补签",
    "shareImage" to "生成分享图",
    "shareImageSaved" to "图片已保存到相册",
    "shareImageSaveFailed" to "图片保存失败",
    "shareTemplateTitle" to "选择分享图模板",
    "shareTemplate1" to "模板 1 · 标准白卡",
    "shareTemplate2" to "模板 2 · 海报热力",
    "shareTemplate3" to "模板 3 · 水墨方卡",
    "shareTemplate4" to "模板 4 · 数据仪表盘",
    "confirmMakeup" to "确认补签",
    "makeupConfirmed" to "已确认补签 {count} 天",
    "makeupUnlimited" to "补签不限次数",
    "makeupRemaining" to "本月剩余补签 {count} 次",
    "makeupUnlimitedShort" to "本月不限次数",
    "makeupRemainingShort" to "本月剩余 {count} 次",
    "makeupSelected" to "已选择 {count} 天",
    "makeupNoDatesSelected" to "请选择补签日期",
    "makeupCompletedLegend" to "已完成",
    "makeupAvailableLegend" to "可补签",
    "makeupSelectedLegend" to "已选择",
    "makeupUnavailableLegend" to "不可用",
    "checkinRecords" to "签到记录",
    "noCheckinRecords" to "暂无签到记录",
    "makeupCheckin" to "补签",
    "normalCheckin" to "签到",
    "itemMissing" to "习惯不存在",
    "allDayCheckin" to "全天可签到",
    "checkinHeatmap" to "习惯热力图",
    "heatmapLess" to "少",
    "heatmapMore" to "多",
    "heatmapOnce" to "1次",
    "heatmapTwoThree" to "2-3次",
    "heatmapFourSix" to "4-6次",
    "heatmapSevenPlus" to "7+次",
    "today" to "今天",
    "checkinTip" to "坚持记录，每一次点亮都是进步。",
    "checkinTodayCta" to "点亮今天",
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
    "badge_first_light_title" to "初次点亮",
    "badge_week_streak_title" to "七日连光",
    "badge_month_streak_title" to "三十日微光",
    "badge_hundred_lights_title" to "百次记录",
    "badge_steady_flow_title" to "稳定节奏",
    "badge_first_habit_light_title" to "第一束光",
    "badge_seven_day_runner_title" to "七日同行",
    "badge_thirty_day_runner_title" to "一月成线",
    "badge_three_habits_lit_title" to "三线并进",
    "badge_hundred_total_lights_title" to "百次点亮",
)

private val en = mapOf(
    "settings" to "Settings",
    "settingsSub" to "Theme, language, dashboard, and data migration.",
    "backHome" to "Home",
    "backCheckin" to "Back to check-in",
    "checkin" to "Check-in",
    "checkinAction" to "Check in",
    "share" to "Share",
    "brandTagline" to "Habit heatmap",
    "settingsLocalAutoSave" to "Local data · autosaved",
    "dashboardIntroSubtitle" to "Show up daily and watch change take shape",
    "empty" to "No habits yet.",
    "createFirst" to "Create first",
    "itemCount" to "{count} habits",
    "uncategorized" to "Uncategorized",
    "currentStreak" to "Current streak",
    "longestStreak" to "Longest streak",
    "completionRate" to "Completion rate",
    "totalCheckins" to "Total check-ins",
    "streakShort" to "Streak",
    "longestShort" to "Best",
    "totalShort" to "Total",
    "dayUnit" to "d",
    "timesUnit" to "times",
    "newItem" to "New habit",
    "editItem" to "Edit habit",
    "newItemSub" to "Give a long-term goal its own heatmap.",
    "editItemSub" to "Adjust category, time, color, and check-in rules.",
    "itemName" to "Habit name",
    "itemNamePlaceholder" to "Enter habit name",
    "description" to "Description",
    "descriptionPlaceholder" to "Enter description (optional)",
    "exampleHabitName" to "Habit name",
    "exampleHabitDescription" to "Example habit description",
    "selectIcon" to "Select icon",
    "allIcons" to "All icons",
    "selectCategory" to "Select category",
    "selectColor" to "Select color",
    "unset" to "Not set",
    "category" to "Category",
    "icon" to "Icon",
    "color" to "Color",
    "startDate" to "Start date",
    "endDate" to "End date",
    "dateRangeInvalid" to "End date cannot be earlier than start date",
    "unlimited" to "No end date",
    "dailyTarget" to "Daily target",
    "todayGoal" to "Today goal",
    "completedLabel" to "completed",
    "validTime" to "Valid time",
    "allDay" to "All day",
    "timeRange" to "Time range",
    "startTime" to "Start time",
    "endTime" to "End time",
    "timeRangeInvalid" to "End time cannot be earlier than start time",
    "allowExtra" to "Allow extra check-ins after target",
    "allowMakeup" to "Allow makeup check-ins",
    "makeupMonthlyLimit" to "Monthly makeup limit",
    "showOnDashboard" to "Show on dashboard",
    "createItem" to "Create habit",
    "save" to "Save",
    "deleteItem" to "Delete habit",
    "archive" to "Archive",
    "deleteTitle" to "Delete this habit?",
    "deleteMessage" to "All check-in records for this habit will be deleted.",
    "delete" to "Delete",
    "cancel" to "Cancel",
    "confirm" to "Confirm",
    "display" to "Display",
    "language" to "Language",
    "theme" to "Theme",
    "system" to "System",
    "light" to "Light",
    "dark" to "Dark",
    "dashboardMode" to "Default dashboard view",
    "all" to "All",
    "category" to "Categories",
    "dashboardDisplay" to "Home display",
    "settingsEnabledCount" to "{count} enabled",
    "todayStatus" to "Today status",
    "categories" to "Categories",
    "settingsCategoryCount" to "{count} categories",
    "newCategory" to "New category name",
    "addCategory" to "Add category",
    "show" to "Show",
    "hide" to "Hide",
    "data" to "Data",
    "export" to "Export",
    "import" to "Import",
    "exported" to "Exported",
    "imported" to "Imported",
    "settingsDataSummary" to "Export / Import",
    "earnedBadges" to "Earned badges",
    "settingsBadgeCount" to "{count} earned",
    "achievements" to "Achievements",
    "earnedAchievements" to "Earned achievements",
    "achievementCount" to "{count} achievements",
    "noAchievements" to "No achievements yet",
    "noEarnedBadges" to "No badges earned yet",
    "archivedItems" to "Archived habits",
    "settingsArchivedCount" to "{count} archived",
    "archivedEmpty" to "No archived habits.",
    "unarchive" to "Restore",
    "checked" to "Lit up",
    "unchecked" to "Unlit",
    "noData" to "No data",
    "makeupEntry" to "Makeup",
    "shareImage" to "Save image",
    "shareImageSaved" to "Image saved to gallery",
    "shareImageSaveFailed" to "Failed to save image",
    "shareTemplateTitle" to "Choose a share template",
    "shareTemplate1" to "Template 1 · Classic",
    "shareTemplate2" to "Template 2 · Poster",
    "shareTemplate3" to "Template 3 · Zen square",
    "shareTemplate4" to "Template 4 · Dashboard",
    "confirmMakeup" to "Confirm makeup",
    "makeupConfirmed" to "Confirmed {count} makeup day(s)",
    "makeupUnlimited" to "Unlimited makeup check-ins",
    "makeupRemaining" to "{count} makeup check-in(s) left this month",
    "makeupUnlimitedShort" to "No monthly limit",
    "makeupRemainingShort" to "{count} left this month",
    "makeupSelected" to "{count} day(s) selected",
    "makeupNoDatesSelected" to "Select makeup date(s)",
    "makeupCompletedLegend" to "Completed",
    "makeupAvailableLegend" to "Available",
    "makeupSelectedLegend" to "Selected",
    "makeupUnavailableLegend" to "Unavailable",
    "checkinRecords" to "Check-in records",
    "noCheckinRecords" to "No check-in records yet",
    "makeupCheckin" to "Makeup",
    "normalCheckin" to "Normal",
    "itemMissing" to "Habit not found",
    "allDayCheckin" to "Available all day",
    "checkinHeatmap" to "Habit heatmap",
    "heatmapLess" to "Less",
    "heatmapMore" to "More",
    "heatmapOnce" to "1x",
    "heatmapTwoThree" to "2-3x",
    "heatmapFourSix" to "4-6x",
    "heatmapSevenPlus" to "7+x",
    "today" to "Today",
    "checkinTip" to "Keep recording. Every light is progress.",
    "checkinTodayCta" to "Light up today",
    "streakDays" to "{count}-day streak",
    "statusAvailable" to "Available today",
    "statusNotStarted" to "Not started",
    "statusEnded" to "Ended",
    "statusBefore" to "Not time yet",
    "statusAfter" to "Window closed",
    "statusCompleted" to "Lit today",
    "statusContinue" to "Done, can continue",
    "hintAvailable" to "Tap to light up today",
    "hintNotStarted" to "Start date has not arrived",
    "hintEnded" to "This habit has ended",
    "hintBefore" to "Come back later",
    "hintAfter" to "Continue tomorrow",
    "hintCompleted" to "Completed today",
    "hintContinue" to "Target reached, still recordable",
    "color_green" to "Green",
    "color_blue" to "Blue",
    "color_purple" to "Purple",
    "color_orange" to "Orange",
    "color_red" to "Red",
    "color_teal" to "Teal",
    "color_pink" to "Pink",
    "color_gray" to "Gray",
    "badge_first_light_title" to "First light",
    "badge_week_streak_title" to "Seven-day glow",
    "badge_month_streak_title" to "Thirty-day glow",
    "badge_hundred_lights_title" to "Hundred records",
    "badge_steady_flow_title" to "Steady rhythm",
    "badge_first_habit_light_title" to "First spark",
    "badge_seven_day_runner_title" to "Seven days",
    "badge_thirty_day_runner_title" to "One-month line",
    "badge_three_habits_lit_title" to "Three lanes",
    "badge_hundred_total_lights_title" to "Hundred lights",
)
