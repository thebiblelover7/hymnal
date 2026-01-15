package org.sda.hymnal.screen

import org.sda.hymnal.data.Hymn
import org.sda.hymnal.data.Hymnal
import org.sda.hymnal.data.Hymnals

data class HymnalState(
    val currentScreen: Screen = NavigationScreens.Home,
    val currentHymn: Hymn? = null,
    val currentHymnal: Hymnal = Hymnals.NewEnglish,
    val currentHymns: List<Hymn> = emptyList(),
    val allHymns: List<Hymn> = emptyList(),
    val isLoadingHymns: Boolean = true,
    val currentSearchString: String = ""
)