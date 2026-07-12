package org.sda.hymnal.data.setting

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Settings(
    @PrimaryKey val id: Int = 0,
    val hymnal: String,
    @ColumnInfo(name = "font_size") val fontSize: Float
)