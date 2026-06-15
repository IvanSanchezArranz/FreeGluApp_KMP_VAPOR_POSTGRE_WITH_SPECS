package com.ivan.freeglukmp

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.ivan.freeglukmp.di.initKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    try {
        initKoin()
    } catch (e: Exception) {
        // Already started, ignore
    }
    ComposeViewport {
        App()
    }
}