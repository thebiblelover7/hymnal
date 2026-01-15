package org.sda.hymnal.data

import kotlinx.serialization.Serializable

@Serializable
data class JsonHymn(
    val number: Int,
    val title: String,
    val content: String,
    val author: String? = null
)