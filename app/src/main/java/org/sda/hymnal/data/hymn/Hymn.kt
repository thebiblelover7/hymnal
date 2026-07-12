package org.sda.hymnal.data.hymn

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import org.sda.hymnal.data.hymnal.Hymnal
import org.sda.hymnal.data.hymnal.hymnalList

@ProvidedTypeConverter
class HymnConverter(
    private val context: Context
) {
    @TypeConverter
    fun convertToHymn(dbHymn: DbHymn): Hymn {
        val sheetsStr: List<String> = Json.decodeFromString(dbHymn.sheetMusic)
        val sheetsInt = sheetsStr.map { sheetPath ->
            context.resources.getIdentifier(
                sheetPath,
                "drawable",
                context.packageName
            )
        }
        return Hymn(
            title = dbHymn.title,
            hymnal = hymnalList.find { it.fileName == dbHymn.hymnal }!!,
            number = dbHymn.number,
            text = dbHymn.text,
            sheetMusic = sheetsInt,
            sheetMusicStr = dbHymn.sheetMusic,
            favorite = dbHymn.favorite,
            dbHymnEntry = dbHymn,
        )
    }

    @TypeConverter
    fun convertToDbHymn(hymn: Hymn): DbHymn {
        return DbHymn(
            hymnal = hymn.hymnal.fileName,
            number = hymn.number,
            title = hymn.title,
            favorite = hymn.favorite,
            sheetMusic = hymn.sheetMusicStr,
            text = hymn.text
        )
    }
}


val hymnTags = listOf("1.", "2.", "3.", "4.", "5.", "6.", "7.", "8.", "9.", "10.", "CHORUS:", "Refrain", "Coro", "Côro:", "Припев:")
data class Hymn(
    val title: String,
    val hymnal: Hymnal,
    val number: Int,
    val text: String,
    val sheetMusic: List<Int> = emptyList(),
    val favorite: Boolean = false,
    val dbHymnEntry: DbHymn? = null,
    val sheetMusicStr: String = ""
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
    val text: String
)