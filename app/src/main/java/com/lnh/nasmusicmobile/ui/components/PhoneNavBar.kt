package com.lnh.nasmusicmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lnh.nasmusicmobile.R
import com.lnh.nasmusicmobile.data.model.Screen
import com.lnh.nasmusicmobile.ui.theme.NasMusicColors

/**
 * 竖屏底部导航项（**5 项**）。
 *
 * 设置入口不放底栏 —— 由竖屏顶栏右上角的齿轮按钮提供（见 `PhoneTopBar`），
 * 否则会出现两个入口。字形说明：`nav_now_playing_short` 是短标签，
 * `nav_now_playing`（"Now Playing"）在底栏窄宽度下会被裁掉。
 */
private data class PhoneNavItem(val screen: Screen, val labelRes: Int, val icon: ImageVector)

private val PHONE_NAV_ITEMS = listOf(
    PhoneNavItem(Screen.Home, R.string.nav_home, Icons.Default.Home),
    PhoneNavItem(Screen.Library, R.string.nav_library, Icons.Default.LibraryMusic),
    PhoneNavItem(Screen.NowPlaying, R.string.nav_now_playing_short, Icons.Default.PlayArrow),
    PhoneNavItem(Screen.Queue, R.string.nav_queue, Icons.AutoMirrored.Filled.QueueMusic),
    PhoneNavItem(Screen.Mine, R.string.nav_mine, Icons.Default.Person),
)

/**
 * 竖屏底部导航栏（手机单平台化 §3 目标信息架构）。
 *
 * 尺寸口径（§2.7 触摸目标）：
 * - 容器高 **56 Compose dp**，子项 `fillMaxHeight()`（不加垂直 padding，否则热区不足）
 * - 图标 **24dp** + 3dp 间距 + **11sp 文字标签**（`lineHeight` 13sp 收紧，两行内容
 *   合计约 40dp，在 56dp 内居中且不挤；标签颜色随 `LocalContentColor` 走，选中/按下自动变色）
 * - 无障碍语义仍由外层 `contentDescription` 提供（标签文字已在视觉上呈现，语义层不再重复播报）
 * - `navigationBarsPadding()`：竖屏显示系统栏 → 三键/手势导航安全区生效
 */
@Composable
fun PhoneNavBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NasMusicColors.Surface)
            .navigationBarsPadding()
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PHONE_NAV_ITEMS.forEach { item ->
            val selected = item.screen == currentScreen
            FocusableSurface(
                onClick = { onNavigate(item.screen) },
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape = RoundedCornerShape(0.dp),
                focusedScale = 1.0f,
                animationDurationMs = 150,
                containerColor = Color.Transparent,
                focusedContainerColor = NasMusicColors.Primary.copy(alpha = 0.2f),
                // 图标颜色从 LocalContentColor 继承（不能显式指定，否则会回退到
                // Material3 的默认黑，在深色底上完全看不见）
                contentColor = if (selected) NasMusicColors.Primary else NasMusicColors.TextPrimary,
                focusedContentColor = NasMusicColors.Primary,
                // 手机没有焦点边框，按下高亮是唯一的"已响应"视觉反馈
                pressedContainerColor = NasMusicColors.Primary.copy(alpha = 0.25f),
                pressedScale = 0.96f,
            ) {
                // ⚠️ `fillMaxSize()` 不可省：FocusableSurface 内部 Box 默认 TopStart 对齐
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { contentDescription = context.getString(item.labelRes) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = stringResource(item.labelRes),
                        color = LocalContentColor.current,
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
