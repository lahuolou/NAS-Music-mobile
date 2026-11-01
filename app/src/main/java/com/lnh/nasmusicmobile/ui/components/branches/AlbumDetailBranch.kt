package com.lnh.nasmusicmobile.ui.components.branches

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.lnh.nasmusicmobile.data.model.Screen
import com.lnh.nasmusicmobile.data.model.Song
import com.lnh.nasmusicmobile.ui.viewmodel.MainViewModel
import com.lnh.nasmusicmobile.data.model.*
import com.lnh.nasmusicmobile.ui.screens.*
import com.lnh.nasmusicmobile.ui.screens.library.*
import com.lnh.nasmusicmobile.ui.screens.settings.*
import com.lnh.nasmusicmobile.ui.screens.netdisk.*
import com.lnh.nasmusicmobile.ui.screens.stats.*
import com.lnh.nasmusicmobile.ui.viewmodel.*
/**
 * AlbumDetail 分支提取自 AppRoot（Method too large 根治：分支下沉 branches/）。
 * 分支体逐行搬迁，外层共享状态经参数注入。
 */
@Composable
internal fun AlbumDetailBranch(
    viewModel: MainViewModel,
    onPickSongForPlaylist: (Song) -> Unit
) {
                    // M7 修复：下载进度流下沉到分支内收集（原 AppRoot 顶层收集下传）
                    val songDownloadStates by viewModel.songDownloadStates.collectAsState(initial = emptyMap())
                    val selectedAlbum by viewModel.selectedAlbum.collectAsState(initial = null)
                    val mergedAlbums by viewModel.mergedAlbums.collectAsState(initial = emptyList())
                    val albumSongsCache by viewModel.albumSongsCache.collectAsState(initial = emptyMap())
                    // 用 mergedAlbums 中的实时专辑（含已异步解析封面），避免 openAlbumDetail 时冻结快照无封面
                    val liveAlbum = selectedAlbum?.let { sa -> mergedAlbums.firstOrNull { it.id == sa.id } ?: sa }
                    val albumSongs = liveAlbum?.let { albumSongsCache[it.id] } ?: emptyList()
                    val favoriteIds by viewModel.favoriteIds.collectAsState(initial = emptySet())
                    if (liveAlbum != null) {
                        AlbumDetailScreen(
                            album = liveAlbum,
                            songs = albumSongs,
                            onPlaySong = { song ->
                                val albumSongs = selectedAlbum?.let { viewModel.getAlbumSongsCache(it.id) } ?: listOf(song)
                                viewModel.playQueue(albumSongs, albumSongs.indexOf(song).coerceAtLeast(0))
                                viewModel.navVM.navigateTo(Screen.NowPlaying)
                            },
                            onPlayAll = { songList ->
                                viewModel.playQueue(songList)
                                viewModel.navVM.navigateTo(Screen.NowPlaying)
                            },
                            onBack = { viewModel.navVM.navigateTo(Screen.Library) },
                            queueSongIds = viewModel.queueSongIds.collectAsState(initial = emptySet()).value,
                            onToggleQueue = { song -> viewModel.playerVM.toggleQueueSong(song) },
                            favoriteIds = favoriteIds,
                            onToggleFavorite = { song -> viewModel.toggleNetworkFavorite(song) },
                            onAddToPlaylist = { song -> onPickSongForPlaylist(song) },
                            downloadStates = songDownloadStates,
                            onDownloadSong = { song -> viewModel.downloadVM.downloadSong(song) },
                            onDeleteDownloadSong = { song -> viewModel.downloadVM.deleteDownload(song) }
                        )
                    }
}
