# 任务清单：发现页电台曲目时长显示修复

依赖规格：`docs/specs/radio-live-duration/spec.md`（已实施）

## T1 数据层判据
- [x] 文件：`app/src/main/java/com/lnh/nasmusicmobile/data/model/RadioStation.kt`
- [x] 内容：新增顶层扩展 `fun Song.isLiveRadio(): Boolean = networkSource.isRadioSong()`
- [x] 验收：编译通过；不改 `durationMs = Long.MAX_VALUE` 哨兵

## T2 共享时长渲染组件
- [x] 文件：新增 `app/src/main/java/com/lnh/nasmusicmobile/ui/components/song/SongDurationText.kt`
- [x] 内容：`internal @Composable SongDurationText(song, color, fontSize, modifier)`；
  `isLiveRadio()` → `stringResource(R.string.network_radio_live)`，否则 `TimeUtils.formatDuration`；
  `maxLines = 1`、`softWrap = false`
- [x] 验收：无新增字符串资源

## T3 替换列表行调用点
- [x] 文件：`ui/components/song/UnifiedSongRow.kt`（`MODE_ROW` / `MODE_CARD`）
- [x] 内容：改用 `SongDurationText`
- [x] 验收：`MODE_ROW` 中时长不再压扁歌名列
- [x] 补充（设备复测发现）：竖屏时 `actionButtons()` 下沉到行尾第二行右对齐，横屏保持单行。
  仅替换时长文案时，竖屏歌名列仍只剩 1~2 字符（4 个操作按钮约占 44% 行宽）。

## T4 替换队列 / 歌曲信息面板
- [x] 文件：`ui/screens/QueueScreen.kt`、`ui/components/SongInfoPanel.kt`
- [x] 内容：队列用 `SongDurationText`；信息面板就地判 `isLiveRadio()`
- [x] 验收：电台曲目在这两处显示「直播」

## T5 单测
- [x] 文件：新增 `app/src/test/java/com/lnh/nasmusicmobile/data/model/SongLiveRadioTest.kt`
- [x] 用例：电台 → true；普通网络 / NAS → false；`formatDuration(Long.MAX_VALUE) == "153722867280912:55"`
- [x] 验收：3 例全部通过。
  ⚠️ 本机 `./gradlew.bat testDebugUnitTest` 因 Gradle worker 的 @argfile 按平台编码（CP936）解码、
  中文 `GRADLE_USER_HOME` 路径被解坏而失败（`ClassNotFoundException: GradleWorkerMain`，环境性、与本次改动无关）。
  已用同一份 Gradle 测试 classpath（转 GBK 编码的 argfile + `org.junit.runner.JUnitCore`）直接执行，`OK (3 tests)`。

## T6 构建 + 设备回归
- [x] `./gradlew.bat assembleDebug --no-daemon -Pkotlin.compiler.execution.strategy=in-process` BUILD SUCCESSFUL
- [x] 安装到 emulator-5554，验证：全「所有」20 行显示台名 + 「直播」；「粤语」筛选歌名完整
- [x] 截图存 `output/`：`discover_fixed_portrait.png`、`discover_yue_fixed.png`、`device_after_fix.png`

## T7 文档
- [x] `CHANGELOG.md` 当前版本段一行（只写做了什么）
- [x] `docs/technical-overview.md` 追加 §10.285
