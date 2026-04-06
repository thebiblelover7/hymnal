package org.sda.hymnal.screen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import org.sda.hymnal.data.Hymn
import org.sda.hymnal.data.hymnTags


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HymnScreen(
    onClickBack: () -> Unit,
    hymn: Hymn,
    hymnTotal: Int,
    onPreviousHymnClick: () -> Unit,
    onNextHymnClick: () -> Unit,
    onSheetMusicClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            HymnScreenTopBar(
                title = "Hymn " + hymn.number.toString(),
                onClickBack = onClickBack
            )
        },
        bottomBar = {

        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(padding)
        ) {
            HymnScreenBottomBar(
                hymn = hymn,
                hymnTotal = hymnTotal,
                modifier = Modifier
                    .align(Alignment.BottomCenter).offset(y = -FloatingToolbarDefaults.ScreenOffset).zIndex(1f),
                onPreviousHymnClick = onPreviousHymnClick,
                onNextHymnClick = onNextHymnClick,
                onSheetMusicClick = onSheetMusicClick
            )
            AnimatedContent(
                targetState = hymn,
                transitionSpec = {
                    val forward = targetState.number > initialState.number
                    (fadeIn() + slideInHorizontally(
                        initialOffsetX = { fullWidth -> if(forward) { fullWidth } else {-fullWidth} })).togetherWith(
                        exit = fadeOut() + slideOutHorizontally(
                            targetOffsetX = { fullWidth -> if(forward) { -fullWidth } else { fullWidth } }
                        )
                    )
                }
            ) { hymn ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
    //                    .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = hymn.number.toString(),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif,
                        modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
//            style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = hymn.title,
//            fontFamily = FontFamily.Serif,
                        fontSize = 36.sp,
                        lineHeight = 40.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
//        Text(
//            text = hymn.hymnal.title,
////            fontFamily = FontFamily.Serif,
//            fontSize = 18.sp,
//            fontStyle = FontStyle.Italic,
//            color = MaterialTheme.colorScheme.onBackground.copy(0.8f),
//        )
                    val textLines = hymn.text.lines()
                    Text(
                        text = buildAnnotatedString {
                            for (line in textLines) {
                                if (line in hymnTags) {
                                    withStyle(
                                        style = SpanStyle(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onBackground.copy(0.8f)
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
                        fontSize = 18.sp,
                        lineHeight = 27.sp,
                        style = TextStyle(
                            lineBreak = LineBreak.Simple
                        ),
                        modifier = Modifier.padding(12.dp, 12.dp, 12.dp, 64.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HymnScreenTopBar(
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
                    contentDescription = "Back"
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HymnScreenBottomBar(
    hymn: Hymn,
    hymnTotal: Int,
    modifier: Modifier,
    onPreviousHymnClick: () -> Unit,
    onNextHymnClick: () -> Unit,
    onSheetMusicClick: () -> Unit
) {
    HorizontalFloatingToolbar(
        modifier = modifier,
        leadingContent = {
            AppBarRow {
                clickableItem(
                    onClick = onPreviousHymnClick,
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowLeft,
                            contentDescription = "left arrow"
                        )
                    },
                    label = "Previous Hymn",
                    enabled = hymn.number != 1
                )
                clickableItem(
                    onClick = onNextHymnClick,
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowRight,
                            contentDescription = "right arrow"
                        )
                    },
                    label = "Next Hymn",
                    enabled = hymn.number != hymnTotal
                )
            }
        },
        expanded = true,
        content = {
            if (hymn.sheetMusic.isNotEmpty()) {
                TooltipBox(
                    positionProvider =
                        TooltipDefaults.rememberTooltipPositionProvider(
                            TooltipAnchorPosition.Above
                        ),
                    tooltip = { PlainTooltip { Text("Open Sheet Music") } },
                    state = rememberTooltipState(),
                ) {
                    FilledIconButton(
                        modifier = Modifier.width(64.dp),
                        onClick = onSheetMusicClick
                    ) {
                        Icon(Icons.Filled.MusicNote, contentDescription = "Music Note")
                    }
                }
            }
        }
    )
}

@SuppressLint("DiscouragedApi")
@Composable
fun SheetMusicScreen(
    onClickBack: () -> Unit,
    hymn: Hymn
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            HymnScreenTopBar(
                title = "Sheet Music",
                onClickBack = onClickBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .zoomable(rememberZoomableState())
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            for (resource in hymn.sheetMusic) {
                if (resource != 0) {
                    Image(
                        painter = painterResource(resource),
                        contentDescription = "Sheet music",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}