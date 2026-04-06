package org.sda.hymnal.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import org.sda.hymnal.BottomHymnalBar
import org.sda.hymnal.R
import org.sda.hymnal.data.Hymn


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    currentScreen: Screen,
    onNavClick: (screen: Screen) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomHymnalBar(
                currentScreen = currentScreen,
                onNavClick = onNavClick,
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_monochrome),
                        contentDescription = "logo",
                        modifier = Modifier.padding(24.dp)
                    )
                    Text(
                        text = "Hymnal",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                    )
                    Text(
                        text = "A Seventh-Day Adventist hymnal Android app in various languages.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Row(
                modifier = Modifier
                    .height(100.dp)
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //            Column(
                //                modifier = Modifier.padding(36.dp)
                //            ) {
                Icon(
                    imageVector = Icons.Filled.Build,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Source Code",
                    modifier = Modifier.padding(12.dp)
                )
                Text(
                    text = buildAnnotatedString {
                        withLink(
                            LinkAnnotation.Url(
                                "https://github.com/thebiblelover7/hymnal"
                            )
                        ) {
                            append("Source Code")
                        }
                    },
                    color = MaterialTheme.colorScheme.primary
                )
                //            }
            }
        }
    }
}


@SuppressLint("DiscouragedApi")
@Composable
fun SupportScreen(
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