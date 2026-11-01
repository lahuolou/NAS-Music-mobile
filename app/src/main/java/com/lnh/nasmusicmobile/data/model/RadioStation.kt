package com.lnh.nasmusicmobile.data.model

/**
 * 互联网电台（radio-browser.info）
 *
 * 字段对齐 radio-browser JSON 响应（/json/stations/search）。
 * stationuuid 跨服务器稳定，收藏/播放均使用它作为稳定标识。
 */
data class RadioStation(
    val uuid: String,
    val name: String,
    val urlResolved: String,
    val faviconUrl: String? = null,
    val countryCode: String = "",
    val country: String = "",
    val tags: List<String> = emptyList(),
    val votes: Int = 0,
    val bitrate: Int = 0,
    val codec: String = ""
) {
    /**
     * 电台 → 歌曲映射（复用播放链路）
     *
     * - streamUrl 为直链，无需 resolvePlayUrl
     * - durationMs = Long.MAX_VALUE 表示无限流（直播态），UI 层据 networkSource=="radio" 显示"直播"
     */
    fun toSong(): Song = Song(
        id = "radio_${uuid}",
        title = name,
        artist = if (country.isNotBlank()) "电台 · $country" else "电台",
        coverUrl = faviconUrl?.takeIf { it.isNotBlank() },
        streamUrl = urlResolved,
        durationMs = Long.MAX_VALUE,
        isNetworkSong = true,
        networkSource = "radio",
        networkId = uuid
    )

    companion object {
        const val SOURCE_ID = "radio"
    }
}

/**
 * 是否为电台歌曲（供播放链路 / UI 判断直播态）
 */
fun String?.isRadioSong(): Boolean = this == RadioStation.SOURCE_ID

/**
 * 直播态判据（结构化，⛔ 不要比较文案、也不要比较 `durationMs` 的哨兵值）。
 *
 * [RadioStation.toSong] 用 `durationMs = Long.MAX_VALUE` 表示无限直播流，
 * 而 `TimeUtils.formatDuration(Long.MAX_VALUE)` 会产出 `153722867280912:55`
 * ——列表行不判这个标志就会把巨串直接渲染出来（2026-11-02 发现页"歌名数字串"事故）。
 */
fun Song.isLiveRadio(): Boolean = networkSource.isRadioSong()