package org.sda.hymnal.data.setting

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings")
    fun getSettings(): List<Settings>

    @Upsert
    suspend fun upsertSetting(settings: Settings)
}