package com.lnh.nasmusicmobile.ui.components.branches

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.lnh.nasmusicmobile.data.model.ServerConfig
import com.lnh.nasmusicmobile.ui.viewmodel.MainViewModel
import com.lnh.nasmusicmobile.data.model.*
import com.lnh.nasmusicmobile.ui.screens.*
import com.lnh.nasmusicmobile.ui.screens.library.*
import com.lnh.nasmusicmobile.ui.screens.settings.*
import com.lnh.nasmusicmobile.ui.screens.netdisk.*
import com.lnh.nasmusicmobile.ui.screens.stats.*
import com.lnh.nasmusicmobile.ui.viewmodel.*
/**
 * ServerConnect 分支提取自 AppRoot（Method too large 根治：分支下沉 branches/）。
 * 分支体逐行搬迁，外层共享状态经参数注入。
 */
@Composable
internal fun ServerConnectBranch(
    viewModel: MainViewModel,
    serverConfig: ServerConfig,
    isConnected: Boolean,
    serverDisplayName: String,
    isLoading: Boolean,
    onConnect: (ServerConfig) -> Unit
) {
                    ServerConnectScreen(
                        initialConfig = serverConfig,
                        isConnected = isConnected,
                        serverDisplayName = serverDisplayName,
                        isConnecting = isLoading,
                        onConnect = onConnect,
                        onDisconnect = { viewModel.serverVM.disconnect() }
                    )
}
