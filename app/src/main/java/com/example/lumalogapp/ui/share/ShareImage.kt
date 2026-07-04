package com.example.lumalogapp.ui.share

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.lumalogapp.R
import com.example.lumalogapp.data.Badge
import com.example.lumalogapp.data.DashboardItem
import com.example.lumalogapp.data.HeatmapDay
import com.example.lumalogapp.data.itemBadges
import com.example.lumalogapp.ui.components.lumaIconDrawableFor
import com.example.lumalogapp.ui.i18n.LumaStrings
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class ShareTemplate(val number: Int) {
    Classic(1),
    Poster(2),
    Zen(3),
    Dashboard(4),
}

private data class HeatmapCell(
    val day: HeatmapDay?,
    val visible: Boolean,
)

private data class ShareColors(
    val primary: Int,
    val primaryDark: Int,
    val bg: Int,
    val surface: Int,
    val text: Int,
    val muted: Int,
    val outline: Int,
    val emptyCell: Int,
)

private data class ShareStat(
    val value: String,
    val label: String,
)

fun saveHabitImage(
    context: Context,
    entry: DashboardItem,
    strings: LumaStrings,
    darkTheme: Boolean,
    template: ShareTemplate = ShareTemplate.Classic,
) {
    val badges = itemBadges(entry.stats).filter { it.earned }
    val bitmap = renderHabitShareBitmap(context, entry, badges, strings, darkTheme, template)
    val resolver = context.contentResolver
    val fileName = "lumalog-${entry.item.id}-template${template.number}-${DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now())}.png"
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
    context: Context,
    entry: DashboardItem,
    badges: List<Badge>,
    strings: LumaStrings,
    darkTheme: Boolean,
    template: ShareTemplate,
): Bitmap {
    val colors = shareColors(themeColor(entry.item.colorTheme), darkTheme)
    val size = when (template) {
        ShareTemplate.Classic -> 1536 to 1136
        ShareTemplate.Poster -> 1536 to 1112
        ShareTemplate.Zen -> 1240 to 1240
        ShareTemplate.Dashboard -> 1640 to 1080
    }
    val bitmap = Bitmap.createBitmap(size.first, size.second, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    when (template) {
        ShareTemplate.Classic -> drawClassicTemplate(canvas, context, entry, badges, strings, colors)
        ShareTemplate.Poster -> drawPosterTemplate(canvas, context, entry, badges, strings, colors)
        ShareTemplate.Zen -> drawZenTemplate(canvas, context, entry, badges, strings, colors)
        ShareTemplate.Dashboard -> drawDashboardTemplate(canvas, context, entry, badges, strings, colors)
    }
    return bitmap
}

private fun drawClassicTemplate(
    canvas: Canvas,
    context: Context,
    entry: DashboardItem,
    badges: List<Badge>,
    strings: LumaStrings,
    colors: ShareColors,
) {
    canvas.drawColor(colors.bg)
    drawSoftCard(canvas, RectF(36f, 58f, 1500f, 1080f), 36f, colors.surface, withAlpha(colors.primary, 0.55f), 0.0f)

    drawIconTile(canvas, context, entry.item.iconKey, RectF(92f, 103f, 228f, 239f), 30f, colors)
    drawFittedText(canvas, entry.item.name, 266f, 161f, 920f, 58f, 38f, colors.text, Typeface.BOLD)
    drawMetaLine(canvas, entry, strings, 266f, 222f, 1040f, 29f, colors)

    drawStatRow(
        canvas = canvas,
        stats = shareStats(entry, strings),
        centers = listOf(246f, 596f, 944f, 1290f),
        valueY = 309f,
        labelY = 351f,
        valueSize = 43f,
        labelSize = 25f,
        colors = colors,
        dividerTop = 273f,
        dividerBottom = 350f,
    )

    drawHeatmapGrid(
        canvas = canvas,
        days = entry.heatmap,
        colorTheme = entry.item.colorTheme,
        strings = strings,
        gridLeft = 92f,
        gridTop = 425f,
        gridWidth = 1352f,
        maxCellSize = 48f,
        horizontalGap = 10f,
        verticalGap = 8f,
        radius = 7f,
        labelX = 164f,
        showWeekdays = false,
        showMonths = true,
        colors = colors,
    )

    drawBadgesClassic(canvas, badges.take(3), strings, 92f, 858f, 360f, colors)
    drawBrand(canvas, 92f, 1016f, strings, colors, 25f, mark = false, accentBrand = true)
}

private fun drawPosterTemplate(
    canvas: Canvas,
    context: Context,
    entry: DashboardItem,
    badges: List<Badge>,
    strings: LumaStrings,
    colors: ShareColors,
) {
    canvas.drawColor(Color.rgb(252, 254, 251))
    val cardRect = RectF(36f, 36f, 1500f, 1064f)
    drawSoftCard(canvas, cardRect, 34f, Color.rgb(253, 255, 252), withAlpha(colors.primary, 0.55f), 0.12f)
    canvas.save()
    clipRoundRect(canvas, cardRect, 34f)
    drawDottedField(canvas, RectF(54f, 54f, 1482f, 1046f), colors.primary)
    drawSproutWatermark(canvas, 1360f, 940f, 190f, colors.primary)
    canvas.restore()

    drawPosterTitle(canvas, context, entry, 93f, 197f, colors)
    drawMetaLine(canvas, entry, strings, 95f, 269f, 610f, 34f, colors, highlightStreak = true)

    drawStatRow(
        canvas = canvas,
        stats = shareStats(entry, strings),
        centers = listOf(842f, 1030f, 1214f, 1392f),
        valueY = 188f,
        labelY = 234f,
        valueSize = 58f,
        labelSize = 27f,
        colors = colors,
        dividerTop = 130f,
        dividerBottom = 253f,
    )

    drawHeatmapGrid(
        canvas = canvas,
        days = entry.heatmap,
        colorTheme = entry.item.colorTheme,
        strings = strings,
        gridLeft = 94f,
        gridTop = 342f,
        gridWidth = 1347f,
        maxCellSize = 47f,
        horizontalGap = 15f,
        verticalGap = 13f,
        radius = 8f,
        labelX = 0f,
        showWeekdays = false,
        showMonths = true,
        colors = colors,
        saturated = true,
    )

    drawBadgesPoster(canvas, badges.take(3), strings, 94f, 800f, colors)
    drawBrand(canvas, 92f, 1000f, strings, colors, 30f, mark = false, accentBrand = false)
}

private fun drawZenTemplate(
    canvas: Canvas,
    context: Context,
    entry: DashboardItem,
    badges: List<Badge>,
    strings: LumaStrings,
    colors: ShareColors,
) {
    canvas.drawColor(colors.bg)
    val cardRect = RectF(40f, 73f, 1200f, 1188f)
    drawSoftCard(canvas, cardRect, 44f, Color.rgb(253, 255, 251), withAlpha(colors.primary, 0.16f), 0.10f)
    canvas.save()
    val clipPath = Path().apply { addRoundRect(cardRect, 44f, 44f, Path.Direction.CW) }
    canvas.clipPath(clipPath)
    drawPaperTexture(canvas, RectF(cardRect.left + 2f, cardRect.top + 2f, cardRect.right - 2f, cardRect.bottom - 2f), colors.primary)
    drawWatercolorMountains(canvas, cardRect, colors.primary)
    drawFloatingLeaves(canvas, colors.primary)
    canvas.restore()

    drawIconTile(canvas, context, entry.item.iconKey, RectF(526f, 114f, 714f, 302f), 94f, colors)
    drawFittedText(
        canvas = canvas,
        value = entry.item.name,
        x = 620f,
        y = 388f,
        maxWidth = 720f,
        size = 58f,
        minSize = 38f,
        color = colors.text,
        typefaceStyle = Typeface.BOLD,
        align = Paint.Align.CENTER,
    )
    drawFittedText(
        canvas = canvas,
        value = "${strings.categoryName(entry.category?.name ?: strings.t("uncategorized"))} / ${strings.t("streakDays", "count" to entry.stats.currentStreak.toString())}",
        x = 620f,
        y = 439f,
        maxWidth = 620f,
        size = 29f,
        minSize = 22f,
        color = colors.primaryDark,
        typefaceStyle = Typeface.BOLD,
        align = Paint.Align.CENTER,
    )

    drawSoftCard(canvas, RectF(124f, 464f, 1116f, 572f), 21f, withAlpha(Color.WHITE, 0.82f), withAlpha(colors.primary, 0.13f), 0.0f)
    drawStatRow(
        canvas = canvas,
        stats = shareStats(entry, strings),
        centers = listOf(248f, 496f, 744f, 992f),
        valueY = 516f,
        labelY = 549f,
        valueSize = 34f,
        labelSize = 21f,
        colors = colors,
        dividerTop = 501f,
        dividerBottom = 540f,
    )

    drawHeatmapGrid(
        canvas = canvas,
        days = entry.heatmap,
        colorTheme = entry.item.colorTheme,
        strings = strings,
        gridLeft = 124f,
        gridTop = 646f,
        gridWidth = 992f,
        maxCellSize = 38f,
        horizontalGap = 7f,
        verticalGap = 5f,
        radius = 5f,
        labelX = 158f,
        showWeekdays = false,
        showMonths = true,
        colors = colors,
    )

    drawBadgesZen(canvas, badges.take(3), strings, 124f, 968f, colors)
    drawBrandCentered(canvas, 620f, 1128f, strings, colors, 29f, accentBrand = false)
    drawFittedText(
        canvas = canvas,
        value = if (isChinese(strings)) "记录微小习惯 · 见证持续成长" else "Track small habits · witness steady growth",
        x = 620f,
        y = 1170f,
        maxWidth = 520f,
        size = 21f,
        minSize = 17f,
        color = colors.muted,
        typefaceStyle = Typeface.NORMAL,
        align = Paint.Align.CENTER,
    )
}

private fun drawDashboardTemplate(
    canvas: Canvas,
    context: Context,
    entry: DashboardItem,
    badges: List<Badge>,
    strings: LumaStrings,
    colors: ShareColors,
) {
    canvas.drawColor(Color.rgb(247, 250, 252))
    drawSoftCard(canvas, RectF(40f, 40f, 1600f, 1030f), 50f, colors.surface, withAlpha(colors.primary, 0.45f), 0.12f)

    drawIconTile(canvas, context, entry.item.iconKey, RectF(88f, 96f, 238f, 246f), 28f, colors)
    drawFittedText(canvas, entry.item.name, 282f, 166f, 420f, 59f, 39f, colors.text, Typeface.BOLD)
    drawMetaLine(canvas, entry, strings, 282f, 225f, 420f, 33f, colors, highlightStreak = true)

    val stats = shareStats(entry, strings)
    val cards = listOf(
        RectF(758f, 103f, 944f, 226f),
        RectF(964f, 103f, 1150f, 226f),
        RectF(1170f, 103f, 1356f, 226f),
        RectF(1376f, 103f, 1562f, 226f),
    )
    cards.forEachIndexed { index, rect ->
        drawDashboardStatCard(canvas, context, rect, stats[index], index, colors)
    }

    drawHeatmapGrid(
        canvas = canvas,
        days = entry.heatmap,
        colorTheme = entry.item.colorTheme,
        strings = strings,
        gridLeft = 92f,
        gridTop = 336f,
        gridWidth = 1456f,
        maxCellSize = 47f,
        horizontalGap = 14f,
        verticalGap = 14f,
        radius = 7f,
        labelX = 0f,
        showWeekdays = false,
        showMonths = true,
        colors = colors,
        saturated = true,
    )

    drawBadgesDashboard(canvas, badges.take(3), strings, 92f, 792f, colors)
    drawBrand(canvas, 92f, 958f, strings, colors, 24f, mark = false, accentBrand = false)
}

private fun shareStats(entry: DashboardItem, strings: LumaStrings): List<ShareStat> = listOf(
    ShareStat(entry.stats.currentStreak.toString(), strings.t("currentStreak")),
    ShareStat(entry.stats.longestStreak.toString(), strings.t("longestStreak")),
    ShareStat("${(entry.stats.completionRate * 100).roundToInt()}%", strings.t("completionRate")),
    ShareStat(entry.stats.totalCheckins.toString(), strings.t("totalCheckins")),
)

private fun drawPosterTitle(
    canvas: Canvas,
    context: Context,
    entry: DashboardItem,
    x: Float,
    y: Float,
    colors: ShareColors,
) {
    val iconRect = RectF(x, y - 78f, x + 92f, y + 14f)
    drawIconTile(
        canvas = canvas,
        context = context,
        iconKey = entry.item.iconKey,
        rect = iconRect,
        radius = 28f,
        colors = colors,
        transparentTile = true,
    )

    var title = entry.item.name
    val titlePaint = textPaint(86f, colors.text, Typeface.BOLD)
    val titleX = iconRect.right + 18f
    val maxTitleWidth = 590f
    while (titlePaint.textSize > 54f && titlePaint.measureText(title) > maxTitleWidth) {
        titlePaint.textSize -= 1f
    }
    while (title.length > 4 && titlePaint.measureText(title) > maxTitleWidth) {
        title = "${title.dropLast(4)}..."
    }
    canvas.drawText(title, titleX, y, titlePaint)
}

private fun shareColors(primary: Int, darkTheme: Boolean): ShareColors {
    val tunedPrimary = if (darkTheme) blend(primary, Color.rgb(34, 197, 94), 0.18f) else primary
    return ShareColors(
        primary = tunedPrimary,
        primaryDark = blend(Color.rgb(5, 99, 47), tunedPrimary, 0.42f),
        bg = Color.rgb(247, 250, 252),
        surface = Color.rgb(255, 255, 255),
        text = Color.rgb(16, 25, 36),
        muted = Color.rgb(91, 105, 124),
        outline = Color.rgb(218, 226, 232),
        emptyCell = blend(Color.WHITE, tunedPrimary, 0.11f),
    )
}

private fun drawStatRow(
    canvas: Canvas,
    stats: List<ShareStat>,
    centers: List<Float>,
    valueY: Float,
    labelY: Float,
    valueSize: Float,
    labelSize: Float,
    colors: ShareColors,
    dividerTop: Float,
    dividerBottom: Float,
) {
    val valuePaint = textPaint(valueSize, colors.primaryDark, Typeface.BOLD).apply { textAlign = Paint.Align.CENTER }
    val labelPaint = textPaint(labelSize, colors.muted, Typeface.BOLD).apply { textAlign = Paint.Align.CENTER }
    val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = withAlpha(colors.outline, 0.86f)
        strokeWidth = 1.6f
    }
    stats.forEachIndexed { index, stat ->
        canvas.drawText(stat.value, centers[index], valueY, valuePaint)
        drawFittedText(canvas, stat.label, centers[index], labelY, 180f, labelSize, labelSize - 5f, colors.muted, Typeface.BOLD, Paint.Align.CENTER)
        if (index < stats.lastIndex) {
            val x = (centers[index] + centers[index + 1]) / 2f
            canvas.drawLine(x, dividerTop, x, dividerBottom, dividerPaint)
        }
    }
}

private fun drawDashboardStatCard(canvas: Canvas, context: Context, rect: RectF, stat: ShareStat, index: Int, colors: ShareColors) {
    drawSoftCard(canvas, rect, 18f, Color.WHITE, withAlpha(Color.rgb(148, 163, 184), 0.26f), 0.04f)
    val iconCenterX = rect.left + 54f
    val iconCenterY = rect.top + 61f
    drawDashboardStatIcon(canvas, context, iconCenterX, iconCenterY, 46f, index, colors)
    drawFittedText(canvas, stat.value, rect.left + 124f, rect.top + 64f, 90f, 40f, 28f, colors.primaryDark, Typeface.BOLD, Paint.Align.CENTER)
    drawFittedText(canvas, stat.label, rect.left + 124f, rect.top + 95f, 105f, 19f, 15f, colors.muted, Typeface.BOLD, Paint.Align.CENTER)
}

private fun drawDashboardStatIcon(canvas: Canvas, context: Context, cx: Float, cy: Float, size: Float, index: Int, colors: ShareColors) {
    val drawable = ContextCompat.getDrawable(context, dashboardStatIconRes(index))?.mutate() ?: return
    val halfSize = size / 2f
    drawable.setTint(colors.primary)
    drawable.setBounds(
        (cx - halfSize).roundToInt(),
        (cy - halfSize).roundToInt(),
        (cx + halfSize).roundToInt(),
        (cy + halfSize).roundToInt(),
    )
    drawable.draw(canvas)
}

private fun dashboardStatIconRes(index: Int): Int = when (index) {
    0 -> R.drawable.ic_stat_flame
    1 -> R.drawable.ic_stat_rise
    2 -> R.drawable.ic_stat_progress
    else -> R.drawable.ic_stat_star
}

private fun drawHeatmapGrid(
    canvas: Canvas,
    days: List<HeatmapDay>,
    colorTheme: String,
    strings: LumaStrings,
    gridLeft: Float,
    gridTop: Float,
    gridWidth: Float,
    maxCellSize: Float,
    horizontalGap: Float,
    verticalGap: Float,
    radius: Float,
    labelX: Float,
    showWeekdays: Boolean,
    showMonths: Boolean,
    colors: ShareColors,
    saturated: Boolean = false,
) {
    val weeks = buildWeeks(days)
    if (weeks.isEmpty()) return

    val computedSize = (gridWidth - horizontalGap * (weeks.size - 1)) / weeks.size
    val cellSize = min(maxCellSize, computedSize)
    val contentWidth = cellSize * weeks.size + horizontalGap * (weeks.size - 1)
    val left = gridLeft + (gridWidth - contentWidth) / 2f
    val labelPaint = textPaint(if (cellSize < 26f) 18f else 23f, colors.muted, Typeface.BOLD)
    val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val empty = if (saturated) blend(Color.WHITE, colors.primary, 0.10f) else colors.emptyCell

    if (showMonths) {
        weeks.forEachIndexed { column, week ->
            val marker = monthMarker(column, week)
            if (marker != null) {
                canvas.drawText(strings.monthLabel(marker), left + column * (cellSize + horizontalGap), gridTop - 20f, labelPaint)
            }
        }
    }

    if (showWeekdays) {
        val weekdays = weekdayLabels(strings)
        val weekdayPaint = textPaint(if (cellSize < 26f) 18f else 24f, colors.muted, Typeface.BOLD)
        weekdays.forEachIndexed { row, label ->
            canvas.drawText(label, labelX, gridTop + row * (cellSize + verticalGap) + cellSize * 0.72f, weekdayPaint)
        }
    }

    weeks.forEachIndexed { column, week ->
        week.forEachIndexed { row, heatmapCell ->
            if (!heatmapCell.visible) return@forEachIndexed
            val day = heatmapCell.day
            cellPaint.color = if (day == null) empty else heatmapColor(colorTheme, day.level, empty, saturated)
            val x = left + column * (cellSize + horizontalGap)
            val y = gridTop + row * (cellSize + verticalGap)
            canvas.drawRoundRect(RectF(x, y, x + cellSize, y + cellSize), radius, radius, cellPaint)
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

private fun monthMarker(column: Int, week: List<HeatmapCell>): LocalDate? {
    val dates = week.mapNotNull { cell -> cell.day?.date?.let { runCatching { LocalDate.parse(it) }.getOrNull() } }
    val monthStart = dates.firstOrNull { it.dayOfMonth == 1 }
    return monthStart ?: dates.firstOrNull().takeIf { column == 0 }
}

private fun weekdayLabels(strings: LumaStrings): List<String> {
    val labels = strings.weekdayLabels()
    val mondayFirst = labels.drop(1) + labels.take(1)
    return if (mondayFirst.firstOrNull() == "一") mondayFirst.map { "周$it" } else mondayFirst
}

private fun drawBadgesClassic(
    canvas: Canvas,
    badges: List<Badge>,
    strings: LumaStrings,
    left: Float,
    top: Float,
    width: Float,
    colors: ShareColors,
) {
    drawBadgeCards(
        canvas = canvas,
        badges = badges,
        strings = strings,
        left = left,
        top = top,
        itemWidth = 350f,
        itemHeight = 84f,
        gap = 24f,
        iconSize = 44f,
        emptyWidth = width,
        adaptiveWidth = true,
        minItemWidth = 258f,
        maxItemWidth = 350f,
        horizontalPadding = 30f,
        titleBaselineRatio = 0.43f,
        subtitleBaselineRatio = 0.76f,
        textAlign = Paint.Align.CENTER,
        colors = colors,
    )
}

private fun drawBadgesPoster(canvas: Canvas, badges: List<Badge>, strings: LumaStrings, left: Float, top: Float, colors: ShareColors) {
    drawBadgeCards(
        canvas = canvas,
        badges = badges,
        strings = strings,
        left = left,
        top = top,
        itemWidth = 390f,
        itemHeight = 116f,
        gap = 35f,
        iconSize = 64f,
        emptyWidth = 1240f,
        adaptiveWidth = true,
        minItemWidth = 286f,
        maxItemWidth = 390f,
        horizontalPadding = 30f,
        titleBaselineRatio = 0.43f,
        subtitleBaselineRatio = 0.76f,
        textAlign = Paint.Align.CENTER,
        colors = colors,
    )
}

private fun drawBadgesZen(canvas: Canvas, badges: List<Badge>, strings: LumaStrings, left: Float, top: Float, colors: ShareColors) {
    drawBadgeCards(
        canvas = canvas,
        badges = badges,
        strings = strings,
        left = left,
        top = top,
        itemWidth = 306f,
        itemHeight = 110f,
        gap = 37f,
        iconSize = 58f,
        emptyWidth = 992f,
        adaptiveWidth = true,
        minItemWidth = 270f,
        maxItemWidth = 306f,
        horizontalPadding = 30f,
        titleBaselineRatio = 0.43f,
        subtitleBaselineRatio = 0.76f,
        textAlign = Paint.Align.CENTER,
        colors = colors,
    )
}

private fun drawBadgesDashboard(canvas: Canvas, badges: List<Badge>, strings: LumaStrings, left: Float, top: Float, colors: ShareColors) {
    drawBadgeCards(
        canvas = canvas,
        badges = badges,
        strings = strings,
        left = left,
        top = top,
        itemWidth = 360f,
        itemHeight = 98f,
        gap = 32f,
        iconSize = 54f,
        emptyWidth = 1144f,
        adaptiveWidth = true,
        minItemWidth = 286f,
        maxItemWidth = 360f,
        horizontalPadding = 30f,
        titleBaselineRatio = 0.43f,
        subtitleBaselineRatio = 0.76f,
        textAlign = Paint.Align.CENTER,
        colors = colors,
    )
}

private fun drawBadgeCards(
    canvas: Canvas,
    badges: List<Badge>,
    strings: LumaStrings,
    left: Float,
    top: Float,
    itemWidth: Float,
    itemHeight: Float,
    gap: Float,
    iconSize: Float,
    emptyWidth: Float,
    adaptiveWidth: Boolean = false,
    minItemWidth: Float = itemWidth,
    maxItemWidth: Float = itemWidth,
    horizontalPadding: Float = 24f,
    titleBaselineRatio: Float = 0.43f,
    subtitleBaselineRatio: Float = 0.71f,
    textAlign: Paint.Align = Paint.Align.LEFT,
    colors: ShareColors,
) {
    if (badges.isEmpty()) {
        drawSoftCard(canvas, RectF(left, top, left + emptyWidth, top + itemHeight), 18f, withAlpha(Color.WHITE, 0.78f), withAlpha(colors.primary, 0.10f), 0.02f)
        drawFittedText(
            canvas = canvas,
            value = strings.t("noEarnedBadges"),
            x = left + emptyWidth / 2f,
            y = top + itemHeight * 0.58f,
            maxWidth = emptyWidth - 80f,
            size = 24f,
            minSize = 18f,
            color = colors.muted,
            typefaceStyle = Typeface.BOLD,
            align = Paint.Align.CENTER,
        )
        return
    }

    val titleMeasurePaint = textPaint(25f, colors.text, Typeface.BOLD)
    val subtitleMeasurePaint = textPaint(19f, colors.muted, Typeface.NORMAL)
    var x = left
    badges.forEach { badge ->
        val measuredWidth = horizontalPadding + iconSize + 26f +
            max(titleMeasurePaint.measureText(strings.badgeTitle(badge)), subtitleMeasurePaint.measureText(badgeSubtitle(badge, strings))) +
            horizontalPadding
        val cardWidth = if (adaptiveWidth) measuredWidth.coerceIn(minItemWidth, maxItemWidth) else itemWidth
        drawSoftCard(canvas, RectF(x, top, x + cardWidth, top + itemHeight), 18f, withAlpha(Color.WHITE, 0.80f), withAlpha(colors.primary, 0.11f), 0.02f)
        val iconLeft = x + horizontalPadding
        val iconTop = top + (itemHeight - iconSize) / 2f
        drawBadgeIcon(canvas, badge, iconLeft, iconTop, iconSize, colors)
        val textLeft = iconLeft + iconSize + 26f
        val textRight = x + cardWidth - horizontalPadding
        val textX = if (textAlign == Paint.Align.CENTER) (textLeft + textRight) / 2f else textLeft
        val maxTextWidth = textRight - textLeft
        drawFittedText(canvas, strings.badgeTitle(badge), textX, top + itemHeight * titleBaselineRatio, maxTextWidth, 25f, 17f, colors.text, Typeface.BOLD, textAlign)
        drawFittedText(canvas, badgeSubtitle(badge, strings), textX, top + itemHeight * subtitleBaselineRatio, maxTextWidth, 19f, 14f, colors.muted, Typeface.NORMAL, textAlign)
        x += cardWidth + gap
    }
}

private fun drawCircleBadge(canvas: Canvas, badge: Badge, left: Float, top: Float, size: Float, colors: ShareColors) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = withAlpha(colors.primary, 0.06f)
        style = Paint.Style.FILL
    }
    canvas.drawCircle(left + size / 2f, top + size / 2f, size / 2f, paint)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 2f
    paint.color = withAlpha(colors.primary, 0.15f)
    canvas.drawCircle(left + size / 2f, top + size / 2f, size / 2f - 1f, paint)
    drawBadgeIcon(canvas, badge, left + size * 0.22f, top + size * 0.22f, size * 0.56f, colors)
}

private fun drawBadgeIcon(canvas: Canvas, badge: Badge, left: Float, top: Float, size: Float, colors: ShareColors) {
    val accent = when (badge.level) {
        "gold" -> Color.rgb(250, 204, 21)
        "silver" -> Color.rgb(203, 213, 225)
        else -> Color.rgb(217, 154, 91)
    }
    val green = colors.primary
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = size * 0.09f
        strokeCap = Paint.Cap.ROUND
        color = accent
    }
    val centerX = left + size / 2f
    val centerY = top + size / 2f

    when (badge.id) {
        "week_streak", "seven_day_runner" -> {
            canvas.drawArc(RectF(left + size * 0.15f, top + size * 0.18f, left + size * 0.85f, top + size * 0.85f), 190f, 160f, false, paint)
            paint.style = Paint.Style.FILL
            repeat(5) { index ->
                paint.color = if (index % 2 == 0) green else accent
                canvas.drawCircle(left + size * 0.20f + index * size * 0.14f, top + size * 0.72f - min(index, 2) * size * 0.07f, size * 0.075f, paint)
            }
        }
        "month_streak", "thirty_day_runner" -> {
            canvas.drawCircle(centerX, centerY, size * 0.28f, paint)
            paint.color = green
            paint.strokeWidth = size * 0.06f
            canvas.drawCircle(centerX, centerY, size * 0.16f, paint)
        }
        "hundred_lights", "hundred_total_lights", "three_habits_lit" -> {
            paint.style = Paint.Style.FILL
            repeat(3) { x ->
                repeat(3) { y ->
                    paint.color = listOf(green, accent, Color.rgb(132, 204, 22))[(x + y) % 3]
                    val cellLeft = left + size * 0.24f + x * size * 0.20f
                    val cellTop = top + size * 0.24f + y * size * 0.20f
                    canvas.drawRoundRect(RectF(cellLeft, cellTop, cellLeft + size * 0.13f, cellTop + size * 0.13f), size * 0.04f, size * 0.04f, paint)
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
            paint.strokeWidth = size * 0.045f
            canvas.drawPath(path, paint)
        }
        else -> {
            canvas.drawCircle(centerX, centerY, size * 0.33f, paint)
            paint.color = green
            paint.strokeWidth = size * 0.07f
            canvas.drawCircle(centerX, centerY, size * 0.19f, paint)
            paint.style = Paint.Style.FILL
            canvas.drawCircle(centerX, centerY, size * 0.11f, paint)
        }
    }
}

private fun badgeSubtitle(badge: Badge, strings: LumaStrings): String {
    val zh = isChinese(strings)
    return when (badge.id) {
        "first_light", "first_habit_light" -> if (zh) "点亮第一天" else "First day lit"
        "week_streak", "seven_day_runner" -> if (zh) "连续点亮 7 天" else "7-day streak"
        "month_streak", "thirty_day_runner" -> if (zh) "连续点亮 30 天" else "30-day streak"
        "hundred_lights", "hundred_total_lights" -> if (zh) "累计记录 100 次" else "100 total records"
        "three_habits_lit" -> if (zh) "三条习惯并进" else "Three active habits"
        "steady_flow" -> if (zh) "完成率达到 80%" else "80% completion"
        else -> badge.description
    }
}

private fun drawIconTile(
    canvas: Canvas,
    context: Context,
    iconKey: String,
    rect: RectF,
    radius: Float,
    colors: ShareColors,
    transparentTile: Boolean = false,
) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    if (!transparentTile) {
        paint.shader = RadialGradient(
            rect.centerX(),
            rect.top + rect.height() * 0.22f,
            rect.width() * 0.82f,
            intArrayOf(withAlpha(colors.primary, 0.15f), withAlpha(colors.primary, 0.06f), Color.rgb(246, 250, 246)),
            floatArrayOf(0f, 0.62f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = withAlpha(colors.primary, 0.08f)
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.style = Paint.Style.FILL
    }

    val drawable = ContextCompat.getDrawable(context, lumaIconDrawableFor(iconKey))?.mutate()
    if (drawable != null) {
        drawable.setTint(colors.primary)
        val padding = rect.width() * if (transparentTile) 0.10f else 0.20f
        drawable.setBounds(
            (rect.left + padding).toInt(),
            (rect.top + padding).toInt(),
            (rect.right - padding).toInt(),
            (rect.bottom - padding).toInt(),
        )
        drawable.draw(canvas)
    }
}

private fun drawMetaLine(
    canvas: Canvas,
    entry: DashboardItem,
    strings: LumaStrings,
    x: Float,
    y: Float,
    maxWidth: Float,
    size: Float,
    colors: ShareColors,
    highlightStreak: Boolean = false,
) {
    val category = strings.categoryName(entry.category?.name ?: strings.t("uncategorized"))
    val streak = strings.t("streakDays", "count" to entry.stats.currentStreak.toString())
    val categoryPaint = textPaint(size, colors.primaryDark, Typeface.BOLD)
    val mutedPaint = textPaint(size, colors.muted, Typeface.BOLD)

    var drawCategory = category
    while (drawCategory.length > 1 && categoryPaint.measureText("$drawCategory / $streak") > maxWidth) {
        drawCategory = drawCategory.dropLast(1)
    }
    val categoryText = if (drawCategory == category) category else "$drawCategory..."
    canvas.drawText(categoryText, x, y, categoryPaint)
    val slashX = x + categoryPaint.measureText(categoryText)
    canvas.drawText(" / ", slashX, y, mutedPaint)
    drawFittedText(canvas, streak, slashX + mutedPaint.measureText(" / "), y, maxWidth - (slashX - x), size, size - 7f, if (highlightStreak) colors.primaryDark else colors.muted, Typeface.BOLD)
}

private fun drawBrand(
    canvas: Canvas,
    x: Float,
    y: Float,
    strings: LumaStrings,
    colors: ShareColors,
    size: Float,
    mark: Boolean,
    accentBrand: Boolean,
) {
    var textX = x
    if (mark) {
        drawBrandMark(canvas, x, y - size + 2f, size * 1.55f, colors)
        textX += size * 2.05f
    }
    val brandColor = if (accentBrand) colors.primaryDark else colors.text
    val brandPaint = textPaint(size, brandColor, Typeface.BOLD)
    val slashPaint = textPaint(size, colors.muted, Typeface.BOLD)
    canvas.drawText("LumaLog", textX, y, brandPaint)
    val slashX = textX + brandPaint.measureText("LumaLog")
    canvas.drawText(" / ${strings.t("brandTagline")}", slashX + size * 0.35f, y, slashPaint)
}

private fun drawBrandCentered(
    canvas: Canvas,
    centerX: Float,
    y: Float,
    strings: LumaStrings,
    colors: ShareColors,
    size: Float,
    accentBrand: Boolean,
) {
    val brandColor = if (accentBrand) colors.primaryDark else colors.text
    val brandPaint = textPaint(size, brandColor, Typeface.BOLD)
    val slashPaint = textPaint(size, colors.muted, Typeface.BOLD)
    val brand = "LumaLog"
    val suffix = " / ${strings.t("brandTagline")}"
    val totalWidth = brandPaint.measureText(brand) + size * 0.35f + slashPaint.measureText(suffix)
    val startX = centerX - totalWidth / 2f
    canvas.drawText(brand, startX, y, brandPaint)
    canvas.drawText(suffix, startX + brandPaint.measureText(brand) + size * 0.35f, y, slashPaint)
}

private fun drawBrandMark(canvas: Canvas, left: Float, top: Float, size: Float, colors: ShareColors) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val rect = RectF(left, top, left + size, top + size)
    paint.shader = LinearGradient(left, top, left + size, top + size, colors.primaryDark, colors.primary, Shader.TileMode.CLAMP)
    canvas.drawRoundRect(rect, size * 0.22f, size * 0.22f, paint)
    paint.shader = null
    paint.color = Color.WHITE
    val gap = size * 0.08f
    val cell = size * 0.14f
    repeat(3) { x ->
        repeat(3) { y ->
            val cellLeft = left + size * 0.24f + x * (cell + gap)
            val cellTop = top + size * 0.22f + y * (cell + gap)
            canvas.drawRoundRect(RectF(cellLeft, cellTop, cellLeft + cell, cellTop + cell), cell * 0.34f, cell * 0.34f, paint)
        }
    }
}

private fun drawSoftCard(canvas: Canvas, rect: RectF, radius: Float, fill: Int, stroke: Int, shadowAlpha: Float) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    if (shadowAlpha > 0f) {
        repeat(5) { index ->
            val spread = (5 - index) * 3f
            val shadow = RectF(rect.left - spread, rect.top - spread + 7f, rect.right + spread, rect.bottom + spread + 7f)
            paint.color = withAlpha(Color.rgb(15, 23, 42), shadowAlpha * (index + 1) / 22f)
            canvas.drawRoundRect(shadow, radius + spread, radius + spread, paint)
        }
    }
    paint.color = fill
    paint.style = Paint.Style.FILL
    canvas.drawRoundRect(rect, radius, radius, paint)
    paint.color = stroke
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 1.5f
    canvas.drawRoundRect(rect, radius, radius, paint)
    paint.style = Paint.Style.FILL
}

private fun clipRoundRect(canvas: Canvas, rect: RectF, radius: Float) {
    val path = Path().apply { addRoundRect(rect, radius, radius, Path.Direction.CW) }
    canvas.clipPath(path)
}

private fun drawDottedField(canvas: Canvas, rect: RectF, primary: Int) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = withAlpha(primary, 0.045f) }
    var y = rect.top + 12f
    while (y < rect.bottom - 12f) {
        var x = rect.left + 12f
        while (x < rect.right - 12f) {
            canvas.drawCircle(x, y, 1.1f, paint)
            x += 12f
        }
        y += 12f
    }
}

private fun drawPaperTexture(canvas: Canvas, rect: RectF, primary: Int) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = withAlpha(primary, 0.025f)
        strokeWidth = 1f
    }
    var y = rect.top + 26f
    while (y < rect.bottom - 40f) {
        canvas.drawLine(rect.left + 30f, y, rect.right - 30f, y + 6f, paint)
        y += 32f
    }
}

private fun drawWatercolorMountains(canvas: Canvas, rect: RectF, primary: Int) {
    val left = rect.left
    val right = rect.right
    val bottom = rect.bottom + 1f
    val width = right - left
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.shader = LinearGradient(
        0f,
        bottom - 238f,
        0f,
        bottom,
        intArrayOf(withAlpha(primary, 0.0f), withAlpha(primary, 0.055f), withAlpha(primary, 0.09f)),
        floatArrayOf(0f, 0.58f, 1f),
        Shader.TileMode.CLAMP,
    )
    val mist = Path().apply {
        moveTo(left, bottom - 120f)
        cubicTo(left + width * 0.14f, bottom - 154f, left + width * 0.26f, bottom - 108f, left + width * 0.38f, bottom - 140f)
        cubicTo(left + width * 0.52f, bottom - 174f, left + width * 0.64f, bottom - 116f, left + width * 0.78f, bottom - 150f)
        cubicTo(left + width * 0.88f, bottom - 172f, left + width * 0.94f, bottom - 154f, right, bottom - 180f)
        lineTo(right, bottom)
        lineTo(left, bottom)
        close()
    }
    canvas.drawPath(mist, paint)

    paint.shader = LinearGradient(
        0f,
        bottom - 178f,
        0f,
        bottom,
        withAlpha(primary, 0.015f),
        withAlpha(primary, 0.11f),
        Shader.TileMode.CLAMP,
    )
    val shore = Path().apply {
        moveTo(left, bottom - 80f)
        cubicTo(left + width * 0.15f, bottom - 126f, left + width * 0.27f, bottom - 78f, left + width * 0.39f, bottom - 112f)
        cubicTo(left + width * 0.52f, bottom - 146f, left + width * 0.65f, bottom - 76f, left + width * 0.78f, bottom - 122f)
        cubicTo(left + width * 0.88f, bottom - 156f, left + width * 0.94f, bottom - 124f, right, bottom - 150f)
        lineTo(right, bottom)
        lineTo(left, bottom)
        close()
    }
    canvas.drawPath(shore, paint)
    paint.shader = null

    val ridgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = withAlpha(Color.rgb(83, 128, 96), 0.11f)
        style = Paint.Style.STROKE
        strokeWidth = 2.4f
        strokeCap = Paint.Cap.ROUND
    }
    listOf(bottom - 130f, bottom - 78f).forEachIndexed { index, baseline ->
        val ridge = Path().apply {
            moveTo(left, baseline)
            cubicTo(left + width * 0.14f, baseline - 28f, left + width * 0.27f, baseline + 12f, left + width * 0.40f, baseline - 18f)
            cubicTo(left + width * 0.55f, baseline - 54f, left + width * 0.66f, baseline + 16f, left + width * 0.79f, baseline - 16f)
            cubicTo(left + width * 0.90f, baseline - 34f, left + width * 0.95f, baseline - 18f, right, baseline - 36f)
        }
        ridgePaint.alpha = ((0.105f - index * 0.03f) * 255).roundToInt()
        canvas.drawPath(ridge, ridgePaint)
    }

    paint.style = Paint.Style.FILL
    paint.color = withAlpha(Color.WHITE, 0.34f)
    canvas.drawOval(RectF(left + width * 0.08f, bottom - 176f, right - width * 0.08f, bottom - 106f), paint)
    paint.color = withAlpha(primary, 0.035f)
    canvas.drawOval(RectF(left - width * 0.03f, bottom - 100f, right + width * 0.03f, bottom), paint)
}

private fun drawFloatingLeaves(canvas: Canvas, primary: Int) {
    drawLeaf(canvas, 370f, 200f, 34f, 12f, 12f, withAlpha(primary, 0.34f))
    drawLeaf(canvas, 438f, 255f, 42f, 16f, 35f, withAlpha(primary, 0.20f))
    drawLeaf(canvas, 775f, 239f, 32f, 12f, -10f, withAlpha(primary, 0.25f))
    drawLeaf(canvas, 856f, 202f, 26f, 10f, -32f, withAlpha(primary, 0.26f))
    drawLeaf(canvas, 930f, 134f, 20f, 8f, 0f, withAlpha(primary, 0.18f))
}

private fun drawSproutWatermark(canvas: Canvas, cx: Float, cy: Float, size: Float, primary: Int) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = withAlpha(primary, 0.08f)
        style = Paint.Style.STROKE
        strokeWidth = 18f
        strokeCap = Paint.Cap.ROUND
    }
    val stem = Path().apply {
        moveTo(cx + 50f, cy + 120f)
        cubicTo(cx + 55f, cy + 45f, cx + 12f, cy - 30f, cx - 8f, cy - 90f)
    }
    canvas.drawPath(stem, paint)
    drawLeaf(canvas, cx - 65f, cy - 70f, size * 1.1f, size * 0.42f, 28f, withAlpha(primary, 0.07f))
    drawLeaf(canvas, cx + 65f, cy - 102f, size * 1.15f, size * 0.42f, -38f, withAlpha(primary, 0.07f))
    drawLeaf(canvas, cx - 8f, cy + 44f, size * 1.7f, size * 0.42f, -6f, withAlpha(primary, 0.065f))
}

private fun drawLeaf(canvas: Canvas, cx: Float, cy: Float, width: Float, height: Float, angle: Float, color: Int) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
    canvas.save()
    canvas.rotate(angle, cx, cy)
    val path = Path().apply {
        moveTo(cx - width / 2f, cy)
        cubicTo(cx - width * 0.20f, cy - height, cx + width * 0.28f, cy - height, cx + width / 2f, cy)
        cubicTo(cx + width * 0.25f, cy + height, cx - width * 0.20f, cy + height, cx - width / 2f, cy)
        close()
    }
    canvas.drawPath(path, paint)
    paint.color = withAlpha(Color.rgb(63, 114, 73), 0.16f)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 1.4f
    canvas.drawLine(cx - width * 0.22f, cy, cx + width * 0.26f, cy, paint)
    canvas.restore()
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

private fun isChinese(strings: LumaStrings): Boolean =
    strings.weekdayLabels().firstOrNull() == "日"

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

private fun heatmapColor(theme: String, level: Int, emptyColor: Int, saturated: Boolean): Int {
    val base = themeColor(theme)
    return when (level) {
        1 -> blend(emptyColor, base, if (saturated) 0.34f else 0.28f)
        2 -> blend(emptyColor, base, if (saturated) 0.56f else 0.48f)
        3 -> blend(emptyColor, base, if (saturated) 0.78f else 0.72f)
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

private fun withAlpha(color: Int, alpha: Float): Int =
    Color.argb((alpha.coerceIn(0f, 1f) * 255).roundToInt(), Color.red(color), Color.green(color), Color.blue(color))
