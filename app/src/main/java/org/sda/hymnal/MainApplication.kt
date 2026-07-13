package org.sda.hymnal

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.sda.hymnal.screen.HomeScreen
import org.sda.hymnal.screen.HymnScreen
import org.sda.hymnal.screen.HymnalEvent
import org.sda.hymnal.data.HymnalViewModel
import org.sda.hymnal.screen.ListScreen
import org.sda.hymnal.screen.NavigationScreens
import org.sda.hymnal.screen.PlaylistHymnsScreen
import org.sda.hymnal.screen.PlaylistsScreen
import org.sda.hymnal.screen.Screen
import org.sda.hymnal.screen.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("DiscouragedApi")
@Composable
fun MainApplication(hymnalViewModel: HymnalViewModel) {
    val navController = rememberNavController()
    val hymnalState = hymnalViewModel.hymnalState.collectAsState()
    val isLoadingHymns = hymnalState.value.isLoadingHymns

    LaunchedEffect(Unit) {
        hymnalViewModel.viewModelScope.launch {
//            for (hymnal in hymnalList) {
//                hymnalViewModel.onEvent(HymnalEvent.LoadHymns(loadHymns(context, hymnal)))
//            }
            hymnalViewModel.onEvent(HymnalEvent.LoadSettings)
//            hymnalViewModel.onEvent(HymnalEvent.LoadHymns(emptyList()))
//            hymnalViewModel.onEvent(HymnalEvent.SetLoadingHymns(false))
//            hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymnal(Hymnals.NewEnglish))
        }

    }
    if (isLoadingHymns) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }
    } else {
        NavHost(
            navController = navController,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background),
            startDestination = NavigationScreens.Home,
//            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
//            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
//            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End) },
//            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) }
        ) {
            homeScreen(
                screenObject = NavigationScreens.Home,
                hymnalViewModel = hymnalViewModel
            ) {
                val searchNumber = hymnalState.value.currentSearchNumber
                HomeScreen(
                    currentScreen = hymnalState.value.currentScreen,
                    snackbarHost = {
                        SnackbarHost(hostState = hymnalState.value.snackbarHostState)
                    },
                    onNavClick = { screen ->
                        navController.popBackStack()
                        navController.navigate(screen)
                    },
                    searchString = searchNumber,
                    currentHymn = hymnalState.value.currentSearchHymn,
                    onSearchStringChange = { searchNumber ->
                        hymnalViewModel.onEvent(HymnalEvent.SetSearchNumber(searchNumber))
                    },
                    onHymnSubmit = {
                        if (hymnalState.value.currentSearchHymn != null
                            && hymnalState.value.currentHymnPair?.first == hymnalState.value.currentSearchHymn)
                        {
                            hymnalViewModel.onEvent(HymnalEvent.SetInPlaylist(false))
                            navController.navigate(NavigationScreens.Hymn)
                        } else {
                            hymnalViewModel.onEvent(HymnalEvent.SetSearchNumber(searchNumber))
                        }
                    },
                    currentHymnal = hymnalState.value.currentHymnal,
                    onHymnalClick = { hymnal ->
                        hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymnal(hymnal))
                    }
                )
            }
            homeScreen(
                screenObject = NavigationScreens.List,
                hymnalViewModel = hymnalViewModel
            ) {
                ListScreen(
                    currentScreen = hymnalState.value.currentScreen,
                    snackbarHost = {
                        SnackbarHost(hostState = hymnalState.value.snackbarHostState)
                    },
                    onNavClick = { screen ->
                        navController.popBackStack()
                        navController.navigate(screen)
                    },
                    hymns = hymnalState.value.currentHymns.map { hymn ->
                        Pair(hymn, null)
                    }.toMutableList(),
                    searchQuery = hymnalState.value.currentSearchString,
                    isPerformingSearch = hymnalState.value.isPerformingSearch,
                    onSearchChange = { query ->
                        hymnalViewModel.onEvent(HymnalEvent.SetSearchString(query))
                    },
                    searchResults = hymnalState.value.searchedHymns,
                    onHymnClick = { hymn ->
                        hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymn(hymn))
                        hymnalViewModel.onEvent(HymnalEvent.SetInPlaylist(false))
                        navController.navigate(NavigationScreens.Hymn)
                    }
                )
            }
            homeScreen(
                screenObject = NavigationScreens.PlaylistList,
                hymnalViewModel = hymnalViewModel
            ) {
                PlaylistsScreen(
                    snackbarHost = {
                        SnackbarHost(hostState = hymnalState.value.snackbarHostState)
                    },
                    currentScreen = hymnalState.value.currentScreen,
                    onNavClick = { screen ->
                        navController.popBackStack()
                        navController.navigate(screen)
                    },
                    playlists = hymnalState.value.playlists,
                    onPlaylistClick = { playlist ->
                        hymnalViewModel.onEvent(HymnalEvent.LoadPlaylist(playlist))
                        navController.navigate(NavigationScreens.PlaylistHymnsList)
                    },
                    onPlaylistAddClick = { playlistName ->
                        hymnalViewModel.onEvent(HymnalEvent.AddPlaylist(playlistName))
                    },
                    onPlaylistDeleteClick = { playlist ->
                        hymnalViewModel.onEvent(HymnalEvent.DeletePlaylist(playlist))
                    },
                    onPlaylistRenameClick = { playlist, name ->
                        hymnalViewModel.onEvent(HymnalEvent.RenamePlaylist(playlist, name))
                    },
                    onClickBack = {
                        navController.navigateUp()
                    }
                )
            }
            homeScreen(
                screenObject = NavigationScreens.PlaylistHymnsList,
                hymnalViewModel = hymnalViewModel
            ) {
                PlaylistHymnsScreen(
                    currentScreen = hymnalState.value.currentScreen,
                    snackbarHost = {
                        SnackbarHost(hostState = hymnalState.value.snackbarHostState)
                    },
                    onNavClick = { screen ->
                        navController.popBackStack()
                        navController.navigate(screen)
                    },
                    hymns = hymnalState.value.currentPlaylistPair,
                    onHymnClick = { hymn ->
                        hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymn(hymn))
                        hymnalViewModel.onEvent(HymnalEvent.SetInPlaylist(true))
                        navController.navigate(NavigationScreens.Hymn)
                    },
                    onClickBack = {
                        navController.navigateUp()
                    },
                    playlist = hymnalState.value.currentPlaylist ?: return@homeScreen,
                    onRemoveClick = { hymnPair, playlist ->
                        hymnalViewModel.onEvent(HymnalEvent.RemoveHymnFromPlaylist(hymnPair, playlist))
                    },
                    onMoveClick = { hymnPair, playlist, moveBy ->
                        hymnalViewModel.onEvent(HymnalEvent.MoveHymnInPlaylist(hymnPair, playlist, moveBy))
                    }
                )
            }
            homeScreen(
                screenObject = NavigationScreens.Settings,
                hymnalViewModel = hymnalViewModel
            ) {
                SettingsScreen(
                    currentScreen = hymnalState.value.currentScreen,
                    snackbarHost = {
                        SnackbarHost(hostState = hymnalState.value.snackbarHostState)
                    },
                    fontSize = hymnalState.value.settings.fontSize,
                    onNavClick = { screen ->
                        navController.popBackStack()
                        navController.navigate(screen)
                    },
                    onFontSizeSet = { fontSize ->
                        hymnalViewModel.onEvent(HymnalEvent.SetFontSize(fontSize))
                    }
                )
            }
            screen(
                screenObject = NavigationScreens.Hymn,
                hymnalViewModel = hymnalViewModel
            ) {
                HymnScreen(
                    snackbarHost = {
                        SnackbarHost(hostState = hymnalState.value.snackbarHostState)
                    },
                    onClickBack = {
                        navController.navigateUp()
                    },
                    fontSize = hymnalState.value.settings.fontSize,
                    hymn = hymnalState.value.currentHymnPair?.first ?: return@screen,
//                    hymnPosition = if (hymnalState.value.isInPlaylist) hymnalState.value.,
                    hymnIndex = if (hymnalState.value.isInPlaylist) {
                        Pair(hymnalState.value.currentHymnPair?.second?.position ?: return@screen, hymnalState.value.currentPlaylist?.count ?: return@screen)
                    } else {
                        Pair(hymnalState.value.currentHymnPair?.first?.number ?: return@screen, hymnalState.value.currentHymns.size)
                    },
//                    hymnTotal = hymnalState.value.currentHymns.size,
                    isLyricsScreen = hymnalState.value.isLyricsScreen,
                    onPreviousHymnClick = {
                        if (hymnalState.value.isInPlaylist) {
                            val index = hymnalState.value.currentPlaylistPair.indexOf(hymnalState.value.currentHymnPair)
                            if (index != -1 && index > 0) {
                                hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymn(hymnalState.value.currentPlaylistPair[index - 1]))
                            }
                        } else {
                            val index = hymnalState.value.currentHymns.indexOf(hymnalState.value.currentHymnPair?.first)
                            if (index != -1 && index > 0) {
                                hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymn(Pair(hymnalState.value.currentHymns[index - 1], null)))
                            }
                        }
                    },
                    onNextHymnClick = {
                        if (hymnalState.value.isInPlaylist) {
                            val index = hymnalState.value.currentPlaylistPair.indexOf(hymnalState.value.currentHymnPair)
                            if (index != -1 && index < hymnalState.value.currentPlaylistPair.size - 1) {
                                hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymn(hymnalState.value.currentPlaylistPair[index + 1]))
                            }
                        } else {
                            val index = hymnalState.value.currentHymns.indexOf(hymnalState.value.currentHymnPair?.first)
                            if (index != -1 && index < hymnalState.value.currentHymns.size - 1) {
                                hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymn(Pair(hymnalState.value.currentHymns[index + 1], null)))
                            }
                        }
                    },
                    onSheetMusicClick = {
                        hymnalViewModel.onEvent(HymnalEvent.SetLyricsScreen(false))
                    },
                    onLyricsClick = {
                        hymnalViewModel.onEvent(HymnalEvent.SetLyricsScreen(true))
                    },
                    onFavoriteClick = {
                        hymnalViewModel.onEvent(HymnalEvent.SetFavorite)
                    },
                    onPlaylistAddClick = {
                        navController.navigate(NavigationScreens.PlaylistSelector)
                    }
                )
            }
            screen(
                screenObject = NavigationScreens.PlaylistSelector,
                hymnalViewModel = hymnalViewModel
            ) {
                PlaylistsScreen(
                    snackbarHost = {
                        SnackbarHost(hostState = hymnalState.value.snackbarHostState)
                    },
                    currentScreen = hymnalState.value.currentScreen,
                    onNavClick = {},
                    playlists = hymnalState.value.playlists,
                    onPlaylistClick = { playlist ->
                        val currentHymnPair = hymnalState.value.currentHymnPair
                        if (currentHymnPair != null) {
                            hymnalViewModel.onEvent(
                                HymnalEvent.AddHymnToPlaylist(
                                    hymnPair = currentHymnPair,
                                    playlist = playlist
                                )
                            )

                        }
                        navController.popBackStack()
                    },
                    onPlaylistAddClick = { playlistName ->
                        hymnalViewModel.onEvent(HymnalEvent.AddPlaylist(playlistName))
                    },
                    onPlaylistDeleteClick = { playlist ->
                        hymnalViewModel.onEvent(HymnalEvent.DeletePlaylist(playlist))
                    },
                    onPlaylistRenameClick = { playlist, name ->
                        hymnalViewModel.onEvent(HymnalEvent.RenamePlaylist(playlist, name))
                    },
                    onClickBack = {
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}

inline fun <reified S : Screen> NavGraphBuilder.screen(
    screenObject: S,
    hymnalViewModel: HymnalViewModel,
    crossinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable<S>(
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) }
    ) { navBackStackEntry ->
        hymnalViewModel.onEvent(HymnalEvent.SetCurrentScreen(screenObject))
        content(navBackStackEntry)
    }
}

inline fun <reified S : Screen> NavGraphBuilder.homeScreen(
    screenObject: S,
    hymnalViewModel: HymnalViewModel,
    crossinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable<S> { navBackStackEntry ->
        hymnalViewModel.onEvent(HymnalEvent.SetCurrentScreen(screenObject))
        content(navBackStackEntry)
    }
}

class NavItem(
    val title: String,
    val screen: Screen,
    val icon: ImageVector,
    val iconSelected: ImageVector,
    val inBar: Boolean = true
)

@Composable
fun BottomHymnalBar(
    currentScreen: Screen,
    onNavClick: (screen: Screen) -> Unit
) {
    val navItems = listOf(
        NavItem(
            title = stringResource(R.string.nav_home),
            screen = NavigationScreens.Home,
            icon = Icons.Outlined.Home,
            iconSelected = Icons.Filled.Home
        ),
        NavItem(
            title = stringResource(R.string.nav_list),
            screen = NavigationScreens.List,
            icon = Icons.AutoMirrored.Default.List,
            iconSelected = Icons.AutoMirrored.Default.List
        ),
        NavItem(
            title = stringResource(R.string.nav_playlists),
            screen = NavigationScreens.PlaylistList,
            icon = Icons.Outlined.Star,
            iconSelected = Icons.Filled.Star
        ),
        NavItem(
            title = stringResource(R.string.nav_playlists),
            screen = NavigationScreens.PlaylistList,
            icon = Icons.Outlined.Star,
            iconSelected = Icons.Filled.Star,
            inBar = false
        ),
        NavItem(
            title = stringResource(R.string.nav_settings),
            screen = NavigationScreens.Settings,
            icon = Icons.Outlined.Settings,
            iconSelected = Icons.Filled.Settings
        )
    )
    AnimatedContent(navItems.any { it.screen == currentScreen }) { targetState ->
        if (targetState) {
            NavigationBar {
                navItems.forEach { navItem ->
                    if (navItem.inBar) {
                        NavigationBarItem(
                            selected = currentScreen == navItem.screen,
                            onClick = {
                                onNavClick(navItem.screen)
                            },
                            icon = {
                                val selected = currentScreen == navItem.screen
                                Icon(
                                    imageVector = if (selected) navItem.iconSelected else navItem.icon,
                                    contentDescription = navItem.title
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HymnalTopBar(
    title: String,
    onClickBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(title)
        },
        navigationIcon = {
            IconButton(
                onClick = onClickBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(R.string.icon_back)
                )
            }
        }
    )
}