package com.likebamboo.vicky

import androidx.compose.ui.window.application
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val windowState = rememberWindowState(width = 600.dp, height = 500.dp)
    Window(
        onCloseRequest = ::exitApplication,
        resizable = true,
        title = "工作助手",
        state = windowState,
    ) {
        MainView()
    }
}