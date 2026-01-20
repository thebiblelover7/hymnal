package org.sda.hymnal.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.sda.hymnal.data.Hymn

class HymnalViewModel() : ViewModel() {
    private val _hymnalState = MutableStateFlow(HymnalState())

    val hymnalState = _hymnalState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000),
        HymnalState())

    fun onEvent(event: HymnalEvent) {
        viewModelScope.launch {
            when (event) {
                is HymnalEvent.SetCurrentScreen -> {
                    _hymnalState.update {
                        it.copy(
                            currentScreen = event.screen
                        )
                    }
                }

                is HymnalEvent.SetCurrentHymn -> {
                    _hymnalState.update {
                        it.copy(
                            currentHymn = event.hymn
                        )
                    }
                }

                is HymnalEvent.SetCurrentSheetMusic -> {
                    val hymn = _hymnalState.value.currentHymn!!
                    val resourcedHymn = Hymn(title = hymn.title, hymnal = hymn.hymnal, number = hymn.number, text = hymn.text, sheetMusic = event.resources)
                    _hymnalState.update {
                        it.copy(
                            currentHymn = resourcedHymn
                        )
                    }
                }

                is HymnalEvent.LoadHymns -> {
                    _hymnalState.update {
                        it.copy(
                            allHymns = _hymnalState.value.allHymns + event.hymns
                        )
                    }
                }

                is HymnalEvent.SetLoadingHymns -> {
                    _hymnalState.update {
                        it.copy(
                            isLoadingHymns = event.isLoadingHymns
                        )
                    }
                }

                is HymnalEvent.SetCurrentHymnal -> {
                    val currentHymns = _hymnalState.value.allHymns.filter { it.hymnal == event.hymnal }
                    _hymnalState.update {
                        it.copy(
                            currentHymnal = event.hymnal,
                            currentHymns = currentHymns
                        )
                    }
                }

                is HymnalEvent.SetSearchString -> {
                    _hymnalState.update {
                        it.copy(
                            currentSearchString = event.searchString
                        )
                    }
                }
            }
        }
    }
}