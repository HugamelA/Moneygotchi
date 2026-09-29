package com.example.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.draw.alpha

private const val DEBUG_DEV_BUTTON_ALWAYS_ON = false

// ЭКРАН НАСТРОЙКИ
@Composable
fun SettingsScreen(
    storage: AppStorage,
    onBack: () -> Unit,
    onPetDeleted: () -> Unit,
    onRepeatTutorial: () -> Unit,
    onOpenDev: () -> Unit,
    onOpenParent: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }
    var devTaps by remember { mutableIntStateOf(0) }
    var showParentGate by remember { mutableStateOf(false) }

    BackHandler { onBack() }

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
                onClick = onBack,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Назад", fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Настройки", fontSize = 28.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(32.dp))

        // Повторить обучение
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRepeatTutorial() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎓", fontSize = 22.sp)
                Spacer(Modifier.width(12.dp))
                Text(
                    "Пройти обучение снова",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Раздел для родителей
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showParentGate = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("👨‍👩‍👧", fontSize = 22.sp)
                Spacer(Modifier.width(12.dp))
                Text(
                    "Для родителей",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Удаление питомца
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showConfirm = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🗑", fontSize = 22.sp)
                Spacer(Modifier.width(12.dp))
                Text(
                    "Удаление питомца",
                    fontSize = 18.sp,
                    color = Color(0xFFB00020),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Кнопка разработчика
        if (DEBUG_DEV_BUTTON_ALWAYS_ON) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenDev() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🛠", fontSize = 22.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Режим разработчика",
                        fontSize = 18.sp,
                        color = Color(0xFFB00020),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            // Оригинальная скрытая зона
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .alpha(if (devTaps >= 2) 0.25f else 0f)
                    .background(Color.Gray, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        devTaps++
                        if (devTaps >= 4) {
                            devTaps = 0
                            onOpenDev()
                        }
                    }
            )
        }

        Spacer(Modifier.height(8.dp))
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Удалить питомца?") },
            text = {
                Text(
                    "Все данные будут стёрты, и вы начнёте с самого начала. " +
                            "Это действие нельзя отменить."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirm = false
                        storage.reset()
                        onPetDeleted()
                        storage.tutorialShown = false
                        storage.tutorialActive = true
                        storage.tutorialStep = 0
                    }
                ) {
                    Text("Удалить", color = Color(0xFFB00020))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showParentGate) {
        ParentGate(
            onPass = {
                showParentGate = false
                onOpenParent()
            },
            onDismiss = { showParentGate = false }
        )
    }
}