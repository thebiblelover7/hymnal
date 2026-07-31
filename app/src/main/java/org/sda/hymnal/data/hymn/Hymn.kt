package org.sda.hymnal.data.hymn

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Fts5
import androidx.room3.FtsOptions
import org.sda.hymnal.data.hymnal.Hymnal
import java.io.File

val hymnTags = listOf(
    "1.",
    "2.",
    "3.",
    "4.",
    "5.",
    "6.",
    "7.",
    "8.",
    "9.",
    "10.",
    "CHORUS:",
    "Refrain",
    "Coro",
    "Côro:",
    "Припев:"
)
val hymnTagsExpression = hymnTags.joinToString(
    prefix = "WHEN line1 LIKE '",
    separator = "' OR line1 LIKE '",
    postfix = "' "
)

data class Hymn(
    val title: String,
    val hymnal: Hymnal,
    val number: Int,
    val text: String,
    val sheetMusic: List<Int> = emptyList(),
    val favorite: Boolean = false,
    val sheetMusicStr: String = "",     // Basically only used for conversion back to DbHymn
    val firstLine: String,
    val sheetMusicFiles: List<File?> = emptyList()
)

@Entity(
    primaryKeys = ["hymnal", "number"],
    tableName = "hymns"
)
data class DbHymn(
    val hymnal: String,
    val number: Int,
    val title: String,
    val favorite: Boolean,
    @ColumnInfo(name = "sheet_music") val sheetMusic: String,
    val text: String,
    @ColumnInfo(
        name = "first_line",
        defaultValue = ""
    ) val firstLine: String
)

@Entity(tableName = "hymns_fts")
@Fts5(
    tokenizer = FtsOptions.TOKENIZER_UNICODE61,
    contentEntity = DbHymn::class
)
data class DbHymnFTS(
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(
        name = "first_line",
        defaultValue = ""
    )
    val firstLine: String,
    @ColumnInfo(name = "text")
    val text: String,
)