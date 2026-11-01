package com.lnh.nasmusicmobile.ui

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 「手机竖屏形态」覆盖门禁（2026-11-01 手机单平台化恢复竖屏后重写）。
 *
 * 前身是 2026-10-28 竖屏下线时的**反向残留扫描**；竖屏恢复为优先形态后方向反转：
 * main 源集必须始终保留竖屏形态的判据与壳层组件，防止后来者又把它们当"死代码"删掉。
 *
 * 判定规则：以下标识符必须至少各出现一次（大小写敏感）——
 * - `UiMode.PhonePortrait`（形态枚举与派生入口）
 * - `LocalUiPortrait`（CompositionLocal）
 * - `PhoneNavBar` / `MiniPlayer` / `PhoneTopBar`（竖屏壳层组件）
 */
class ScreenUiModeCoverageTest {

    @Test
    fun `main 源集必须保留手机竖屏形态`() {
        val roots = listOf(
            File("src/main/java/com/lnh/nasmusicmobile"),
            File("app/src/main/java/com/lnh/nasmusicmobile"),
            File("../app/src/main/java/com/lnh/nasmusicmobile"),
        ).firstOrNull { it.isDirectory }
        assertNotNull("找不到 main 源码目录（user.dir=${File("").absolutePath}）", roots)
        val root = requireNotNull(roots)

        val text = buildString {
            root.walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .forEach { appendLine(it.readText()) }
        }
        assertTrue("没有扫到任何源文件 —— 源目录定位可能失败：${root.absolutePath}", text.isNotEmpty())

        val missing = REQUIRED_TOKENS.filter { !text.contains(it) }
        assertTrue(
            "手机竖屏形态被移除了（2026-11-01 起为默认形态），缺失标识符：" +
                missing.joinToString(", ") +
                "\n修法：恢复对应枚举值 / CompositionLocal / 壳层组件。",
            missing.isEmpty(),
        )
    }

    /** 负向用例：护栏本身必须有效（缺失任一标识符都要判红）。 */
    @Test
    fun `缺失竖屏标识符必须被判违规`() {
        val sample = "package p\nfun f() = UiMode.PhoneLandscape"
        val missing = REQUIRED_TOKENS.filter { !sample.contains(it) }
        assertTrue(
            "护栏失效：缺少竖屏标识符的源码没被判违规",
            missing.isNotEmpty(),
        )
    }

    private val REQUIRED_TOKENS = listOf(
        "PhonePortrait",
        "LocalUiPortrait",
        "PhoneNavBar",
        "MiniPlayer",
        "PhoneTopBar",
    )
}
