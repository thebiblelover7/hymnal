package org.sda.hymnal.screen

import kotlinx.serialization.Serializable

@Serializable
object NavigationScreens {
    @Serializable
    object Home: Screen("Home")
    @Serializable
    object List: Screen("Hymns List")
    @Serializable
    object PlaylistList: Screen("Playlists")
    @Serializable
    object PlaylistSelector: Screen("Select a Playlist")
    @Serializable
    object PlaylistHymnsList: Screen("Hymns in Playlist")
    @Serializable
    object Hymn: Screen("Hymn")
    @Serializable
    object SheetMusic: Screen("Sheet Music")
    @Serializable
    object Settings: Screen("Info")
}