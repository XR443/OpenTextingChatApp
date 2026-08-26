package org.github.raspopov

import android.app.job.JobScheduler
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.github.raspopov.services.platform.ClipboardManager
import org.github.raspopov.views.ObserverViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.compose.viewmodel.koinViewModel
import org.koin.plugin.module.dsl.startKoin
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class MainActivity : ComponentActivity() {


    var stopWatcherFunction: () -> Unit = {}
    override fun onCreate(savedInstanceState: Bundle?) {
//        WorkManager
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ClipboardManager.init(applicationContext)
        val koinApplication = org.koin.core.context.startKoin {
            // Log Koin into Android logger
            androidLogger()
            // Reference Android context
            androidContext(this@MainActivity)
            // Load modules
            modules(androidModule)
        }
        stopWatcherFunction = {
            val observerViewModel = koinApplication.koin.get<ObserverViewModel>()
            observerViewModel.stopWatcher()
        }
        setContent {
            App()
        }
    }

    override fun onStop() {
        super.onStop()
        stopWatcherFunction()
    }
}

@OptIn(ExperimentalUuidApi::class)
@Preview
@Composable
fun AppAndroidPreview() {
    App()
}