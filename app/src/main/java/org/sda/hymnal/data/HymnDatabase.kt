package org.sda.hymnal.data

import android.content.Context
import androidx.room3.AutoMigration
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.sda.hymnal.data.hymn.DbHymn
import org.sda.hymnal.data.hymn.DbHymnFTS
import org.sda.hymnal.data.hymn.HymnDao
import org.sda.hymnal.data.playlist.Playlist
import org.sda.hymnal.data.playlist.PlaylistDao
import org.sda.hymnal.data.playlist.PlaylistHymn
import org.sda.hymnal.data.playlist.PlaylistHymnDao
import org.sda.hymnal.data.setting.SettingDao
import org.sda.hymnal.data.setting.Settings

@Database(
    entities = [DbHymn::class, DbHymnFTS::class, Settings::class, Playlist::class, PlaylistHymn::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
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
                )
                    .setDriver(BundledSQLiteDriver())
                    .createFromAsset("databases/hymns.db")
                    .addCallback(object : Callback() {
                        override suspend fun onCreate(connection: SQLiteConnection) {
                            super.onCreate(connection)
                            connection.prepare("INSERT INTO hymns_fts(hymns_fts) VALUES('rebuild')").use { statement ->
                                statement.step()
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}