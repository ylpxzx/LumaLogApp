package com.example.lumalogapp.ui.share

import android.content.Context
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.lumalogapp.data.Badge
import com.example.lumalogapp.data.DashboardItem
import com.example.lumalogapp.data.HeatmapDay
import com.example.lumalogapp.data.itemBadges
import com.example.lumalogapp.ui.i18n.LumaStrings
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.min

private data class HeatmapCell(
    val day: HeatmapDay?,
    val visible: Boolean,
)

fun saveHabitImage(
    context: Context,
    entry: DashboardItem,
    strings: LumaStrings,
    darkTheme: Boolean,
) {
    val badges = itemBadges(entry.stats).filter { it.earned }
    val bitmap = renderHabitShareBitmap(entry, badges, strings, darkTheme)
    val resolver = context.contentResolver
    val fileName = "lumalog-${entry.item.id}-${DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now())}.png"
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/LumaLog")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val uri = resolver.insert(collection, values)
    if (uri == null) {
        Toast.makeText(context, strings.t("shareImageSaveFailed"), Toast.LENGTH_SHORT).show()
        return
    }

    runCatching {
        resolver.openOutputStream(uri)?.use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        } ?: error("Unable to open image output stream")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver.update(
                uri,
                ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                null,
                null,
            )
        }
    }.onSuccess {
        Toast.makeText(context, strings.t("shareImageSaved"), Toast.LENGTH_SHORT).show()
    }.onFailure {
        resolver.delete(uri, null, null)
        Toast.makeText(context, strings.t("shareImageSaveFailed"), Toast.LENGTH_SHORT).show()
    }
}

private fun renderHabitShareBitmap(
    entry: DashboardItem,
    badges: List<Badge>,
    strings: LumaStrings,
    darkTheme: Boolean,
): Bitmap {
    val width = 1200
    val height = 960
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val bg = if (darkTheme) Color.rgb(12, 17, 24) else Color.rgb(246, 248, 250)
    val surface = if (darkTheme) Color.rgb(17, 24, 34) else Color.rgb(255, 255, 255)
    val text = if (darkTheme) Color.rgb(235, 241, 248) else Color.rgb(16, 25, 36)
    val muted = if (darkTheme) Color.rgb(151, 164, 183) else Color.rgb(98, 113, 132)
    val outline = if (darkTheme) Color.rgb(45, 57, 75) else Color.rgb(219, 225, 232)
    val primary = themeColor(entry.item.colorTheme)

    canvas.drawColor(bg)
    val card = RectF(48f, 44f, width - 48f, height - 44f)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = surface
    canvas.drawRoundRect(card, 28f, 28f, paint)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 2f
    paint.color = outline
    canvas.drawRoundRect(card, 28f, 28f, paint)
    paint.style = Paint.Style.FILL

    drawFittedText(
        canvas = canvas,
        value = entry.item.name,
        x = 92f,
        y = 132f,
        maxWidth = 980f,
        size = 54f,
        minSize = 36f,
        color = text,
        typefaceStyle = Typeface.BOLD,
    )
    drawFittedText(
        canvas = canvas,
        value = "${strings.categoryName(entry.category?.name ?: strings.t("uncategorized"))} / ${
            strings.t("streakDays", "count" to entry.stats.currentStreak.toString())
        }",
        x = 94f,
        y = 178f,
        maxWidth = 1000f,
        size = 26f,
        minSize = 20f,
        color = muted,
        typefaceStyle = Typeface.BOLD,
    )

    drawStats(canvas, entry, strings, text, muted, primary)
    drawHeatmap(canvas, entry.heatmap, entry.item.colorTheme, strings, darkTheme, 92f, 340f, width - 184f)

    val badgeTitlePaint = textPaint(25f, muted, Typeface.BOLD)
    canvas.drawText(strings.t("earnedBadges"), 92f, 696f, badgeTitlePaint)
    if (badges.isEmpty()) {
        val emptyPaint = textPaint(22f, muted, Typeface.BOLD)
        canvas.drawText(strings.t("noEarnedBadges"), 92f, 770f, emptyPaint)
    } else {
        drawBadges(canvas, badges.take(3), strings, 92f, 736f, text)
    }

    val brandPaint = textPaint(22f, muted, Typeface.BOLD)
    canvas.drawText("LumaLog / ${strings.t("brandTagline")}", 92f, 884f, brandPaint)
    return bitmap
}

private fun drawStats(
    canvas: Canvas,
    entry: DashboardItem,
    strings: LumaStrings,
    text: Int,
    muted: Int,
    primary: Int,
) {
    val values = listOf(
        entry.stats.currentStreak.toString() to strings.t("currentStreak"),
        entry.stats.longestStreak.toString() to strings.t("longestStreak"),
        "${(entry.stats.completionRate * 100).toInt()}%" to strings.t("completionRate"),
        entry.stats.totalCheckins.toString() to strings.t("totalCheckins"),
    )
    val startX = 92f
    val y = 232f
    val itemWidth = 248f
    values.forEachIndexed { index, (value, label) ->
        val left = startX + index * itemWidth
        val valuePaint = textPaint(34f, if (index == 0) primary else text, Typeface.BOLD)
        canvas.drawText(value, left, y, valuePaint)
        drawFittedText(
            canvas = canvas,
            value = label,
            x = left,
            y = y + 36f,
            maxWidth = 190f,
            size = 21f,
            minSize = 16f,
            color = muted,
            typefaceStyle = Typeface.BOLD,
        )
    }
}

private fun drawHeatmap(
    canvas: Canvas,
    days: List<HeatmapDay>,
    colorTheme: String,
    strings: LumaStrings,
    darkTheme: Boolean,
    left: Float,
    top: Float,
    width: Float,
) {
    val weeks = buildWeeks(days)
    if (weeks.isEmpty()) return
    val gap = 6f
    val cellSize = (width - gap * (weeks.size - 1)) / weeks.size
    val labelPaint = textPaint(21f, if (darkTheme) Color.rgb(148, 163, 184) else Color.rgb(100, 116, 139), Typeface.BOLD)
    val emptyColor = if (darkTheme) {
        blend(Color.rgb(17, 24, 34), Color.rgb(25, 34, 48), 0.86f)
    } else {
        blend(Color.WHITE, Color.rgb(238, 242, 247), 0.62f)
    }
    val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    weeks.forEachIndexed { column, week ->
        val dates = week.mapNotNull { cell -> cell.day?.date?.let { runCatching { LocalDate.parse(it) }.getOrNull() } }
        val monthStart = dates.firstOrNull { it.dayOfMonth == 1 }
        val marker = monthStart ?: dates.firstOrNull().takeIf { column == 0 }
        if (marker != null) {
            canvas.drawText(strings.monthLabel(marker), left + column * (cellSize + gap), top - 18f, labelPaint)
        }
        week.forEachIndexed { row, heatmapCell ->
            val day = heatmapCell.day
            if (!heatmapCell.visible) return@forEachIndexed
            cellPaint.color = if (day == null) emptyColor else heatmapColor(colorTheme, day.level, emptyColor)
            val x = left + column * (cellSize + gap)
            val y = top + row * (cellSize + gap)
            canvas.drawRoundRect(RectF(x, y, x + cellSize, y + cellSize), 6f, 6f, cellPaint)
        }
    }
}

private fun buildWeeks(days: List<HeatmapDay>): List<List<HeatmapCell>> {
    val parsedDays = days.mapNotNull { day ->
        runCatching { LocalDate.parse(day.date) }.getOrNull()?.let { it to day }
    }
    if (parsedDays.isEmpty()) return emptyList()
    val leadingSlots = parsedDays.first().first.dayOfWeek.value - 1
    val trailingSlots = (7 - ((leadingSlots + parsedDays.size) % 7)) % 7
    val firstDate = parsedDays.first().first
    val leadingCells = List(leadingSlots) { index ->
        val date = firstDate.minusDays((leadingSlots - index).toLong())
        HeatmapCell(
            day = HeatmapDay(date = date.toString(), count = 0, completed = false, level = 0),
            visible = true,
        )
    }
    return (leadingCells +
        parsedDays.map { HeatmapCell(day = it.second, visible = true) } +
        List(trailingSlots) { HeatmapCell(day = null, visible = false) })
        .chunked(7)
}

private fun drawBadges(canvas: Canvas, badges: List<Badge>, strings: LumaStrings, left: Float, top: Float, text: Int) {
    val itemWidth = 246f
    badges.forEachIndexed { index, badge ->
        val column = index % 3
        val x = left + column * itemWidth
        val y = top
        drawBadgeIcon(canvas, badge, x + 24f, y, 66f)
        drawFittedText(
            canvas = canvas,
            value = strings.badgeTitle(badge),
            x = x + 57f,
            y = y + 98f,
            maxWidth = 170f,
            size = 21f,
            minSize = 15f,
            color = text,
            typefaceStyle = Typeface.BOLD,
            align = Paint.Align.CENTER,
        )
    }
}

private fun drawBadgeIcon(canvas: Canvas, badge: Badge, left: Float, top: Float, size: Float) {
    val accent = when (badge.level) {
        "gold" -> Color.rgb(250, 204, 21)
        "silver" -> Color.rgb(203, 213, 225)
        else -> Color.rgb(217, 154, 91)
    }
    val green = Color.rgb(34, 197, 94)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        color = accent
    }
    val centerX = left + size / 2f
    val centerY = top + size / 2f

    when (badge.id) {
        "week_streak", "seven_day_runner" -> {
            canvas.drawArc(RectF(left + 10f, top + 12f, left + size - 10f, top + size - 10f), 190f, 160f, false, paint)
            paint.style = Paint.Style.FILL
            repeat(5) { index ->
                paint.color = if (index % 2 == 0) green else accent
                canvas.drawCircle(left + 14f + index * 9f, top + 48f - min(index, 2) * 5f, 5f, paint)
            }
        }
        "month_streak", "thirty_day_runner" -> {
            canvas.drawCircle(centerX, centerY, size * 0.28f, paint)
            paint.color = green
            paint.strokeWidth = 4f
            canvas.drawCircle(centerX, centerY, size * 0.16f, paint)
        }
        "hundred_lights", "hundred_total_lights", "three_habits_lit" -> {
            paint.style = Paint.Style.FILL
            repeat(3) { x ->
                repeat(3) { y ->
                    paint.color = listOf(green, accent, Color.rgb(132, 204, 22))[(x + y) % 3]
                    val cellLeft = left + 16f + x * 13f
                    val cellTop = top + 16f + y * 13f
                    canvas.drawRoundRect(RectF(cellLeft, cellTop, cellLeft + 9f, cellTop + 9f), 3f, 3f, paint)
                }
            }
        }
        "steady_flow" -> {
            val path = Path().apply {
                moveTo(left + size * 0.14f, top + size * 0.58f)
                cubicTo(left + size * 0.32f, top + size * 0.20f, left + size * 0.46f, top + size * 0.20f, left + size * 0.58f, top + size * 0.58f)
                cubicTo(left + size * 0.70f, top + size * 0.90f, left + size * 0.84f, top + size * 0.90f, left + size * 0.92f, top + size * 0.58f)
            }
            canvas.drawPath(path, paint)
            paint.color = green
            paint.strokeWidth = 3f
            canvas.drawPath(path, paint)
        }
        else -> {
            canvas.drawCircle(centerX, centerY, size * 0.26f, paint)
            paint.style = Paint.Style.FILL
            paint.color = green
            canvas.drawCircle(centerX, centerY, size * 0.13f, paint)
        }
    }
}

private fun textPaint(size: Float, color: Int, typefaceStyle: Int = Typeface.NORMAL): Paint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        textSize = size
        typeface = Typeface.create(Typeface.DEFAULT, typefaceStyle)
    }

private fun drawFittedText(
    canvas: Canvas,
    value: String,
    x: Float,
    y: Float,
    maxWidth: Float,
    size: Float,
    minSize: Float,
    color: Int,
    typefaceStyle: Int = Typeface.NORMAL,
    align: Paint.Align = Paint.Align.LEFT,
) {
    var drawValue = value
    val paint = textPaint(size, color, typefaceStyle).apply { textAlign = align }
    while (paint.textSize > minSize && paint.measureText(drawValue) > maxWidth) {
        paint.textSize -= 1f
    }
    while (drawValue.length > 4 && paint.measureText(drawValue) > maxWidth) {
        drawValue = "${drawValue.dropLast(4)}..."
    }
    canvas.drawText(drawValue, x, y, paint)
}

private fun themeColor(theme: String): Int = when (theme) {
    "blue" -> Color.rgb(59, 130, 246)
    "purple" -> Color.rgb(168, 85, 247)
    "orange" -> Color.rgb(249, 115, 22)
    "red" -> Color.rgb(239, 68, 68)
    "teal" -> Color.rgb(20, 184, 166)
    "pink" -> Color.rgb(236, 72, 153)
    "gray" -> Color.rgb(100, 116, 139)
    else -> Color.rgb(34, 197, 94)
}

private fun heatmapColor(theme: String, level: Int, emptyColor: Int): Int {
    val base = themeColor(theme)
    return when (level) {
        1 -> blend(emptyColor, base, 0.28f)
        2 -> blend(emptyColor, base, 0.48f)
        3 -> blend(emptyColor, base, 0.72f)
        4 -> base
        else -> emptyColor
    }
}

private fun blend(background: Int, foreground: Int, alpha: Float): Int {
    val inverse = 1f - alpha
    return Color.rgb(
        (Color.red(background) * inverse + Color.red(foreground) * alpha).toInt(),
        (Color.green(background) * inverse + Color.green(foreground) * alpha).toInt(),
        (Color.blue(background) * inverse + Color.blue(foreground) * alpha).toInt(),
    )
}
