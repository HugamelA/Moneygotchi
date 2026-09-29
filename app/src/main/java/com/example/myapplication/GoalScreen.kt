package com.example.myapplication

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

// ЭКРАН ЦЕЛИ
@Composable
fun GoalScreen(
    storage: AppStorage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    remember { ShopCatalog.load(context); 0 }

    var goalName by remember { mutableStateOf(storage.goalName) }
    var goalPrice by remember { mutableStateOf(storage.goalPrice) }
    var piggyBalance by remember { mutableStateOf(storage.piggyBalance) }
    var showCancelConfirm by remember { mutableStateOf(false) }

    val hasGoal = goalName.isNotBlank() && goalPrice > 0
    val reached = hasGoal && piggyBalance >= goalPrice

    val goalProduct = ShopCatalog.products.firstOrNull { it.name == goalName }
    val goalIcon = goalProduct?.iconRes
    val goalEmoji = goalProduct?.emoji ?: "🎯"

    BackHandler { onBack() }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE8F5E9))) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Верхняя панель
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onBack() }
                ) {
                    Text(
                        "Назад",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                Text(
                    "Моя цель",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (!hasGoal) {
                // Нет цели
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎯", fontSize = 72.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "У тебя пока нет цели",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Зайди в магазин и выбери товар,\nна который хочешь копить.",
                            fontSize = 14.sp,
                            color = Color(0xFF888888),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Есть цель
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(16.dp))

                    if (goalIcon != null) {
                        Image(
                            painter = painterResource(id = goalIcon),
                            contentDescription = goalName,
                            modifier = Modifier.size(120.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(goalEmoji, fontSize = 96.sp)
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        goalName,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        "$goalPrice ${coinsWord(goalPrice)}",
                        fontSize = 16.sp,
                        color = Color(0xFF888888)
                    )

                    Spacer(Modifier.height(32.dp))

                    // Прогресс
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Накоплено",
                                fontSize = 14.sp,
                                color = Color(0xFF888888)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "$piggyBalance из $goalPrice",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (reached) Color(0xFF4CAF50)
                                else Color(0xFF333333)
                            )
                            Spacer(Modifier.height(14.dp))

                            LinearProgressIndicator(
                                progress = {
                                    (piggyBalance.toFloat() / goalPrice)
                                        .coerceIn(0f, 1f)
                                },
                                modifier = Modifier.fillMaxWidth().height(14.dp),
                                color = if (reached) Color(0xFF4CAF50)
                                else Color(0xFFFF9800)
                            )

                            Spacer(Modifier.height(10.dp))

                            if (reached) {
                                Text(
                                    "🎉 Цель достигнута!",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Нажми на банку с накоплениями, чтобы взять монеты и купить.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF888888),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            } else {
                                Text(
                                    "Осталось накопить: ${goalPrice - piggyBalance} ${coinsWord(goalPrice - piggyBalance)}",
                                    fontSize = 13.sp,
                                    color = Color(0xFF888888)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Кнопка отмены цели
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp, Color(0xFFE53935)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showCancelConfirm = true }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 14.dp)
                        ) {
                            Text(
                                "Отменить цель",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE53935)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    // Подтверждение отмены
    if (showCancelConfirm) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            title = { Text("Отменить цель?") },
            text = {
                Text(
                    "Прогресс сохранится в копилке, но цель исчезнет. " +
                            "Ты сможешь поставить новую."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showCancelConfirm = false
                    storage.clearGoal()
                    goalName = ""
                    goalPrice = 0
                }) {
                    Text("Отменить", color = Color(0xFFE53935))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirm = false }) {
                    Text("Оставить")
                }
            }
        )
    }
}