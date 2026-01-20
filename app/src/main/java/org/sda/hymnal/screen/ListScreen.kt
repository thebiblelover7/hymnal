package org.sda.hymnal.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sda.hymnal.data.Hymn
import org.sda.hymnal.data.hymnTags


@Composable
fun ListScreen(padding: PaddingValues, hymns: List<Hymn>, onHymnClick: (hymn: Hymn) -> Unit) {
    val regex = Regex("(${hymnTags.joinToString(separator = "|")})\n")
    val regex2 = Regex("/1/")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ){
        LazyColumn() {
            for (hymn in hymns) {
                item {
                    Row(
                        modifier = Modifier
                            .clickable(
                                enabled = true,
                                onClick = { onHymnClick(hymn) }
                            )
                            .padding(2.dp)
                            .height(64.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(10.dp)
                                .weight(2f),
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
                                .padding(10.dp)
                                .weight(10f),
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
                            val shortTextLine = hymn.text.take(50).replace(regex = regex, replacement = "")
                            Text(
                                text = shortTextLine,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}