package com.example.lumalogapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumalogapp.data.Badge
import com.example.lumalogapp.ui.i18n.LumaStrings
import com.example.lumalogapp.ui.utils.colorThemes
import com.example.lumalogapp.ui.utils.themeColor

@Composable
fun TopTitle(title: String, subtitle: String, trailing: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        trailing()
    }
}

@Composable
fun FormPanel(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> PreferenceChips(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (value, label) ->
                FilterChip(selected = selected == value, onClick = { onSelect(value) }, label = { Text(label) })
            }
        }
    }
}

@Composable
fun SwitchRow(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorThemePicker(selected: String, strings: LumaStrings, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        colorThemes.forEach { theme ->
            FilterChip(
                selected = selected == theme,
                onClick = { onSelect(theme) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(themeColor(theme)),
                        )
                        Text(strings.colorName(theme))
                    }
                },
            )
        }
    }
}

@Composable
fun LumaLogo() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Canvas(modifier = Modifier.size(58.dp)) {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF172033),
                        Color(0xFF101827),
                        Color(0xFF0B111B),
                    ),
                ),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x6686EFAC), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.72f, size.height * 0.24f),
                    radius = size.width * 0.72f,
                ),
                center = androidx.compose.ui.geometry.Offset(size.width * 0.72f, size.height * 0.24f),
                radius = size.width * 0.72f,
            )

            val gridLeft = 10.dp.toPx()
            val gridTop = 8.dp.toPx()
            val gap = 2.1.dp.toPx()
            val cell = 5.dp.toPx()
            val lit = mapOf(
                36 to Color(0xFF14B8A6),
                30 to Color(0x9922C55E),
                25 to Color(0xCC22C55E),
                26 to Color(0xFF4ADE80),
                20 to Color(0xB822C55E),
                15 to Color(0xFF4ADE80),
                10 to Color(0xDD22C55E),
                5 to Color(0xFFBBF7D0),
                11 to Color(0xDD4ADE80),
                41 to Color(0xB322C55E),
            )
            repeat(6) { x ->
                repeat(7) { y ->
                    val index = y * 6 + x
                    drawRoundRect(
                        color = lit[index] ?: Color(0xFF263244),
                        topLeft = androidx.compose.ui.geometry.Offset(
                            gridLeft + x * (cell + gap),
                            gridTop + y * (cell + gap),
                        ),
                        size = androidx.compose.ui.geometry.Size(cell, cell),
                        cornerRadius = CornerRadius(2.5.dp.toPx()),
                    )
                }
            }

            val streak = Path().apply {
                moveTo(gridLeft + cell * 0.5f, gridTop + (cell + gap) * 6 + cell * 0.5f)
                cubicTo(
                    gridLeft + (cell + gap) * 1.2f,
                    gridTop + (cell + gap) * 5.7f,
                    gridLeft + (cell + gap) * 2.2f,
                    gridTop + (cell + gap) * 3.7f,
                    gridLeft + (cell + gap) * 3.2f,
                    gridTop + (cell + gap) * 3.2f,
                )
                cubicTo(
                    gridLeft + (cell + gap) * 4.1f,
                    gridTop + (cell + gap) * 2.7f,
                    gridLeft + (cell + gap) * 4.3f,
                    gridTop + (cell + gap) * 1.1f,
                    gridLeft + (cell + gap) * 5 + cell * 0.5f,
                    gridTop + cell * 0.5f,
                )
            }
            drawPath(
                path = streak,
                brush = Brush.linearGradient(listOf(Color(0xFF14B8A6), Color(0xFFBBF7D0))),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
            )
            val sparkCenter = androidx.compose.ui.geometry.Offset(
                gridLeft + (cell + gap) * 5 + cell * 0.5f,
                gridTop + cell * 0.5f,
            )
            drawCircle(color = Color(0xFFDCFCE7), radius = 3.4.dp.toPx(), center = sparkCenter)
            drawCircle(color = Color(0xFF16A34A), radius = 1.5.dp.toPx(), center = sparkCenter)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                buildAnnotatedString {
                    append("Luma")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append("Log") }
                },
                fontSize = 28.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                listOf(Color(0xFF14B8A6), Color(0xFF22C55E), Color(0xFFBBF7D0)).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(color),
                    )
                }
                Text(
                    text = "HABIT HEATMAP",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.3.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun RoundIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = CircleShape,
        border = null,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        modifier = Modifier.size(42.dp),
    ) {
        content()
    }
}

@Composable
fun AchievementBadge(badge: Badge, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(76.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        BadgeIcon(badge = badge, modifier = Modifier.size(54.dp))
        Text(
            text = badge.title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun BadgeIcon(badge: Badge, modifier: Modifier = Modifier) {
    val accent = when (badge.level) {
        "gold" -> Color(0xFFFACC15)
        "silver" -> Color(0xFFCBD5E1)
        else -> Color(0xFFD99A5B)
    }
    val green = Color(0xFF22C55E)
    Canvas(modifier = modifier) {
        drawRoundRect(
            brush = Brush.linearGradient(listOf(Color(0xFF172033), Color(0xFF101827))),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
        )
        drawRoundRect(
            color = accent.copy(alpha = 0.18f),
            topLeft = androidx.compose.ui.geometry.Offset(5.dp.toPx(), 5.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(size.width - 10.dp.toPx(), size.height - 10.dp.toPx()),
            cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx()),
        )

        when (badge.id) {
            "week_streak", "seven_day_runner" -> {
                drawArc(
                    color = accent,
                    startAngle = 190f,
                    sweepAngle = 160f,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.68f, size.height * 0.56f),
                    topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.16f, size.height * 0.22f),
                )
                repeat(5) { index ->
                    drawCircle(
                        color = if (index % 2 == 0) green else accent,
                        radius = 3.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(size.width * (0.22f + index * 0.14f), size.height * (0.68f - index.coerceAtMost(2) * 0.08f)),
                    )
                }
            }
            "month_streak", "thirty_day_runner" -> {
                drawCircle(color = accent, radius = size.width * 0.27f, center = center, style = Stroke(width = 4.dp.toPx()))
                drawCircle(color = green, radius = size.width * 0.16f, center = center, style = Stroke(width = 2.dp.toPx()))
            }
            "hundred_lights", "hundred_total_lights", "three_habits_lit" -> {
                repeat(3) { x ->
                    repeat(3) { y ->
                        drawRoundRect(
                            color = listOf(green, accent, Color(0xFF84CC16))[(x + y) % 3],
                            topLeft = androidx.compose.ui.geometry.Offset(15.dp.toPx() + x * 10.dp.toPx(), 15.dp.toPx() + y * 10.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(7.dp.toPx(), 7.dp.toPx()),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                        )
                    }
                }
            }
            "steady_flow" -> {
                val path = Path().apply {
                    moveTo(size.width * 0.16f, size.height * 0.58f)
                    cubicTo(size.width * 0.32f, size.height * 0.22f, size.width * 0.46f, size.height * 0.22f, size.width * 0.58f, size.height * 0.58f)
                    cubicTo(size.width * 0.70f, size.height * 0.92f, size.width * 0.84f, size.height * 0.92f, size.width * 0.92f, size.height * 0.58f)
                }
                drawPath(path = path, color = accent, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
                drawPath(path = path, color = green, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            }
            else -> {
                drawCircle(color = accent, radius = size.width * 0.26f, center = center, style = Stroke(width = 4.dp.toPx()))
                drawCircle(color = green, radius = size.width * 0.14f, center = center)
            }
        }
    }
}
