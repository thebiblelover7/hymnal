package org.sda.hymnal.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import org.sda.hymnal.BottomHymnalBar
import org.sda.hymnal.HymnalTopBar
import org.sda.hymnal.R
import org.sda.hymnal.data.hymn.Hymn
import org.sda.hymnal.data.hymn.hymnTags
import org.sda.hymnal.data.playlist.Playlist
import org.sda.hymnal.data.playlist.PlaylistHymn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    snackbarHost: @Composable () -> Unit,
    currentScreen: Screen,
    onClickBack: () -> Unit,
    onNavClick: (screen: Screen) -> Unit,
    playlists: MutableList<Playlist>,
    onPlaylistClick: (playlist: Playlist) -> Unit,
    onPlaylistAddClick: (playlistName: String) -> Unit,
    onPlaylistRenameClick: (playlist: Playlist, name: String) -> Unit,
    onPlaylistDeleteClick: (playlist: Playlist) -> Unit,
) {
    var createPlaylistDialog by remember { mutableStateOf(false) }
    var renamingPlaylist by remember {mutableStateOf<Playlist?>(null)}
    var renamePlaylistDialog by remember { mutableStateOf(false) }
    Scaffold(
        snackbarHost = snackbarHost,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentScreen == NavigationScreens.PlaylistSelector) {
                HymnalTopBar(
                    title = stringResource(R.string.add_to_playlist),
                    onClickBack = onClickBack
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {createPlaylistDialog = true}
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.icon_add))
            }
        },
        bottomBar = {
            BottomHymnalBar(
                currentScreen = currentScreen,
                onNavClick = onNavClick,
            )
        }
    ) { padding ->
        val listState = rememberLazyListState()
        Box {
            if (createPlaylistDialog) {
                PlaylistDialog(
                    onPlaylistAddClick = onPlaylistAddClick,
                    onPlaylistDialogVisible = { visible ->
                        createPlaylistDialog = visible
                    },
                    enterString = stringResource(R.string.dialog_add)
                )
            }
            if (renamePlaylistDialog) {
                PlaylistDialog(
                    onPlaylistAddClick = { name ->
                        if (renamingPlaylist != null) {
                            onPlaylistRenameClick(renamingPlaylist!!, name)
                        }
                    },
                    onPlaylistDialogVisible = { visible ->
                        renamePlaylistDialog = visible
                    },
                    enterString = stringResource(R.string.dialog_rename),
                    startingText = if (renamingPlaylist != null) {renamingPlaylist!!.name} else {""}
                )
            }
            LazyColumnScrollbar(
                state = listState,
                modifier = Modifier.padding(padding),
                settings = ScrollbarSettings.Default.copy(
                    thumbUnselectedColor = MaterialTheme.colorScheme.secondary.copy(0.5f),
                    thumbSelectedColor = MaterialTheme.colorScheme.secondary
                ),
                //            indicatorContent = { index, isThumbSelected ->
                //                Text(
                //                    text = "i: $index",
                //                    Modifier.background(if (isThumbSelected) Color.Red else Color.Black, CircleShape)
                //                )
                //            }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    //                contentPadding = padding,
                    state = listState
                ) {
                    items(playlists) { rawPlaylist ->
                        val playlist = if (rawPlaylist.id == "favorites") {
                            rawPlaylist.copy(
                                name = stringResource(R.string.favorites)
                            )
                        } else {rawPlaylist}
                        PlaylistItem(
                            playlist = playlist,
                            onPlaylistClick = onPlaylistClick,
                            onDeleteClick = onPlaylistDeleteClick,
                            onPlaylistRenameClick = { playlist ->
                                renamingPlaylist = playlist
                                renamePlaylistDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistDropdownMenu(
    onDeleteClick: () -> Unit,
    onPlaylistRenameClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { expanded = !expanded}
        ) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.icon_more))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = {Text(stringResource(R.string.rename_playlist))},
                onClick = {
                    onPlaylistRenameClick()
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = {Text(stringResource(R.string.delete_playlist))},
                onClick = {
                    onDeleteClick()
                    expanded = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistHymnsScreen(
    snackbarHost: @Composable () -> Unit,
    currentScreen: Screen,
//    playlistPair: List<Pair<Hymn, PlaylistHymn?>>,
    playlist: Playlist,
    onNavClick: (screen: Screen) -> Unit,
    hymns: MutableList<Pair<Hymn, PlaylistHymn?>>,
    onHymnClick: (hymnPair: Pair<Hymn, PlaylistHymn?>) -> Unit,
    onRemoveClick: (hymnPair: Pair<Hymn, PlaylistHymn?>, playlist: Playlist) -> Unit,
    onMoveClick: (hymnPair: Pair<Hymn, PlaylistHymn?>, playlist: Playlist, moveBy: Int) -> Unit,
    onClickBack: () -> Unit
) {
    Scaffold(
        snackbarHost = snackbarHost,
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomHymnalBar(
                currentScreen = currentScreen,
                onNavClick = onNavClick,
            )
        },
        topBar = {
            HymnalTopBar(
                title = stringResource(R.string.playlist_hymns, playlist.name),
                onClickBack = onClickBack
            )
        }
    ) { padding ->
        val listState = rememberLazyListState()
        LazyColumnScrollbar(
            state = listState,
            modifier = Modifier.padding(padding),
            settings = ScrollbarSettings.Default.copy(
                thumbUnselectedColor = MaterialTheme.colorScheme.secondary.copy(0.5f),
                thumbSelectedColor = MaterialTheme.colorScheme.secondary
            ),
//            indicatorContent = { index, isThumbSelected ->
//                Text(
//                    text = "i: $index",
//                    Modifier.background(if (isThumbSelected) Color.Red else Color.Black, CircleShape)
//                )
//            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(vertical = 10.dp),
//                contentPadding = padding,
                state = listState
            ) {
                items(hymns) { hymn ->
                    PlaylistHymnListItem(
                        hymnPair = hymn,
                        onHymnClick = onHymnClick,
                        onRemoveClick = { onRemoveClick(hymn, playlist) },
                        onMoveClick = { moveBy ->
                            onMoveClick(hymn, playlist, moveBy)
                        },
                        index = Pair(hymn.second?.position ?: 0, playlist.count)
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistDialog(
    onPlaylistAddClick: (playlistName: String) -> Unit,
    onPlaylistDialogVisible: (visible: Boolean) -> Unit,
    startingText: String = "",
    enterString: String
) {
    var playlistNameString by remember { mutableStateOf(startingText)}
    AlertDialog(
        onDismissRequest = {
            onPlaylistDialogVisible(false)
            playlistNameString = ""
        },
        text = {
            TextField(
                label = {
                    Text(stringResource(R.string.playlist_name))
                },
                value = playlistNameString,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onAny = {
                        onPlaylistAddClick(playlistNameString)
                        onPlaylistDialogVisible(false)
                        playlistNameString = ""
                    }
                ),
                onValueChange = { playlistNameString = it }
            )
        },
        dismissButton = {
            TextButton (
                onClick = {
                    onPlaylistDialogVisible(false)
                    playlistNameString = startingText
                }
            ) { Text(stringResource(R.string.dialog_close)) }
        },
        confirmButton = {
            TextButton (
                onClick = {
                    onPlaylistAddClick(playlistNameString)
                    onPlaylistDialogVisible(false)
                    playlistNameString = ""
                }
            ) { Text(enterString) }
//                        onPlaylistAddClick(playlistNameString)
//                        createPlaylistDialog = false
        }
    )
}

@Composable
fun PlaylistItem(
    playlist: Playlist,
    onPlaylistClick: (playlist: Playlist) -> Unit,
    onDeleteClick: (playlist: Playlist) -> Unit,
    onPlaylistRenameClick: (playlist: Playlist) -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(
                enabled = true,
                onClick = { onPlaylistClick(playlist) }
            )
            .padding(vertical = 0.dp, horizontal = 10.dp),
//            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(9f)
                .padding(vertical = 10.dp, horizontal = 10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = playlist.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${playlist.count} hymns",
                fontSize = 14.sp,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onBackground.copy(
                    alpha = 0.8f
                )
            )
        }
        if (playlist.id != "favorites") {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                PlaylistDropdownMenu(
                    onDeleteClick = {
                        onDeleteClick(playlist)
                    },
                    onPlaylistRenameClick = {
                        onPlaylistRenameClick(playlist)
                    }
                )
            }
        }
    }
}


@Composable
fun PlaylistHymnListItem(
//    hymn: Hymn,
    hymnPair: Pair<Hymn, PlaylistHymn?>,
    onHymnClick: (hymnPair: Pair<Hymn, PlaylistHymn?>) -> Unit,
    onRemoveClick: () -> Unit,
    onMoveClick: (moveBy: Int) -> Unit,
    index: Pair<Int, Int>
) {
    val regex = Regex("(${hymnTags.joinToString(separator = "|")})\n")
    Row(
        modifier = Modifier
            .clickable(
                enabled = true,
                onClick = { onHymnClick(hymnPair) }
            )
            .padding(vertical = 0.dp, horizontal = 5.dp),
//            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .defaultMinSize(minWidth = 80.dp)
                .weight(2f)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = hymnPair.first.number.toString(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp
            )
        }
        Column(
            modifier = Modifier
                .weight(6f)
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = hymnPair.first.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            val shortTextLine = hymnPair.first.text.take(50)
                .replace(regex = regex, replacement = "")
            Text(
                text = shortTextLine,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground.copy(
                    alpha = 0.8f
                )
            )
        }
        Column(
            modifier = Modifier
//                .defaultMinSize(minWidth = 80.dp)
                .weight(1f)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val showMoveUp = if (hymnPair.second?.playlist != "favorites") {index.first > 1} else {false}
            val showMoveDown = if (hymnPair.second?.playlist != "favorites") {index.first < index.second} else {false}
            PlaylistHymnDropdownMenu (
                onRemoveClick = onRemoveClick,
                onMoveClick = { moveBy ->
                    onMoveClick(moveBy)
                },
                showMoveUp = showMoveUp,
                showMoveDown = showMoveDown
            )
        }
    }
}

@Composable
fun PlaylistHymnDropdownMenu(
    onRemoveClick: () -> Unit,
    onMoveClick: (moveBy: Int) -> Unit,
    showMoveUp: Boolean,
    showMoveDown: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { expanded = !expanded}
        ) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.icon_more))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = {Text(stringResource(R.string.remove_from_playlist))},
                onClick = {
                    onRemoveClick()
                    expanded = false
                }
            )
            if (showMoveUp) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.playlist_move_up)) },
                    onClick = {
                        onMoveClick(-1)
                        expanded = false
                    }
                )
            }
            if (showMoveDown) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.playlist_move_down)) },
                    onClick = {
                        onMoveClick(1)
                        expanded = false
                    }
                )
            }
        }
    }
}