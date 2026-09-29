package com.example.myapplication

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt


// МЕНЮ ПИТОМЦА: КРУЖКИ И КАРУСЕЛИ
@Composable
internal fun PetMenuLayer(
    stage: String,
    storage: AppStorage,
    petInventory: Map<String, Int>,
    onStageChange: (String) -> Unit,
    onInventoryRefresh: () -> Unit,
    onUse: (ShopProduct) -> Unit
) {
    // 3 кружка
    if (stage == "bubbles") {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val w = maxWidth.value
            val h = maxHeight.value

            // 🍽️
            Box(
                modifier = Modifier
                    .offset(
                        x = (0.28f * w - 36).dp,
                        y = (0.72f * h - 36).dp
                    )
                    .size(72.dp)
                    .pointerInput(Unit) { detectTapGestures { } }
            ) {
                PetActionAnchor(
                    emoji = "🍽️",
                    onClick = {
                        onInventoryRefresh()
                        onStageChange("feed")
                    }
                )
            }

            // 🎮
            Box(
                modifier = Modifier
                    .offset(
                        x = (0.72f * w - 36).dp,
                        y = (0.72f * h - 36).dp
                    )
                    .size(72.dp)
                    .pointerInput(Unit) { detectTapGestures { } }
            ) {
                PetActionAnchor(
                    emoji = "🎮",
                    onClick = {
                        onInventoryRefresh()
                        onStageChange("play")
                    }
                )
            }

            // 🧼
            Box(
                modifier = Modifier
                    .offset(
                        x = (0.50f * w - 36).dp,
                        y = (0.46f * h - 36).dp
                    )
                    .size(72.dp)
                    .pointerInput(Unit) { detectTapGestures { } }
            ) {
                PetActionAnchor(
                    emoji = "🧼",
                    onClick = {
                        onInventoryRefresh()
                        onStageChange("wash")
                    }
                )
            }
        }
    }

    // Карусель еды
    if (stage == "feed") {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val w = maxWidth.value
            val h = maxHeight.value

            PetActionPanel(
                categories = listOf("food", "treats"),
                anchorEmoji = "🍽️",
                emptyText = "Нет еды.\nКупи в магазине.",
                allInventory = petInventory,
                petGenitive = currentPetTypeGenitive(storage.petAppearance),
                centerX = (0.28f * w).dp,
                centerY = (0.72f * h).dp,
                baseAngleDeg = 45f,
                crossOffsetX = (-52).dp,
                onClose = { onStageChange("bubbles") },
                onUse = onUse
            )
        }
    }

    // Карусель игрушек
    if (stage == "play") {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val w = maxWidth.value
            val h = maxHeight.value

            PetActionPanel(
                categories = listOf("toys"),
                anchorEmoji = "🎮",
                emptyText = "Нет игрушек.\nКупи в магазине.",
                allInventory = petInventory,
                petGenitive = currentPetTypeGenitive(storage.petAppearance),
                centerX = (0.72f * w).dp,
                centerY = (0.72f * h).dp,
                baseAngleDeg = 135f,
                crossOffsetX = 52.dp,
                onClose = { onStageChange("bubbles") },
                onUse = onUse
            )
        }
    }

    // Карусель ухода
    if (stage == "wash") {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val w = maxWidth.value
            val h = maxHeight.value

            PetActionPanel(
                categories = listOf("care"),
                anchorEmoji = "🧼",
                emptyText = "Нет средств ухода.\nКупи в магазине.",
                allInventory = petInventory,
                petGenitive = currentPetTypeGenitive(storage.petAppearance),
                centerX = (0.50f * w).dp,
                centerY = (0.46f * h).dp,
                baseAngleDeg = 90f,
                crossOffsetX = 0.dp,
                onClose = { onStageChange("bubbles") },
                onUse = onUse
            )
        }
    }
}


// ПАНЕЛЬ ДЕЙСТВИЯ ПИТОМЦА (радиальная карусель)
@Composable
private fun PetActionPanel(
    categories: List<String>,
    anchorEmoji: String,
    emptyText: String,
    allInventory: Map<String, Int>,
    petGenitive: String,
    centerX: Dp,
    centerY: Dp,
    baseAngleDeg: Float,
    crossOffsetX: Dp,
    onClose: () -> Unit,
    onUse: (ShopProduct) -> Unit
) {
    val items = allInventory.mapNotNull { (id, qty) ->
        val product = ShopCatalog.productById(id)
        if (product == null || qty <= 0) return@mapNotNull null
        if (product.categoryId in categories) product to qty else null
    }

    val anchorSize = 72
    val crossSize = 36
    val itemSize = 60

    Box(
        modifier = Modifier
            .offset(
                x = centerX - (anchorSize / 2).dp,
                y = centerY - (anchorSize / 2).dp
            )
            .size(anchorSize.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(2.dp, Color(0x33000000)),
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    anchorEmoji,
                    fontSize = 28.sp
                )
            }
        }
    }

    if (items.isEmpty()) {
        // Пустое состояние
        Box(
            modifier = Modifier
                .offset(
                    x = centerX - 80.dp,
                    y = centerY + (anchorSize / 2 + 16).dp
                )
                .width(160.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 4.dp
            ) {
                Text(
                    emptyText,
                    fontSize = 11.sp,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }

        // Крестик поверх всего
        CrossButton(
            centerX = centerX,
            centerY = centerY,
            anchorSize = anchorSize,
            crossSize = crossSize,
            crossOffsetX = crossOffsetX,
            onClose = onClose
        )
        return
    }

    RadialCarousel(
        items = items,
        centerX = centerX,
        centerY = centerY,
        baseAngleDeg = baseAngleDeg,
        radiusDp = 110f,
        itemSizeDp = itemSize,
        petGenitive = petGenitive,
        onUse = onUse
    )

    // Крестик поверх всего
    CrossButton(
        centerX = centerX,
        centerY = centerY,
        anchorSize = anchorSize,
        crossSize = crossSize,
        crossOffsetX = crossOffsetX,
        onClose = onClose
    )
}

// РАДИАЛЬНАЯ КАРУСЕЛЬ
@Composable
private fun RadialCarousel(
    items: List<Pair<ShopProduct, Int>>,
    centerX: Dp,
    centerY: Dp,
    baseAngleDeg: Float,
    radiusDp: Float,
    itemSizeDp: Int,
    petGenitive: String,
    onUse: (ShopProduct) -> Unit
) {
    val itemCount = items.size
    if (itemCount == 0) return

    val position = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val anglePerSlot = 40f

    LaunchedEffect(itemCount) {
        if (itemCount <= 0) return@LaunchedEffect
        val maxValid = (itemCount - 1).toFloat()
        val current = position.value
        if (current > maxValid || current < 0f) {
            position.animateTo(
                targetValue = current.coerceIn(0f, maxValid),
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            )
        }
    }

    fun angleOf(px: Float, py: Float, cx: Float, cy: Float): Float =
        Math.toDegrees(Math.atan2((py - cy).toDouble(), (px - cx).toDouble())).toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(itemCount) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false)

                        val cxPx = centerX.toPx()
                        val cyPx = centerY.toPx()
                        val radiusPx = radiusDp.dp.toPx()
                        val itemSizePx = itemSizeDp.dp.toPx()

                        // Для 1 элемента — просто тап без вращения
                        if (itemCount <= 1) {
                            var stillPressed = true
                            while (stillPressed) {
                                val ev = awaitPointerEvent()
                                val ch = ev.changes.firstOrNull { it.id == down.id }
                                if (ch == null || !ch.pressed) stillPressed = false
                            }

                            val centerRad = Math.toRadians(baseAngleDeg.toDouble())
                            val itemCenterX = cxPx + (Math.cos(centerRad) * radiusPx).toFloat()
                            val itemCenterY = cyPx + (Math.sin(centerRad) * radiusPx).toFloat()

                            val dist = Math.hypot(
                                (down.position.x - itemCenterX).toDouble(),
                                (down.position.y - itemCenterY).toDouble()
                            ).toFloat()

                            if (dist < itemSizePx) {
                                onUse(items[0].first)
                            }
                            continue
                        }

                        // Для 3+ — обычная логика с вращением
                        var lastAngle = angleOf(
                            down.position.x, down.position.y, cxPx, cyPx
                        )
                        var accumulated = 0f
                        var moved = false
                        var localPos = position.value

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                            if (change == null || !change.pressed) break

                            val currentAngle = angleOf(
                                change.position.x, change.position.y, cxPx, cyPx
                            )
                            var delta = currentAngle - lastAngle
                            if (delta > 180f) delta -= 360f
                            if (delta < -180f) delta += 360f
                            lastAngle = currentAngle

                            accumulated += delta
                            if (abs(accumulated) > 15f) moved = true

                            if (moved) {
                                val step = -delta / anglePerSlot
                                localPos += step

                                val bounded = when {
                                    itemCount <= 3 ->
                                        localPos.coerceIn(0f, (itemCount - 1).toFloat())
                                    else ->
                                        ((localPos % itemCount) + itemCount) % itemCount
                                }
                                localPos = bounded
                                scope.launch { position.snapTo(bounded) }
                            }
                            change.consume()
                        }

                        if (!moved) {
                            val snapped = position.value.roundToInt()
                            val idx = when {
                                itemCount <= 3 ->
                                    snapped.coerceIn(0, itemCount - 1)
                                else ->
                                    ((snapped % itemCount) + itemCount) % itemCount
                            }

                            val centerRad = Math.toRadians(baseAngleDeg.toDouble())
                            val itemCenterX = cxPx + (Math.cos(centerRad) * radiusPx).toFloat()
                            val itemCenterY = cyPx + (Math.sin(centerRad) * radiusPx).toFloat()

                            val dist = Math.hypot(
                                (down.position.x - itemCenterX).toDouble(),
                                (down.position.y - itemCenterY).toDouble()
                            ).toFloat()

                            if (dist < itemSizePx) {
                                onUse(items[idx].first)
                            }
                        } else {
                            val target = when {
                                itemCount <= 3 ->
                                    position.value.roundToInt().toFloat()
                                        .coerceIn(0f, (itemCount - 1).toFloat())
                                else ->
                                    position.value.roundToInt().toFloat()
                            }
                            scope.launch {
                                position.animateTo(
                                    targetValue = target,
                                    animationSpec = tween(
                                        durationMillis = 350,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                            }
                        }
                    }
                }
            }
    ) {
        // Элементы
        val currentPos = position.value

        for (i in 0 until itemCount) {
            val (product, quantity) = items[i]

            // До 3 элементов — без цикла
            val d: Float = when {
                itemCount <= 3 -> i - currentPos
                else -> {
                    var x = ((i - currentPos) % itemCount + itemCount) % itemCount
                    if (x > itemCount / 2f) x -= itemCount
                    x
                }
            }

            if (abs(d) > 1.5f) continue

            val slotAngle = baseAngleDeg + d * anglePerSlot
            val angleRad = Math.toRadians(slotAngle.toDouble())
            val dx = (Math.cos(angleRad) * radiusDp).toFloat()
            val dy = (Math.sin(angleRad) * radiusDp).toFloat()
            val itemX = centerX.value + dx
            val itemY = centerY.value + dy

            val scale = (1f - 0.18f * abs(d)).coerceIn(0.55f, 1f)
            val alpha = (1f - 0.35f * abs(d)).coerceIn(0f, 1f)
            val size = (itemSizeDp * scale).toInt()

            Box(
                modifier = Modifier
                    .offset(
                        x = (itemX - size / 2f).dp,
                        y = (itemY - size / 2f).dp
                    )
                    .size(size.dp)
                    .alpha(alpha)
            ) {
                // Круг с иконкой
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (product.iconRes != null) {
                            Image(
                                painter = painterResource(id = product.iconRes),
                                contentDescription = product.displayName(petGenitive),
                                modifier = Modifier.fillMaxSize(0.7f),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(product.emoji, fontSize = (26 * scale).sp)
                        }
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE53935),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "$quantity",
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// КРЕСТИК
@Composable
private fun CrossButton(
    centerX: Dp,
    centerY: Dp,
    anchorSize: Int,
    crossSize: Int,
    crossOffsetX: Dp,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset(
                x = centerX + crossOffsetX - (crossSize / 2).dp,
                y = centerY - (anchorSize / 2 + crossSize / 2 + 8).dp
            )
            .size(crossSize.dp)
            .pointerInput(Unit) {
                detectTapGestures { onClose() }
            }
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFE53935),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "✕",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun PetActionAnchor(
    emoji: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(2.dp, Color(0x33000000)),
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 34.sp)
        }
    }
}