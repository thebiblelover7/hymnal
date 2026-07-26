package org.sda.hymnal.screen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExpandedFullScreenContainedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberContainedSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import org.sda.hymnal.BottomHymnalBar
import org.sda.hymnal.R
import org.sda.hymnal.data.hymn.Hymn
import org.sda.hymnal.data.playlist.PlaylistHymn
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    FlowPreview::class
)
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
    onSearch: (searchQuery: String) -> Unit,
    onSearchChange: (query: String) -> Unit,
    searchResults: SnapshotStateList<Hymn>,
) {
    val query = searchQuery.collectAsState()
    LaunchedEffect(query.value) {
        delay(300.milliseconds)
        onSearch(query.value)
    }
    val scope = rememberCoroutineScope()
    val scrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()
    val textFieldState = rememberTextFieldState()
    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text }
            .debounce(300.milliseconds)
            .distinctUntilChanged()
            .collect { query ->
                onSearch(query.toString())
            }
    }
    val searchState = rememberContainedSearchBarState()
    val inputField =
        @Composable {
            SearchBarDefaults.InputField(
                textFieldState = textFieldState,
                searchBarState = searchState,
                onSearch = {
                    onSearch(it)
                    Log.d("search", "searching: $it")
                },
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
                    AnimatedVisibility(
                        visible = searchState.currentValue == SearchBarValue.Expanded,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (textFieldState.text.isNotEmpty()) textFieldState.clearText() else searchState.animateToCollapsed()
                                }
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
                }
            )
        }
    Scaffold(
        snackbarHost = snackbarHost,
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .fillMaxSize(),
        topBar = {
            AppBarWithSearch(
                state = searchState,
                scrollBehavior = scrollBehavior,
                inputField = inputField,
            )
            ExpandedFullScreenContainedSearchBar(
                state = searchState,
                inputField = inputField

            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(searchResults, key = {"${it.number} ${it.hymnal.fileName}"}) { hymn ->
                        HymnListItem(
                            hymnPair = Pair(hymn, null),
                            modifier = Modifier.animateItem()
                                .clickable(onClick = {onHymnClick(Pair(hymn, null))}),
                        )
                    }
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = !isSearchActive,
                enter = slideInVertically(),
                exit = slideOutVertically()
            ) {
                BottomHymnalBar(
                    currentScreen = currentScreen,
                    onNavClick = onNavClick,
                )
            }
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
            indicatorContent = { index, isThumbSelected ->
                AnimatedVisibility(
                    enter = fadeIn() + slideInHorizontally(initialOffsetX = {it / 2}),
                    exit = fadeOut() + slideOutHorizontally(targetOffsetX = {it / 2}),
                    visible = isThumbSelected,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = "${hymns[index].first.number}",
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.background(
                            color = MaterialTheme.colorScheme.secondary.copy(0.8f),
                            shape = CircleShape
                        )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(vertical = 10.dp),
                state = listState
            ) {
                items(hymns) { hymn ->
                    HymnListItem(
                        hymnPair = hymn,
                        modifier = Modifier.animateItem()
                            .clickable(onClick = {onHymnClick(hymn)}),
                    )
                }
            }
        }
    }
}

@Composable
fun HymnListItem(
    hymnPair: Pair<Hymn, PlaylistHymn?>,
    modifier: Modifier,
    trailingContent: @Composable () -> Unit = { },
) {
    ListItem(
        modifier = modifier,
        leadingContent = {
            Text(
                text = hymnPair.first.number.toString(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.defaultMinSize(minWidth = 50.dp)
            )
        },
        headlineContent = {
            Text(
                text = hymnPair.first.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            val shortTextLine = hymnPair.first.firstLine + TextOverflow.Ellipsis.toString()
            Text(
                text = shortTextLine,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground.copy(
                    alpha = 0.8f
                )
            )
        },
        trailingContent = trailingContent
    )
}