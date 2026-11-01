package com.lnh.nasmusicmobile.ui.components.branches

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.lnh.nasmusicmobile.data.model.Screen
import com.lnh.nasmusicmobile.ui.viewmodel.MainViewModel
import com.lnh.nasmusicmobile.data.model.*
import com.lnh.nasmusicmobile.ui.screens.*
import com.lnh.nasmusicmobile.ui.screens.library.*
import com.lnh.nasmusicmobile.ui.screens.settings.*
import com.lnh.nasmusicmobile.ui.screens.netdisk.*
import com.lnh.nasmusicmobile.ui.screens.stats.*
import com.lnh.nasmusicmobile.ui.viewmodel.*
/**
 * Equalizer 分支提取自 AppRoot（Method too large 根治：分支下沉 branches/）。
 * 分支体逐行搬迁，外层共享状态经参数注入。
 */
@Composable
internal fun EqualizerBranch(
    viewModel: MainViewModel,
    settings: com.lnh.nasmusicmobile.data.model.AppSettings
) {
                    val equalizerPreset by viewModel.equalizerPreset.collectAsState(initial = EqualizerPreset.NORMAL)
                    val equalizerBands by viewModel.equalizerBands.collectAsState(initial = emptyList())
                    EqualizerScreen(
                        presets = EqualizerPreset.values().toList(),
                        currentPreset = equalizerPreset,
                        currentBands = equalizerBands,
                        onSelectPreset = { viewModel.setEqualizerPreset(it) },
                        onAdjustBand = { index, value -> viewModel.setEqualizerBand(index, value) },
                        onBack = { viewModel.navVM.navigateTo(Screen.Settings) },
                        visualizerTheme = settings.visualizerTheme
                    )
}
