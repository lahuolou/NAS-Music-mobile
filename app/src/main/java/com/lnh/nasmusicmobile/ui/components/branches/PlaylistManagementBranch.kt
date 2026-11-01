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
 * PlaylistManagement 分支提取自 AppRoot（Method too large 根治：分支下沉 branches/）。
 * 分支体逐行搬迁，外层共享状态经参数注入。
 */
@Composable
internal fun PlaylistManagementBranch(
    viewModel: MainViewModel,
) {
                    val playlistsState by viewModel.playlists.collectAsState(initial = UiState.Success(emptyList()))
                    val selectedPlaylistSongsState by viewModel.selectedPlaylistSongs.collectAsState(initial = UiState.Success(emptyList()))
                    PlaylistManagementScreen(
                        playlists = playlistsState.dataOrNull() ?: emptyList(),
                        selectedPlaylistSongs = selectedPlaylistSongsState.dataOrNull() ?: emptyList(),
                        isLoading = false,
                        onSelectPlaylist = { viewModel.selectPlaylist(it) },
                        onCreatePlaylist = { name -> viewModel.createPlaylist(name) },
                        onDeletePlaylist = { viewModel.deletePlaylist(it) },
                        onPlayPlaylist = { playlist ->
                            viewModel.playPlaylist(playlist)
                            viewModel.navVM.navigateTo(Screen.NowPlaying)
                        },
                        onRemoveSong = { songId -> viewModel.removeFromPlaylist(songId) },
                        onBack = { viewModel.navVM.navigateTo(Screen.Library) }
                    )
}
