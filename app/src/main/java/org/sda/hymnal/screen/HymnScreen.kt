package org.sda.hymnal.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sda.hymnal.R
import org.sda.hymnal.data.Hymn
import org.sda.hymnal.data.Hymnals
import org.sda.hymnal.data.hymnTags


@Composable
fun HymnScreen(padding: PaddingValues, hymn: Hymn) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
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
                        withStyle(style = SpanStyle(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground.copy(0.8f)
                        )) {
                            append(line +"\n")
                        }
                    } else {
                        append(line + "\n")
                    }
                }
            },
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            lineHeight = 27.sp,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun SheetMusicScreen(padding: PaddingValues, hymn: Hymn) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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