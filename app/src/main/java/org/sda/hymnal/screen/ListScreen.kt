package org.sda.hymnal.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import org.sda.hymnal.BottomHymnalBar
import org.sda.hymnal.R
import org.sda.hymnal.data.hymn.Hymn
import org.sda.hymnal.data.hymn.hymnTags
import org.sda.hymnal.data.playlist.PlaylistHymn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    currentScreen: Screen,
    snackbarHost: @Composable () -> Unit,
    onNavClick: (screen: Screen) -> Unit,
    hymns: MutableList<Pair<Hymn, PlaylistHymn?>>,
    onHymnClick: (hymnPair: Pair<Hymn, PlaylistHymn?>) -> Unit,
    isSearchActive: Boolean,
    setSearchActive: (isSearchActive: Boolean) -> Unit,
    searchQuery: MutableStateFlow<String>,
    onSearchChange: (query: String) -> Unit,
    searchResults: StateFlow<List<Hymn>>,
) {
    val query = searchQuery.collectAsState()
    val searchedHymns = searchResults.collectAsState()
    Scaffold(
        snackbarHost = snackbarHost,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            val colors1 = SearchBarDefaults.colors()
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = query.value,
                            onQueryChange = { onSearchChange(it) },
                            expanded = isSearchActive,
                            onExpandedChange = setSearchActive,
                            onSearch = {},
                            modifier = Modifier,
                            placeholder = {
                                Text(stringResource(R.string.search_hymns))
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = stringResource(R.string.icon_search)
                                )
                            },
                            trailingIcon = {
                                if (isSearchActive) {
                                    IconButton(
                                        onClick = {
                                            if (query.value.isNotEmpty()) onSearchChange("") else setSearchActive(false)
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = stringResource(
                                                R.string.icon_clear
                                            )
                                        )
                                    }
                                }
                            },
                        )
                    },
                    expanded = isSearchActive,
                    onExpandedChange = setSearchActive,
                    modifier = Modifier
                        .align(Alignment.Center),
                    //                    .fillMaxWidth(),
                    //                    .background(MaterialTheme.colorScheme.background),
                    //                    .padding(horizontal = 12.dp),
                    shape = SearchBarDefaults.inputFieldShape,
                    colors = colors1,
                    tonalElevation = SearchBarDefaults.TonalElevation,
                    shadowElevation = SearchBarDefaults.ShadowElevation,
                    windowInsets = SearchBarDefaults.windowInsets,
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(searchedHymns.value) { hymn ->
                            HymnListItem(Pair(hymn, null), onHymnClick)
                        }
                    }
                }
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
                    HymnListItem(hymn, onHymnClick)
                }
            }
        }
    }
}


@Composable
fun HymnListItem(
//    hymn: Hymn,
    hymnPair: Pair<Hymn, PlaylistHymn?>,
    onHymnClick: (hymnPair: Pair<Hymn, PlaylistHymn?>) -> Unit
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
                .fillMaxWidth()
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
    }
}