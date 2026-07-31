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

class HymnalsImportConditions {
    enum class State {
        NONE,
        IMPORTING,
        IMPORTING_SHEETS,
        IMPORTING_REMOVING_OLD,
        IMPORTING_REMOVING_COMPLETED,
        NEW_VERSION_EXISTS,
        FAILED,
        FAILED_HYMNAL_NOT_EXISTS,
        FAILED_ALREADY_EXISTS,
        FAILED_TO_PARSE,
        COMPLETED,
        REMOVING,
        REMOVING_SHEETS,
        REMOVING_FAILED,
        REMOVING_COMPLETED,
    }
    val failed = listOf(
        State.FAILED,
        State.FAILED_TO_PARSE,
        State.FAILED_ALREADY_EXISTS,
        State.FAILED_HYMNAL_NOT_EXISTS,
        State.REMOVING_FAILED
    )
    val completed = listOf(
        State.COMPLETED,
        State.REMOVING_COMPLETED,
        State.IMPORTING_REMOVING_COMPLETED
    )
    val finished = failed + completed
    val importing = listOf(
        State.IMPORTING,
        State.IMPORTING_SHEETS,
        State.IMPORTING_REMOVING_OLD
    )
    val removing = listOf(
        State.REMOVING,
        State.REMOVING_SHEETS
    )
    val progressing = importing + removing
}