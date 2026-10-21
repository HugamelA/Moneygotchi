package com.example.app

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

object SoundManager {
    private var bgmPlayer: MediaPlayer? = null
    private var currentBgmResId: Int = 0

    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<Int, Int>()
    private var isInitialized = false

    var musicVolume: Float = 0.4f
        set(value) {
            field = value
            if (value <= 0.01f) {
                pauseBgm()
            } else {
                bgmPlayer?.setVolume(value, value)
                resumeBgm()
            }
        }

    var sfxVolume: Float = 0.7f

    fun init(context: Context) {
        if (isInitialized) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        val sfxList = listOf(
            R.raw.sfx_ui_tap,
            R.raw.sfx_ui_shop_click,
            R.raw.sfx_ui_coin,
            R.raw.sfx_ui_success,
            R.raw.sfx_ui_error,
            R.raw.sfx_ui_book_leafing,
            R.raw.sfx_ui_pen,
            R.raw.sfx_pet_cat_happy,
            R.raw.sfx_pet_cat_neutral,
            R.raw.sfx_pet_cat_sad,
            R.raw.sfx_pet_dog_happy,
            R.raw.sfx_pet_dog_neutral,
            R.raw.sfx_pet_dog_sad,
            R.raw.sfx_pet_dragon_happy,
            R.raw.sfx_pet_dragon_neutral,
            R.raw.sfx_pet_dragon_sad
        )

        sfxList.forEach { resId ->
            soundMap[resId] = soundPool?.load(context, resId, 1) ?: 0
        }

        isInitialized = true
    }

    // Управление фоновой музыкой
    fun playBgm(context: Context, resId: Int) {
        if (musicVolume <= 0.01f) return
        if (currentBgmResId == resId && bgmPlayer?.isPlaying == true) return

        stopBgm()
        currentBgmResId = resId

        bgmPlayer = MediaPlayer.create(context, resId)?.apply {
            isLooping = true
            setVolume(musicVolume, musicVolume)
            start()
        }
    }

    fun pauseBgm() {
        if (bgmPlayer?.isPlaying == true) bgmPlayer?.pause()
    }

    fun resumeBgm() {
        if (musicVolume > 0.01f && bgmPlayer != null && bgmPlayer?.isPlaying == false) {
            bgmPlayer?.start()
        }
    }

    fun stopBgm() {
        bgmPlayer?.stop()
        bgmPlayer?.release()
        bgmPlayer = null
    }

    // Проигрывание коротких звуков (SFX)
    fun playSfx(resId: Int) {
        if (sfxVolume <= 0.01f) return
        val soundId = soundMap[resId] ?: return
        soundPool?.play(soundId, sfxVolume, sfxVolume, 1, 0, 1f)
    }

    fun getPetSoundResId(petAppearance: Int, happiness: Int): Int {
        val mood = moodIndex(happiness)

        return when (petAppearance) {
            0 -> when (mood) {
                0 -> R.raw.sfx_pet_dragon_sad
                1 -> R.raw.sfx_pet_dragon_neutral
                else -> R.raw.sfx_pet_dragon_happy
            }
            1 -> when (mood) {
                0 -> R.raw.sfx_pet_dog_sad
                1 -> R.raw.sfx_pet_dog_neutral
                else -> R.raw.sfx_pet_dog_happy
            }
            2 -> when (mood) {
                0 -> R.raw.sfx_pet_cat_sad
                1 -> R.raw.sfx_pet_cat_neutral
                else -> R.raw.sfx_pet_cat_happy
            }
            else -> R.raw.sfx_ui_tap
        }
    }
}