package org.sda.hymnal.data

val hymnTags = listOf("1.", "2.", "3.", "4.", "5.", "6.", "7.", "8.", "9.", "10.", "CHORUS:")
class Hymn(
    val title: String,
    val hymnal: Hymnal,
    val number: Int,
    val text: String,
    val sheetMusic: List<Int> = emptyList(),
)