package com.example.myapplication

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ParentScreen(
    storage: AppStorage,
    onBack: () -> Unit,
    onOpenDev: () -> Unit
) {
    BackHandler { onBack() }

    var showBonusDialog by remember { mutableStateOf(false) }
    var snapshot by remember { mutableStateOf(0) }

    val overall = remember(snapshot) { ParentData.overallProgress(storage) }
    val petMetrics = remember(snapshot) { ParentData.petMetrics(storage) }
    val goalMetrics = remember(snapshot) { ParentData.goalProgress(storage) }
    val taskMetrics = remember(snapshot) { ParentData.taskMetrics(storage) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Шапка
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
            Spacer(Modifier.height(8.dp))
            Text(
                "Раздел для родителей",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Прогресс ребёнка без оценок и сравнений",
                fontSize = 13.sp,
                color = Color(0xFF888888)
            )

            Spacer(Modifier.height(20.dp))

            // Общий прогресс
            SectionTitle("Общий прогресс")
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp)) {
                    overall.forEach { MetricRow(it) }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Здоровье питомца
            SectionTitle("Здоровье питомца")
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp)) {
                    petMetrics.forEach { MetricRow(it) }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Цели
            SectionTitle("Цели")
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp)) {
                    goalMetrics.forEach { MetricRow(it) }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Задания
            SectionTitle("Задания")
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp)) {
                    taskMetrics.forEach { MetricRow(it) }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Наградить
            SectionTitle("Наградить ребёнка")
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showBonusDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎁", fontSize = 22.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Добавить монеты",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Сейчас в кошельке: ${storage.coins}",
                            fontSize = 12.sp,
                            color = Color(0xFF888888)
                        )
                    }
                    Text("→", fontSize = 20.sp, color = Color(0xFF888888))
                }
            }

            Spacer(Modifier.height(20.dp))

            // Скрытый dev-вход
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable { onOpenDev() }
            )

            Spacer(Modifier.height(20.dp))
        }
    }

    if (showBonusDialog) {
        BonusDialog(
            storage = storage,
            onDismiss = {
                showBonusDialog = false
                snapshot++
            }
        )
    }
}

// ВСПОМОГАТЕЛЬНЫЕ

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF333333),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun MetricRow(m: ParentMetric) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(m.label, fontSize = 14.sp, color = Color(0xFF666666))
            if (m.hint.isNotBlank()) {
                Text(m.hint, fontSize = 11.sp, color = Color(0xFFAAAAAA))
            }
        }
        Text(m.value, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BonusDialog(
    storage: AppStorage,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    val amount = amountText.toIntOrNull() ?: 0
    val valid = amount in 1..999

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Наградить ребёнка") },
        text = {
            Column {
                Text("Сколько монет добавить?", fontSize = 14.sp)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { ch -> ch.isDigit() }.take(3)
                    },
                    singleLine = true,
                    placeholder = { Text("Например, 20") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Монеты придут в общий кошелёк.",
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    storage.addParentBonus(amount)
                    onDismiss()
                }
            ) {
                Text("Наградить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}