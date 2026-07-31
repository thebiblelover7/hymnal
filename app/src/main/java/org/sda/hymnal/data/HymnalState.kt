package org.sda.hymnal.data

import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.flow.MutableStateFlow
import org.sda.hymnal.data.hymn.Hymn
import org.sda.hymnal.data.hymnal.DefaultHymnals
import org.sda.hymnal.data.hymnal.Hymnal
import org.sda.hymnal.data.hymnal.HymnalsImportConditions
import org.sda.hymnal.data.playlist.Playlist
import org.sda.hymnal.data.playlist.PlaylistHymn
import org.sda.hymnal.data.setting.Settings
import org.sda.hymnal.screen.NavigationScreens
import org.sda.hymnal.screen.Screen

data class HymnalState(
    val hymnals: SnapshotStateList<Hymnal> = mutableStateListOf(),
    val hymnalCurrentlyRemoving: Hymnal? = null,
    val hymnalsImportState: HymnalsImportConditions.State = HymnalsImportConditions.State.NONE,
    val hymnalImportUri: Uri? = null,
    val currentScreen: Screen = NavigationScreens.Home,
    val currentHymn: Hymn? = null,
    val currentHymnPair: Pair<Hymn, PlaylistHymn?>? = null,
    val currentHymnal: Hymnal = DefaultHymnals.NewEnglish,
    val settings: Settings = Settings(hymnal = DefaultHymnals.NewEnglish.fileName, fontSize = 1f),
    val currentHymns: MutableList<Hymn> = mutableListOf(),
    val searchedHymns: SnapshotStateList<Hymn> = mutableStateListOf(),
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