package com.example.lumalogapp.ui.utils

import androidx.compose.ui.graphics.Color
import com.example.lumalogapp.data.Item
import com.example.lumalogapp.data.TimeMode
import com.example.lumalogapp.ui.i18n.LumaStrings

fun timeHint(item: Item, strings: LumaStrings): String {
    return if (item.timeMode == TimeMode.AllDay) {
        strings.t("allDayCheckin")
    } else {
        "${item.validStartTime} - ${item.validEndTime}"
    }
}

val colorThemes = listOf("green", "blue", "purple", "orange", "red", "teal", "pink", "gray")

fun themeColor(theme: String) = when (theme) {
    "blue" -> Color(0xFF3B82F6)
    "purple" -> Color(0xFFA855F7)
    "orange" -> Color(0xFFF97316)
    "red" -> Color(0xFFEF4444)
    "teal" -> Color(0xFF14B8A6)
    "pink" -> Color(0xFFEC4899)
    "gray" -> Color(0xFF64748B)
    else -> Color(0xFF22C55E)
}

fun heatmapColor(theme: String, level: Int, emptyColor: Color): Color {
    val base = themeColor(theme)
    return when (level) {
        1 -> base.copy(alpha = 0.28f)
        2 -> base.copy(alpha = 0.48f)
        3 -> base.copy(alpha = 0.72f)
        4 -> base
        else -> emptyColor
    }
}
