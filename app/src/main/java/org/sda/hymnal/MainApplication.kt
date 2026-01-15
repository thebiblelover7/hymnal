package org.sda.hymnal

import android.content.Context
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.sda.hymnal.data.Hymn
import org.sda.hymnal.data.Hymnal
import org.sda.hymnal.data.Hymnals
import org.sda.hymnal.data.JsonHymn
import org.sda.hymnal.data.hymnalList
import org.sda.hymnal.screen.HomeScreen
import org.sda.hymnal.screen.ListScreen
import org.sda.hymnal.screen.HymnScreen
import org.sda.hymnal.screen.HymnalEvent
import org.sda.hymnal.screen.NavigationScreens
import org.sda.hymnal.screen.HymnalViewModel
import org.sda.hymnal.screen.Screen

fun loadHymns(context: Context, hymnal: Hymnal): List<Hymn>{
//    for (hymnal in hymnalList) {
    val loadedHymnString = try {
        context.assets.open(hymnal.fileName).bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    val jsonHymns = if (loadedHymnString != null) {
        Json.decodeFromString<List<JsonHymn>>(loadedHymnString)
    } else {
        emptyList<JsonHymn>()
    }

    val hymns: List<Hymn> = jsonHymns.map { jsonHymn ->
        Hymn(
            title = jsonHymn.title,
            hymnal = hymnal,
            number = jsonHymn.number,
            text = jsonHymn.content
        )
    }
//    }

//    val hymns = emptyList<Hymn>()

    return hymns
}

@Composable
fun MainApplication(context: Context, hymnalViewModel: HymnalViewModel) {
    val navController = rememberNavController()
    val hymnalState = hymnalViewModel.hymnalState.collectAsState()
    val currentScreen = hymnalState.value.currentScreen
    val isLoadingHymns = hymnalState.value.isLoadingHymns

    LaunchedEffect(Unit) {
        hymnalViewModel.viewModelScope.launch {
            for (hymnal in hymnalList) {
                hymnalViewModel.onEvent(HymnalEvent.LoadHymns(loadHymns(context, hymnal)))
            }
            hymnalViewModel.onEvent(HymnalEvent.SetLoadingHymns(false))
            hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymnal(Hymnals.NewEnglish))
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
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            topBar = { TopHymnalBar() },
            floatingActionButton = {
                if (currentScreen == NavigationScreens.Home) {
                    HymnalFab(
                        onClick = {
                            navController.navigate(NavigationScreens.List)
                        }
                    )
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { padding ->
            NavHost(
                navController = navController,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background),
                startDestination = NavigationScreens.Home,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) }
            ) {
                screen(
                    screenObject = NavigationScreens.Home,
                    hymnalViewModel = hymnalViewModel
                ) {
                    val searchString = hymnalState.value.currentSearchString
                    HomeScreen(
                        searchString = searchString,
                        currentHymn = hymnalState.value.currentHymn,
                        onSearchStringChange = { searchString ->
                            hymnalViewModel.onEvent(HymnalEvent.SetSearchString(searchString))
                            val searchStringInt = searchString.toIntOrNull()
                            if (searchStringInt != null) {
                                val hymn =
                                    hymnalState.value.currentHymns.find { searchStringInt == it.number }
                                hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymn(hymn))
                            } else {
                                hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymn(null))
                            }
                        },
                        onHymnSubmit = {
                            if (hymnalState.value.currentHymn != null) {
                                navController.navigate(NavigationScreens.Hymn)
                            }
                        },
                        currentHymnal = hymnalState.value.currentHymnal,
                        onHymnalClick = { hymnal ->
                            hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymnal(hymnal))
                            hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymn(null))
                        },
                    )
                }
                screen(
                    screenObject = NavigationScreens.List,
                    hymnalViewModel = hymnalViewModel
                ) {
                    ListScreen(
                        padding = padding,
                        hymns = hymnalState.value.currentHymns,
                        onHymnClick = { hymn ->
                            hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymn(hymn))
                            navController.navigate(NavigationScreens.Hymn)
                        }
                    )
                }
                screen(
                    screenObject = NavigationScreens.Hymn,
                    hymnalViewModel = hymnalViewModel
                ) {
                    HymnScreen(
                        padding = padding,
                        hymn = hymnalState.value.currentHymn!!
                    )
                }
            }
        }
    }
}

inline fun <reified S : Screen> NavGraphBuilder.screen(
    screenObject: S,
    hymnalViewModel: HymnalViewModel,
    crossinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable<S> { navBackStackEntry ->
        hymnalViewModel.onEvent(HymnalEvent.SetCurrentScreen(screenObject))
        content(navBackStackEntry)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopHymnalBar() {
//    TopAppBar(
//        title = {},
//        actions = {
//
//        }
//    )
}

@Composable
fun HymnalFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.List,
            contentDescription = "List of Hymns"
        )
    }
}