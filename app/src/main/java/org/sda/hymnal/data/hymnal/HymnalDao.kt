package org.sda.hymnal.data.hymnal

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Upsert

@Dao
interface HymnalDao {
    @Query(
        "SELECT * FROM hymnals"
    )
    fun getAll(): MutableList<Hymnal>

    @Upsert
    fun addHymnal(hymnal: Hymnal)

    @Delete
    fun deleteHymnal(hymnal: Hymnal)
}