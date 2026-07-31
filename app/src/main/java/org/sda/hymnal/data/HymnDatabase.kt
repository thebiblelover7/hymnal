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
import org.sda.hymnal.data.hymnal.Hymnal
import org.sda.hymnal.data.hymnal.HymnalDao
import org.sda.hymnal.data.playlist.Playlist
import org.sda.hymnal.data.playlist.PlaylistDao
import org.sda.hymnal.data.playlist.PlaylistHymn
import org.sda.hymnal.data.playlist.PlaylistHymnDao
import org.sda.hymnal.data.setting.SettingDao
import org.sda.hymnal.data.setting.Settings

@Database(
    entities = [DbHymn::class, DbHymnFTS::class, Settings::class, Playlist::class, PlaylistHymn::class, Hymnal::class],
    version = 6,
    autoMigrations = [
        AutoMigration(      // Add FTS5 table for search
            from = 1,
            to = 2,
            spec = Migrations.AutoMigrationRebuildFts::class
        ),
        AutoMigration(      // Add first_line column to hymns
            from = 2,
            to = 3,
            spec = Migrations.AutoMigration2To3::class
        ),
        AutoMigration(      // Add first_line to FTS5 for search priority
            from = 3,
            to = 4,
            spec = Migrations.AutoMigrationRebuildFts::class
        ),
        AutoMigration(      // Switch to unicode61 tokenizer for FTS5
            from = 4,
            to = 5,
            spec = Migrations.AutoMigrationRebuildFts::class
        ),
        AutoMigration(      // Add hymnals table to support adding custom hymnals
            from = 5,
            to = 6,
            spec = Migrations.AutoMigration5To6::class
        )
    ]
)
abstract class HymnDatabase : RoomDatabase() {
    abstract val hymnDao: HymnDao
    abstract val hymnalDao: HymnalDao

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
                            connection.prepare("INSERT INTO hymns_fts(hymns_fts) VALUES('rebuild')")
                                .use { statement ->
                                    statement.step()
                                }
                        }
                    })
                    .build()
                    .also { Instance = it }
            }
        }
    }
}