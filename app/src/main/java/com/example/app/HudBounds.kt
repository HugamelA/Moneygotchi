package com.example.app

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.geometry.Rect

object HudBounds {
    val map = mutableStateMapOf<String, Rect>()

    val insets: Map<String, List<Int>> = mapOf(
        "Сытость"    to listOf(-6, -4, 80, -4),
        "Настроение" to listOf(-6, -4, 80, -4)
    )

    fun clear() {
        map.clear()
    }
}