package com.lnh.nasmusicmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lnh.nasmusicmobile.R
import com.lnh.nasmusicmobile.data.model.Screen
import com.lnh.nasmusicmobile.ui.theme.FontSize
import com.lnh.nasmusicmobile.ui.theme.NasMusicColors

/**
 * 竖屏顶栏（手机单平台化 §3）。
 *
 * 56dp 内容行 + `statusBarsPadding()` + `displayCutoutPadding()`：
 * 竖屏显示系统状态栏，顶栏必须让出状态栏与挖孔/刘海区域。
 *
 * 设置入口放在这里（齿轮）—— 底部导航因此只有 5 项，不会出现两个设置入口。
 */
@Composable
fun PhoneTopBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NasMusicColors.Surface)
            .statusBarsPadding()
            .displayCutoutPadding()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(30.dp)
                .height(30.dp)
                .background(NasMusicColors.Primary, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "\u266A",
                color = NasMusicColors.TextPrimary,
                fontSize = FontSize.small()
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "NAS Music",
            color = NasMusicColors.TextPrimary,
            fontSize = FontSize.body()
        )
        Spacer(modifier = Modifier.weight(1f))
        val settingsCd = stringResource(R.string.nav_settings_cd)
        FocusableSurface(
            onClick = { onNavigate(Screen.Settings) },
            modifier = Modifier
                .height(PHONE_TOUCH_TARGET)
                .width(PHONE_TOUCH_TARGET)
                .semantics { contentDescription = settingsCd },
            shape = RoundedCornerShape(8.dp),
            containerColor = Color.Transparent,
            focusedContainerColor = NasMusicColors.Primary.copy(alpha = 0.2f),
            pressedContainerColor = NasMusicColors.Primary.copy(alpha = 0.25f),
            contentColor = if (currentScreen == Screen.Settings) {
                NasMusicColors.Primary
            } else {
                NasMusicColors.TextPrimary
            },
            focusedContentColor = NasMusicColors.Primary,
            focusedScale = 1.0f,
            animationDurationMs = 150,
            pressedScale = 0.92f,
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.width(24.dp).height(24.dp)
                )
            }
        }
    }
}
