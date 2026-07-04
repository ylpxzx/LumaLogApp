package com.example.lumalogapp.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.lumalogapp.R
import com.example.lumalogapp.ui.i18n.LumaStrings
import com.example.lumalogapp.ui.utils.themeColor

const val DefaultLumaIconKey = "chuangye"

data class LumaIconOption(
    val key: String,
    val theme: String,
    val drawableRes: Int,
)

val lumaIconOptions = listOf(
    LumaIconOption("chuangye", "green", R.drawable.ic_luma_chuangye),
    LumaIconOption("xinsui", "red", R.drawable.ic_luma_xinsui),
    LumaIconOption("jianshen", "orange", R.drawable.ic_luma_jianshen),
    LumaIconOption("paobu1", "green", R.drawable.ic_luma_paobu1),
    LumaIconOption("yuedu", "teal", R.drawable.ic_luma_yuedu),
    LumaIconOption("daima", "purple", R.drawable.ic_luma_daima),
    LumaIconOption("youxi", "purple", R.drawable.ic_luma_youxi),
    LumaIconOption("naozhong", "orange", R.drawable.ic_luma_naozhong),
    LumaIconOption("jinqian", "green", R.drawable.ic_luma_jinqian),
    LumaIconOption("kafei", "orange", R.drawable.ic_luma_kafei),
    LumaIconOption("youyong", "teal", R.drawable.ic_luma_youyong),
    LumaIconOption("lanqiu", "orange", R.drawable.ic_luma_lanqiu),
    LumaIconOption("kezuofan", "orange", R.drawable.ic_luma_kezuofan),
    LumaIconOption("naichaxiaochi", "orange", R.drawable.ic_luma_naichaxiaochi),
    LumaIconOption("lingshi", "orange", R.drawable.ic_luma_lingshi),
    LumaIconOption("jucan", "orange", R.drawable.ic_luma_jucan),
    LumaIconOption("jingchanghejiu", "red", R.drawable.ic_luma_jingchanghejiu),
    LumaIconOption("smoking", "red", R.drawable.ic_luma_smoking),
    LumaIconOption("bushengbing", "green", R.drawable.ic_luma_bushengbing),
    LumaIconOption("yiyuan", "green", R.drawable.ic_luma_yiyuan),
    LumaIconOption("meirong", "pink", R.drawable.ic_luma_meirong),
    LumaIconOption("meirong_copy", "pink", R.drawable.ic_luma_meirong_copy),
    LumaIconOption("aiqingyuehui", "pink", R.drawable.ic_luma_aiqingyuehui),
    LumaIconOption("shejiao", "blue", R.drawable.ic_luma_shejiao),
    LumaIconOption("tongxun", "blue", R.drawable.ic_luma_tongxun),
    LumaIconOption("lvyou", "teal", R.drawable.ic_luma_lvyou),
    LumaIconOption("qiche", "blue", R.drawable.ic_luma_qiche),
    LumaIconOption("zhufang", "blue", R.drawable.ic_luma_zhufang),
    LumaIconOption("weixiuoff", "gray", R.drawable.ic_luma_weixiuoff),
    LumaIconOption("xiangzi", "gray", R.drawable.ic_luma_xiangzi),
    LumaIconOption("chuangye3", "green", R.drawable.ic_luma_chuangye3),
    LumaIconOption("a_068_jianzhi", "blue", R.drawable.ic_luma_a_068_jianzhi),
    LumaIconOption("fabu", "green", R.drawable.ic_luma_fabu),
    LumaIconOption("shouye", "green", R.drawable.ic_luma_shouye),
    LumaIconOption("dantupailie", "gray", R.drawable.ic_luma_dantupailie),
    LumaIconOption("luyin", "green", R.drawable.ic_luma_luyin),
    LumaIconOption("sanjiaoxing", "orange", R.drawable.ic_luma_sanjiaoxing),
    LumaIconOption("xiazai49", "blue", R.drawable.ic_luma_xiazai49),
    LumaIconOption("yule", "purple", R.drawable.ic_luma_yule),
    LumaIconOption("qita", "gray", R.drawable.ic_luma_qita),
    LumaIconOption("qita1", "gray", R.drawable.ic_luma_qita1),
    LumaIconOption("qita2", "gray", R.drawable.ic_luma_qita2),
    LumaIconOption("qitafuwu", "gray", R.drawable.ic_luma_qitafuwu),
)

fun normalizeLumaIconKey(key: String): String {
    val normalized = when (key) {
        "briefcase" -> "chuangye"
        "rocket" -> "fabu"
        "broken_heart" -> "xinsui"
        "shirt" -> "meirong"
        "game" -> "youxi"
        "meirong-copy" -> "meirong_copy"
        "a-068_jianzhi" -> "a_068_jianzhi"
        else -> key
    }
    return if (lumaIconOptions.any { it.key == normalized }) normalized else DefaultLumaIconKey
}

private fun lumaIconOption(key: String): LumaIconOption {
    val normalizedKey = normalizeLumaIconKey(key)
    return lumaIconOptions.firstOrNull { it.key == normalizedKey } ?: lumaIconOptions.first()
}

@DrawableRes
fun lumaIconDrawableFor(key: String): Int = lumaIconOption(key).drawableRes

@Composable
fun LumaIconBadge(
    iconKey: String,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    selected: Boolean = false,
    accentColor: Color? = null,
) {
    val option = lumaIconOption(iconKey)
    val color = accentColor ?: themeColor(option.theme)
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
        Image(
            painter = painterResource(option.drawableRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color),
            modifier = Modifier.size(size * 0.56f),
        )
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
                val selected = normalizeLumaIconKey(selectedKey) == option.key
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
