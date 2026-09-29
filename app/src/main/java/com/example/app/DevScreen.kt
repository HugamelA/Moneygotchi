package com.example.myapplication

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.LaunchedEffect

// РЕЖИМ РАЗРАБОТЧИКА
@Composable
fun DevScreen(
    storage: AppStorage,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var appearance by remember { mutableStateOf(storage.petAppearance) }
    var color by remember { mutableStateOf(storage.petColor) }
    var coinsText by remember { mutableStateOf(storage.coins.toString()) }
    var hunger by remember { mutableStateOf(storage.hunger) }
    var happiness by remember { mutableStateOf(storage.happiness) }
    var level by remember { mutableStateOf(storage.petLevel) }
    var days by remember { mutableStateOf(storage.daysWithPet) }
    var inventory by remember { mutableStateOf(storage.getInventory()) }
    var inventoryTick by remember { mutableStateOf(0) }
    var stars by remember { mutableStateOf(storage.stars) }
    var stage by remember { mutableStateOf(storage.petStage()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
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

        Text("🛠 Режим разработчика", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Вид питомца
            DevSection(title = "Вид питомца") {
                petOptions.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { pet ->
                            val idx = petOptions.indexOf(pet)
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .background(
                                        if (appearance == idx) Color(0xFFBBDEFB)
                                        else Color(0xFFEEEEEE),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        appearance = idx
                                        storage.petAppearance = idx
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = pet.images[0]),
                                    contentDescription = pet.name,
                                    modifier = Modifier.size(50.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Раскраска
            DevSection(title = "Раскраска (1–4)") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(colorNames.size) { idx ->
                        Button(
                            onClick = {
                                color = idx
                                storage.petColor = idx
                            },
                            colors = if (color == idx) ButtonDefaults.buttonColors()
                            else ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("${idx + 1}", fontSize = 16.sp)
                        }
                    }
                }
            }

            // Дни
            DevSection(title = "Дней с питомцем: $days") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            days += 1
                            storage.daysWithPet = days
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("+1 день") }

                    Button(
                        onClick = {
                            days += 7
                            storage.daysWithPet = days
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("+7 дней") }

                    OutlinedButton(
                        onClick = {
                            days = 1
                            storage.daysWithPet = 1
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Сброс") }
                }
            }

            // Монеты
            DevSection(title = "Монеты") {
                OutlinedTextField(
                    value = coinsText,
                    onValueChange = { new ->
                        if (new.all { it.isDigit() } && new.length <= 9) {
                            coinsText = new
                            storage.coins = new.toIntOrNull() ?: 0
                        }
                    },
                    label = { Text("Баланс") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            storage.coins += 100
                            coinsText = storage.coins.toString()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("+100") }

                    Button(
                        onClick = {
                            storage.coins += 1000
                            coinsText = storage.coins.toString()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("+1000") }

                    OutlinedButton(
                        onClick = {
                            storage.coins = 0
                            coinsText = "0"
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Сброс") }
                }
            }

            // Сытость
            DevSection(title = "Сытость: $hunger") {
                Slider(
                    value = hunger.toFloat(),
                    onValueChange = {
                        hunger = it.toInt()
                        storage.hunger = hunger
                    },
                    valueRange = 0f..100f
                )
            }

            // Настроение
            DevSection(title = "Настроение: $happiness") {
                Slider(
                    value = happiness.toFloat(),
                    onValueChange = {
                        happiness = it.toInt()
                        storage.happiness = happiness
                    },
                    valueRange = 0f..100f
                )
            }

            // Уровень
            DevSection(title = "Уровень: $level") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (level > 1) {
                                level -= 1
                                storage.petLevel = level
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("−1") }

                    Text("$level", fontSize = 22.sp, fontWeight = FontWeight.Bold)

                    Button(
                        onClick = {
                            level += 1
                            storage.petLevel = level
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("+1") }
                }
            }

            // Звёзды
            DevSection(
                title = "Звёзды: $stars / ${storage.starsToNextLevel()}   " +
                        "(${stage.displayName})"
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            storage.addStars(1)
                            stars = storage.stars
                            level = storage.petLevel
                            stage = storage.petStage()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("+1 ⭐") }

                    Button(
                        onClick = {
                            storage.addStars(5)
                            stars = storage.stars
                            level = storage.petLevel
                            stage = storage.petStage()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("+5 ⭐") }

                    Button(
                        onClick = {
                            storage.addStars(10)
                            stars = storage.stars
                            level = storage.petLevel
                            stage = storage.petStage()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("+10 ⭐") }
                }

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            storage.stars = 0
                            stars = 0
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Сбросить звёзды") }

                    OutlinedButton(
                        onClick = {
                            // Сброс звёзд + уровня к нулю
                            storage.stars = 0
                            storage.petLevel = 1
                            stars = 0
                            level = 1
                            stage = storage.petStage()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Сбросить всё") }
                }
            }

            // Инвентарь
            DevSection(title = "Инвентарь") {
                val context = androidx.compose.ui.platform.LocalContext.current
                LaunchedEffect(Unit) { ShopCatalog.load(context) }

                val totalItems = inventory.values.sum()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Всего позиций: ${inventory.size}, всего предметов: $totalItems",
                        fontSize = 13.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = {
                            storage.clearInventory()
                            inventory = storage.getInventory()
                            inventoryTick++
                        }
                    ) {
                        Text("Очистить")
                    }
                }

                Spacer(Modifier.height(8.dp))

                val currentPetType = currentPetTypeId(storage.petAppearance)
                val visibleProducts = ShopCatalog.products
                    .filter { it.fitsPet(currentPetType) }

                visibleProducts.forEach { product ->
                    val qty = inventory[product.id] ?: 0
                    val name = product.name
                    val emoji = product.emoji

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Text(emoji, fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            name,
                            fontSize = 13.sp,
                            color = if (qty > 0) Color(0xFF333333)
                            else Color(0xFFAAAAAA),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "$qty",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (qty > 0) Color(0xFF333333)
                            else Color(0xFFAAAAAA),
                            modifier = Modifier.width(36.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        OutlinedButton(
                            onClick = {
                                if (qty > 1) {
                                    storage.setInventoryQuantity(product.id, qty - 1)
                                } else {
                                    storage.setInventoryQuantity(product.id, 0)
                                }
                                inventory = storage.getInventory()
                                inventoryTick++
                            },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("−", fontSize = 16.sp)
                        }
                        Spacer(Modifier.width(4.dp))
                        Button(
                            onClick = {
                                storage.setInventoryQuantity(product.id, qty + 1)
                                inventory = storage.getInventory()
                                inventoryTick++
                            },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("+", fontSize = 16.sp)
                        }
                    }
                }
            }

            // Задания
            DevSection(title = "Задания") {
                var tasksReset by remember { mutableStateOf(false) }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (tasksReset) "Задания сброшены"
                        else "Заданий выполнено: ${storage.getCompletedTasksCount()}",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedButton(
                        onClick = {
                            storage.resetCompletedTasks()
                            tasksReset = true
                        }
                    ) {
                        Text("Сбросить задания")
                    }
                }
            }
        }
    }
}

// СЕКЦИЯ РЕЖИМА РАЗРАБОТЧИКА
@Composable
fun DevSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
        Spacer(Modifier.height(8.dp))
        content()
    }
}