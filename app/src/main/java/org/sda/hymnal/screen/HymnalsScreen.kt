package org.sda.hymnal.screen

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import org.sda.hymnal.HymnalTopBar
import org.sda.hymnal.R
import org.sda.hymnal.data.hymnal.Hymnal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HymnalsScreen(
    snackbarHost: @Composable () -> Unit,
    onClickBack: () -> Unit,
    hymnals: MutableList<Hymnal>,
    setHymnalUri: (hymnalUri: Uri?) -> Unit
) {
    var hymnalUri: Uri? by remember { mutableStateOf(null) }
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        hymnalUri = uri
    }
    LaunchedEffect(hymnalUri) {
        Log.d("hymnals", "hymnalUri: $hymnalUri")
        if (hymnalUri != null) {
            setHymnalUri(hymnalUri)
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
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    fileLauncher.launch(arrayOf("*/*"))
                }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.hymnals_import)
                )
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding
        ) {
            items(hymnals) { hymnal ->
                HymnalListItem(
                    modifier = Modifier
                        .animateItem(),
                    hymnal = hymnal
                )
            }
        }
    }
}

@Composable
fun HymnalListItem(
    modifier: Modifier,
    hymnal: Hymnal
) {
    ListItem(
        modifier = modifier,
        headlineContent = {
            Text(hymnal.title)
        },
        supportingContent = {
            Text("v${hymnal.version}, userAdded: ${hymnal.userAdded}")
        }
    )
}