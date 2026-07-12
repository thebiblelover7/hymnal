package org.sda.hymnal.data.playlist

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.ExperimentalUuidApi

@Entity(tableName = "playlists")
@OptIn(ExperimentalUuidApi::class)
data class Playlist(
    @PrimaryKey
    val id: String,
    val name: String,
    val count: Int
)

@Entity(
    tableName = "playlist_hymns"
)
data class PlaylistHymn(
    @PrimaryKey
    val id: String,
    val hymnal: String,
    val number: Int,
    val playlist: String,
    val position: Int
)


