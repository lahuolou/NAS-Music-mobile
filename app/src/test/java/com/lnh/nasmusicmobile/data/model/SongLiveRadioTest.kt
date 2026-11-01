package com.lnh.nasmusicmobile.data.model

import com.lnh.nasmusicmobile.util.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 直播态判据与时长显示的回归护栏（2026-11-02 发现页"歌名数字串"事故）。
 *
 * 成因：电台曲目 `durationMs = Long.MAX_VALUE` 被无条件交给
 * [TimeUtils.formatDuration]，渲染出 `153722867280912:55`，
 * 并在列表行的 Row 里把歌名列挤到 0 宽。
 */
class SongLiveRadioTest {

    @Test
    fun `radio station maps to live song`() {
        val station = RadioStation(
            uuid = "288d0219-4d05-4f5d-8b1d-671bb6c51733",
            name = "安徽小说评书广播",
            urlResolved = "http://example.com/live.mp3",
            country = "China"
        )
        val song = station.toSong()

        assertTrue(song.isLiveRadio())
        assertEquals("安徽小说评书广播", song.title)
        assertEquals(Long.MAX_VALUE, song.durationMs)
    }

    @Test
    fun `non radio songs are not live`() {
        assertFalse(Song(id = "nas_1", title = "你瞒我瞒").isLiveRadio())
        assertFalse(
            Song(
                id = "ntwk_meting_25718007",
                title = "你瞒我瞒",
                isNetworkSong = true,
                networkSource = "meting",
                networkId = "25718007"
            ).isLiveRadio()
        )
    }

    /** 回归锚点：巨串就是"数字串"现象本身，说明列表行必须走 [SongDurationText] 的直播分支 */
    @Test
    fun `formatDuration of live sentinel is the giant number`() {
        assertEquals("153722867280912:55", TimeUtils.formatDuration(Long.MAX_VALUE))
    }
}
