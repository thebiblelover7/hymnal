@file:OptIn(ExperimentalUuidApi::class)

package org.sda.hymnal.data.playlist

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Upsert
import kotlin.uuid.ExperimentalUuidApi

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists")
    fun getAll(): MutableList<Playlist>

    @Upsert
    suspend fun upsertPlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

}

@Dao
interface PlaylistHymnDao {
    @Query("SELECT * FROM playlist_hymns WHERE playlist = :playlist")
    fun getPlaylist(playlist: String): MutableList<PlaylistHymn>

    @Upsert
    suspend fun upsertPlaylistHymn(playlistHymn: PlaylistHymn)

    @Delete
    suspend fun deletePlaylistHymn(playlistHymn: PlaylistHymn)
}