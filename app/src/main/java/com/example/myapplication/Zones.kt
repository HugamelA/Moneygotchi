package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column

// Флаг: показывать ли зоны и разрешить их перетаскивание
const val DEBUG_ZONES = false
const val DEBUG_TUTORIAL = false
const val DEBUG_TASKS_LAYOUT = false

val BOOK_LEFT_PAGE = ZoneRect("Левая страница", 0.110f, 0.073f, 0.368f, 0.796f)
val BOOK_RIGHT_PAGE = ZoneRect("Правая страница", 0.510f, 0.073f, 0.420f, 0.800f)
// Зона листка с ответами
val PAPER_ZONE = ZoneRect("Лист", 0.06f, 0.066f, 0.869f, 0.901f)

// Позиция и размеры зоны, чтобы их можно было двигать
data class ZoneRect(
    val name: String,
    var x: Float,
    var y: Float,
    var w: Float,
    var h: Float
)

// НЕВИДИМАЯ КЛИКАБЕЛЬНАЯ ЗОНА
@Composable
fun ClickableZone(
    x: Dp, y: Dp,
    width: Dp, height: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(width, height)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
    )
}

// ОТЛАДОЧНАЯ ЗОНА (видимая, перетаскиваемая)
@Composable
fun DebugZone(
    rect: ZoneRect,
    parentWidth: Dp,
    parentHeight: Dp,
    onClick: () -> Unit
) {
    var offsetX by remember { mutableStateOf(rect.x * parentWidth.value) }
    var offsetY by remember { mutableStateOf(rect.y * parentHeight.value) }
    var sizeW by remember { mutableStateOf(rect.w * parentWidth.value) }
    var sizeH by remember { mutableStateOf(rect.h * parentHeight.value) }

    var showInfo by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
            .size(sizeW.dp, sizeH.dp)
            .background(Color.Red.copy(alpha = 0.25f))
            .border(2.dp, Color.Red)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { showInfo = true })
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX = (offsetX + dragAmount.x)
                        .coerceIn(0f, parentWidth.value - sizeW)
                    offsetY = (offsetY + dragAmount.y)
                        .coerceIn(0f, parentHeight.value - sizeH)
                    rect.x = offsetX / parentWidth.value
                    rect.y = offsetY / parentHeight.value
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Уголок для растягивания
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(32.dp)
                .background(Color(0xFF00AA00), RoundedCornerShape(topStart = 10.dp))
                .border(2.dp, Color.White, RoundedCornerShape(topStart = 10.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newW = (sizeW + dragAmount.x)
                            .coerceIn(20f, parentWidth.value - offsetX)
                        val newH = (sizeH + dragAmount.y)
                            .coerceIn(20f, parentHeight.value - offsetY)
                        sizeW = newW
                        sizeH = newH
                        rect.w = newW / parentWidth.value
                        rect.h = newH / parentHeight.value
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text("⇲", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }

    // Окошко с параметрами
    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text(rect.name) },
            text = {
                Column {
                    Text("x = %.3f".format(rect.x), fontSize = 16.sp)
                    Text("y = %.3f".format(rect.y), fontSize = 16.sp)
                    Text("w = %.3f".format(rect.w), fontSize = 16.sp)
                    Text("h = %.3f".format(rect.h), fontSize = 16.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showInfo = false
                    onClick()
                }) {
                    Text("ОК")
                }
            }
        )
    }
}

// ОТЛАДКА ЭКРАНА БЮДЖЕТА
const val DEBUG_COINS_LAYOUT = false

// Зоны страницы «Конверты»
val COINS_NEEDS  = ZoneRect("Конверт Обязательное",   0.089f, 0.126f, 0.819f, 0.197f)
val COINS_WANTS  = ZoneRect("Конверт Необязательное", 0.089f, 0.326f, 0.819f, 0.197f)
val COINS_PIGGY  = ZoneRect("Копилка",                0.033f, 0.522f, 0.582f, 0.200f)
val COINS_BALANCE = ZoneRect("Баланс",                0.593f, 0.727f, 0.374f, 0.119f)

val PIGGY_LABEL  = ZoneRect("Надпись копилки", 0.620f, 0.570f, 0.350f, 0.085f)

val COIN_5   = ZoneRect("Монетка 5",    0.033f, 0.76f, 0.16f, 0.10f)
val COIN_10  = ZoneRect("Монетка 10",   0.205f, 0.76f, 0.16f, 0.10f)
val COIN_20  = ZoneRect("Монетка 20",   0.377f, 0.76f, 0.16f, 0.10f)
val COIN_50  = ZoneRect("Монетка 50",   0.033f, 0.86f, 0.16f, 0.10f)
val COIN_100 = ZoneRect("Монетка 100",  0.205f, 0.86f, 0.16f, 0.10f)