package com.lnh.nasmusicmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lnh.nasmusicmobile.R
import com.lnh.nasmusicmobile.data.model.Song
import com.lnh.nasmusicmobile.ui.theme.FontSize
import com.lnh.nasmusicmobile.ui.theme.NasMusicColors
import kotlinx.coroutines.flow.StateFlow

/**
 * 竖屏迷你播放条（手机单平台化 §3）。
 *
 * ⚠️ **进度/时长必须在本组件内部订阅**：`AppRoot` 顶层不收集 `progress`/`duration` ——
 * 进度由 `PlayerManager` 的 1000ms 轮询驱动，顶层收集会每秒驱动整棵 UI 树重组。
 * 这里接收 [StateFlow] 并在本 composable 内 `collectAsState` → 每秒只重组自身。
 *
 * inset：系统导航栏安全区由**底部导航栏**（[PhoneNavBar] 的 `navigationBarsPadding`）统一让出，
 * 本组件始终叠在导航栏之上，**不再重复加 padding**（否则会多出一条导航栏高度的空白）。
 */
@Composable
fun MiniPlayer(
    song: Song,
    coverCandidates: List<String>,
    progressFlow: StateFlow<Long>,
    durationFlow: StateFlow<Long>,
    isPlaying: Boolean,
    onExpand: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress by progressFlow.collectAsState(initial = 0L)
    val duration by durationFlow.collectAsState(initial = 0L)
    val fraction = if (duration > 0L) {
        (progress.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    // pointerInput(Unit) 不随重组重启，直接捕获 onExpand 会拿到旧 lambda
    val currentOnExpand by rememberUpdatedState(onExpand)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(NasMusicColors.Surface)
    ) {
        // 顶部 2dp 进度细线
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(fraction)
                .height(2.dp)
                .background(NasMusicColors.Primary)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(top = 2.dp)
                .clickable(onClick = onExpand)
                .pointerInput(Unit) {
                    // 48dp 位移视为一次有效上滑（§2.7 触摸目标口径）
                    val thresholdPx = 48.dp.toPx()
                    var accumulated = 0f
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (accumulated <= -thresholdPx) currentOnExpand()
                            accumulated = 0f
                        },
                        onDragCancel = { accumulated = 0f },
                    ) { _, dragAmount ->
                        accumulated += dragAmount
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NasMusicColors.SurfaceVariant)
            ) {
                CoverCarousel(
                    coverCandidates = coverCandidates,
                    isPlaying = isPlaying,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    color = NasMusicColors.TextPrimary,
                    fontSize = FontSize.button(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = song.artist.ifBlank { "—" },
                    color = NasMusicColors.TextSecondary,
                    fontSize = FontSize.caption(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            MiniPlayerIconButton(
                contentDescription = stringResource(
                    if (isPlaying) R.string.miniplayer_cd_pause else R.string.miniplayer_cd_play
                ),
                onClick = onPlayPause,
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
            MiniPlayerIconButton(
                contentDescription = stringResource(R.string.miniplayer_cd_next),
                onClick = onNext,
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

/**
 * **[PHONE_TOUCH_TARGET]（56 Compose dp）** 触摸目标，配 64dp 容器高度。
 */
@Composable
private fun MiniPlayerIconButton(
    contentDescription: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    FocusableSurface(
        onClick = onClick,
        modifier = Modifier
            .size(PHONE_TOUCH_TARGET)
            .semantics { this.contentDescription = contentDescription },
        shape = RoundedCornerShape(8.dp),
        containerColor = Color.Transparent,
        focusedContainerColor = NasMusicColors.Primary.copy(alpha = 0.2f),
        pressedContainerColor = NasMusicColors.Primary.copy(alpha = 0.35f),
        contentColor = NasMusicColors.TextPrimary,
        focusedContentColor = NasMusicColors.Primary,
        focusedScale = 1.06f,
        animationDurationMs = 150,
        pressedScale = 0.92f,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            content()
        }
    }
}
