package org.sda.hymnal.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
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
import org.sda.hymnal.R
import org.sda.hymnal.data.hymn.Hymn
import org.sda.hymnal.data.hymn.HymnConverter
import org.sda.hymnal.data.hymnal.hymnalList
import org.sda.hymnal.data.playlist.Playlist
import org.sda.hymnal.data.playlist.PlaylistHymn
import org.sda.hymnal.screen.HymnalEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class HymnalViewModel(
    private val applicationContext: Context
) : ViewModel() {
    private val _hymnalState = MutableStateFlow(HymnalState())
    private val hymnMapper by lazy { HymnConverter(applicationContext) }

    val hymnDb by lazy {
        HymnDatabase.getDatabase(applicationContext)
    }

    private val hymnDao = hymnDb.hymnDao
    private val settingDao = hymnDb.settingDao
    private val playlistDao = hymnDb.playlistDao
    private val playlistHymnDao = hymnDb.playlistHymnDao
//    private val dbHymns =
//        CoroutineScope(Dispatchers.IO).launch { hymnDao.getAll() }

    val hymnalState = _hymnalState.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(1000),
        HymnalState()
    )

    val currentHymns = _hymnalState.map { it.currentHymns }

    init {
        @OptIn(FlowPreview::class)
        val searchedHymns: StateFlow<List<Hymn>> = _hymnalState.value.currentSearchString
            .debounce(300L.milliseconds)
            .combine(currentHymns) { query, items ->
                FuzzySearch.search(
                    query = query,
                    items = items,
                    threshold = 0.5,
                    titleWeight = 1.0,
                    bodyWeight = 0.2,
                    titleSelector = {it.title},
                    bodySelector = {it.text}
                ).distinct()
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        _hymnalState.update {
            it.copy(
                searchedHymns = searchedHymns
            )
        }
    }
    @OptIn(ExperimentalUuidApi::class)
    fun onEvent(event: HymnalEvent) {
        viewModelScope.launch {
            when (event) {
                is HymnalEvent.AddPlaylist -> {
                    val newPlaylist = Playlist(
                        id = Uuid.generateV7().toString(),
                        name = event.playlistName,
                        count = 0
                    )
                    _hymnalState.value.playlists.add(newPlaylist)
                    playlistDao.upsertPlaylist(newPlaylist)
                }

                is HymnalEvent.DeletePlaylist -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        _hymnalState.value.playlists.remove(event.playlist)
                        playlistDao.deletePlaylist(event.playlist)

                        val playlistHymns = playlistHymnDao.getPlaylist(event.playlist.id)
                        for (playlistHymn in playlistHymns) {
                            playlistHymnDao.deletePlaylistHymn(playlistHymn)
                        }
                    }
                }

                is HymnalEvent.RenamePlaylist -> {
                    val index = _hymnalState.value.playlists.indexOf(event.playlist)

                    val renamedPlaylist = event.playlist.copy(
                        name = event.name
                    )
                    if (index != -1) {
                        _hymnalState.value.playlists[index] = renamedPlaylist
                    }

                    playlistDao.upsertPlaylist(renamedPlaylist)
                }

                is HymnalEvent.AddHymnToPlaylist -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val toAdd = true        // TODO allow removal
                        val playlists = _hymnalState.value.playlists
                        val playlist = event.playlist
                        val currentHymn = event.hymnPair.first
                        val currentPlaylistPlaylistHymns = playlistHymnDao.getPlaylist(playlist.id)
                        currentPlaylistPlaylistHymns.sortBy { it.position }

                        if (playlist.id == "favorites") {
                            onEvent(HymnalEvent.SetFavorite)
                            return@launch
                        }
                        if (toAdd) {    // to be added
                            // Update playlistHymns list
                            val position = if (currentPlaylistPlaylistHymns.isNotEmpty()) {currentPlaylistPlaylistHymns.last().position + 1} else { 1 }
                            val playlistHymnToAdd = PlaylistHymn(
                                id = Uuid.generateV7().toString(),
                                hymnal = currentHymn.hymnal.fileName,
                                number = currentHymn.number,
                                playlist = playlist.id,
                                position = position
                            )
                            currentPlaylistPlaylistHymns.add(playlistHymnToAdd)
                            playlistHymnDao.upsertPlaylistHymn(playlistHymnToAdd)
                            _hymnalState.value.currentPlaylistHymns.add(currentHymn)

                            // Update playlist hymn count
                            val updatedPlaylist = playlist.copy(count = playlist.count + 1)
                            playlists[playlists.indexOf(playlist)] = updatedPlaylist
                            playlistDao.upsertPlaylist(updatedPlaylist)
                            _hymnalState.update {
                                it.copy(
                                    playlists = playlists,
                                    currentPlaylistPlaylistHymns = currentPlaylistPlaylistHymns
                                )
                            }
                        } else {                        // to be removed
                            // Update playlistHymns list
                            val playlistHymn = currentPlaylistPlaylistHymns.find {
                                it.hymnal == currentHymn.hymnal.fileName && it.number == currentHymn.number
                            }
                            if (currentPlaylistPlaylistHymns.remove(playlistHymn)) {
                                playlistHymnDao.deletePlaylistHymn(playlistHymn!!)
                            }
                            _hymnalState.value.currentPlaylistHymns.remove(event.hymnPair.first)

                            // Update playlist hymn count
                            val updatedPlaylist = playlist.copy(count = playlist.count - 1)
                            playlists[playlists.indexOf(playlist)] = updatedPlaylist
                            playlistDao.upsertPlaylist(updatedPlaylist)
                            _hymnalState.update {
                                it.copy(
                                    playlists = playlists,
                                    currentPlaylistPlaylistHymns = currentPlaylistPlaylistHymns
                                )
                            }
                        }
                        onEvent(HymnalEvent.ShowSnackbar(
                            applicationContext.getString(
                                R.string.hymn_added_to_playlist,
                                event.playlist.name
                            )))
                    }
                }

                is HymnalEvent.RemoveHymnFromPlaylist -> {
                    val hymnPairPlaylistHymn = event.hymnPair.second
                    val playlist = event.playlist
                    if (hymnPairPlaylistHymn != null) {
                        if (playlist.id == "favorites") {
                            onEvent(HymnalEvent.SetFavorite)
                            return@launch
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            val playlistHymns = playlistHymnDao.getPlaylist(playlist.id)
                            playlistHymns.sortBy { it.position }
                            val playlistHymnIndex = playlistHymns.indexOf(hymnPairPlaylistHymn)
                            if (playlistHymnIndex != -1) {
                                playlistHymnDao.deletePlaylistHymn(hymnPairPlaylistHymn)
                                _hymnalState.value.currentPlaylistPair.remove(event.hymnPair)
                                playlistHymns.remove(hymnPairPlaylistHymn)

                                for (indexToModify in playlistHymnIndex until playlistHymns.size) {
                                    val hymnPairToModify = _hymnalState.value.currentPlaylistPair[indexToModify]
                                    val playlistHymnToModify = playlistHymns[indexToModify]
                                    val previousPosition = playlistHymnToModify.position
                                    val modifiedHymn = playlistHymnToModify.copy(
                                        position = previousPosition - 1
                                    )
                                    val modifiedHymnPair = hymnPairToModify.copy(
                                        second = modifiedHymn
                                    )
                                    _hymnalState.value.currentPlaylistPair[indexToModify] = modifiedHymnPair
                                    playlistHymnDao.upsertPlaylistHymn(modifiedHymn)
                                }

                                val modifiedPlaylist = playlist.copy(
                                    count = playlist.count - 1
                                )
                                val indexOfPlaylist = _hymnalState.value.playlists.indexOf(playlist)
                                if (indexOfPlaylist != -1) {
                                    _hymnalState.value.playlists[indexOfPlaylist] = modifiedPlaylist
                                }
                                playlistDao.upsertPlaylist(modifiedPlaylist)

                            }
                        }
                    }

                }

                is HymnalEvent.MoveHymnInPlaylist -> {
                    val hymnPairPlaylistHymn = event.hymnPair.second
                    Log.d("movePlaylist", "hymnPairPlaylistHymn: $hymnPairPlaylistHymn")
                    val playlist = event.playlist
                    val moveBy = event.moveBy
                    if (hymnPairPlaylistHymn != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val playlistHymns = playlistHymnDao.getPlaylist(playlist.id)
                            playlistHymns.sortBy { it.position }
                            Log.d("movePlaylist", "playlistHymns: $playlistHymns")
                            val playlistHymnIndex = playlistHymns.indexOf(hymnPairPlaylistHymn)
                            if (playlistHymnIndex != -1) {
                                val range = if (moveBy < 0) {moveBy..0} else {(0..moveBy)}
                                for (currentMoveBy in range) {
                                    Log.d("movePlaylist", "currentMoveBy: $currentMoveBy")
                                    if (currentMoveBy != 0) {
                                        val hymnToModifyIndex = playlistHymnIndex + currentMoveBy
                                        Log.d("movePlaylist", "hymnToModifyIndex: $hymnToModifyIndex")
                                        if (hymnToModifyIndex in playlistHymns.indices) {
                                            val hymnToModify = playlistHymns[hymnToModifyIndex]
                                            Log.d("movePlaylist", "hymnToModify: $hymnToModify")
                                            val moveByDifference = if (moveBy < 0) {
                                                1
                                            } else {
                                                -1
                                            }
                                            // TODO make sure that it's not at the beginning or the end of
                                            //  the playlist so we can prevent the index OOB error
                                            val modifiedHymnPosition =
                                                hymnToModify.position + moveByDifference

                                            val modifiedHymn = hymnToModify.copy(
                                                position = modifiedHymnPosition
                                            )

                                            Log.d("movePlaylist", "modifiedHymn: $modifiedHymn")

                                            playlistHymnDao.upsertPlaylistHymn(modifiedHymn)
                                            if (_hymnalState.value.currentPlaylist == playlist) {
                                                _hymnalState.value.currentPlaylistPair[hymnToModifyIndex] =
                                                    _hymnalState.value.currentPlaylistPair[hymnToModifyIndex].copy(
                                                        second = modifiedHymn
                                                    )
                                            }
                                        }
                                    }
                                }

                                // Modify the actual hymn
                                val actualHymnIndex = playlistHymnIndex
                                val actualHymn = playlistHymns[actualHymnIndex]
                                // TODO make sure that it's not at the beginning or the end of
                                //  the playlist so we can prevent the index OOB error
                                var actualModifiedHymnPosition =
                                    actualHymn.position + moveBy
                                if (actualModifiedHymnPosition > playlistHymns.size) {
                                    actualModifiedHymnPosition = playlistHymns.size
                                }
                                if (actualModifiedHymnPosition < 1) {
                                    actualModifiedHymnPosition = 1
                                }

                                val actualModifiedHymn = actualHymn.copy(
                                    position = actualModifiedHymnPosition
                                )

                                playlistHymnDao.upsertPlaylistHymn(actualModifiedHymn)
                                if (_hymnalState.value.currentPlaylist == playlist) {
                                    _hymnalState.value.currentPlaylistPair[actualHymnIndex] =
                                        _hymnalState.value.currentPlaylistPair[actualHymnIndex].copy(
                                            second = actualModifiedHymn
                                        )
                                }

                                if (_hymnalState.value.currentPlaylist == playlist) {
//                                    _hymnalState.value.currentPlaylistPair.sortBy { it.second!!.position }
                                    onEvent(HymnalEvent.LoadPlaylist(playlist))
                                }
                            }
                        }
                    }

                }

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
//                            currentHymn = event.hymn,
                            currentHymnPair = event.hymnPair,
                            isLyricsScreen = if (event.hymnPair?.first?.sheetMusic?.isEmpty() ?: true) {true} else {_hymnalState.value.isLyricsScreen}
                        )
                    }
                }

                is HymnalEvent.SetCurrentSheetMusic -> {
                    val hymn = _hymnalState.value.currentHymnPair!!
                    val resourcedHymn = hymn.copy(first = hymn.first.copy(sheetMusic = event.resources))
                    _hymnalState.update {
                        it.copy(
                            currentHymnPair = resourcedHymn
                        )
                    }
                }

                is HymnalEvent.LoadSettings -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val dbSettings = settingDao.getSettings().first()
                        val playlists = playlistDao.getAll()
                        _hymnalState.value.playlists.clear()
                        _hymnalState.value.playlists.addAll(playlists)
                        _hymnalState.update { it ->
                            it.copy(
                                currentHymnal = hymnalList.find { it.fileName == dbSettings.hymnal}!!,
                                isLoadingHymns = false,
                                settings = _hymnalState.value.settings.copy(fontSize = dbSettings.fontSize),
                            )
                        }
                        onEvent(HymnalEvent.LoadHymns(emptyList()))
                    }
                }

                is HymnalEvent.LoadPlaylist -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        Log.d("playlist","loading playlist")
//                        _hymnalState.value.currentPlaylistHymns.clear()
                        _hymnalState.value.currentPlaylistPair.clear()
                        val currentPlaylistPlaylistHymns = playlistHymnDao.getPlaylist(event.playlist.id)
                        currentPlaylistPlaylistHymns.sortBy { if(event.playlist.id != "favorites") {it.position} else {it.number} }
                        _hymnalState.update {
                            it.copy(
                                currentPlaylist = event.playlist,
//                                currentPlaylistPlaylistHymns =
                            )
                        }
                        for (playlistHymn in currentPlaylistPlaylistHymns) {
//                            _hymnalState.value.currentPlaylistHymns.add(
//                                hymnMapper.convertToHymn(hymnDao.getHymn(hymn.hymnal, hymn.number))
//                            )
                            _hymnalState.value.currentPlaylistPair.add(
                                Pair(hymnMapper.convertToHymn(hymnDao.getHymn(playlistHymn.hymnal, playlistHymn.number)), playlistHymn)
                            )
                        }
                    }
                }

                is HymnalEvent.SetSettings -> {

                }

                is HymnalEvent.SetFavorite -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val playlists = _hymnalState.value.playlists
                        val favoritesPlaylist =
                            _hymnalState.value.playlists.find { it.id == "favorites" }
                        val currentHymn = _hymnalState.value.currentHymnPair!!
                        val modifiedHymn = currentHymn.copy(
                            first = currentHymn.first.copy(favorite = !currentHymn.first.favorite)
                        )
                        hymnDao.setHymn(modifiedHymn.first.let(hymnMapper::convertToDbHymn))
                        val modHymnIndex = _hymnalState.value.currentHymns.indexOf(currentHymn.first)
                        if (modHymnIndex != -1) {
                            _hymnalState.value.currentHymns[modHymnIndex] = modifiedHymn.first
                        }
                        _hymnalState.update {
                            it.copy(
                                currentHymnPair = modifiedHymn
                            )
                        }
                        if (favoritesPlaylist != null) {
                            val playlistHymns = playlistHymnDao.getPlaylist(favoritesPlaylist.id)
                            playlistHymns.sortWith(compareBy({it.hymnal}, {it.number}))
                            if (modifiedHymn.first.favorite) {    // to be favorited
                                // Update playlistHymns list
                                val position = if (playlistHymns.isNotEmpty()) {playlistHymns.last().position + 1} else { 1 }
                                val playlistHymnToAdd = PlaylistHymn(
                                    id = Uuid.generateV7().toString(),
                                    hymnal = currentHymn.first.hymnal.fileName,
                                    number = currentHymn.first.number,
                                    playlist = "favorites",
                                    position = position
                                )
//                                playlistHymns.add(playlistHymnToAdd)
                                playlistHymnDao.upsertPlaylistHymn(playlistHymnToAdd)

                                // Update playlist hymn count
                                val updatedPlaylist = favoritesPlaylist.copy(count = favoritesPlaylist.count + 1)
                                playlists[playlists.indexOf(favoritesPlaylist)] = updatedPlaylist
                                playlistDao.upsertPlaylist(updatedPlaylist)
                                _hymnalState.update {
                                    it.copy(
                                        playlists = playlists,
//                                        currentPlaylistPlaylistHymns = playlistHymns
                                    )
                                }
                            } else {                        // to be unfavorited
                                // Update playlistHymns list
                                val playlistHymn = playlistHymns.find {
                                    it.hymnal == currentHymn.first.hymnal.fileName && it.number == currentHymn.first.number
                                }
                                if (playlistHymns.remove(playlistHymn)) {
                                    playlistHymnDao.deletePlaylistHymn(playlistHymn!!)
                                }

                                // Update playlist hymn count
                                val updatedPlaylist = favoritesPlaylist.copy(count = favoritesPlaylist.count - 1)
                                playlists[playlists.indexOf(favoritesPlaylist)] = updatedPlaylist
                                playlistDao.upsertPlaylist(updatedPlaylist)
                                _hymnalState.update {
                                    it.copy(
                                        playlists = playlists,
//                                        currentPlaylistPlaylistHymns = playlistHymns
                                    )
                                }
                            }
                        }
                    }
                }

                is HymnalEvent.SetInPlaylist -> {
                    _hymnalState.update {
                        it.copy(
                            isInPlaylist = event.inPlaylist
                        )
                    }
                }

                is HymnalEvent.LoadHymns -> {
                    Log.d("loading", "LoadHymns executed ${_hymnalState.value.currentHymnal}")
                    CoroutineScope(Dispatchers.IO).launch {
                        val hymns = hymnDao.getHymnal(_hymnalState.value.currentHymnal.fileName).map(hymnMapper::convertToHymn).toMutableList()
                        _hymnalState.update {
                            it.copy(
                                currentHymns = hymns
                            )
                        }
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
                    CoroutineScope(Dispatchers.IO).launch {
                        val hymns = hymnDao.getHymnal(event.hymnal.fileName)
                            .map(hymnMapper::convertToHymn).toMutableList()
                        _hymnalState.update {
                            it.copy(
                                currentHymnal = event.hymnal,
                                settings = _hymnalState.value.settings.copy(hymnal = event.hymnal.fileName),
                                currentHymns = hymns
                            )
                        }
                        settingDao.upsertSetting(_hymnalState.value.settings)
                        onEvent(HymnalEvent.SetSearchNumber(_hymnalState.value.currentSearchNumber))
                    }
                }

                is HymnalEvent.SetFontSize -> {
                    _hymnalState.update {
                        it.copy(
                            settings = _hymnalState.value.settings.copy(fontSize = event.fontSize)
                        )
                    }
                    settingDao.upsertSetting(_hymnalState.value.settings)
                }

                is HymnalEvent.EnableSearch -> {
                    _hymnalState.update {
                        it.copy(
                            isSearching = event.isSearching
                        )
                    }
                }

                is HymnalEvent.SetLyricsScreen -> {
                    _hymnalState.update {
                        it.copy(
                            isLyricsScreen = event.isLyricsScreen
                        )
                    }
                }

                is HymnalEvent.SetSearchNumber -> {
                    _hymnalState.update {
                        it.copy(
                            currentSearchNumber = event.searchNumber
                        )
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        val searchNumberInt = event.searchNumber.toIntOrNull()
                        if (searchNumberInt != null) {
                            val hymn = _hymnalState.value.currentHymns.find { searchNumberInt == it.number}
                            onEvent(HymnalEvent.SetSearchHymn(hymn))
                        } else {
                            onEvent(HymnalEvent.SetSearchHymn(null))
                        }

                    }
                }

                is HymnalEvent.SetSearchHymn -> {
                    _hymnalState.update {
                        it.copy(
                            currentSearchHymn = event.searchHymn
                        )
                    }
                    onEvent(HymnalEvent.SetCurrentHymn(if (event.searchHymn != null) {
                        Pair(event.searchHymn, null)
                    } else {
                        null
                    }))
                }

                is HymnalEvent.SetSearchString -> {
                    _hymnalState.value.currentSearchString.value = event.searchString
                }

                is HymnalEvent.ShowSnackbar -> {
                    Log.d("snack", "showing snack")
                    _hymnalState.value.snackbarHostState.showSnackbar(event.message, withDismissAction = true)
                }
            }
        }
    }
}