package com.likebamboo.vicky

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import com.likebamboo.vicky.main.MainScreen
import com.likebamboo.vicky.main.MainViewModel

@Composable
fun App() {
    MaterialTheme {
        val viewModel = MainViewModel()
        MainScreen(viewModel)
    }
}