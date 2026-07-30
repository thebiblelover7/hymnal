package org.sda.hymnal.data.hymn

import kotlinx.serialization.Serializable

@Serializable
data class JsonHymn(
    val number: Int,
    val title: String,
    val originalTitle: String? = null,
    val content: String,
    val author: String? = null
)