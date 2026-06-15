package com.ivan.freeglukmp

import androidx.compose.ui.window.ComposeUIViewController
import com.ivan.freeglukmp.di.initKoin

fun MainViewController() = ComposeUIViewController {
    try {
        initKoin()
    } catch (e: Exception) {
        // Already started, ignore
    }
    App()
}