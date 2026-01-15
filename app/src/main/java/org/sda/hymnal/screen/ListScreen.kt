package org.sda.hymnal.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sda.hymnal.data.Hymn


@Composable
fun ListScreen(padding: PaddingValues, hymns: List<Hymn>, onHymnClick: (hymn: Hymn) -> Unit) {
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
                            .padding(12.dp)
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
                                fontWeight = FontWeight.SemiBold
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
                                text = hymn.title
                            )
                            Text(
                                text = hymn.hymnal.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}