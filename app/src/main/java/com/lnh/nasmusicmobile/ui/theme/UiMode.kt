package com.lnh.nasmusicmobile.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 形态因子（Form Factor）。
 *
 * - [TV]：电视端（含只上报 `android.hardware.type.television` 的非认证盒子）
 * - [PhonePortrait]：手机竖屏（2026-11-01 手机单平台化恢复：竖屏优先，横屏兼容）
 * - [PhoneLandscape]：手机横屏（沿用原横屏布局代码路径）
 *
 * 形态由设备类型 + 当前屏幕方向共同决定（见 [deriveUiMode]）。Manifest 不再锁定
 * 方向，手机默认竖屏启动、旋转到横屏时跟随切换形态。
 */
enum class UiMode { TV, PhonePortrait, PhoneLandscape }

/** 当前形态因子（由 `MainActivity` 提供，默认 TV 以保持既有测试/预览行为） */
val LocalUiMode = staticCompositionLocalOf { UiMode.TV }

/** 当前是否为手机竖屏形态（各页面按需读取，避免重复比较枚举） */
val LocalUiPortrait = staticCompositionLocalOf { false }

/**
 * 纯函数：设备类型 + 屏幕方向 → 形态因子。
 *
 * TV 判定优先；非 TV 按方向区分 [UiMode.PhonePortrait] / [UiMode.PhoneLandscape]。
 *
 * 不依赖 Compose / Android 运行时，因此可在纯 JVM 单测中直接断言。
 */
fun deriveUiMode(isTV: Boolean, isPortrait: Boolean): UiMode = when {
    isTV -> UiMode.TV
    isPortrait -> UiMode.PhonePortrait
    else -> UiMode.PhoneLandscape
}
