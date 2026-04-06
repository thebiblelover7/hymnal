package org.sda.hymnal.screen

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.sda.hymnal.data.Hymn
import org.sda.hymnal.data.Hymnal
import org.sda.hymnal.data.Hymnals

data class HymnalState(
    val currentScreen: Screen = NavigationScreens.Home,
    val currentHymn: Hymn? = null,
    val currentHymnal: Hymnal = Hymnals.NewEnglish,
    val currentHymns: List<Hymn> = emptyList(),
    val searchedHymns: StateFlow<List<Hymn>> = MutableStateFlow(emptyList()),
    val allHymns: List<Hymn> = emptyList(),
    val isLoadingHymns: Boolean = true,
    val isSearching: Boolean = false,
    val isPerformingSearch: Boolean = false,
    val currentSearchNumber: String = "",
    val currentSearchHymn: Hymn? = null,
    val currentSearchString: MutableStateFlow<String> = MutableStateFlow("")
)