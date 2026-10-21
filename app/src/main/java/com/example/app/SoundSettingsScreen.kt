package com.example.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SoundSettingsScreen(
    storage: AppStorage,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var musicVolume by remember { mutableStateOf(storage.musicVolume) }
    var sfxVolume by remember { mutableStateOf(storage.sfxVolume) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    SoundManager.playSfx(R.raw.sfx_ui_tap)
                    onBack()
                },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Назад", fontSize = 16.sp, color = Color(0xFF333333))
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Звук", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))

        // Ползунок музыки
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Музыка", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Text("${(musicVolume * 100).toInt()}%", fontSize = 18.sp)
                }
                Slider(
                    value = musicVolume,
                    onValueChange = { newValue ->
                        musicVolume = newValue
                        storage.musicVolume = newValue
                        SoundManager.musicVolume = newValue
                    },
                    valueRange = 0f..1f
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Ползунок звуков
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Звуки", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Text("${(sfxVolume * 100).toInt()}%", fontSize = 18.sp)
                }
                Slider(
                    value = sfxVolume,
                    onValueChange = { newValue ->
                        sfxVolume = newValue
                        storage.sfxVolume = newValue
                        SoundManager.sfxVolume = newValue
                    },
                    onValueChangeFinished = {
                        SoundManager.playSfx(R.raw.sfx_ui_tap)
                    },
                    valueRange = 0f..1f
                )
            }
        }
    }
}