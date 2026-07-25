package org.sda.hymnal.data.setting

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings")
    fun getSettings(): List<Settings>

    @Upsert
    suspend fun upsertSetting(settings: Settings)
}