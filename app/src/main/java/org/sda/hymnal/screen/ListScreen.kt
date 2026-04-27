package org.sda.hymnal.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import org.sda.hymnal.BottomHymnalBar
import org.sda.hymnal.data.Hymn
import org.sda.hymnal.data.hymnTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    currentScreen: Screen,
    onNavClick: (screen: Screen) -> Unit,
    hymns: List<Hymn>,
    onHymnClick: (hymn: Hymn) -> Unit,
    isPerformingSearch: Boolean,
    searchQuery: MutableStateFlow<String>,
    onSearchChange: (query: String) -> Unit,
    searchResults: StateFlow<List<Hymn>>,
) {
    var searchActive by remember { mutableStateOf(false) }
    val query = searchQuery.collectAsState()
    val searchedHymns = searchResults.collectAsState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            val colors1 = SearchBarDefaults.colors()
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = query.value,
                        onQueryChange = {onSearchChange(it)},
                        expanded = searchActive,
                        onExpandedChange = {
                            searchActive = it
                        },
                        onSearch = {},
                        modifier = Modifier,
                        placeholder = {
                                Text("Search hymns...")
                            },
                        leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search icon")
                            },
                        trailingIcon = {
                                if (searchActive) {
                                    IconButton(
                                        onClick = {
                                            if (query.value.isNotEmpty()) onSearchChange("") else searchActive = false
                                        }
                                    ) { Icon(Icons.Default.Close, contentDescription = "Clear search")}
                                }
                            },
                    )
                },
                expanded = searchActive,
                onExpandedChange = { searchActive = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 12.dp),
                shape = SearchBarDefaults.inputFieldShape,
                colors = colors1,
                tonalElevation = SearchBarDefaults.TonalElevation,
                shadowElevation = SearchBarDefaults.ShadowElevation,
                windowInsets = SearchBarDefaults.windowInsets,
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isPerformingSearch) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    items(searchedHymns.value) { hymn ->
                        HymnListItem(hymn, onHymnClick)
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
            settings = ScrollbarSettings.Default.copy(
                thumbUnselectedColor = MaterialTheme.colorScheme.onPrimary,
                thumbSelectedColor = MaterialTheme.colorScheme.onSecondary
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
                contentPadding = padding,
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
    hymn: Hymn,
    onHymnClick: (hymn: Hymn) -> Unit
) {
    val regex = Regex("(${hymnTags.joinToString(separator = "|")})\n")
    Row(
        modifier = Modifier
            .clickable(
                enabled = true,
                onClick = { onHymnClick(hymn) }
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
                text = hymn.number.toString(),
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
                text = hymn.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            val shortTextLine = hymn.text.take(50)
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