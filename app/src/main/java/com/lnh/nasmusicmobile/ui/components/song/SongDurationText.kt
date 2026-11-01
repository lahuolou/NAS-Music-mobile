package com.lnh.nasmusicmobile.ui.components.song

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import com.lnh.nasmusicmobile.R
import com.lnh.nasmusicmobile.data.model.Song
import com.lnh.nasmusicmobile.data.model.isLiveRadio
import com.lnh.nasmusicmobile.util.TimeUtils

/**
 * 歌曲时长单元格（列表行 / 队列 / 歌曲信息面板共用）。
 *
 * 电台曲目用 `durationMs = Long.MAX_VALUE` 表示无限直播流
 * （见 [com.lnh.nasmusicmobile.data.model.RadioStation.toSong]），
 * 直接 `TimeUtils.formatDuration` 会渲染成 `153722867280912:55` 这类巨串，
 * 并且在 [MODE_ROW][SongRowMode.MODE_ROW] 的 Row 里是**非 weight 先测量子项**，
 * 会把歌名列压到 0 宽。⛔ 不要改回无条件的 `formatDuration`。
 */
@Composable
internal fun SongDurationText(
    song: Song,
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    Text(
        text = if (song.isLiveRadio()) {
            stringResource(R.string.network_radio_live)
        } else {
            TimeUtils.formatDuration(song.durationMs)
        },
        color = color,
        fontSize = fontSize,
        maxLines = 1,
        softWrap = false,
        modifier = modifier
    )
}
