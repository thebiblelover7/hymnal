package org.sda.hymnal.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animate
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.ZoomableContentLocation
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import org.sda.hymnal.HymnalTopBar
import org.sda.hymnal.R
import org.sda.hymnal.data.hymn.Hymn
import org.sda.hymnal.data.hymn.hymnTags


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HymnScreen(
    snackbarHost: @Composable () -> Unit,
    onClickBack: () -> Unit,
    fontSize: Float,
    hymn: Hymn,
    hymnIndex: Pair<Int, Int>,
    onPreviousHymnClick: () -> Unit,
    onNextHymnClick: () -> Unit,
    isLyricsScreen: Boolean,        // False if sheet music is shown
    onSheetMusicClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onPlaylistAddClick: () -> Unit,
    onLyricsClick: () -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val bottomBarScrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        FloatingToolbarExitDirection.Bottom)
    Scaffold(
        snackbarHost = snackbarHost,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .nestedScroll(bottomBarScrollBehavior),
        topBar = {
            HymnalTopBar(
                title = stringResource(R.string.hymn) + " "+ hymn.number.toString(),
                onClickBack = onClickBack,
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        bottomBar = {

        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            HymnScreenBottomBar(
                hymn = hymn,
                hymnIndex = hymnIndex,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = -FloatingToolbarDefaults.ScreenOffset)
                    .padding(8.dp)
                    .zIndex(1f),
                floatingToolbarScrollBehavior = bottomBarScrollBehavior,
                isLyricsScreen = isLyricsScreen,
                onPreviousHymnClick = onPreviousHymnClick,
                onNextHymnClick = onNextHymnClick,
                onSheetMusicClick = onSheetMusicClick,
                onFavoriteClick = onFavoriteClick,
                onPlaylistAddClick = onPlaylistAddClick,
                onLyricsClick = onLyricsClick
            )
            AnimatedContent(isLyricsScreen) { isLyricsScreen ->
                if (isLyricsScreen) {
                    AnimatedContent(
                        targetState = Pair(hymn, hymnIndex),
                        transitionSpec = {
                            val forward = targetState.second.first > initialState.second.first
                            (fadeIn() + slideInHorizontally(
                                initialOffsetX = { fullWidth -> if(forward) { fullWidth } else {-fullWidth} })).togetherWith(
                                exit = fadeOut() + slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> if(forward) { -fullWidth } else { fullWidth } }
                                )
                            )
                        },
                        contentKey = { it.second.first }
                    ) { statePair ->
                        val hymn = statePair.first
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(24.dp)
                                .padding(padding),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = hymn.number.toString(),
                                fontSize = (48 * fontSize).sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Serif,
                                modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
                            )
                            Text(
                                text = hymn.title,
                                fontSize = (36 * fontSize).sp,
                                lineHeight = (40 * fontSize).sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                            val textLines = hymn.text.lines()
                            Text(
                                text = buildAnnotatedString {
                                    for (line in textLines) {
                                        if (line in hymnTags) {
                                            withStyle(
                                                style = SpanStyle(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onBackground.copy(
                                                        0.8f
                                                    )
                                                )
                                            ) {
                                                append(line + "\n")
                                            }
                                        } else {
                                            append(line + "\n")
                                        }
                                    }
                                },
                                textAlign = TextAlign.Center,
                                fontSize = (18 * fontSize).sp,
                                lineHeight = (27 * fontSize).sp,
                                style = TextStyle(
                                    lineBreak = LineBreak.Simple
                                ),
                                modifier = Modifier.padding(12.dp, 12.dp, 12.dp, 64.dp)
                            )
                        }
                    }
                } else {
                    AnimatedContent(
                        targetState = Pair(hymn, hymnIndex),
                        transitionSpec = {
                            val forward = targetState.second.first > initialState.second.first
                            (fadeIn() + slideInHorizontally(
                                initialOffsetX = { fullWidth -> if(forward) { fullWidth } else {-fullWidth} })).togetherWith(
                                exit = fadeOut() + slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> if(forward) { -fullWidth } else { fullWidth } }
                                )
                            )
                        },
                        contentKey = { it.second.first }
                    ) { statePair ->
                        val hymn = statePair.first
                        val pagerState = rememberPagerState { hymn.sheetMusic.size }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                        ) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                                    .nestedScroll(bottomBarScrollBehavior)
                            ) { page ->
                                val resource = hymn.sheetMusic[page]
                                if (resource != 0) {
                                    val painter = painterResource(resource)
                                    val zoomableState = rememberZoomableState(
                                        zoomSpec = ZoomSpec(maxZoomFactor = 4f)
                                    ).apply {
                                        setContentLocation(
                                            ZoomableContentLocation.scaledInsideAndCenterAligned(painter.intrinsicSize)
                                        )
                                    }
                                    var showToolbar by remember { mutableStateOf(true) }
                                    LaunchedEffect(zoomableState.zoomFraction) {
                                        snapshotFlow { zoomableState.zoomFraction }
                                            .collect { fraction ->
                                                val isZoomed = (fraction ?: -1f) > 0f
                                                showToolbar = !isZoomed
                                            }
                                    }
                                    LaunchedEffect(showToolbar) {
                                        snapshotFlow { showToolbar }.collect { showToolbar ->
                                            val targetBottomOffset = if (showToolbar) {
                                                0f
                                            } else {
                                                bottomBarScrollBehavior.state.offsetLimit
                                            }
                                            val targetTopOffset = if (showToolbar) {
                                                0f
                                            } else {
                                                topAppBarScrollBehavior.state.heightOffsetLimit
                                            }
                                            launch {
                                                animate(
                                                    initialValue = bottomBarScrollBehavior.state.offset,
                                                    targetValue = targetBottomOffset
                                                ) { value, _ ->
                                                    bottomBarScrollBehavior.state.offset = value
                                                }
                                            }
                                            launch {
                                                animate(
                                                    initialValue = topAppBarScrollBehavior.state.heightOffset,
                                                    targetValue = targetTopOffset
                                                ) { value, _ ->
                                                    topAppBarScrollBehavior.state.heightOffset =
                                                        value
                                                }
                                            }
                                        }
                                    }
                                    Image(
                                        painter = painterResource(resource),
                                        contentDescription = stringResource(R.string.icon_sheet_music),
                                        alignment = Alignment.Center,
                                        contentScale = ContentScale.Inside,
                                        modifier = Modifier
                                            .zoomable(
                                                state = zoomableState,
                                                onClick = {
                                                    showToolbar = !showToolbar
                                                }
                                            )
                                            .fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HymnScreenBottomBar(
    hymn: Hymn,
    hymnIndex: Pair<Int, Int>,
    modifier: Modifier,
    floatingToolbarScrollBehavior: FloatingToolbarScrollBehavior? = null,
    isLyricsScreen: Boolean,
    onPreviousHymnClick: () -> Unit,
    onNextHymnClick: () -> Unit,
    onSheetMusicClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onPlaylistAddClick: () -> Unit,
    onLyricsClick: () -> Unit
) {
    HorizontalFloatingToolbar(
        modifier = modifier,
        scrollBehavior = floatingToolbarScrollBehavior,
        content = {
            val strPrevHymn = stringResource(R.string.previous_hymn)
            val strNextHymn = stringResource(R.string.next_hymn)
            val strFavorite = stringResource(R.string.favorite)
            val strPlaylistAdd = stringResource(R.string.add_to_playlist)
            val strSheetMusic = stringResource(R.string.sheet_music)
            val strLyrics = stringResource(R.string.lyrics)
            AppBarRow {
                clickableItem(
                    onClick = onPreviousHymnClick,
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowLeft,
                            contentDescription = stringResource(R.string.icon_left_arrow)
                        )
                    },
                    label = strPrevHymn,
                    enabled = hymnIndex.first > 1
                )
                clickableItem(
                    onClick = onNextHymnClick,
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowRight,
                            contentDescription = stringResource(R.string.icon_right_arrow)
                        )
                    },
                    label = strNextHymn,
                    enabled = hymnIndex.first < hymnIndex.second
                )
                toggleableItem(
                    checked = hymn.favorite,
                    onCheckedChange = { onFavoriteClick() },
                    icon = {
                        AnimatedContent(hymn.favorite) { favorite ->
                            if (favorite) {
                                Icon(
                                    Icons.Filled.Favorite,
                                    contentDescription = stringResource(R.string.icon_favorite)
                                )
                            } else {
                                Icon(
                                    Icons.Filled.FavoriteBorder,
                                    contentDescription = stringResource(R.string.icon_favorite)
                                )
                            }
                        }
                    },
                    label = strFavorite
                )
                clickableItem(
                    onClick = onPlaylistAddClick,
                    icon = {
                        Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = stringResource(R.string.icon_add_to_playlist))
                    },
                    label = strPlaylistAdd
                )
                clickableItem(
                    onClick = if (isLyricsScreen) {onSheetMusicClick} else {onLyricsClick},
                    icon = {
                        AnimatedContent(isLyricsScreen) { isLyricsScreen ->
                            if (isLyricsScreen) {
                                Icon(
                                    Icons.Default.Piano,
                                    contentDescription = stringResource(R.string.icon_music_note)
                                )
                            } else {
                                Icon(
                                    Icons.Default.Lyrics,
                                    contentDescription = stringResource(R.string.lyrics)
                                )
                            }
                        }
                    },
                    label = if (isLyricsScreen) {strSheetMusic} else {strLyrics},
                    enabled = if (isLyricsScreen) {hymn.sheetMusic.isNotEmpty()} else {true}
                )
            }
        },
        expanded = true,
    )
}