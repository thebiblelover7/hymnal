package org.sda.hymnal.data.hymnal

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(
    tableName = "hymnals"
)
open class Hymnal(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "file_name")
    val fileName: String,
    val version: Int,
    val title: String,
    val userAdded: Boolean = true       // if user added this hymnal
)