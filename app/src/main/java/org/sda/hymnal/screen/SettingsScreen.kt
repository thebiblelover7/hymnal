package org.sda.hymnal.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sda.hymnal.BottomHymnalBar
import org.sda.hymnal.R


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    snackbarHost: @Composable () -> Unit,
    currentScreen: Screen,
    fontSize: Float,
    onNavClick: (screen: Screen) -> Unit,
    onFontSizeSet: (fontSize: Float) -> Unit,
) {
    Scaffold(
        snackbarHost = snackbarHost,
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
            Row(
                modifier = Modifier.padding(36.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_monochrome),
                        contentDescription = stringResource(R.string.icon_logo),
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.width(128.dp)
                    )
                    Text(
                        text = stringResource(R.string.app_title),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                    )
                    Text(
                        text = stringResource(R.string.app_description),
                        fontSize = 18.sp,
                        lineHeight = 27.sp,
//                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Row(
                modifier = Modifier
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //            Column(
                //                modifier = Modifier.padding(36.dp)
                //            ) {
                Icon(
                    imageVector = Icons.Filled.Build,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = stringResource(R.string.app_source_code),
                    modifier = Modifier.padding(12.dp)
                )
                Text(
                    text = buildAnnotatedString {
                        withLink(
                            LinkAnnotation.Url(
                                "https://github.com/thebiblelover7/hymnal"
                            )
                        ) {
                            append(stringResource(R.string.app_source_code))
                        }
                    },
                    color = MaterialTheme.colorScheme.primary
                )
                //            }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_font_size),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Slider(
                    value = fontSize,
                    onValueChange = onFontSizeSet,
                    steps = 4,
                    valueRange = 0.5f..1.75f,
                    modifier = Modifier
                        .size(128.dp)
                        .weight(2f)
                )
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.settings_example_font),
                    fontSize = (18 * fontSize).sp,
                    lineHeight = (27 * fontSize).sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
                )
            }
        }
    }
}