package org.sda.hymnal.data.setting

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class Settings(
    @PrimaryKey val id: Int = 0,
    val hymnal: String,
    @ColumnInfo(name = "font_size") val fontSize: Float
)