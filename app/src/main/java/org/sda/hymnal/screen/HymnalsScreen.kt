package org.sda.hymnal.screen

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.sda.hymnal.HymnalTopBar
import org.sda.hymnal.R
import org.sda.hymnal.data.hymnal.Hymnal
import org.sda.hymnal.data.hymnal.HymnalsImportConditions
import org.sda.hymnal.data.hymnal.defaultHymnals

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HymnalsScreen(
    snackbarHost: @Composable () -> Unit,
    onClickBack: () -> Unit,
    hymnals: SnapshotStateList<Hymnal>,
    importHymnal: (hymnalUri: Uri?) -> Unit,
    removeHymnal: (hymnal: Hymnal) -> Unit,
    hymnalCurrentlyRemoving: Hymnal?,
    setImportState: (state: HymnalsImportConditions.State) -> Unit,
    hymnalUri: Uri?,
    setImportUri: (uri: Uri?) -> Unit,
    fromImport: Boolean,
    hymnalsImportState: HymnalsImportConditions.State
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val fileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (!fromImport) {
                setImportUri(uri)
            }
        }
    LaunchedEffect(hymnalUri) {
        Log.d("hymnals", "hymnalUri: $hymnalUri")
        if (hymnalUri != null) {
            importHymnal(hymnalUri)
        }
    }
    Scaffold(
        snackbarHost = snackbarHost,
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .fillMaxSize(),
        topBar = {
            HymnalTopBar(
                title = stringResource(R.string.hymnals_manage),
                onClickBack = onClickBack,
                scrollBehavior = topAppBarScrollBehavior,
                showClickBack = if (fromImport) {
                    false
                } else {
                    !HymnalsImportConditions().progressing.contains(hymnalsImportState)
                }
            )
        },
        floatingActionButton = {
            if (!fromImport) {
                FloatingActionButton(
                    onClick = {
//                    setImportState(HymnalsImportConditions.State.IMPORTING)
                        fileLauncher.launch(
                            arrayOf(
                                "application/octet-stream",
                                "application/json",
                                "application/zip"
                            )
                        )
                    }
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.hymnals_import)
                    )
                }
            }
        }
    ) { padding ->
        BackHandler(enabled = HymnalsImportConditions().progressing.contains(hymnalsImportState)) { }
        Box {
            AnimatedVisibility(
                visible = hymnalsImportState != HymnalsImportConditions.State.NONE,
                modifier = Modifier
                    .zIndex(5f)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        hymnalsImportState,
                        contentAlignment = Alignment.Center
                    ) { hymnalsImportState ->
                        val modifier = Modifier
                            .padding(20.dp)
                            .size(48.dp)
                        if (HymnalsImportConditions().progressing.contains(hymnalsImportState)) {
                            CircularProgressIndicator(
                                modifier = modifier
                            )
                        } else if (HymnalsImportConditions().completed.contains(hymnalsImportState)) {
                            Icon(
                                Icons.Default.Check,
                                modifier = modifier,
                                contentDescription = hymnalsImportState.name
                            )
                        } else if (HymnalsImportConditions().failed.contains(hymnalsImportState)) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                modifier = modifier,
                                contentDescription = hymnalsImportState.name
                            )
                        }
                    }
                    AnimatedContent(
                        targetState = hymnalsImportState,
                        contentAlignment = Alignment.Center,
                        transitionSpec = {
                            ContentTransform(
                                targetContentEnter = fadeIn() + slideInVertically(),
                                initialContentExit = fadeOut() + slideOutVertically(),
                                sizeTransform = null
                            )
                        }
                    ) { hymnalsImportState ->
                        val text = when (hymnalsImportState) {
                            HymnalsImportConditions.State.NONE -> ""
                            HymnalsImportConditions.State.IMPORTING -> "Importing hymnal..."
                            HymnalsImportConditions.State.IMPORTING_SHEETS -> "Importing sheet music..."
                            HymnalsImportConditions.State.IMPORTING_REMOVING_OLD -> "Removing previous hymnal version..."
                            HymnalsImportConditions.State.FAILED -> "Import failed"
                            HymnalsImportConditions.State.FAILED_ALREADY_EXISTS -> "Import failed. Hymnal already exists."
                            HymnalsImportConditions.State.FAILED_TO_PARSE -> "Import failed with failed to parse hymnal.json"
                            HymnalsImportConditions.State.FAILED_HYMNAL_NOT_EXISTS -> "Import failed. hymnal.json not found."
                            HymnalsImportConditions.State.COMPLETED -> "Import completed successfully"
                            HymnalsImportConditions.State.REMOVING -> "Removing hymnal..."
                            HymnalsImportConditions.State.REMOVING_SHEETS -> "Removing hymnal sheet music..."
                            HymnalsImportConditions.State.REMOVING_FAILED -> "Removing failed"
                            HymnalsImportConditions.State.REMOVING_COMPLETED -> "Removed successfully"
                            HymnalsImportConditions.State.NEW_VERSION_EXISTS -> "Would you like to remove the old hymnal version?"
                            HymnalsImportConditions.State.IMPORTING_REMOVING_COMPLETED -> "Removed old version successfully"
                        }
                        Text(
                            text = text,
                            color = Color.Unspecified.copy(alpha = 0.8f),
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                    AnimatedVisibility(
                        visible = HymnalsImportConditions().finished.contains(hymnalsImportState)
                    ) {
                        Button(
                            onClick = { setImportState(HymnalsImportConditions.State.NONE) },
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(stringResource(R.string.hymnals_import_close))
                        }
                    }
                    AnimatedVisibility(
                        visible = hymnalsImportState == HymnalsImportConditions.State.NEW_VERSION_EXISTS
                    ) {
                        Button(
                            onClick = {
                                if (hymnalCurrentlyRemoving != null) {
                                    removeHymnal(hymnalCurrentlyRemoving)
                                }
                            },
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text("Yes, remove it")
                        }
                    }
                    val removingCompleted =
                        hymnalsImportState == HymnalsImportConditions.State.IMPORTING_REMOVING_COMPLETED
                    LaunchedEffect(
                        removingCompleted
                    ) {
                        Log.d("hymnals", "removingCompleted: $removingCompleted")
                        if (removingCompleted) {
                            importHymnal(hymnalUri)
                        }
                    }
                    AnimatedVisibility(
                        visible = hymnalsImportState == HymnalsImportConditions.State.IMPORTING_REMOVING_COMPLETED
                    ) {
                        Button(
                            onClick = {
                                importHymnal(hymnalUri)
                            },
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text("Yes, import")
                        }
                    }
                }
            }
            LazyColumn(
                contentPadding = padding
            ) {
                items(hymnals) { hymnal ->
                    HymnalListItem(
                        modifier = Modifier
                            .animateItem(),
                        hymnal = hymnal,
                        onRemoveClick = {
                            removeHymnal(hymnal)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HymnalListItem(
    modifier: Modifier,
    hymnal: Hymnal,
    onRemoveClick: () -> Unit
) {
    ListItem(
        modifier = modifier,
        headlineContent = {
            Text(hymnal.title)
        },
        supportingContent = {
            Text(
                text = if (hymnal.userAdded) {
                    "Version: ${hymnal.version}"
                } else {
                    "Default"
                }
            )
        },
        trailingContent = {
            if (defaultHymnals.none { it.id == hymnal.id }) {
                HymnalsDropdownMenu(
                    onRemoveClick = onRemoveClick
                )
            }
        }
    )
}

@Composable
fun HymnalsDropdownMenu(
    onRemoveClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { expanded = !expanded }
        ) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.icon_more))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.remove_from_playlist)
                    )
                },
                text = { Text(stringResource(R.string.hymnals_remove)) },
                onClick = {
                    onRemoveClick()
                    expanded = false
                }
            )
        }
    }
}