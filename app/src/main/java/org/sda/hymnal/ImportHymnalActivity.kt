package org.sda.hymnal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.sda.hymnal.data.HymnalViewModel
import org.sda.hymnal.data.hymnal.HymnalsImportConditions
import org.sda.hymnal.screen.HymnalEvent
import org.sda.hymnal.ui.theme.HymnalTheme

@Suppress("UNCHECKED_CAST")
class ImportHymnalActivity : ComponentActivity() {
    private val hymnalViewModel by viewModels<HymnalViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HymnalViewModel(applicationContext) as T
                }
            }
        }
    )

    private val openFileUri = mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        openFileUri.value = intent?.data

        enableEdgeToEdge()
        setContent {
            val currentUri by openFileUri
            val hymnalState = hymnalViewModel.hymnalState.collectAsState()
            val isLoadingHymns = hymnalState.value.isLoadingHymns

            LaunchedEffect(isLoadingHymns) {
                if (!isLoadingHymns) {
                    Log.d("hymnals", "currentUri: $currentUri")
                    if (currentUri != null) {
                        hymnalViewModel.onEvent(HymnalEvent.SetImportUri(currentUri))
                        hymnalViewModel.onEvent(HymnalEvent.AddHymnal(currentUri))
                    }
                } else {
                    Log.d("hymnals", "setting import state to IMPORTING")
                    hymnalViewModel.onEvent(HymnalEvent.SetImportState(HymnalsImportConditions.State.IMPORTING))
                }
            }

            HymnalTheme {
                MainApplication(hymnalViewModel, fromImport = true)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openFileUri.value = intent.data
    }
}