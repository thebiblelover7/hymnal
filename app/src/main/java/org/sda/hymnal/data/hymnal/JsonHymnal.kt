package org.sda.hymnal.data.hymnal

import kotlinx.serialization.Serializable
import org.sda.hymnal.data.hymn.JsonHymn

@Serializable
data class JsonHymnal(
    val id: String,
    val version: Int,
    val metadata: JsonHymnalMetadata,
    val hymns: List<JsonHymn>
)

@Serializable
data class JsonHymnalMetadata(
    val title: String,
    val language: String
)