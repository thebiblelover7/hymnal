package org.sda.hymnal.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.sda.hymnal.data.FuzzySearch
import org.sda.hymnal.data.Hymn

class HymnalViewModel() : ViewModel() {
    private val _hymnalState = MutableStateFlow(HymnalState())

    val hymnalState = _hymnalState.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(1000),
        HymnalState()
    )

    val currentHymns = _hymnalState.map { it.currentHymns }

    init {
        @OptIn(FlowPreview::class)
        val searchedHymns: StateFlow<List<Hymn>> = _hymnalState.value.currentSearchString
            .debounce(300L)
            .combine(currentHymns) { query, items ->
                FuzzySearch.search(
                    query = query,
                    items = items,
                    threshold = 0.5,
                    titleWeight = 1.0,
                    bodyWeight = 0.2,
                    titleSelector = {it.title},
                    bodySelector = {it.text}
                )
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        _hymnalState.update {
            it.copy(
                searchedHymns = searchedHymns
            )
        }
    }
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
                is HymnalEvent.EnableSearch -> {
                    _hymnalState.update {
                        it.copy(
                            isSearching = event.isSearching
                        )
                    }
                }
                is HymnalEvent.SetSearchNumber -> {
                    _hymnalState.update {
                        it.copy(
                            currentSearchNumber = event.searchNumber
                        )
                    }
                }
                is HymnalEvent.SetSearchHymn -> {
                    _hymnalState.update {
                        it.copy(
                            currentSearchHymn = event.searchHymn
                        )
                    }
                }
                is HymnalEvent.SetSearchString -> {
                    _hymnalState.value.currentSearchString.value = event.searchString
                }
            }
        }
    }
}