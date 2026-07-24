package org.sda.hymnal.data

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.sda.hymnal.data.hymn.Hymn
import org.sda.hymnal.data.hymnal.Hymnal
import org.sda.hymnal.data.hymnal.Hymnals
import org.sda.hymnal.data.playlist.Playlist
import org.sda.hymnal.data.playlist.PlaylistHymn
import org.sda.hymnal.data.setting.Settings
import org.sda.hymnal.screen.NavigationScreens
import org.sda.hymnal.screen.Screen

data class HymnalState(
    val currentScreen: Screen = NavigationScreens.Home,
    val currentHymn: Hymn? = null,
    val currentHymnPair: Pair<Hymn, PlaylistHymn?>? = null,
    val currentHymnal: Hymnal = Hymnals.NewEnglish,
    val settings: Settings = Settings(hymnal = Hymnals.NewEnglish.fileName, fontSize = 1f),
    val currentHymns: MutableList<Hymn> = mutableListOf(),
    val searchedHymns: StateFlow<List<Hymn>> = MutableStateFlow(emptyList()),
    val allHymns: List<Hymn> = emptyList(),
    val isInPlaylist: Boolean = false,
    val isLyricsScreen: Boolean = true,
    val isLoadingHymns: Boolean = true,
    val isSearching: Boolean = false,
    val isPerformingSearch: Boolean = false,
    val isSearchActive: Boolean = false,
    val currentSearchNumber: String = "",
    val currentSearchHymn: Hymn? = null,
    val currentSearchString: MutableStateFlow<String> = MutableStateFlow(""),

    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val playlists: SnapshotStateList<Playlist> = mutableStateListOf(),
    val currentPlaylist: Playlist? = null,
    val currentPlaylistPair: SnapshotStateList<Pair<Hymn, PlaylistHymn?>> = mutableStateListOf(),
    val currentPlaylistPlaylistHymns: MutableList<PlaylistHymn> = mutableListOf(),
    val currentPlaylistHymns: MutableList<Hymn> = mutableListOf()
)