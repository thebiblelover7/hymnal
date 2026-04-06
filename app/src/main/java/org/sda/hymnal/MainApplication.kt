package org.sda.hymnal

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.sda.hymnal.data.Hymn
import org.sda.hymnal.data.Hymnal
import org.sda.hymnal.data.Hymnals
import org.sda.hymnal.data.JsonHymn
import org.sda.hymnal.data.hymnalList
import org.sda.hymnal.screen.HomeScreen
import org.sda.hymnal.screen.HymnScreen
import org.sda.hymnal.screen.HymnalEvent
import org.sda.hymnal.screen.HymnalViewModel
import org.sda.hymnal.screen.ListScreen
import org.sda.hymnal.screen.NavigationScreens
import org.sda.hymnal.screen.Screen
import org.sda.hymnal.screen.SettingsScreen
import org.sda.hymnal.screen.SheetMusicScreen

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

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("DiscouragedApi")
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
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
            rememberTopAppBarState()
        )
        var searchDebounceJob: Job? by remember { mutableStateOf(null)}
        val searchCoroutineScope = rememberCoroutineScope()
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
                    onNavClick = { screen ->
                        navController.popBackStack()
                        navController.navigate(screen)
                    },
                    searchString = searchNumber,
                    currentHymn = hymnalState.value.currentSearchHymn,
                    onSearchStringChange = { searchNumber ->
                        hymnalViewModel.onEvent(HymnalEvent.SetSearchNumber(searchNumber))
                        val searchNumberInt = searchNumber.toIntOrNull()
                        if (searchNumberInt != null) {
                            val hymn =
                                hymnalState.value.currentHymns.find { searchNumberInt == it.number }
                            hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymn(hymn))
                            hymnalViewModel.onEvent(HymnalEvent.SetSearchHymn(hymn))
                            if (hymn != null) {
                                val sheetMusic = getSheetMusic(context, hymn)
                                hymnalViewModel.onEvent(HymnalEvent.SetCurrentSheetMusic(sheetMusic))
                            }
                        } else {
                            hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymn(null))
                            hymnalViewModel.onEvent(HymnalEvent.SetSearchHymn(null))
                        }
                    },
                    onHymnSubmit = {
                        if (hymnalState.value.currentSearchHymn != null && hymnalState.value.currentHymn == hymnalState.value.currentSearchHymn) {
                            navController.navigate(NavigationScreens.Hymn)
                        } else {
                            val searchStringInt = searchNumber.toIntOrNull()
                            if (searchStringInt != null) {
                                val hymn =
                                    hymnalState.value.currentHymns.find { searchStringInt == it.number }
                                hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymn(hymn))
                                hymnalViewModel.onEvent(HymnalEvent.SetSearchHymn(hymn))
                                if (hymn != null) {
                                    val sheetMusic = getSheetMusic(context, hymn)
                                    hymnalViewModel.onEvent(HymnalEvent.SetCurrentSheetMusic(sheetMusic))
                                    navController.navigate(NavigationScreens.Hymn)
                                }
                            }
                        }
                    },
                    currentHymnal = hymnalState.value.currentHymnal,
                    onHymnalClick = { hymnal ->
                        searchCoroutineScope.launch {
                            hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymnal(hymnal))
                            delay(100)  // Needs a small delay to load the hymnal in the background
                            hymnalViewModel.onEvent(HymnalEvent.SetSearchNumber(searchNumber))
                            val searchNumberInt = searchNumber.toIntOrNull()
                            if (searchNumberInt != null) {
                                val hymn =
                                    hymnalState.value.currentHymns.find { searchNumberInt == it.number }
                                hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymn(hymn))
                                hymnalViewModel.onEvent(HymnalEvent.SetSearchHymn(hymn))
                                if (hymn != null) {
                                    val sheetMusic = getSheetMusic(context, hymn)
                                    hymnalViewModel.onEvent(
                                        HymnalEvent.SetCurrentSheetMusic(
                                            sheetMusic
                                        )
                                    )
                                }
                            } else {
                                hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymn(null))
                                hymnalViewModel.onEvent(HymnalEvent.SetSearchHymn(null))
                            }
                        }
                    }
                )
            }
            homeScreen(
                screenObject = NavigationScreens.List,
                hymnalViewModel = hymnalViewModel
            ) {
                ListScreen(
                    currentScreen = hymnalState.value.currentScreen,
                    onNavClick = { screen ->
                        navController.popBackStack()
                        navController.navigate(screen)
                    },
                    hymns = hymnalState.value.currentHymns,
                    searchQuery = hymnalState.value.currentSearchString,
                    isPerformingSearch = hymnalState.value.isPerformingSearch,
                    onSearchChange = { query ->
                        Log.d("search", "onSearchChange")
                        hymnalViewModel.onEvent(HymnalEvent.SetSearchString(query))
//                        hymnalViewModel.onEvent(HymnalEvent.PerformSearch)
//
//                        searchDebounceJob?.cancel()
//
//                        if (query.isNotBlank()) {
//                            searchDebounceJob = searchCoroutineScope.launch {
//                                delay(1000)
//                                Log.d("search", "actually performing search")
//                            }
//                        }
                    },
                    searchResults = hymnalState.value.searchedHymns,
                    onHymnClick = { hymn ->
                        hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymn(hymn))
                        val sheetMusic = getSheetMusic(context, hymn)
                        hymnalViewModel.onEvent(HymnalEvent.SetCurrentSheetMusic(sheetMusic))
                        navController.navigate(NavigationScreens.Hymn)
                    }
                )
            }
            homeScreen(
                screenObject = NavigationScreens.Info,
                hymnalViewModel = hymnalViewModel
            ) {
                SettingsScreen(
                    currentScreen = hymnalState.value.currentScreen,
                    onNavClick = { screen ->
                        navController.popBackStack()
                        navController.navigate(screen)
                    }
                )
            }
            screen(
                screenObject = NavigationScreens.Hymn,
                hymnalViewModel = hymnalViewModel
            ) {
                HymnScreen(
                    onClickBack = {
                        navController.navigateUp()
                    },
                    hymn = hymnalState.value.currentHymn!!,
                    hymnTotal = hymnalState.value.currentHymns.size,
                    onPreviousHymnClick = {
                        val hymn = hymnalState.value.currentHymns.find { it.number == hymnalState.value.currentHymn!!.number - 1 }
                        if (hymn != null) {
                            hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymn(hymn))
                            val sheetMusic = getSheetMusic(context, hymn)
                            hymnalViewModel.onEvent(HymnalEvent.SetCurrentSheetMusic(sheetMusic))
                        }
                    },
                    onNextHymnClick = {
                        val hymn = hymnalState.value.currentHymns.find { it.number == hymnalState.value.currentHymn!!.number + 1 }
                        if (hymn != null) {
                            hymnalViewModel.onEvent(HymnalEvent.SetCurrentHymn(hymn))
                            val sheetMusic = getSheetMusic(context, hymn)
                            hymnalViewModel.onEvent(HymnalEvent.SetCurrentSheetMusic(sheetMusic))
                        }
                    },
                    onSheetMusicClick = {
                        navController.navigate(NavigationScreens.SheetMusic)
                    }
                )
            }
            screen(
                screenObject = NavigationScreens.SheetMusic,
                hymnalViewModel = hymnalViewModel
            ) {
                val hymn = hymnalState.value.currentHymn!!
                SheetMusicScreen(
                    onClickBack = {
                        navController.navigateUp()
                    },
                    hymn = hymn
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

@SuppressLint("DiscouragedApi")
fun getSheetMusic(context: Context, hymn: Hymn): List<Int> {
    val resourceArray = arrayListOf<Int>()
    if (hymn.hymnal.sheetsPrefix == "") {       // Exit early if there is no sheetsPrefix
        return resourceArray.toList()
    }
    val hymnName = hymn.hymnal.sheetsPrefix + hymn.number.toString().padStart(3,'0')
    if (hymnName.toIntOrNull() != null) {       // Have to check if hymnName is just integer in which case sheet music doesn't exist
        return resourceArray.toList()
    }
    val resource = context.resources.getIdentifier(
        hymnName,
        "drawable",
        context.packageName
    )
    if (resource != 0) {                        // Check if resource exists
        resourceArray.add(resource)
    } else {
        return resourceArray.toList()
    }
    for (num in 1..6) {
        val resource = context.resources.getIdentifier(
            hymn.hymnal.sheetsPrefix + hymn.number.toString().padStart(3,'0') + "_" + num,
            "drawable",
            context.packageName
        )
        if (resource != 0) {
            resourceArray.add(resource)
        }
    }
    return resourceArray.toList()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopHymnalBar(
    currentScreen: Screen,
    onClickBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    AnimatedContent(currentScreen)
    { currentScreen ->
        when (currentScreen) {
            is NavigationScreens.Home -> {

            }
            is NavigationScreens.Hymn -> {
                TopAppBar(
                    title = {
                        Text("Lyrics")
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onClickBack
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
            is NavigationScreens.List -> {

//                if (isSearching) {
//                } else {
//                    TopAppBar(
//                        title = {
//                            Text("List of Hymns")
//                        },
//                        navigationIcon = {
//                            IconButton(
//                                onClick = onClickBack
//                            ) {
//                                Icon(
//                                    painter = painterResource(R.drawable.outline_arrow_back_24),
//                                    contentDescription = "Back"
//                                )
//                            }
//                        }
//                    )
//                }
            }
            is NavigationScreens.SheetMusic -> {
                TopAppBar(
                    title = {
                        Text("Sheet Music")
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onClickBack
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        }
    }
}

class NavItem(
    val title: String,
    val screen: Screen,
    val icon: ImageVector,
    val iconSelected: ImageVector
)

@Composable
fun BottomHymnalBar(
    currentScreen: Screen,
    onNavClick: (screen: Screen) -> Unit
) {
    val navScreens = listOf<Screen>(NavigationScreens.Home, NavigationScreens.List,
        NavigationScreens.Info)

    AnimatedContent(navScreens.contains(currentScreen)) { targetState ->
        if (targetState) {
            val navItems = listOf(
                NavItem(
                    title = "Home",
                    screen = NavigationScreens.Home,
                    icon = Icons.Outlined.Home,
                    iconSelected = Icons.Filled.Home
                ),
                NavItem(
                    title = "List",
                    screen = NavigationScreens.List,
                    icon = Icons.AutoMirrored.Default.List,
                    iconSelected = Icons.AutoMirrored.Default.List
                ),
                NavItem(
                    title = "Info",
                    screen = NavigationScreens.Info,
                    icon = Icons.Outlined.Info,
                    iconSelected = Icons.Filled.Info
                )
            )
            NavigationBar(

            ) {
                navItems.forEach { navItem ->
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