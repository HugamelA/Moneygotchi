package com.example.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Row

@Composable
fun CoinsScreen(
    storage: AppStorage,
    onBack: () -> Unit
) {
    var coins by remember { mutableStateOf(storage.coins) }
    var needs by remember { mutableStateOf(storage.coinsNeeds) }
    var wants by remember { mutableStateOf(storage.coinsWants) }
    var piggy by remember { mutableStateOf(storage.piggyBalance) }

    var draggedValue by remember { mutableStateOf<Int?>(null) }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }
    var dragSize by remember { mutableStateOf(56.dp) }

    var needsBounds by remember { mutableStateOf<Rect?>(null) }
    var wantsBounds by remember { mutableStateOf<Rect?>(null) }
    var piggyBounds by remember { mutableStateOf<Rect?>(null) }

    val coinCenters = remember { mutableStateMapOf<Int, Offset>() }
    val coinSizes = remember { mutableStateMapOf<Int, Dp>() }

    BackHandler { onBack() }

    val dragSizePx = with(LocalDensity.current) { dragSize.toPx() }

    Box(modifier = Modifier.fillMaxSize()) {

        // ЗАДНИК
        Image(
            painter = painterResource(id = R.drawable.coins_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // ОТЛАДКА
        if (DEBUG_COINS_LAYOUT) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val w = maxWidth
                val h = maxHeight
                DebugZone(COINS_NEEDS,   w, h, onClick = {})
                DebugZone(COINS_WANTS,   w, h, onClick = {})
                DebugZone(COINS_PIGGY,   w, h, onClick = {})
                DebugZone(COINS_BALANCE, w, h, onClick = {})
                DebugZone(COIN_5,        w, h, onClick = {})
                DebugZone(COIN_10,       w, h, onClick = {})
                DebugZone(COIN_20,       w, h, onClick = {})
                DebugZone(COIN_50,       w, h, onClick = {})
                DebugZone(COIN_100,      w, h, onClick = {})
            }
            return
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val w = maxWidth.value
            val h = maxHeight.value

            // Кнопка Назад
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onBack() },
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Text(
                    "Назад",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Инструкция сверху
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 62.dp, start = 24.dp, end = 24.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.85f),
                shadowElevation = 2.dp
            ) {
                Text(
                    "Перетаскивай монетки в конверты и копилку",
                    fontSize = 13.sp,
                    color = Color(0xFF555555),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            // Конверт «Обязательное»
            Box(
                modifier = Modifier
                    .offset(
                        x = (COINS_NEEDS.x * w).dp,
                        y = (COINS_NEEDS.y * h).dp
                    )
                    .size(
                        width = (COINS_NEEDS.w * w).dp,
                        height = (COINS_NEEDS.h * h).dp
                    )
                    .onGloballyPositioned { needsBounds = it.boundsInRoot() },
                contentAlignment = Alignment.TopCenter
            ) {
                Box(modifier = Modifier.padding(top = 8.dp)) {
                    EnvelopeText(
                        amount = needs,
                        description = "Монет на обязательные покупки"
                    )
                }

            }

            // Конверт «Необязательное»
            Box(
                modifier = Modifier
                    .offset(
                        x = (COINS_WANTS.x * w).dp,
                        y = (COINS_WANTS.y * h).dp
                    )
                    .size(
                        width = (COINS_WANTS.w * w).dp,
                        height = (COINS_WANTS.h * h).dp
                    )
                    .onGloballyPositioned { wantsBounds = it.boundsInRoot() },
                contentAlignment = Alignment.TopCenter
            ) {
                Box(modifier = Modifier.padding(top = 8.dp)) {
                    EnvelopeText(
                        amount = wants,
                        description = "Монет на необязательные покупки"
                    )
                }
            }

            // Копилка
            Box(
                modifier = Modifier
                    .offset(
                        x = (COINS_PIGGY.x * w).dp,
                        y = (COINS_PIGGY.y * h).dp
                    )
                    .size(
                        width = (COINS_PIGGY.w * w).dp,
                        height = (COINS_PIGGY.h * h).dp
                    )
                    .onGloballyPositioned { piggyBounds = it.boundsInRoot() }
            )

            // Плашка баланса
            Box(
                modifier = Modifier
                    .offset(
                        x = (COINS_BALANCE.x * w).dp,
                        y = (COINS_BALANCE.y * h).dp
                    )
                    .size(
                        width = (COINS_BALANCE.w * w).dp,
                        height = (COINS_BALANCE.h * h).dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF5D4037).copy(alpha = 0.9f),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            "Баланс:\n$coins ${coinsWord(coins)}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Надпись копилки
            Box(
                modifier = Modifier
                    .offset(
                        x = (PIGGY_LABEL.x * w).dp,
                        y = (PIGGY_LABEL.y * h).dp
                    )
                    .size(
                        width = (PIGGY_LABEL.w * w).dp,
                        height = (PIGGY_LABEL.h * h).dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF8D6E63).copy(alpha = 0.9f),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "В копилке: $piggy ${coinsWord(piggy)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Монетки
            val coinZones = listOf(
                Triple(COIN_5,   5,   R.drawable.coin_5),
                Triple(COIN_10,  10,  R.drawable.coin_10),
                Triple(COIN_20,  20,  R.drawable.coin_20),
                Triple(COIN_50,  50,  R.drawable.coin_50),
                Triple(COIN_100, 100, R.drawable.coin_100)
            )

            coinZones.forEach { (zone, value, imageRes) ->
                val available = value <= coins

                val zoneWidthDp  = zone.w * w
                val zoneHeightDp = zone.h * h
                val coinSizeDp   = minOf(zoneWidthDp, zoneHeightDp).dp

                Box(
                    modifier = Modifier
                        .offset(
                            x = (zone.x * w).dp,
                            y = (zone.y * h).dp
                        )
                        .size(coinSizeDp),
                    contentAlignment = Alignment.Center
                ) {
                    if (available) {
                        CoinImage(
                            value = value,
                            imageRes = imageRes,
                            size = coinSizeDp,
                            onPositioned = {
                                coinCenters[value] = it
                                coinSizes[value] = coinSizeDp
                            },
                            onDragStart = {
                                draggedValue = value
                                dragPosition = coinCenters[value] ?: Offset.Zero
                                dragSize = coinSizes[value] ?: coinSizeDp
                            },
                            onDrag = { delta -> dragPosition += delta },
                            onDragEnd = {
                                val target = when {
                                    needsBounds?.contains(dragPosition) == true -> "needs"
                                    wantsBounds?.contains(dragPosition) == true -> "wants"
                                    piggyBounds?.contains(dragPosition) == true -> "piggy"
                                    else -> null
                                }
                                val v = draggedValue
                                if (target != null && v != null && coins >= v) {
                                    coins -= v
                                    when (target) {
                                        "needs" -> {
                                            needs += v
                                            storage.budgetSpreadToday = true
                                        }
                                        "wants" -> {
                                            wants += v
                                            storage.budgetSpreadToday = true
                                        }
                                        "piggy" -> {
                                            piggy += v
                                            storage.piggyAddedToday = true
                                        }
                                    }
                                    storage.coins = coins
                                    storage.coinsNeeds = needs
                                    storage.coinsWants = wants
                                    storage.piggyBalance = piggy
                                }
                                draggedValue = null
                            }
                        )
                    }
                }
            }
        }

        // Перемещение монетки
        draggedValue?.let { value ->
            val imageRes = when (value) {
                5   -> R.drawable.coin_5
                10  -> R.drawable.coin_10
                20  -> R.drawable.coin_20
                50  -> R.drawable.coin_50
                100 -> R.drawable.coin_100
                else -> R.drawable.coin_5
            }
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (dragPosition.x - dragSizePx / 2).roundToInt(),
                            (dragPosition.y - dragSizePx / 2).roundToInt()
                        )
                    }
                    .size(dragSize),
                contentScale = ContentScale.Fit
            )
        }
    }
}

// ТЕКСТ НА КОНВЕРТЕ
@Composable
private fun EnvelopeText(
    amount: Int,
    description: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 12.dp)
    ) {
        Text(
            description,
            fontSize = 15.sp,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "$amount",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
    }
}

// МОНЕТКА (перетаскиваемая)
@Composable
private fun CoinImage(
    value: Int,
    imageRes: Int,
    size: Dp,
    onPositioned: (Offset) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = "Монетка $value",
        modifier = Modifier
            .size(size)
            .onGloballyPositioned { onPositioned(it.boundsInRoot().center) }
            .pointerInput(value) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDrag = { change, drag ->
                        change.consume()
                        onDrag(drag)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            },
        contentScale = ContentScale.Fit
    )
}