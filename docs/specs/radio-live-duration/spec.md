# 规格：发现页电台曲目时长显示修复（"歌名为数字串"）

状态：已实施（2026-11-02）

## 1. 现象

「发现」页在筛选全为「所有」时，20 首列表的每一行歌名位置显示
`153722867`（第一行）+ `280912:55`（第二行）；换到「粤语」等筛选后，
歌名只剩 1~2 个字符可见（如 `「`、`翌`），并随时间变化。

## 2. 根因（已在设备上验证，与数据源无关）

1. `RadioStation.toSong()` 用 `durationMs = Long.MAX_VALUE` 表示无限直播流
   （[RadioStation.kt](file:///e:/NASMusic-mobile/app/src/main/java/com/lnh/nasmusicmobile/data/model/RadioStation.kt#L25-L33)；
   KDoc 明写「UI 层据 `networkSource=="radio"` 显示『直播』」，但该约定**从未在任何列表行落地**）。
2. `TimeUtils.formatDuration(Long.MAX_VALUE)` 结果为 `"153722867280912:55"`
   （`9223372036854775807/1000 = 9223372036854775` → 分钟 `153722867280912`、秒 `55`），
   即用户看到的数字串。
3. 4 处**无条件**用该函数渲染时长，没有电台分支：
   - [UnifiedSongRow.kt:507](file:///e:/NASMusic-mobile/app/src/main/java/com/lnh/nasmusicmobile/ui/components/song/UnifiedSongRow.kt#L506-L510)（`MODE_ROW`）
   - [UnifiedSongRow.kt:846](file:///e:/NASMusic-mobile/app/src/main/java/com/lnh/nasmusicmobile/ui/components/song/UnifiedSongRow.kt#L845-L849)（`MODE_CARD`）
   - [QueueScreen.kt:363](file:///e:/NASMusic-mobile/app/src/main/java/com/lnh/nasmusicmobile/ui/screens/QueueScreen.kt#L363)
   - [SongInfoPanel.kt:95](file:///e:/NASMusic-mobile/app/src/main/java/com/lnh/nasmusicmobile/ui/components/SongInfoPanel.kt#L95)
4. 布局放大器（`MODE_ROW`）：时长 `Text` 与歌名/歌手所在的
   `Column(Modifier.weight(1f))` **同处一个 Row**，且它是非 weight 子项、先被测量；
   19 字符的巨串吃光可用宽度 → 歌名列被压到 ≈0，只剩 1~2 字符（与截图完全一致）。
5. 发现页全「所有」时走 `loadDiscoverFallback()`
   （[MainViewModel.kt:1649-1666](file:///e:/NASMusic-mobile/app/src/main/java/com/lnh/nasmusicmobile/ui/viewmodel/MainViewModel.kt#L1649-L1666)）
   的电台兜底：`stations.shuffled().take(20).map { it.toSong() }` ⇒ 20 行全是电台。

**反证**：点击首行后日志为
`playNetworkSong ENTRY: id=radio_288d0219-… title=安徽小说评书广播 networkSource=radio`
—— 数据层标题完全正确，只有显示错误。

## 3. 修复方案

### 3.1 结构化判据（数据层，零文案依赖）

[RadioStation.kt](file:///e:/NASMusic-mobile/app/src/main/java/com/lnh/nasmusicmobile/data/model/RadioStation.kt#L44-L47)
追加：

```kotlin
/** 直播态判据（结构化，⛔ 不要比较文案 / 不要比较 durationMs 的魔法值） */
fun Song.isLiveRadio(): Boolean = networkSource.isRadioSong()
```

### 3.2 共享时长渲染（UI 层，单点实现）

新增 `app/src/main/java/com/lnh/nasmusicmobile/ui/components/song/SongDurationText.kt`：

```kotlin
@Composable
internal fun SongDurationText(
    song: Song,
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    Text(
        text = if (song.isLiveRadio()) stringResource(R.string.network_radio_live)
               else TimeUtils.formatDuration(song.durationMs),
        color = color,
        fontSize = fontSize,
        maxLines = 1,
        softWrap = false,
        modifier = modifier
    )
}
```

复用既有资源 `R.string.network_radio_live`（中「直播」/ 英「Live」），不新增字符串。

### 3.3 替换 4 处调用点

- `UnifiedSongRow.kt` 的 `MODE_ROW`（507）与 `MODE_CARD`（846）→ `SongDurationText(...)`
- `QueueScreen.kt:363` → `SongDurationText(...)`
- `SongInfoPanel.kt:95` → 该处是 `InfoRow(label, value: String)`，改为
  `if (song.isLiveRadio()) stringResource(R.string.network_radio_live) else TimeUtils.formatDuration(song.durationMs)`
  （`InfoRow` 接收 String，不改造 `InfoRow` 本身）

### 3.4 实施期补充：竖屏操作按钮下沉（设备复测发现）

仅替换时长文案后，设备的竖屏歌名列仍只剩 1~2 个字符 —— 行内 4 个操作按钮
（各 56dp + 10dp 间距）约占 44% 行宽。因此在 `MODE_ROW` 中按 `LocalUiPortrait`
把 `actionButtons()` 移到行尾第二行右对齐；横屏保持单行（原布局逐字等价）。

### 3.5 不改的事项

- ⛔ 不改 `TimeUtils.formatDuration` 的语义/签名（`PlayerControls` 等已有自己的 `isLive` 分支）。
- ⛔ 不改 `RadioStation.durationMs = Long.MAX_VALUE` 哨兵（播放/进度链路依赖它）。
- ⛔ 不改 `loadDiscoverFallback()` 的 20 首电台兜底逻辑。

## 4. 验证

1. 编译：`./gradlew.bat assembleDebug --no-daemon -Pkotlin.compiler.execution.strategy=in-process`
2. 新增单测 `app/src/test/java/com/lnh/nasmusicmobile/data/model/SongLiveRadioTest.kt`（纯 JUnit）：
   - `RadioStation(...).toSong().isLiveRadio() == true`
   - 普通网络曲目 / NAS 曲目 `== false`
   - 回归锚点：`TimeUtils.formatDuration(Long.MAX_VALUE) == "153722867280912:55"`
   - 运行：`./gradlew.bat testDebugUnitTest --tests "*SongLiveRadioTest" --no-daemon -Pkotlin.compiler.execution.strategy=in-process`
3. 设备回归（emulator-5554）：发现页全「所有」→ 20 行显示真实台名 + 「直播」，歌名列正常；
   「粤语」筛选 → 歌名完整滚动，不再出现 1~2 字符碎片。截图存 `output/`。
4. 文档：`CHANGELOG.md` 当前版本段加一行；`docs/technical-overview.md` 追加 §10.285（根因 + 验证）。

## 5. 影响面与风险

- 仅改 5 个文件 + 1 个新文件（渲染层），数据与播放链路零改动。
- `MODE_CARD` / 队列 / 歌曲信息面板同样被修正（此前同样会显示巨串）。
- 「直播」占位与 NowPlaying 的 `player_radio_live`（「● 电台直播」）文案不同，但语义一致，属既有资源分工。
