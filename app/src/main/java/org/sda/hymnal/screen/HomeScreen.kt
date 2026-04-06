package org.sda.hymnal.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sda.hymnal.BottomHymnalBar
import org.sda.hymnal.data.Hymn
import org.sda.hymnal.data.Hymnal
import org.sda.hymnal.data.hymnalList

@Composable
fun HomeScreen(
    currentScreen: Screen,
    onNavClick: (screen: Screen) -> Unit,
    searchString: String,
    currentHymn: Hymn?,
    onSearchStringChange: (searchString: String) -> Unit,
    onHymnSubmit: () -> Unit,
    currentHymnal: Hymnal,
    onHymnalClick: (hymnal: Hymnal) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {},
        bottomBar = {
            BottomHymnalBar(
                currentScreen = currentScreen,
                onNavClick = { screen -> onNavClick(screen) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .imePadding()
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
//        val textFieldValue = remember { mutableStateOf<String>("") }
//        val textFieldNumber = rememberTextFieldState()
            Row(
                modifier = Modifier.weight(4f)
            ) { }
            Row(
                modifier = Modifier.padding(6.dp)
            ) {
                Text(
                    text = currentHymn?.title ?: "",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(
                modifier = Modifier
//                .weight(1f)
                    .requiredHeight(96.dp)
                    .padding(6.dp)
            ) {
                TextField(
                    modifier = Modifier.width(148.dp),
                    leadingIcon = {
                        Text(
                            text = "#",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            fontSize = 36.sp
                        )
                    },
                    value = searchString,
                    textStyle = TextStyle.Default.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    singleLine = true,
                    onValueChange = {
                        onSearchStringChange(it)
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { onHymnSubmit() }
                    ),
                )
            }
            Row(
                modifier = Modifier
                    .weight(4f)
                    .padding(6.dp)
            ) {
                Button(
                    onClick = onHymnSubmit
                ) {
                    Text("Open")
                }
            }
            Row(
                modifier = Modifier.weight(1f)
            ) {
                HymnalDropdown(
                    currentHymnal = currentHymnal,
                    onHymnalClick = onHymnalClick
                )
            }
        }
    }
}

@Composable
fun HymnalDropdown(currentHymnal: Hymnal, onHymnalClick: (hymnal: Hymnal) -> Unit) {
    val isDropdownOpen = remember{ mutableStateOf(false)}
    Box {
        Row(
            modifier = Modifier
                .clickable(
                    enabled = true,
                    onClick = { isDropdownOpen.value = !isDropdownOpen.value }
                )
                .padding(12.dp)
        ) {
            Text(currentHymnal.title)
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Dropdown arrow"
            )
        }

        DropdownMenu(
            expanded = isDropdownOpen.value,
            onDismissRequest = { isDropdownOpen.value = false }
        ) {
            for (hymnal in hymnalList) {
                DropdownMenuItem(
                    text = { Text(hymnal.title) },
                    onClick = {
                        onHymnalClick(hymnal)
                        isDropdownOpen.value = false
                    }
                )
            }
        }
    }
}