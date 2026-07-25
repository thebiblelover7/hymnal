package org.sda.hymnal.data.hymn

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Update

@Dao
interface HymnDao {
    @Query("SELECT * FROM hymns")
    fun getAll(): List<DbHymn>

    @Query("SELECT * FROM hymns WHERE hymnal = :hymnal AND number = :number LIMIT 1")
    fun getHymn(hymnal: String, number: Int): DbHymn

    @Query("SELECT * FROM hymns WHERE hymnal = :hymnal")
    fun getHymnal(hymnal: String): MutableList<DbHymn>

    @Query("""
        SELECT *
        FROM hymns
        JOIN hymns_fts ON hymns.rowid = hymns_fts.rowid
        WHERE hymns_fts MATCH :searchQuery
    """)
    suspend fun searchHymns(searchQuery: String): MutableList<DbHymn>

    @Query("""
        SELECT *, bm25(hymns_fts, 5.0, 1.0) AS score
        FROM hymns
        JOIN hymns_fts ON hymns.rowid = hymns_fts.rowid
        WHERE hymnal = :hymnal
            AND hymns_fts MATCH :query
            AND score <= :scoreThreshold
        ORDER BY score
    """)
    suspend fun searchBMHymns(
        query: String,
        hymnal: String,
        scoreThreshold: Double = -5.0
    ): MutableList<DbHymn>
    @Update
    suspend fun setHymn(dbHymn: DbHymn)
}