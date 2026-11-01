# 验收清单：发现页电台曲目时长显示修复

- [x] 电台曲目在列表行（`MODE_ROW`）显示「直播」，不出现 `153722867280912:55` 这类巨串
- [x] 电台曲目在卡片（`MODE_CARD`）、播放队列、歌曲信息面板的时长位置同样显示「直播」
- [x] 直播态判据是结构化的（`networkSource == "radio"`），不是文案比较、也不是 `durationMs` 哨兵比较
- [x] 复用既有字符串资源 `network_radio_live`，未新增字符串
- [x] `TimeUtils.formatDuration` 语义与 `RadioStation.durationMs = Long.MAX_VALUE` 哨兵均未改动
- [x] `loadDiscoverFallback()` 的电台兜底逻辑未改动
- [x] 竖屏 `MODE_ROW` 歌名恢复可读宽度（操作按钮下沉第二行），横屏布局保持原样
- [x] `SongLiveRadioTest` 3 例通过（电台 → true；普通网络/NAS → false；巨串回归锚点）
- [x] `assembleDebug` 构建成功，产物安装到设备可运行
- [x] 设备实测：全「所有」→ 20 行台名 + 「直播」；「粤语」→ 歌名完整、时长 `3:57` 等正常
- [x] `CHANGELOG.md` 与 `docs/technical-overview.md` §10.285 已记录
