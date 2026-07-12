package org.sda.hymnal.data.hymn

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update

@Dao
interface HymnDao {
    @Query("SELECT * FROM hymns")
    fun getAll(): List<DbHymn>

    @Query("SELECT * FROM hymns WHERE hymnal = :hymnal AND number = :number LIMIT 1")
    fun getHymn(hymnal: String, number: Int): DbHymn

    @Query("SELECT * FROM hymns WHERE hymnal = :hymnal")
    fun getHymnal(hymnal: String): MutableList<DbHymn>

    @Update
    suspend fun setHymn(dbHymn: DbHymn)
}