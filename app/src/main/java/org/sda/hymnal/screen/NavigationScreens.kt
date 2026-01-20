package org.sda.hymnal.screen

import kotlinx.serialization.Serializable

@Serializable
object NavigationScreens {
    @Serializable
    object Home: Screen("Home")
    @Serializable
    object List: Screen("Hymns List")
    @Serializable
    object Hymn: Screen("Hymn")
    @Serializable
    object SheetMusic: Screen("Sheet Music")
}