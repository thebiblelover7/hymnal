package org.sda.hymnal.data.hymn

import kotlinx.serialization.Serializable

@Serializable
data class JsonHymn(
    val number: Int,
    val title: String,
    val content: String,
    val author: String? = null
)