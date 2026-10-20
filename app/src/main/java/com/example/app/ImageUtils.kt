package com.example.app

import android.graphics.Bitmap

fun cropToContent(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    var left = width
    var top = height
    var right = 0
    var bottom = 0

    val step = 4 // Проверяем каждый 4-й пиксель для скорости
    for (y in 0 until height step step) {
        val rowOffset = y * width
        for (x in 0 until width step step) {
            val alpha = (pixels[rowOffset + x] ushr 24) and 0xFF
            if (alpha > 0) {
                if (x < left) left = x
                if (x > right) right = x
                if (y < top) top = y
                if (y > bottom) bottom = y
            }
        }
    }

    if (right < left || bottom < top) return bitmap

    return Bitmap.createBitmap(bitmap, left, top, right - left + 1, bottom - top + 1)
}