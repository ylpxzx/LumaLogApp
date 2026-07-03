package com.example.lumalogapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.lumalogapp.ui.i18n.LumaStrings
import com.example.lumalogapp.ui.utils.themeColor

data class LumaIconOption(
    val key: String,
    val theme: String,
)

val lumaIconOptions = listOf(
    LumaIconOption("rocket", "green"),
    LumaIconOption("broken_heart", "red"),
    LumaIconOption("shirt", "teal"),
    LumaIconOption("game", "purple"),
    LumaIconOption("briefcase", "green"),
)

private fun lumaIconOption(key: String): LumaIconOption {
    return lumaIconOptions.firstOrNull { it.key == key } ?: lumaIconOptions.last()
}

@Composable
fun LumaIconBadge(
    iconKey: String,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    selected: Boolean = false,
) {
    val option = lumaIconOption(iconKey)
    val color = themeColor(option.theme)
    val isDark = MaterialTheme.colorScheme.background == Color(0xFF0C1118)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = if (isDark) 0.18f else 0.12f))
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) color.copy(alpha = 0.72f) else color.copy(alpha = if (isDark) 0.30f else 0.18f),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        LumaIconGlyph(
            key = option.key,
            color = color,
            modifier = Modifier.size(size * 0.58f),
        )
    }
}

@Composable
private fun LumaIconGlyph(key: String, color: Color, modifier: Modifier = Modifier) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = w * 0.105f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        when (key) {
            "rocket" -> {
                val body = Path().apply {
                    moveTo(w * 0.34f, h * 0.66f)
                    cubicTo(w * 0.42f, h * 0.34f, w * 0.62f, h * 0.18f, w * 0.78f, h * 0.16f)
                    cubicTo(w * 0.78f, h * 0.34f, w * 0.64f, h * 0.56f, w * 0.36f, h * 0.68f)
                    close()
                }
                drawPath(body, color.copy(alpha = 0.18f))
                drawPath(body, color, style = stroke)
                drawCircle(color, radius = w * 0.055f, center = Offset(w * 0.62f, h * 0.35f))
                drawLine(color, Offset(w * 0.34f, h * 0.66f), Offset(w * 0.21f, h * 0.79f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(w * 0.42f, h * 0.72f), Offset(w * 0.37f, h * 0.87f), stroke.width, StrokeCap.Round)
            }

            "broken_heart" -> {
                val heart = Path().apply {
                    moveTo(w * 0.50f, h * 0.78f)
                    cubicTo(w * 0.18f, h * 0.57f, w * 0.15f, h * 0.29f, w * 0.35f, h * 0.24f)
                    cubicTo(w * 0.45f, h * 0.21f, w * 0.50f, h * 0.28f, w * 0.54f, h * 0.35f)
                    cubicTo(w * 0.60f, h * 0.24f, w * 0.72f, h * 0.20f, w * 0.83f, h * 0.31f)
                    cubicTo(w * 0.98f, h * 0.48f, w * 0.78f, h * 0.65f, w * 0.50f, h * 0.78f)
                    close()
                }
                drawPath(heart, color.copy(alpha = 0.92f))
                val crack = Path().apply {
                    moveTo(w * 0.55f, h * 0.36f)
                    lineTo(w * 0.46f, h * 0.49f)
                    lineTo(w * 0.56f, h * 0.55f)
                    lineTo(w * 0.48f, h * 0.72f)
                }
                drawPath(crack, surfaceColor.copy(alpha = 0.88f), style = Stroke(width = w * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }

            "shirt" -> {
                val shirt = Path().apply {
                    moveTo(w * 0.25f, h * 0.28f)
                    lineTo(w * 0.38f, h * 0.21f)
                    quadraticBezierTo(w * 0.50f, h * 0.34f, w * 0.62f, h * 0.21f)
                    lineTo(w * 0.75f, h * 0.28f)
                    lineTo(w * 0.82f, h * 0.44f)
                    lineTo(w * 0.69f, h * 0.50f)
                    lineTo(w * 0.69f, h * 0.79f)
                    lineTo(w * 0.31f, h * 0.79f)
                    lineTo(w * 0.31f, h * 0.50f)
                    lineTo(w * 0.18f, h * 0.44f)
                    close()
                }
                drawPath(shirt, color.copy(alpha = 0.18f))
                drawPath(shirt, color, style = stroke)
                drawLine(color, Offset(w * 0.42f, h * 0.25f), Offset(w * 0.58f, h * 0.25f), stroke.width * 0.75f, StrokeCap.Round)
            }

            "game" -> {
                drawRoundRect(
                    color = color.copy(alpha = 0.18f),
                    topLeft = Offset(w * 0.15f, h * 0.34f),
                    size = Size(w * 0.70f, h * 0.38f),
                    cornerRadius = CornerRadius(w * 0.18f, w * 0.18f),
                )
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.15f, h * 0.34f),
                    size = Size(w * 0.70f, h * 0.38f),
                    cornerRadius = CornerRadius(w * 0.18f, w * 0.18f),
                    style = stroke,
                )
                drawLine(color, Offset(w * 0.31f, h * 0.48f), Offset(w * 0.31f, h * 0.60f), stroke.width * 0.72f, StrokeCap.Round)
                drawLine(color, Offset(w * 0.25f, h * 0.54f), Offset(w * 0.37f, h * 0.54f), stroke.width * 0.72f, StrokeCap.Round)
                drawCircle(color, radius = w * 0.035f, center = Offset(w * 0.64f, h * 0.50f))
                drawCircle(color, radius = w * 0.035f, center = Offset(w * 0.73f, h * 0.57f))
            }

            else -> {
                drawRoundRect(
                    color = color.copy(alpha = 0.18f),
                    topLeft = Offset(w * 0.18f, h * 0.36f),
                    size = Size(w * 0.64f, h * 0.42f),
                    cornerRadius = CornerRadius(w * 0.09f, w * 0.09f),
                )
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.18f, h * 0.36f),
                    size = Size(w * 0.64f, h * 0.42f),
                    cornerRadius = CornerRadius(w * 0.09f, w * 0.09f),
                    style = stroke,
                )
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.39f, h * 0.22f),
                    size = Size(w * 0.22f, h * 0.18f),
                    cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
                    style = stroke,
                )
                drawLine(color, Offset(w * 0.20f, h * 0.51f), Offset(w * 0.80f, h * 0.51f), stroke.width * 0.72f, StrokeCap.Round)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun LumaIconPicker(
    selectedKey: String,
    strings: LumaStrings,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(strings.t("icon"), fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            lumaIconOptions.forEach { option ->
                val selected = selectedKey == option.key
                val color = themeColor(option.theme)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f))
                        .border(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) color.copy(alpha = 0.70f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(16.dp),
                        )
                        .combinedClickable(onClick = { onSelect(option.key) }),
                    contentAlignment = Alignment.Center,
                ) {
                    LumaIconBadge(
                        iconKey = option.key,
                        size = 31.dp,
                    )
                }
            }
        }
    }
}
