package com.example.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

@Composable
fun PiggyScreen(
    storage: AppStorage,
    onBack: () -> Unit,
    onRestart: () -> Unit
) {
    val context = LocalContext.current
    remember { ShopCatalog.load(context); 0 }

    var piggyBalance by remember { mutableStateOf(storage.piggyBalance) }
    var goalName by remember { mutableStateOf(storage.goalName) }
    var goalPrice by remember { mutableStateOf(storage.goalPrice) }
    var goalPurchased by remember { mutableStateOf(storage.goalPurchased) }

    var showPurchaseConfirm by remember { mutableStateOf(false) }
    var showPurchaseSuccess by remember { mutableStateOf(false) }
    var purchasedMessage by remember { mutableStateOf("") }
    var showAllGoalsDone by remember { mutableStateOf(false) }
    var showRestartConfirm by remember { mutableStateOf(false) }

    val hasGoal = goalName.isNotBlank() && goalPrice > 0
    val reached = hasGoal && piggyBalance >= goalPrice

    val goalProduct = ShopCatalog.products.firstOrNull { it.name == goalName }
    val goalEmoji = goalProduct?.emoji ?: "🎯"
    val goalIcon = goalProduct?.iconRes

    LaunchedEffect(goalName, piggyBalance) {
        if (!storage.allDoneShown &&
            storage.allSpecialProductsBought() &&
            storage.goalName.isBlank()
        ) {
            showAllGoalsDone = true
            storage.allDoneShown = true
        }
    }

    BackHandler { onBack() }

    Box(modifier = Modifier.fillMaxSize()) {

        // ЗАДНИК
        Image(
            painter = painterResource(id = R.drawable.piggy_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // Кнопка «Назад»
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(80.dp))

            // Плашка с балансом
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFF5D4037).copy(alpha = 0.92f),
                shadowElevation = 6.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.coin),
                            contentDescription = "Монеты",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Unspecified
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        formatCoins(piggyBalance),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Блок цели + кнопка внизу
            if (hasGoal && !goalPurchased) {

                // Карточка цели
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (goalIcon != null) {
                                Image(
                                    painter = painterResource(id = goalIcon),
                                    contentDescription = goalName,
                                    modifier = Modifier.size(72.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text(goalEmoji, fontSize = 52.sp)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Цель:",
                                    fontSize = 12.sp,
                                    color = Color(0xFF888888)
                                )
                                Text(
                                    goalName,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            "$piggyBalance из $goalPrice",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (reached) Color(0xFF4CAF50)
                            else Color(0xFF333333)
                        )

                        Spacer(Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = {
                                (piggyBalance.toFloat() / goalPrice)
                                    .coerceIn(0f, 1f)
                            },
                            modifier = Modifier.fillMaxWidth().height(12.dp),
                            color = if (reached) Color(0xFF4CAF50)
                            else Color(0xFFFF9800)
                        )
                    }
                }

                // Кнопка «Купить»
                if (reached) {
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF4CAF50),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showPurchaseConfirm = true }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            Text(
                                "🎉 Купить за $goalPrice",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Диалог подтверждения покупки
    if (showPurchaseConfirm) {
        AlertDialog(
            onDismissRequest = { showPurchaseConfirm = false },
            title = { Text("Купить цель?") },
            text = {
                Column {
                    Text(
                        "Купить товар «$goalName» за $goalPrice ${coinsWord(goalPrice)}?",
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Монеты спишутся из копилки, а товар окажется у тебя.",
                        fontSize = 13.sp,
                        color = Color(0xFF666666)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showPurchaseConfirm = false

                    val productId = goalProduct?.id
                    if (productId != null) {
                        val boughtName = goalName

                        storage.piggyBalance =
                            (storage.piggyBalance - goalPrice).coerceAtLeast(0)
                        storage.addToInventory(productId, 1)

                        val daysSpent = if (storage.goalStartDay > 0)
                            storage.daysWithPet - storage.goalStartDay
                        else 0
                        storage.recordCompletedGoal(boughtName, daysSpent)
                        storage.goalsCompleted += 1
                        storage.clearGoal()

                        // Включить перк лежанки при покупке
                        if (productId == "special_bed") {
                            storage.bedOwned = true
                        }

                        piggyBalance = storage.piggyBalance
                        goalName = ""
                        goalPrice = 0
                        goalPurchased = false

                        purchasedMessage = "Ты купил «$boughtName»! Молодец!"
                        showPurchaseSuccess = true
                    }
                }) {
                    Text("Купить", color = Color(0xFF4CAF50))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurchaseConfirm = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showPurchaseSuccess) {
        AlertDialog(
            onDismissRequest = { showPurchaseSuccess = false },
            title = { Text("Покупка совершена!") },
            text = { Text(purchasedMessage) },
            confirmButton = {
                TextButton(onClick = { showPurchaseSuccess = false }) {
                    Text("Отлично")
                }
            }
        )
    }

    // Все особые товары куплены
    if (showAllGoalsDone) {
        AlertDialog(
            onDismissRequest = {  },
            title = { Text("🎉 Все цели достигнуты!") },
            text = {
                Column {
                    Text(
                        "Все особые товары были куплены — копить пока не на что.",
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Можешь начать всё заново, либо продолжить заботу о питомце " +
                                "и откладывать монеты.",
                        fontSize = 14.sp,
                        color = Color(0xFF555555),
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Даже если сейчас ты не готов начинать заново, " +
                                "ты всегда сможешь это сделать в настройках.",
                        fontSize = 13.sp,
                        color = Color(0xFF888888),
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAllGoalsDone = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Остаться",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAllGoalsDone = false
                        showRestartConfirm = true
                    }
                ) {
                    Text(
                        "Начать заново",
                        fontSize = 15.sp,
                        color = Color(0xFF888888)
                    )
                }
            }
        )
    }

    if (showRestartConfirm) {
        AlertDialog(
            onDismissRequest = { showRestartConfirm = false },
            title = { Text("Начать заново?") },
            text = {
                Text(
                    "Ты точно хотите начать заново? Весь прогресс — питомец, " +
                            "монеты, задания и цели — будет стёрт. Это действие " +
                            "нельзя отменить.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestartConfirm = false
                        onRestart()
                    }
                ) {
                    Text("Да, начать", color = Color(0xFFB00020))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartConfirm = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}