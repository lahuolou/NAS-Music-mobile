package com.lnh.nasmusicmobile.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.lnh.nasmusicmobile.R
import com.lnh.nasmusicmobile.ui.theme.FontSize
import com.lnh.nasmusicmobile.ui.theme.LocalUiMode
import com.lnh.nasmusicmobile.ui.theme.LocalUiPortrait
import com.lnh.nasmusicmobile.ui.theme.NasMusicColors
import com.lnh.nasmusicmobile.ui.theme.UiMode
/**
 * 公共返回按钮组件
 * 带焦点动画的返回按钮，供各详情屏幕复用
 */
@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FocusableSurface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        focusedScale = 1.08f,
        animationDurationMs = 200,
        containerColor = NasMusicColors.Surface,
        focusedContainerColor = NasMusicColors.Primary.copy(alpha = 0.2f),
        contentColor = NasMusicColors.TextPrimary,
        focusedContentColor = NasMusicColors.Primary
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.common_back_arrow), color = NasMusicColors.TextPrimary, fontSize = FontSize.button(), modifier = Modifier.padding(end = 6.dp))
            Text(text = stringResource(R.string.common_back), color = NasMusicColors.TextPrimary, fontSize = FontSize.button())
        }
    }
}

/**
 * 公共搜索框组件（统一样式：胶囊形、无独立搜索按钮）
 *
 * 点击整个搜索框触发 [onOpenSearch]（调用方负责弹出输入对话框）；
 * 已有搜索词时框内显示 ✕ 清除按钮，点击触发 [onClear]。
 * 宽度由调用方通过 modifier 指定，高度统一 48dp（**竖屏抬到 [PHONE_TOUCH_TARGET]**，见 §2.7）。
 */
@Composable
fun SearchField(
    query: String,
    placeholder: String,
    onOpenSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    FocusableSurface(
        onClick = onOpenSearch,
        modifier = modifier.height(portraitTouchTarget(48.dp)),
        shape = RoundedCornerShape(24.dp),
        containerColor = NasMusicColors.Surface,
        focusedContainerColor = NasMusicColors.FocusOverlay
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = NasMusicColors.TextSecondary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (query.isBlank()) placeholder else query,
                color = if (query.isBlank()) NasMusicColors.TextSecondary else NasMusicColors.TextPrimary,
                fontSize = FontSize.body(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (query.isNotBlank()) {
                FocusableSurface(
                    onClick = onClear,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("✕", color = NasMusicColors.TextPrimary, fontSize = FontSize.body(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
        }
    }
}

/**
 * 歌曲列表列数：TV 两列、手机单列（一行一个歌曲条目）
 *
 * 始终返回 1 列：歌曲条目信息较多（标题+艺术家+专辑+时长+操作按钮），
 * 两列布局下歌名被压缩不可读。统一用一列让 SongRow 占满整行。
 *
 * 注意："我的"页面不使用此函数，其 TV/手机版式差异保持不变。
 */
@Composable
fun songGridColumns(): GridCells = GridCells.Fixed(1)

/**
 * 响应式网格列数 —— **纯函数**（可 JVM 单测，不依赖 Compose 运行时）。
 *
 * 阈值口径沿用项目既有约定：
 * - `>= 1000dp`：TV / 大屏 → [tv]
 * - `600..999dp`：手机横屏 / 小平板（`medium`）→ [medium]
 * - `< 600dp`：手机竖屏 → [phonePortrait]
 *
 * ⚠️ 参数语义：第三参原名为 `phoneLandscape`，但它由 `widthDp >= 600` 触发，
 * **平板竖屏（sw >= 600）也会落进这一支**，故改名为 `medium`（方案 §3.4）。
 */
fun adaptiveColumnsOf(widthDp: Int, tv: Int, phonePortrait: Int, medium: Int): Int = when {
    widthDp >= 1000 -> tv
    widthDp >= 600 -> medium
    else -> phonePortrait
}

/**
 * 响应式网格列数（由 `ui/screens/library/browse/BrowseComponents.kt` 上移，
 * 供全项目复用；与 [songGridColumns] 并列）。
 *
 * 按 `LocalConfiguration.screenWidthDp`（未缩放 Android dp）分档：
 * ≥1000 → [tv]，≥600 → [medium]，其余 → [phonePortrait]。
 */
@Composable
fun adaptiveColumns(tv: Int, phonePortrait: Int, medium: Int = phonePortrait): Int {
    val widthDp = LocalConfiguration.current.screenWidthDp
    return adaptiveColumnsOf(widthDp, tv, phonePortrait, medium)
}

/**
 * 竖屏触摸目标约定值（**Compose dp 口径**，方案 §2.7 第 1 条 / P0-26）。
 *
 * ⚠️ **2026-11-01 起密度口径一分为二**（见 `docs/conventions-adaptive-ui.md` §6.0）：
 * - **竖屏**：`LocalDensity` **不再缩放** ⇒ `X.dp` = `X` 物理 dp，44dp 本就达标；
 * - **横屏**：仍乘 `CompactSizes.PHONE_UI_SCALE`(0.82) ⇒ 44 / 48 / 52 Compose dp 分别只有
 *   36.1 / 39.4 / 42.6 物理 dp，**都不达标**；`56 × 0.82 ≈ 45.9 ≥ 44` ✅。
 *
 * ⇒ 本常量是「竖屏统一大热区」的约定值（现在等于 56 物理 dp，超出必要值但无副作用），
 * 原始依据是横屏口径的 53.66 取整。新增竖屏控件请用 [portraitTouchTarget]。
 *
 * 保留为 `Float` 常量是为了让 [com.lnh.nasmusicmobile.ui.theme.UiModeTest] 能在纯 JVM 下断言该算术。
 */
const val PHONE_TOUCH_TARGET_DP: Float = 56f

/** [PHONE_TOUCH_TARGET_DP] 的 `Dp` 形式 */
val PHONE_TOUCH_TARGET: Dp = PHONE_TOUCH_TARGET_DP.dp

/**
 * 触摸目标尺寸（方案 §2.7 / P0-26）。
 *
 * 竖屏下恒返回 [PHONE_TOUCH_TARGET]（≥ 物理 44dp 的无障碍下限），
 * 横屏 / TV 沿用调用方传入的 [landscape] 取值（与竖屏恢复前逐字等价，B1）。
 *
 * 用法：`Modifier.size(portraitTouchTarget(48.dp))` / `Modifier.height(portraitTouchTarget(44.dp))`
 */
@Composable
fun portraitTouchTarget(landscape: Dp): Dp =
    if (LocalUiPortrait.current) PHONE_TOUCH_TARGET else landscape

/**
 * 对话框 / 弹层的响应式尺寸（方案 §2.4 对话框族 / P1-27）。
 *
 * 竖屏：占满可用宽度（左右各留 16dp），避免固定大宽在窄屏上溢出。
 * 横屏 / TV：沿用调用方传入的 [landscapeWidth]，与竖屏恢复前**逐字等价**（B1）。
 */
@Composable
fun responsiveDialogSize(landscapeWidth: Dp): Modifier =
    if (LocalUiPortrait.current) {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    } else {
        Modifier.width(landscapeWidth)
    }
