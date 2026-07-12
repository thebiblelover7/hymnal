package org.sda.hymnal.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.sda.hymnal.data.hymn.DbHymn
import org.sda.hymnal.data.hymn.HymnDao
import org.sda.hymnal.data.playlist.Playlist
import org.sda.hymnal.data.playlist.PlaylistDao
import org.sda.hymnal.data.playlist.PlaylistHymn
import org.sda.hymnal.data.playlist.PlaylistHymnDao
import org.sda.hymnal.data.setting.SettingDao
import org.sda.hymnal.data.setting.Settings

@Database(
    entities = [DbHymn::class, Settings::class, Playlist::class, PlaylistHymn::class],
    version = 1,
    autoMigrations = [

    ]
)
abstract class HymnDatabase : RoomDatabase() {
    abstract val hymnDao: HymnDao

    abstract val settingDao: SettingDao
    abstract val playlistDao: PlaylistDao
    abstract val playlistHymnDao: PlaylistHymnDao
    companion object {
        @Volatile
        private var Instance: HymnDatabase? = null
        fun getDatabase(context: Context): HymnDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    HymnDatabase::class.java, "hymns"
                ).createFromAsset("databases/hymns.db")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}