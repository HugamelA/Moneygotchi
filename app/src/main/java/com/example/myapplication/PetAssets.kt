package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

object PetAssets {

    fun colorFile(species: String, mood: String, stage: String, color: Int): String =
        "pets/$species/pet_${species}_${mood}_${stage}_$color.png"

    fun maskFile(species: String, mood: String, stage: String): String =
        "pets/$species/mask_${species}_${mood}_${stage}.png"

    // Путь для Coil. Если цветной нет — путь к маске. Если и её нет — null.
    fun petImageAssetPath(
        context: Context,
        species: String,
        mood: String,
        stage: String,
        color: Int
    ): String? {
        val colorPath = colorFile(species, mood, stage, color)
        if (assetExists(context, colorPath)) return "file:///android_asset/$colorPath"

        val maskPath = maskFile(species, mood, stage)
        if (assetExists(context, maskPath)) return "file:///android_asset/$maskPath"

        return null
    }

    // Маска в память для проверки кликов. inSampleSize=2 — вчетверо меньше памяти.
    fun loadMaskBitmap(
        context: Context,
        species: String,
        mood: String,
        stage: String
    ): Bitmap? = try {
        val options = BitmapFactory.Options().apply {
            inSampleSize = 2
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        context.assets.open(maskFile(species, mood, stage)).use { input ->
            BitmapFactory.decodeStream(input, null, options)
        }
    } catch (e: Exception) {
        null
    }

    private fun assetExists(context: Context, path: String): Boolean = try {
        context.assets.open(path).close()
        true
    } catch (e: Exception) {
        false
    }
}