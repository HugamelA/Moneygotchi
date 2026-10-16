package com.example.app

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Rect

object PetBounds {
    val bounds = mutableStateOf<Rect?>(null)

    fun clear() {
        bounds.value = null
    }
}