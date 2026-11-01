# NAS-Music-mobile

一款开源的 **Android 手机 / 平板音乐播放器**（竖屏优先、横屏兼容的触摸形态）：聚合 NAS（Jellyfin / Navidrome / Subsonic / 道理鱼 / 飞牛）、WebDAV / SMB 网络共享、网络音乐、百度网盘、本地音乐等多音乐源，支持逐字卡拉 OK 歌词、K 歌伴奏、MTV 音乐视频、天气电台与全屏可视化等完整功能，面向触摸操作优化（已移除遥控器 D-Pad / 焦点导航等 TV 专属代码）。

![Release](https://img.shields.io/github/v/release/lahuolou/NAS-Music-mobile?color=blue&label=Release)

> ⚠️ **百度网盘目录限制**
>
> 根据百度开放平台 2026 年沙盒策略，第三方应用只能访问 `/apps/NAS-Music-mobile` 目录及其子目录，**无法读取该目录之外的任何文件**。请将音乐文件放入百度网盘的 `/apps/NAS-Music-mobile/` 目录下（例如 `/apps/NAS-Music-mobile/音乐/`），应用内首次使用时会自动创建该目录。放在 `/apps/NAS-Music-mobile/` 之外的文件将无法被本应用访问。

## 功能特性

### 多音乐源

- **NAS**：Jellyfin / Navidrome / Subsonic / 道理鱼 / 飞牛 五种后端，含 CUE 整轨与 HLS 流媒体
- **WebDAV / SMB**：网络共享目录浏览与播放，直接对接 NAS 上的 WebDAV / SMB 共享（SMB 支持 HTTP 流式播放）
- **网络音乐**：在线搜索与播放，5 档音质（自动 / 128 / 192 / 320 / 无损 FLAC），档位不可用时自动降级；搜索命中艺人时可续页取该艺人的全部作品
- **本地音乐**：自动扫描设备存储，Room 索引持久化，增量扫描识别新增 / 更新 / 删除，与 NAS 音乐同一视图浏览
- **百度网盘**：设备码 OAuth 授权，目录浏览、搜索、直链串流
- **独立音乐（Jamendo）**：CC 授权音库，热门榜 + 风格筛选
- **电台**：公共电台目录，标签筛选，直播流即点即播
- **跨源搜索**：NAS / 网络 / 百度 / Jamendo 四源独立开关，并行搜索合并去重，拼音搜索恒启用

### 播放体验

- **播放控制**：顺序 / 单曲循环 / 列表循环 / 随机四种模式，队列增删改与排序，队列持久化（重启自动恢复）
- **音质**：播放页显示当前档位或真实码率，网络歌曲可随时切换档位，支持单曲指定档位
- **应用内音量**：独立于系统音量（0–100%），设置页与播放页均可调节，跨曲交叉淡入自动适配
- **均衡器与可视化**：均衡器预设与频段调节；全屏沉浸式可视化 30+ 套特效（含 three-globe 3D 地球版「世界」，WebView + WebGL 完全离线渲染，参考 [three-globe](https://github.com/vasturiano/three-globe) 与 [three.js](https://github.com/mrdoob/three.js)）；可视化画质可按设备性能分低 / 中 / 高三档选择
- **照片墙可视化**：图库 / 外接存储 / Jellyfin 三来源照片全屏轮播，43 种转场随机切换，可选「仅显示含人像」
- **K 歌**：人声消除伴奏模式、原唱 / 伴奏切换、升降调、播放变速
- **MTV 音乐视频**：播放页一键进入全屏 MV（B 站搜索），切歌自动预搜
- **天气电台**：按实时天气（晴 / 雨 / 雪 / 风 / 阴 / 夜晚）从曲库匹配心情电台，无 NAS 也可用
- **播放统计**：记录播放次数与最后播放时间，驱动「最近播放」

### 歌词

- 逐行滚动 + **逐字高亮**（卡拉 OK 效果），大屏适配
- 多来源获取：内嵌 / 本地 LRC / 网络匹配 / 缓存，可切换来源
- 字号缩放与高亮模式记忆

### 曲库浏览

- 搜索：拼音首字母（`zjl` → 周杰伦）+ 子串匹配
- 发现页：语种 / 纯音乐 / 年代 / 情怀 / 风格 / 主题六维度筛选
- 专辑 / 艺术家 / 流派 / 年代 网格浏览与详情页
- 收藏、最近播放、自建播放列表
- 歌单导入：M3u / 网易云 / JSON / TXT，歌单内 HTTP 直链可直接播放

### 手机体验

- **触摸优先**：按钮 / 列表点击直接响应，按下即时反馈（`pressed*` 视觉），无遥控器焦点环 / D-Pad 导航
- **播放页沉浸模式**：5 秒无操作自动隐藏控制栏，轻触屏幕唤醒并显示控制栏
- 系统栏自动隐藏（横屏沉浸）、前台媒体通知与锁屏控制
- 文本输入使用系统 IME（触摸键盘），搜索历史可用

## 界面预览

- **首页**：当前播放、最近播放、实时天气、均衡器频谱预览
- **播放页**：封面 + 歌词、封面滤镜、进度条与控制按钮、可视化、歌曲详情
- **曲库页**：搜索 / 发现 / 电台 / 专辑 / 艺术家 / 歌曲 / 流派 / 年代
- **详情页**：专辑与艺术家详情，歌曲列表 + 播放全部
- **队列页**：播放队列管理
- **我的页**：收藏 + 最近播放 + 本地歌单
- **设置页**：主题、动画、播放模式、缓存、均衡器、歌词缩放、封面滤镜、天气 API Key、各音乐源端点
- **连接页**：五种 NAS 后端 + WebDAV / SMB，默认端口自动切换
- **关于页**：后端类型 + API 版本号 + 连接状态

## 构建与运行

```
# 设置 JAVA_HOME（以 JDK 17 为例）
export JAVA_HOME="C:\Program Files\Java\jdk-17.0.21.0.2"

# Debug APK
./gradlew assembleDebug

# Release APK（需本地 keystore.properties，含 storeFile / storePassword / keyAlias / keyPassword / cryptoPassphrase）
./gradlew assembleRelease

# 运行测试
./gradlew test

# 推送到手机（将 <手机序列号> 替换为你的设备序列号）
adb -s <手机序列号> install -r app/build/outputs/apk/debug/app-debug.apk
```

## 已知限制

- **照片墙「外接存储」默认关闭**：手机版默认只读图库与 Jellyfin 照片源，「外接存储」开关需在「设置 → 照片墙」手动启用后选择目录
- **仅显示含人像**在低画质档不可用（低端设备性能取舍，设置项自动置灰）
- 人脸检测模型与结果库独立于音乐库（`photo_face.db`），「清除人脸结果」只删检测结果、不动照片

## 鸣谢与衍生说明

本项目是 **hxzhang2000 原创 **[NASMusicTV](https://github.com/hxzhang2000/NASMusicTV)** 的衍生分支（提取手机版）**，保留原版 GPL v3 协议与全部既有功能，在此向原创作者致谢。在原版基础上，本项目进行了以下改动：

### 新增功能

- **手机版提取**：从 NASMusicTV 提取为独立的 Android 手机 / 平板版本，包名 `com.lnh.nasmusicmobile`，触摸交互优先，应用名 NAS Music Mobile
- **WebDAV / SMB 网络共享音乐源**：新增 `backend/files` 适配层（WebDAV 客户端 / SMB 客户端 / 应用内 loopback HTTP 桥），「服务器连接」页支持 WebDAV 与 SMB / Windows 共享直连 NAS 目录
- **百度网盘开放平台 OAuth 授权编辑框**：「服务器连接」页可配置百度网盘开放平台 OAuth 授权（设备码授权流程，授权凭据可编辑），配合沙盒目录 `/apps/NAS-Music-mobile` 使用
- **本地音乐增量扫描**：按内容 URI 线性比对识别新增 / 更新 / 删除歌曲，封面缓存按需失效；补齐 `.opus` 文件系统扫描支持

### 优化改进

- **播放链路稳定性**：协程取消语义修正（取消不再被误报为网络失败）、Future ↔ Job 双向取消、搜索缓存有界 LRU 与单调时钟、随机播放历史统一（`ShuffleHistory`）
- **大曲库封面加载**：封面提取移出主线程（`produceState` + IO 调度）、歌手歌曲增量索引（`ArtistSongIndex`）、封面预取节流与代际取消、封面缓存键含文件时间戳自动失效
- **本地扫描可靠性**：时间单位统一（下载入库毫秒 → Unix 秒）、全量刷新保留未变歌曲的封面缓存

### 移除与弱化

- **TV 专属代码移除**（手机版提取时清理）：
  - 遥控器焦点体系：`FocusIndicator`（焦点环 / 焦点视觉判据）、`DpadInputTracker`（方向键追踪）、`focusRing()` 修饰符及相关测试 —— 触摸设备不显示遥控焦点视觉
  - TV 设备判据：`isTVDevice` / `isTelevisionDevice` / `isStaticRemoteDevice` 等 leanback / television 特性嗅探全部收敛为手机路径（恒 false / 恒启用手机分支）
  - Manifest TV 声明：`android.software.leanback` uses-feature、`LEANBACK_LAUNCHER` intent、`android:banner` / `android:logo`（banner 横幅图）与 leanback 依赖库
  - 形态因子：`UiMode` 由「设备类型 + 当前方向」推导，竖屏走 `PhonePortrait`、横屏走 `PhoneLandscape`；TV 形态已整体移除，`MainActivity` 不再锁定方向
- **手机端弱化项恢复**：手机端系统栏沉浸策略、系统 IME 输入、照片墙图库来源、拼音搜索等在 TV 提取时一并回归手机形态

## 开源协议

GPL v3
