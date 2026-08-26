package org.github.raspopov

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.github.raspopov.views.ObserverViewModel
import org.koin.core.context.startKoin
import kotlin.uuid.ExperimentalUuidApi

@ExperimentalUuidApi
fun main() = application {
    val koinApplication = startKoin {
        modules(desktopModule)
    }

    fun exit(){
        val observerViewModel = koinApplication.koin.get<ObserverViewModel>()
        observerViewModel.stopWatcher()
        exitApplication()
    }
    Window(
//        onCloseRequest = ::exitApplication,
        onCloseRequest = ::exit,
        title = "OpenTextingChatApp",
    ) {
        App()
    }
}
