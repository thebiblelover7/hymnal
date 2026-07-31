package org.sda.hymnal.data

import android.content.Context
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.sda.hymnal.R
import org.sda.hymnal.data.hymn.DbHymn
import org.sda.hymnal.data.hymn.Hymn
import org.sda.hymnal.data.hymn.getSheetMusicResource
import org.sda.hymnal.data.hymnal.Hymnal
import org.sda.hymnal.data.hymnal.HymnalsImportConditions
import org.sda.hymnal.data.hymnal.JsonHymnal
import org.sda.hymnal.data.hymnal.defaultHymnals
import org.sda.hymnal.data.playlist.Playlist
import org.sda.hymnal.data.playlist.PlaylistHymn
import org.sda.hymnal.screen.HymnalEvent
import java.io.File
import java.util.zip.ZipInputStream
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class HymnalViewModel(
    private val applicationContext: Context
) : ViewModel() {
    private val _hymnalState = MutableStateFlow(HymnalState())

    fun convertToHymn(dbHymn: DbHymn): Hymn {
        val hymnal = _hymnalState.value.hymnals.find { it.fileName == dbHymn.hymnal }!!
        val sheetsStr: List<String> = if (dbHymn.sheetMusic.isNotBlank()) {Json.decodeFromString(dbHymn.sheetMusic)} else emptyList()
        val sheetsFiles = if (hymnal.userAdded) {
            sheetsStr.map { sheetPath ->
                val subDir =
                    File(applicationContext.filesDir, "hymnals/${hymnal.id}")
                if (subDir.exists()) {
                    File(subDir, sheetPath)
                } else {
                    null
                }
            }
        } else { emptyList() }
        val sheetsInt = if (!hymnal.userAdded) {sheetsStr.map { sheetPath ->
            getSheetMusicResource(sheetPath)
        }} else {emptyList()}
        return Hymn(
            title = dbHymn.title,
            hymnal = hymnal,
            number = dbHymn.number,
            text = dbHymn.text,
            sheetMusic = sheetsInt,
            sheetMusicFiles = sheetsFiles,
            sheetMusicStr = dbHymn.sheetMusic,
            favorite = dbHymn.favorite,
            firstLine = dbHymn.firstLine
        )
    }

    fun convertToDbHymn(hymn: Hymn): DbHymn {
        return DbHymn(
            hymnal = hymn.hymnal.fileName,
            number = hymn.number,
            title = hymn.title,
            favorite = hymn.favorite,
            sheetMusic = hymn.sheetMusicStr,
            text = hymn.text,
            firstLine = hymn.firstLine
        )
    }

    val hymnDb by lazy {
        HymnDatabase.getDatabase(applicationContext)
    }

    private val hymnDao = hymnDb.hymnDao
    private val hymnalDao = hymnDb.hymnalDao
    private val settingDao = hymnDb.settingDao
    private val playlistDao = hymnDb.playlistDao
    private val playlistHymnDao = hymnDb.playlistHymnDao

    val hymnalState = _hymnalState.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(1000),
        HymnalState()
    )

    @OptIn(ExperimentalUuidApi::class)
    fun onEvent(event: HymnalEvent) {
        viewModelScope.launch {
            when (event) {
                is HymnalEvent.AddHymnal -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val uri = if (event.fileUri != null) event.fileUri else {
                            Log.d("hymnals","setting to none at fileUri")
                            _hymnalState.update { it.copy(
                                hymnalsImportState = HymnalsImportConditions.State.NONE,
                                hymnalCurrentlyRemoving = null
                            ) }
                            return@launch
                        }
                        _hymnalState.update { it.copy(hymnalsImportState = HymnalsImportConditions.State.IMPORTING) }
                        val fileType = applicationContext.contentResolver.getType(uri)
                        var fileName: String? = null
                        val fileQuery = applicationContext.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            cursor.moveToFirst()
                            fileName = cursor.getString(nameIndex)
                            cursor.close()
                        }
                        val fileExtension = fileName?.split(".")?.last()
                        Log.d("hymnals", "fileType: $fileType")
                        Log.d("hymnals", "fileName: $fileName")
                        Log.d("hymnals", "fileExtension: $fileExtension")
                        var jsonString: String
                        var jsonHymnal: JsonHymnal? = null
                        var subDir: File? = null
                        applicationContext.contentResolver.openInputStream(uri).use { stream ->
                            ZipInputStream(stream).use { zipInputStream ->
                                var entry = zipInputStream.nextEntry
                                while (entry != null) {
                                    if (entry.name == "hymnal.json") {
                                        jsonString = zipInputStream.readBytes().toString(Charsets.UTF_8)
                                        jsonHymnal = try {
                                            Json.decodeFromString<JsonHymnal>(jsonString)
                                        } catch (e: IllegalArgumentException) {
                                            _hymnalState.update { it.copy(hymnalsImportState = HymnalsImportConditions.State.FAILED_TO_PARSE) }
                                            Log.e("HymnalViewModel", e.toString())
                                            return@launch
                                        } catch (e: SerializationException) {
                                            _hymnalState.update { it.copy(hymnalsImportState = HymnalsImportConditions.State.FAILED_TO_PARSE) }
                                            Log.e("HymnalViewModel", e.toString())
                                            return@launch
                                        }
                                        subDir = File(applicationContext.filesDir, "hymnals/${jsonHymnal.id}")
                                        if (!subDir.exists()) {
                                            subDir.mkdirs()
                                        }
                                    }
                                    zipInputStream.closeEntry()
                                    entry = zipInputStream.nextEntry
                                }
                                null
                            }
                        }
                        if (jsonHymnal != null) {
                            val filesDir = File(applicationContext.filesDir, "hymnals/${jsonHymnal.id}")
                            if (defaultHymnals.none { it.id == jsonHymnal.id }) {
                                val previousHymnal = _hymnalState.value.hymnals.find { it.id == jsonHymnal.id }
                                if (previousHymnal != null) {
                                    if (jsonHymnal.version > previousHymnal.version) {
                                        _hymnalState.update { it.copy(
                                            hymnalsImportState = HymnalsImportConditions.State.NEW_VERSION_EXISTS,
                                            hymnalCurrentlyRemoving = previousHymnal
                                        ) }

                                        return@launch
                                    } else {
                                        _hymnalState.update { it.copy(hymnalsImportState = HymnalsImportConditions.State.FAILED_ALREADY_EXISTS) }
                                        return@launch
                                    }
                                }
                                val newHymnal = Hymnal(
                                    id = jsonHymnal.id,
                                    fileName = jsonHymnal.id,
                                    version = jsonHymnal.version,
                                    title = "${jsonHymnal.metadata.language} - ${jsonHymnal.metadata.title}",
                                    userAdded = true
                                )
                                _hymnalState.value.hymnals.add(newHymnal)
                                hymnalDao.addHymnal(newHymnal)
                            } else {
                                _hymnalState.update { it.copy(hymnalsImportState = HymnalsImportConditions.State.FAILED_ALREADY_EXISTS) }
                                return@launch
                            }
                            _hymnalState.update { it.copy(hymnalsImportState = HymnalsImportConditions.State.IMPORTING) }
                            applicationContext.contentResolver.openInputStream(uri).use { stream ->
                                ZipInputStream(stream).use { zipInputStream ->
                                    var entry = zipInputStream.nextEntry
                                    while (entry != null) {
                                        if (entry.name.startsWith("sheets/") && !entry.isDirectory) {
                                            _hymnalState.update { it.copy(hymnalsImportState = HymnalsImportConditions.State.IMPORTING_SHEETS) }
                                            val newFile = File(subDir, entry.name.split("/").last())
                                                .writeBytes(zipInputStream.readBytes())
                                        }
                                        zipInputStream.closeEntry()
                                        entry = zipInputStream.nextEntry
                                    }
                                    null
                                }
                            }
                            val sheetMusicList = if (filesDir.exists()) {
                                filesDir.listFiles()?.map { it.name } ?: emptyList()
                            } else {emptyList<String>()}
                            for (hymn in jsonHymnal.hymns) {
                                val sheetMusicFiles = mutableListOf<String>()
                                for (sheet in hymn.sheetMusic) {
                                    if (sheetMusicList.contains(sheet)) {
                                        sheetMusicFiles.add(sheet)
                                    }
                                }
                                val sheetMusic = Json.encodeToString<List<String>>(sheetMusicFiles)
                                val newHymn = DbHymn(
                                    hymnal = jsonHymnal.id,
                                    number = hymn.number,
                                    title = hymn.title,
                                    favorite = false,
                                    sheetMusic = sheetMusic,
                                    text = hymn.content,
                                    firstLine = hymn.content
                                )
                                hymnDao.setHymn(newHymn)
                            }
                            _hymnalState.update { it.copy(
                                hymnalsImportState = HymnalsImportConditions.State.COMPLETED,
                                hymnalCurrentlyRemoving = null
                            ) }
                            onEvent(HymnalEvent.LoadSettings)
                        } else {
                            _hymnalState.update { it.copy( hymnalsImportState = HymnalsImportConditions.State.FAILED_HYMNAL_NOT_EXISTS)}
                        }
                    }
                }

                is HymnalEvent.RemoveHymnal -> {
                    val hymnal = event.hymnal
                    CoroutineScope(Dispatchers.IO).launch {
                        _hymnalState.update { it.copy(hymnalsImportState = HymnalsImportConditions.State.REMOVING) }
                        if (_hymnalState.value.currentHymnal == hymnal) {
                            val hymnalToSet = _hymnalState.value.hymnals.firstOrNull()
                            if (hymnalToSet != null) {
                                onEvent(HymnalEvent.SetCurrentHymnal(hymnalToSet))
                            } else {
                                _hymnalState.update { it.copy(hymnalsImportState = HymnalsImportConditions.State.REMOVING_FAILED) }
                                return@launch
                            }
                        }
                        if (defaultHymnals.none { it.id == hymnal.id }) {
                            // Remove sheet musics first
                            val hymns = hymnDao.getHymnal(hymnal.id).map(::convertToHymn)
                            _hymnalState.update { it.copy(hymnalsImportState = HymnalsImportConditions.State.REMOVING_SHEETS) }
                            var lastSheetMusicFile: File? = null
                            for (hymn in hymns) {
                                val sheetMusics = hymn.sheetMusicFiles
                                for (sheetMusic in sheetMusics) {
                                    if (sheetMusic != null) {
                                        lastSheetMusicFile = sheetMusic
                                        sheetMusic.delete()
                                    }
                                }
                            }
                            lastSheetMusicFile?.parentFile?.parentFile?.deleteRecursively() // delete hymnal directory
                            _hymnalState.value.hymnals.remove(hymnal)
                            _hymnalState.update { it.copy(hymnalsImportState = HymnalsImportConditions.State.REMOVING) }
                            hymnDao.deleteHymnal(hymnal.id)
                            hymnalDao.deleteHymnal(hymnal)
                            _hymnalState.update { it.copy(
                                hymnalsImportState = if (_hymnalState.value.hymnalCurrentlyRemoving == null)
                                    { HymnalsImportConditions.State.REMOVING_COMPLETED } else {
                                    HymnalsImportConditions.State.IMPORTING_REMOVING_COMPLETED }
                            ) }
                        }
                    }
                }

                is HymnalEvent.SetImportState -> {
                    _hymnalState.update {
                        it.copy(
                            hymnalsImportState = event.state
                        )
                    }
                }

                is HymnalEvent.SetImportUri -> {
                    _hymnalState.update {
                        it.copy(
                            hymnalImportUri = event.uri
                        )
                    }
                }

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
                        val playlists = _hymnalState.value.playlists
                        val playlist = event.playlist
                        val currentHymn = event.hymnPair.first
                        if (playlist.id == "favorites") {
                            onEvent(HymnalEvent.SetCurrentHymn(event.hymnPair))
                            onEvent(HymnalEvent.SetFavorite)
                            onEvent(HymnalEvent.ShowSnackbar(
                                applicationContext.getString(
                                    R.string.hymn_added_to_favorites
                                )))
                            return@launch
                        }

                        val currentPlaylistPlaylistHymns = playlistHymnDao.getPlaylist(playlist.id)
                        currentPlaylistPlaylistHymns.sortBy { it.position }
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
                        CoroutineScope(Dispatchers.IO).launch {
                            if (playlist.id == "favorites") {
                                onEvent(HymnalEvent.SetCurrentHymn(event.hymnPair))
                                onEvent(HymnalEvent.SetFavorite)
                                onEvent(HymnalEvent.ShowSnackbar(
                                    applicationContext.getString(R.string.hymn_removed_from_favorites)))
                                return@launch
                            }
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
                                onEvent(HymnalEvent.ShowSnackbar(
                                    applicationContext.getString(
                                        R.string.hymn_removed_from_playlist,
                                        event.playlist.name
                                    )))
                            }
                        }
                    }

                }

                is HymnalEvent.MoveHymnInPlaylist -> {
                    val hymnPairPlaylistHymn = event.hymnPair.second
                    val playlist = event.playlist
                    val moveBy = event.moveBy
                    if (hymnPairPlaylistHymn != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val playlistHymns = playlistHymnDao.getPlaylist(playlist.id)
                            playlistHymns.sortBy { it.position }
                            val playlistHymnIndex = playlistHymns.indexOf(hymnPairPlaylistHymn)
                            if (playlistHymnIndex != -1) {
                                val range = if (moveBy < 0) {moveBy..0} else {(0..moveBy)}
                                for (currentMoveBy in range) {
                                    if (currentMoveBy != 0) {
                                        val hymnToModifyIndex = playlistHymnIndex + currentMoveBy
                                        if (hymnToModifyIndex in playlistHymns.indices) {
                                            val hymnToModify = playlistHymns[hymnToModifyIndex]
                                            val moveByDifference = if (moveBy < 0) {
                                                1
                                            } else {
                                                -1
                                            }
                                            val modifiedHymnPosition =
                                                hymnToModify.position + moveByDifference

                                            val modifiedHymn = hymnToModify.copy(
                                                position = modifiedHymnPosition
                                            )

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
                    val isLyricsScreen = if (event.hymnPair == null) { true } else {
                        if (event.hymnPair.first.hymnal.userAdded) {
                            if (event.hymnPair.first.sheetMusicFiles.isEmpty()) true else {
                                _hymnalState.value.isLyricsScreen
                            }
                        } else {
                            if (event.hymnPair.first.sheetMusic.isEmpty()) true else {
                                _hymnalState.value.isLyricsScreen
                            }
                        }
                    }
                    _hymnalState.update {
                        it.copy(
//                            currentHymn = event.hymn,
                            currentHymnPair = event.hymnPair,
                            isLyricsScreen = isLyricsScreen
                        )
                    }
                }

                is HymnalEvent.SetCurrentSheetMusic -> {
                    val hymn = _hymnalState.value.currentHymnPair ?: return@launch
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
                        val hymnals = hymnalDao.getAll().ifEmpty { return@launch }.toMutableStateList()
                        _hymnalState.update {
                            it.copy(
                                hymnals = hymnals
                            )
                        }
                        val currentHymnal = _hymnalState.value.hymnals.find { it.fileName == dbSettings.hymnal} ?: return@launch
                        _hymnalState.update { it ->
                            it.copy(
                                currentHymnal = currentHymnal,
                                isLoadingHymns = false,
                                settings = _hymnalState.value.settings.copy(fontSize = dbSettings.fontSize),
                            )
                        }
                        onEvent(HymnalEvent.LoadHymns(emptyList()))
                    }
                }

                is HymnalEvent.LoadPlaylist -> {
                    CoroutineScope(Dispatchers.IO).launch {
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
                                Pair(convertToHymn(hymnDao.getHymn(playlistHymn.hymnal, playlistHymn.number)), playlistHymn)
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
                        val currentHymn = _hymnalState.value.currentHymnPair ?: return@launch
                        val modifiedHymn = currentHymn.copy(
                            first = currentHymn.first.copy(favorite = !currentHymn.first.favorite),
                        )
                        val modHymnIndex = _hymnalState.value.currentHymns.indexOf(currentHymn.first)
                        if (modHymnIndex != -1) {
                            _hymnalState.value.currentHymns[modHymnIndex] = modifiedHymn.first
                        }
                        hymnDao.setHymn(modifiedHymn.first.let(::convertToDbHymn))
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
                                if ((_hymnalState.value.currentPlaylist?.id
                                        ?: false) == "favorites"
                                ) {
                                    _hymnalState.value.currentPlaylistPair.add(
                                        modifiedHymn.copy(
                                            second = playlistHymnToAdd
                                        )
                                    )
                                }
                                playlistHymnDao.upsertPlaylistHymn(playlistHymnToAdd)

                                // Update playlist hymn count
                                val updatedPlaylist = favoritesPlaylist.copy(count = favoritesPlaylist.count + 1)
                                playlists[playlists.indexOf(favoritesPlaylist)] = updatedPlaylist
                                playlistDao.upsertPlaylist(updatedPlaylist)
                                _hymnalState.update {
                                    it.copy(
                                        playlists = playlists,
                                    )
                                }
                            } else {                        // to be unfavorited
                                // Update playlistHymns list
                                val playlistHymn = playlistHymns.find {
                                    it.hymnal == currentHymn.first.hymnal.fileName && it.number == currentHymn.first.number
                                } ?: return@launch
                                if (playlistHymns.remove(playlistHymn)) {
                                    playlistHymnDao.deletePlaylistHymn(playlistHymn)
                                }
                                if ((_hymnalState.value.currentPlaylist?.id
                                        ?: false) == "favorites"
                                ) {
                                    _hymnalState.value.currentPlaylistPair.remove(
                                        currentHymn.copy(
                                            second = playlistHymn
                                        )
                                    )
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
                    CoroutineScope(Dispatchers.IO).launch {
                        val hymns = hymnDao.getHymnal(_hymnalState.value.currentHymnal.fileName).map(::convertToHymn).toMutableList()
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
                            .map(::convertToHymn).toMutableList()
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

                is HymnalEvent.SetSearchActive -> {
                    _hymnalState.update {
                        it.copy(
                            isSearchActive = event.isSearchActive
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

                is HymnalEvent.PerformSearch -> {
                    val query = event.query.replace(Regex("\""), "\"\"")
                        .replace(Regex("-"), "")
                    val searchQuery = "\"${query.trim()}\"*"
                    if (searchQuery.length > 5) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val searchedDbHymns = hymnDao.searchBMHymns(
                                searchQuery,
                                _hymnalState.value.currentHymnal.fileName
                            )
                            val searchedHymns =
                                searchedDbHymns.map(::convertToHymn).toMutableStateList()
                            _hymnalState.update {
                                it.copy(
                                    searchedHymns = searchedHymns
                                )
                            }
                        }
                    } else {
                        _hymnalState.update {
                            it.copy(
                                searchedHymns = emptyList<Hymn>().toMutableStateList()
                            )
                        }
                    }
                }

                is HymnalEvent.SetSearchString -> {
                    _hymnalState.value.currentSearchString.value = event.searchString
                }

                is HymnalEvent.ShowSnackbar -> {
                    _hymnalState.value.snackbarHostState.showSnackbar(event.message, withDismissAction = true)
                }
            }
        }
    }
}