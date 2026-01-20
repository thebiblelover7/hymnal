package org.sda.hymnal.screen

import org.sda.hymnal.data.Hymn
import org.sda.hymnal.data.Hymnal

sealed interface HymnalEvent {
    data class SetCurrentScreen(val screen: Screen): HymnalEvent
    data class SetCurrentHymn(val hymn: Hymn?): HymnalEvent
    data class SetCurrentSheetMusic(val resources: List<Int>): HymnalEvent
    data class LoadHymns(val hymns: List<Hymn>): HymnalEvent
    data class SetLoadingHymns(val isLoadingHymns: Boolean): HymnalEvent
    data class SetCurrentHymnal(val hymnal: Hymnal): HymnalEvent
    data class SetSearchString(val searchString: String): HymnalEvent
}