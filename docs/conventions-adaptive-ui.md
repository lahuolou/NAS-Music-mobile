# 自适应 UI 维护约定（手机竖屏 / 手机横屏）

> 来源：`docs/archive/phone-portrait-ui-plan.md` §3（架构）、§2.7（尺寸口径）、§9 P1-32。
> 本文件是**新增页面/组件时必须遵守的清单**，也是 code review 的检查项。
>
> ⚠️ **2026-11-01 手机单平台化改造后按「竖屏优先 + 横屏兼容」重写**：TV 形态已整体移除
> （`androidx.tv:*` 依赖、leanback 清单声明、遥控焦点体系全部删除），本文件中所有
> 「TV / 遥控器 / 焦点环」相关的判据都已作废，改写为「已移除，勿重新引入」。
> 改造实况与验证记录见 `docs/technical-overview.md` §10.283。

---

## 1. 形态因子（Form Factor）

唯一判定入口：`ui/theme/UiMode.kt`

```kotlin
enum class UiMode { TV, PhonePortrait, PhoneLandscape }
val LocalUiMode = staticCompositionLocalOf { UiMode.TV }      // 默认值仅为兼容既有测试/预览
val LocalUiPortrait = staticCompositionLocalOf { false }

fun deriveUiMode(isTV: Boolean, isPortrait: Boolean): UiMode = when {
    isTV -> UiMode.TV
    isPortrait -> UiMode.PhonePortrait
    else -> UiMode.PhoneLandscape
}
```

- `MainActivity` 在组合期读 `LocalConfiguration.current`，用
  `orientation == Configuration.ORIENTATION_PORTRAIT` 得到 `isPortrait`，再调
  `deriveUiMode(isTV = false, isPortrait)`（**`isTV` 恒传 `false`**，`UiMode.TV` 目前没有任何可达路径）。
- 下发方式：`CompositionLocalProvider(LocalUiMode provides uiMode, LocalUiPortrait provides isPortrait, …)`。
  ⚠️ 新组件判断形态**优先用 `LocalUiPortrait.current`**（一个 Boolean，语义明确），
  只有需要区分「横屏 vs 未知」时才读 `LocalUiMode.current == UiMode.PhoneLandscape`。
- ⚠️ **判定不能写成 `remember { derivedStateOf { configuration.orientation } }`**
  —— `configuration` 不是 `State`，会永久读到初值（方案 C4）。直接读 `LocalConfiguration.current`。

---

## 2. ⛔ B1 硬规则：分支谓词只写竖屏

```kotlin
// ✅ 正确
if (LocalUiPortrait.current) { 竖屏版() } else { 横屏版() }

// ⛔ 禁止：TV 已不可达，这么写等于 else 恒真，后面的分支是死代码
if (LocalUiMode.current == UiMode.TV) { … } else { … }
```

原因：**`else` 分支必须保持改动前的横屏布局**。原设计里「手机横屏与 TV 共用同一套布局」，
2026-11-01 后 TV 分支的调用方消失，但横屏布局本身**逐字沿用**——这样"横屏零变化"才可证
（`UiModeTest` 的反向用例：手机不得判定为 TV）。

若某页面确实需要横屏独立分支，必须：
1. 显式写 `LocalUiMode.current == UiMode.PhoneLandscape` 判断；
2. 在代码注释里写明**为什么**不能与竖屏共用；
3. 在 `CHANGELOG.md` 与 `docs/technical-overview.md` 的对应 §10.N 里记录。

---

## 3. 列数：只用 `adaptiveColumns` / `adaptiveColumnsOf`

```kotlin
// ui/components/CommonComponents.kt

/** 纯函数，可 JVM 单测 */
fun adaptiveColumnsOf(widthDp: Int, tv: Int, phonePortrait: Int, medium: Int): Int

/** @Composable 版；读 LocalConfiguration.screenWidthDp 后转调上面那个 */
@Composable
fun adaptiveColumns(tv: Int, phonePortrait: Int, medium: Int = phonePortrait): Int
```

阈值口径（沿用项目既有约定）：

| `screenWidthDp`（**未缩放 Android dp**） | 落点参数 | 典型设备 |
|---|---|---|
| `>= 1000` | `tv` | 大屏 / 大平板（参数名是历史名，**别改签名**，调用点与测试都依赖它） |
| `600 .. 999` | `medium` | 手机横屏 / 小平板 |
| `< 600` | `phonePortrait` | 手机竖屏 |

使用方式：

```kotlin
LazyVerticalGrid(columns = GridCells.Fixed(adaptiveColumns(tv = 6, phonePortrait = 3, medium = 6)))
```

⚠️ `adaptiveColumns` 返回的是 **Int（列数）**，不是 `GridCells`。必须自己包 `GridCells.Fixed(...)`。

⚠️ **不要另造列数 helper**。已有两套，够用：
- `adaptiveColumns`（多列网格，见上）
- `songGridColumns()`（`ui/components/CommonComponents.kt`，**恒 1 列**，歌曲条目专用）

⚠️ **宽度口径**（原「双输入原则」修订版）：`adaptiveColumns` 读的是**未缩放**的
`LocalConfiguration.screenWidthDp`。
- **竖屏**：`MainActivity` 不再缩放密度 ⇒ 未缩放 dp 与实际排版宽度一致，分档不会错配（旧的
  「竖屏 Compose dp ≈ 439 而 screenWidthDp ≈ 360」矛盾已随竖屏取消缩放而消失）。
- **横屏**：排版宽度仍乘 `PHONE_UI_SCALE`(0.82)（物理 1080p 横屏 ≈ 960 未缩放 dp → ≈ 787 Compose dp），
  所以横屏落在 `medium` 档而不是 `tv` 档 —— 这是有意结果，改动密度口径必须同批复核全部列数调用点。

### 各页面现用列数（改动前先对照）

| 位置 | tv | phonePortrait | medium |
|---|---|---|---|
| `browse/AlbumGrid.kt` | 6 | 3 | 6 |
| `browse/ArtistList.kt` | 6 | 3 | 6 |
| `browse/GenreYearLists.kt:83` | 4 | 2 | 3 |
| `browse/GenreYearLists.kt:180` | 5 | 2 | 3 |
| `library/RadioTab.kt` | 3 | 1 | 2 |
| `components/Shimmer.kt`（骨架） | 6 | 3 | 6 |

---

## 4. ~~`AdaptiveLayout` 二分支包装~~ —— 已移除

原 `AdaptiveLayout(phonePortrait = { … }, tv = { … })` 包裹组件已随 2026-11-01 改造**删除**
（全仓零定义，`ScreenUiModeCoverageTest` 也不再把它当 marker）。

页面级 / 子树级分叉**直接写 §2 的 `if/else`**，把两版抽成两个私有 `@Composable` 函数即可，
行为与原来等价，且 diff 可审。

---

## 5. 对话框 / 弹层：`responsiveDialogSize`

```kotlin
@Composable
fun responsiveDialogSize(landscapeWidth: Dp): Modifier
```

- **竖屏**：`fillMaxWidth()` + `padding(horizontal = 16.dp)` —— 占满可用宽度，左右各留 16dp
- **横屏**：`Modifier.width(landscapeWidth)` —— 与改动前逐字等价

用法（放进 Modifier 链）：

```kotlin
Modifier
    .then(responsiveDialogSize(480.dp))
    .clip(RoundedCornerShape(16.dp))
    …
```

⚠️ **本 helper 不再接收 `scrollable` 参数**，也不再自动加 `widthIn(max=420.dp)` / `heightIn(max=80%)`。
需要限高时由调用方自己加 `heightIn(max = …)`：
内部已有 `LazyColumn` / `LazyVerticalGrid` 的对话框**不要**再加同向 `heightIn` 之外的滚动包装，
否则触发 `Vertically scrollable component was measured with an infinity maximum height constraints` 崩溃。

⚠️ 直接写死 `fillMaxWidth(0.92f)` 会把横屏上的 480~720dp 对话框压窄，属**回归**。
必须走本 helper（它内部按 `LocalUiPortrait` 分叉）。

⛔ **新增对话框后必做一次全量自查**（踩过：设置页「删除备份」确认弹窗漏改，
仍写死 `.width(520.dp)`，在窄屏上被窗口裁掉）：

```bash
# 凡出现在 `Dialog { }` 内容根节点上的三位数宽度，逐个确认是否已走 responsiveDialogSize
grep -rnE "\.width\([4-9][0-9]{2}\.dp\)" app/src/main/java/com/lnh/nasmusicmobile/ui
```

判定口径：**该宽度是否会成为对话框/弹层内容的根约束**。若它只是页面内某个普通 `Column`
且该 `Column` 只在 `else`（横屏）分支渲染，则不必改（如 `QueueScreen` 的 340dp 侧卡、
`TextInputDialog` 的 720dp 主栏 —— 都在横屏分支里）。

---

## 6. 尺寸与触摸目标口径

### 6.0 ⚠️ 密度口径一分为二（2026-11-01 起）

| 形态 | `LocalDensity` | 换算 |
|---|---|---|
| **竖屏** | 原密度，**不缩放** | Compose dp = 物理 dp |
| **横屏** | `× PHONE_UI_SCALE`(0.82) | 物理 dp = Compose dp × 0.82 |

`MainActivity` 的注入逻辑：`if (isPortrait) baseDensity else Density(baseDensity.density * 0.82, …)`。
横屏缩放是为了 1080p 横屏 dp 过宽（沿用既有 TV/横屏口径）；竖屏宽度本就接近标准设计宽度
（≈360~410dp），再缩会让字号偏小。

> ✅ **口径已对齐（2026-11-01）**：`CommonComponents.kt` 的 `PHONE_TOUCH_TARGET` KDoc、
> `SmallTouchTargetScanTest` 的类注释、`UiModeTest` 的用例名与注释均已改为「竖屏不缩放 /
> 横屏 ×0.82」的双口径表述。`UiModeTest` 那条
> `横屏下 PHONE_TOUCH_TARGET 满足物理 44dp 而 44 与 48 不满足` 用例本身仍会通过
> （它显式乘 0.82，等价于横屏换算），**不要**因为读起来不像竖屏就删掉它。

### 6.1 ⛔ 触摸目标：竖屏用 `portraitTouchTarget(...)` / `PHONE_TOUCH_TARGET`

```kotlin
// ✅ 正确
Modifier.size(portraitTouchTarget(48.dp))     // 竖屏 → 56dp，横屏 → 48dp
Modifier.height(portraitTouchTarget(44.dp))
Modifier.size(PHONE_TOUCH_TARGET)             // 竖屏专属组件，直接 56dp

// ❌ 错误
Modifier.size(48.dp)   // 写裸值会让横屏只有 39.4 物理 dp，且失去竖屏「统一大热区」的意图
```

| Compose dp | 竖屏物理 dp | 横屏物理 dp（×0.82） | 判定 |
|-----------|-----------|--------------------|------|
| 44 | 44 | 36.08 | 竖屏勉强达标 / 横屏 ❌ |
| 48 | 48 | 39.36 | 竖屏达标 / 横屏 ❌ |
| 52 | 52 | 42.64 | 竖屏达标 / 横屏 ❌ |
| **56** | **56** | **45.92** | ✅ ← `PHONE_TOUCH_TARGET` |

`PHONE_TOUCH_TARGET_DP = 56f` 的**原始依据**是「竖屏 ×0.82 下 52 不达标」；竖屏取消缩放后
它已**超出必要值**，但仍是本仓竖屏热区的统一约定值（更大的点击区无副作用），**维持不动**。

⚠️ **两个坑**：

1. **`padding` 会削热区**。`Modifier.height(52.dp).padding(vertical = 4.dp)` 传给
   `FocusableSurface` 时，`clickable` 加在 padding **之后** → 实际热区只有 44dp。
   要么抬到 `portraitTouchTarget(52.dp)`，要么**同时把垂直 padding 归零**
   （见 `ExportDeviceDialog` 设备项）。
2. **容器即热区**。`PhoneNavBar` / `PhoneTopBar` / `MiniPlayer` 的做法是「子项
   `fillMaxSize()` / 56dp + 容器不加垂直 padding」，热区等于整块容器，不要再套内边距。

### 6.2 已被证明不达标的旧值

`UiModeTest` 里有回归护栏用例（`横屏下 PHONE_TOUCH_TARGET 满足物理 44dp 而 44 与 48 不满足`），
断言 `44 × 0.82 = 36.08`、`48 × 0.82 = 39.36`、`52 × 0.82 = 42.64` 均 `< 44`。
**改这些常量会让该用例失败**，这是有意的（它守的是横屏口径）。

### 6.3 豁免清单（**刻意不改**，不要"顺手修"）

| 位置 | 原因 |
|------|------|
| `KaraokePlaybackScreen` / `MvPlaybackScreen` / `VisualEqualizer` 内的 44 / 48dp | 全屏沉浸页，实际在横屏姿态下使用；2026-11-01 后 `MainActivity` **不再**对它们 `requestedOrientation` 强制横屏（该调用已随 TV 提取移除），但竖屏不缩放密度 ⇒ 44/48 在竖屏也等于物理 44/48，不构成违规 |
| `Shimmer.kt` 骨架屏、各页封面缩略图（40/48dp）、`HomeScreen` 的 40dp logo | **不是触摸目标**（装饰/占位），热区在父行 |
| `Spacer(Modifier.height(40.dp))` 之类 | 纯间距 |

### 6.4 其他

改任何"固定 dp 尺寸"前，先用 §6.0 的两栏换算核一遍物理尺寸（**先判形态，再算**）。

⚠️ **不能靠改全局 density 解决版式**：会同时影响竖屏与横屏。

### 6.5 ⚠️ 自查 grep 的**盲区**：小尺寸 + `clickable`

自查 grep 只覆盖 **40~53dp** 区间：

```
\.(size|height|width)\((\s*)(4[0-9]|5[0-3])\.dp\)
```

**小于 40dp 的写法完全逃过检查** —— 但 `Modifier.size(8.dp).clickable { }` 在竖屏只有
**8 物理 dp** 的热区，比 44/48 那些已知问题更糟。**真实案例（2026-09-19 review）**：
竖屏播放页的 `PortraitModeIndicator` 两个模式圆点写成
`.size(if (active) 8.dp else 6.dp) ... .clickable { onSwitch(m) }`，
热区仅 6~8 dp，而 P0-26 的全量复核没抓到它。

**三个教训**：

1. **自查 grep 的下界必须放到 0**（`([0-3][0-9]|4[0-9]|5[0-3])`），且必须人工确认
   「这个尺寸是不是触摸目标」。装饰性小尺寸（图标、圆点、进度条）挂 `clickable` 一律可疑。
2. ⚠️ **尺寸是表达式时正则匹配不到**：`size(if (a) 8.dp else 6.dp)` 里没有
   `size(<数字>.dp)` 这样的字面形态。写扫描脚本要**取括号配对内容再抽其中的 `.dp` 字面量**，
   否则会**空转**（报"0 处"，看起来干净，实际什么都没查）。
3. ⚠️ **手势可能写在同一行**：`Modifier.size(8.dp).clickable { }`。用 `^\s*\.` 锚行首的
   手势正则**匹配不到行中的 `.clickable`** → 单行写法整类逃检（**首版脚本与首版门禁都在
   此空转**）。必须另设一条不锚行首的同行正则，做**同行 + 后续行双判定**。
   补同行判定后**必须同时排除整行注释**，否则 KDoc 里举例的
   `` `Box(Modifier.size(8.dp).clickable { })` `` 会被误报。

> 📌 **扫描脚本已入库**：`scripts/audit_small_touch_target.py`
> （2026-10-16 由项目根迁入 `scripts/`），**必须先跑 `--selftest` 再实跑** ——
> 这就是本条的教训：**源码扫描型护栏不自证，就无法区分"真干净"和"空转"**。
> 判定规则：取 `size(` 括号配对内容 → 抽出全部 `.dp` 字面量 → 全部 < 40dp 才候选
> （无字面量则跳过；`portraitTouchTarget(44.dp)` 因含 44 而被豁免，属受认可写法）；
> 再判**同行**（不锚行首的 `CLICK_SAME_LINE`）或**后续同链**（`^\s*\.` 锚行首）是否有
> `clickable`，链上有 `fillMaxSize` / `weight(` 撑大的跳过；整行注释（`//` / `*` / `/*`）先排除；
> `Spacer(` / `Divider` 需**向前回看 3 行**排除（它们常写在链的开头）。
>
> 📌 **同范式的第二道门禁已固化**：`app/src/test/java/com/lnh/nasmusicmobile/ui/SmallTouchTargetScanTest.kt`
> （跑在 `testDebugUnitTest` 里，CI 已阻塞）。脚本只能靠人记得跑，而这条规则在项目里
> **已经漏过一次**，故与 `ScreenUiModeCoverageTest`（§9）同范式做成单测。
> 该门禁含**三层自证**：负向用例 ×2（表达式尺寸、单行字面量）+ 误报防线 ×1（注释里的举例）
> + 空转断言（真实扫描里断言扫到的 `.size(` 链数 > 0）。

**正确写法**：视觉元素与热区分离 —— 外层承担热区，内层只做视觉。

```kotlin
Box(
    modifier = Modifier
        .size(portraitTouchTarget(44.dp))   // 竖屏 56dp，横屏 44dp
        .clickable { onSwitch(m) },
    contentAlignment = Alignment.Center,
) {
    Box(Modifier.size(if (active) 8.dp else 6.dp).clip(CircleShape).background(color))
}
```

---

## 7. 新增 Screen / 组件的检查清单

- [ ] 顶层容器宽度不再写死（`width(760.dp)` 之类）→ 改 `fillMaxWidth()` / `widthIn(max = …)`
- [ ] 页 padding 竖屏用 `16.dp`，横屏保持原值（当前基线是 `32.dp`）
- [ ] 一行放不下的「返回 + 标题 + N 个操作」→ 竖屏**拆两行**，操作行加 `horizontalScroll`
- [ ] 多列网格走 `adaptiveColumns(...)`，不写 `GridCells.Fixed(常量)`
- [ ] 对话框走 `responsiveDialogSize(...)`
- [ ] **触摸目标**走 `portraitTouchTarget(x.dp)` 或 `PHONE_TOUCH_TARGET`（§6.1）
- [ ] ⚠️ **小视觉元素（圆点 / 图标 / 进度条）不要直接挂 `clickable`** ——
      改为「外层承担热区 + 内层只做视觉」（§6.5）。`size(8.dp).clickable{}` 这类写法
      由 `SmallTouchTargetScanTest` 门禁兜住（含单行写法），但**表达式尺寸**仍需人工确认
- [ ] 新弹层（`Box` 覆盖层）→ **必须** `RegisterDialogBackHandler(onDismiss)`（方案 §6.3）
- [ ] 页面级 BACK 状态提升到 `NavigationViewModel`（方案 K2），不要藏在页面内部
- [ ] ⚠️ **禁止在 `AppRoot` 顶层订阅 `progress` / `duration`**（方案 K1）：那是 1000ms
      `Handler` 轮询，会导致整棵树每秒重组。需要进度的组件（如 `MiniPlayer`）在**自己内部**订阅
- [ ] 新增 UI 文案**同步** `res/values/strings.xml` 与 `res/values-en/strings.xml`
- [ ] 纯逻辑（列数、方向决策）抽顶层 `internal fun` 并补 JVM 单测

---

## 8. 验证手段（可复现）

> `JAVA_HOME` 取 **本机实测路径** `D:/tools/Android Studio/jbr`（Android Studio 自带 JBR，
> 2026-11-01 验证 `java -version` 输出 OpenJDK 25.0.3）；原 `C:/Program Files/Android/…` 路径本机不存在。
> 换机器时请按 `AGENTS.md` 的「Build (Windows)」段自适应定位。

```bash
# 编译
JAVA_HOME="D:/tools/Android Studio/jbr" \
  ./gradlew.bat :app:compileDebugKotlin --no-daemon \
  -Pkotlin.compiler.execution.strategy=in-process

# 单测（含 UiModeTest 的形态映射 / dp 护栏 + §9 的门禁 + §6.5 的触摸目标扫描）
JAVA_HOME="D:/tools/Android Studio/jbr" \
  ./gradlew.bat testDebugUnitTest --no-daemon \
  -Pkotlin.compiler.execution.strategy=in-process

# lint（低 minSdk 的 API level 错误只有 lint 能抓；基线 0 Error / 96 Warning）
JAVA_HOME="D:/tools/Android Studio/jbr" \
  ./gradlew.bat lintDebug --no-daemon \
  -Pkotlin.compiler.execution.strategy=in-process

# 静态自查：是否还有硬编码大宽度
grep -rnE "\.width\([4-9][0-9]{2}\.dp\)" app/src/main/java/com/lnh/nasmusicmobile/ui

# 静态自查：TV 残留必须为 0（2026-11-01 后标准 material3 是唯一 UI 组件库）
grep -rnE "androidx\.tv|ExperimentalTvMaterial3Api|LEANBACK_LAUNCHER|android\.software\.leanback" \
  app/src/main/java app/src/main/res app/src/main/AndroidManifest.xml
# 期望：仅剩不到 5 处**注释**文字命中，零代码引用

# 静态自查：是否还有裸 44/48/52dp 触摸目标（§6.1；豁免项见 §6.3）
# ⚠️ 下界已从 4x 扩到 0x —— 原来的 40~53dp 区间会漏掉 size(8.dp).clickable 这类
#    更糟的写法（见 §6.5）。命中后逐个确认「是不是触摸目标」。
grep -rnE "\.(size|height|width)\((\s*)([0-3][0-9]|4[0-9]|5[0-3])\.dp\)" app/src/main/java/com/lnh/nasmusicmobile/ui \
  | grep -vE "Karaoke|MvPlayback|VisualEqualizer|Shimmer|Cover|cover|favicon|Spacer"
# ⚠️ 上面这条对「尺寸是表达式」的写法无能为力（size(if (a) 8.dp else 6.dp)）→
#    改用下面这个脚本（已入库 scripts/，与 check_chinese.py 同级；自带负向自证）
python scripts/audit_small_touch_target.py --selftest   # 先自证脚本有效（7 用例）
python scripts/audit_small_touch_target.py              # 从项目根实跑，期望 0 处
# ⚠️ 手工兜底：任何小尺寸（图标 / 圆点 / 进度条）直接挂 clickable 都属违规，
#    应改为「外层 size(portraitTouchTarget(...)) 承担热区 + 内层只做视觉」（§6.5）
# 📌 同规则的单测门禁：SmallTouchTargetScanTest（跑 testDebugUnitTest 即生效，无需记得跑脚本）
```

**B1 回归护栏**：任何"给竖屏加分支"的改动，都要能回答
「`else` 分支是否与改动前逐字一致」。抽取公共子组件是推荐做法 —— 行为零变化，且 diff 可审
（本次改造里 `TvTopNavBar` 即改名为 `AppTopNavBar`，属于"只改名、不动行为"的先例）。

---

## 9. 门禁：main 源集必须保留手机竖屏形态（2026-11-01 重写）

**"删竖屏"编译过、单测过，只有真机才看得见** —— 所以由自动门禁兜住。

### 门禁在哪

`app/src/test/java/com/lnh/nasmusicmobile/ui/ScreenUiModeCoverageTest.kt`
（跑在 `testDebugUnitTest` 里，CI 的 `test` job 已阻塞）

```bash
JAVA_HOME="D:/tools/Android Studio/jbr" \
  ./gradlew.bat testDebugUnitTest --no-daemon \
  -Pkotlin.compiler.execution.strategy=in-process --tests "*ScreenUiModeCoverageTest"
```

### 判定规则（**正向**，与 2026-10-28 版相反）

main 源集（`app/src/main/java/com/lnh/nasmusicmobile`）的全部 `.kt` 文本中，
以下标识符必须**至少各出现一次**（大小写敏感）：

| 标识符 | 守护的东西 |
|---|---|
| `PhonePortrait` | `UiMode` 枚举值 + `deriveUiMode` 分支 |
| `LocalUiPortrait` | 竖屏 CompositionLocal |
| `PhoneNavBar` | 竖屏底部导航 |
| `MiniPlayer` / `PhoneTopBar` | 竖屏迷你播放器 / 竖屏顶栏 |

缺任何一个即判失败，并在消息里列出缺失项。**前身是 2026-10-28 的「扫描竖屏残留」反向门禁**，
竖屏恢复为优先形态后方向反转：现在守的是「别再把竖屏当死代码删掉」。

### 自证

- 负向用例：把只含 `UiMode.PhoneLandscape` 的样本喂给同一套 `REQUIRED_TOKENS`，必须判违规
- 空转断言：扫不到任何 `.kt` 时**直接失败**（不会静默跳过）；
  `user.dir` 已覆盖「模块目录 / 仓库根 / 上一级」三种情形

⚠️ **不要重新引入「marker 白名单」式判定**（旧版按 `LocalUiMode` / `AdaptiveLayout(` /
`responsiveDialogSize(` 等 marker 放行）：`AdaptiveLayout` 已删除，marker 名单会持续漂移。
当前正向判定更简单，也更能表达"竖屏是默认形态"这个事实。

### ⚠️ 为什么不是自定义 lint 规则（方案原文建议 `tools/lint/`）

实测在**当前工具链（AGP 9.2.1 + `com.android.tools.lint` 32.2.1）** 下不可行：

- 自定义 check jar 的类由 `com.intellij.util.lang.UrlClassLoader` 加载，
  而 `SourceCodeScanner` 由 `java.net.URLClassLoader` 加载 → 两个类加载器各持一份 `lint-api`
- 结果：`PortraitScreenUiModeDetector cannot be cast to SourceCodeScanner`，
  `:app:lintAnalyzeDebug` 直接失败
- **不是配置错误**：`lintChecks` 依赖树只有 `project :tools:lint`；check jar 里也只有自己的类 +
  `META-INF/services/...IssueRegistry`，**没有**打包 lint-api

单测门禁零新增依赖、零类加载风险。将来若工具链修好，可原样搬回 `tools/lint/`。

---

## 10. ⛔ 内容色契约：`FocusableSurface` 必须同时下发两个 CompositionLocal

**这是真机反馈「下方主按钮 / 标题行按钮看不清」的根因，也是本项目最容易复发的 UI 缺陷之一。**
（2026-11-01 起组件库改为标准 `androidx.compose.material3`，契约本身不变，只换了包名。）

### 根因

`androidx.compose.material3.LocalContentColor` 的**默认值是 `Color.Black`**：

```kotlin
// androidx/compose/material3/ContentColor.kt
val LocalContentColor = compositionLocalOf { Color.Black }
```

而 `material3` 的 `Icon` / `Text` **都会回退到它**：

```kotlin
// Icon.kt
tint: Color = LocalContentColor.current
// Text.kt
color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }
```

`FocusableSurface` 早期**只**提供自定义的 `LocalFocusableContentColor`，于是它内部凡是
**没显式写 `tint =` / `color =`** 的 `Icon` / `Text` 全部画成**黑色** ——
压在深色底（`Surface #162032` / `SurfaceVariant #1E2D42`）上就是"看不见"。

### 约定

```kotlin
CompositionLocalProvider(
    LocalFocusableContentColor provides targetContentColor,   // 项目自有：给显式读它的组件
    LocalContentColor provides targetContentColor,            // material3 官方：给 Icon/Text 兜底
) { content() }
```

**任何自建容器组件**（`FocusableSurface` 之外新写的 Surface/Card/Button 封装）只要内部可能放
裸 `Icon` / `Text`，就**必须**同时下发这两个 —— 少一个就会出现黑字压深底。

### 检查手段

```bash
# ① 门禁（含 4 组负向自证，防 import 假通过）
JAVA_HOME="D:/tools/Android Studio/jbr" \
  ./gradlew.bat testDebugUnitTest --no-daemon \
  -Pkotlin.compiler.execution.strategy=in-process --tests "*FocusableSurfaceColorContractTest"

# ② 人工复核：确认 FocusableSurface 内容体外层两个 provides 都在
grep -n "LocalContentColor provides\|LocalFocusableContentColor provides" \
  app/src/main/java/com/lnh/nasmusicmobile/ui/components/FocusableSurface.kt
```

---

## 11. 焦点视觉：**已随 TV 移除**（⛔ 不要重新引入）

原「TV 遥控焦点视觉」整条链路在 2026-11-01 单平台化时删除：

| 已删除 | 曾经的职责 |
|---|---|
| `ui/components/FocusIndicator.kt` | `shouldShowFocusVisuals()`：四判据（无触摸屏 / leanback 或 television / 存在 `SOURCE_DPAD` 输入设备 / `DpadInputTracker`） |
| `DpadInputTracker` | 运行中收到方向/确认键后翻转 |
| `Modifier.focusRing(...)` | 外侧柔光带 + 内侧实心环 |
| `FocusIndicatorContractTest` | 修饰符链顺序 / 判据完整性 / D-PAD 探测缓存 |

**现状（源码事实）**：

- `FocusableSurface` 内 `val tvDevice = false`，`val activeFocus = isFocused && tvDevice` ⇒ **恒 false**；
  缩放 / 焦点环 / `focusedContainerColor` / `focusedContentColor` 全部关闭，
  只剩 `pressed*` 系列承担触摸按下反馈。
- `isTVDevice()` 仍保留在 `FocusableSurface.kt`，但**恒返回 `false`**（少量调用点据此恒走手机分支）；
  ⛔ 不要给它加回 leanback / television 嗅探。

### 为什么这条规则仍然必须遵守

`Modifier.clickable` / `focusable()` 的节点在**手机上点一下就会获得焦点，而且焦点会粘住**
（直到点别处才移走）。若把焦点视觉直接绑到 `isFocused`，会出现三个莫名其妙的现象：

- 按钮被点过一次后**永久放大**（P2-34 已修）
- 点过的那一项**永久保持高亮容器色**（如底栏 / 顶栏图标）
- 点过的那一项**永久保持聚焦文字色**

所以**任何新组件都不得直接用 `isFocused` 做视觉**，只保留 `isPressed`。

### ⚠️ `onFocusChanged` 里别读外层派生的值

`onFocusChanged { }` 的 lambda **捕获的是本次组合的值**，焦点刚变化时读到的还是旧值。
需要立即生效的动画必须用回调参数本身：

```kotlin
.onFocusChanged { state ->
    isFocused = state.hasFocus          // ✅ 用 lambda 参数
}
```

未来若真的要做「外接键盘 / 手柄导航」的无障碍支持，先读
`docs/archive/verification/` 的历史判据设计，再按 §2 的形态分叉写，
⛔ 不要复活 `shouldShowFocusVisuals()` 的四判据嗅探。
