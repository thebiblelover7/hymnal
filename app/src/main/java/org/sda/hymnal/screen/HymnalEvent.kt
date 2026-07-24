package org.sda.hymnal.screen

import org.sda.hymnal.data.hymn.Hymn
import org.sda.hymnal.data.hymnal.Hymnal
import org.sda.hymnal.data.playlist.Playlist
import org.sda.hymnal.data.playlist.PlaylistHymn
import org.sda.hymnal.data.setting.Settings

sealed interface HymnalEvent {
    data class AddPlaylist(val playlistName: String): HymnalEvent
    data class RenamePlaylist(val playlist: Playlist, val name: String): HymnalEvent
    data class DeletePlaylist(val playlist: Playlist): HymnalEvent
    data class AddHymnToPlaylist(val hymnPair: Pair<Hymn, PlaylistHymn?>, val playlist: Playlist): HymnalEvent
    data class RemoveHymnFromPlaylist(val hymnPair: Pair<Hymn, PlaylistHymn?>, val playlist: Playlist): HymnalEvent
    data class MoveHymnInPlaylist(val hymnPair: Pair<Hymn, PlaylistHymn?>, val playlist: Playlist, val moveBy: Int): HymnalEvent
    data class SetCurrentScreen(val screen: Screen): HymnalEvent
    data class SetCurrentHymn(val hymnPair: Pair<Hymn, PlaylistHymn?>?): HymnalEvent
    data class SetCurrentSheetMusic(val resources: List<Int>): HymnalEvent
    data class SetInPlaylist(val inPlaylist: Boolean): HymnalEvent
    data class LoadPlaylist(val playlist: Playlist): HymnalEvent
    object LoadSettings: HymnalEvent
    data class SetSettings(val settings: Settings): HymnalEvent
    data class LoadHymns(val hymns: List<Hymn>): HymnalEvent
    data class SetLoadingHymns(val isLoadingHymns: Boolean): HymnalEvent
    data class SetCurrentHymnal(val hymnal: Hymnal): HymnalEvent
    data class SetFontSize(val fontSize: Float): HymnalEvent
    data class SetSearchActive(val isSearchActive: Boolean): HymnalEvent
    data class EnableSearch(val isSearching: Boolean): HymnalEvent
    data class SetSearchNumber(val searchNumber: String): HymnalEvent
    object SetFavorite: HymnalEvent
    data class SetLyricsScreen(val isLyricsScreen: Boolean): HymnalEvent
    data class SetSearchHymn(val searchHymn: Hymn?): HymnalEvent
    data class SetSearchString(val searchString: String): HymnalEvent
    data class ShowSnackbar(val message: String): HymnalEvent
}