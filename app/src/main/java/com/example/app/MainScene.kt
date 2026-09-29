package com.example.app

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size


// СЦЕНА: ФОН, ПИТОМЕЦ, ЗОНЫ
@Composable
internal fun RoomSceneLayer(
    storage: AppStorage,
    context: Context,
    showTutorial: Boolean,
    tutorialFocusZone: String?,
    hasNewTask: Boolean,
    allTasks: List<Task>,
    onShopClick: () -> Unit,
    onCoinsClick: () -> Unit,
    onPiggyClick: () -> Unit,
    onTasksClick: () -> Unit,
    onTelescopeClick: () -> Unit,
    onConsoleGameClick: () -> Unit,
    onShowSleepConfirm: () -> Unit,
    onPetMenuOpen: () -> Unit
) {
    // Фон: комната
    Image(
        painter = painterResource(id = roomBackgroundRes(storage)),
        contentDescription = "Комната",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.FillBounds
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val w = maxWidth
        val h = maxHeight

        val zones = remember {
            listOf(
                ZoneRect("Компьютер",         0.738f, 0.396f, 0.239f, 0.089f),
                ZoneRect("Ящик с конвертами", 0.754f, 0.491f, 0.196f, 0.050f),
                ZoneRect("Банка",             0.874f, 0.556f, 0.126f, 0.080f),
                ZoneRect("Кровать",           0.008f, 0.435f, 0.340f, 0.171f),
                ZoneRect("Полка с книгами",   0.007f, 0.293f, 0.208f, 0.076f),
                ZoneRect("Консоль",           0.613f, 0.851f, 0.354f, 0.119f),
                ZoneRect("Телескоп", 0.514f, 0.405f, 0.160f, 0.140f)
            )
        }

        // ПИТОМЕЦ
        val species = currentPetTypeId(storage.petAppearance)
        val moodStr = when (moodIndex(storage.happiness)) {
            0 -> "sad"
            1 -> "normal"
            else -> "happy"
        }
        val stageStr = when (storage.petStage().toVisualStage()) {
            VisualStage.BABY  -> "kid"
            VisualStage.TEEN  -> "teen"
            VisualStage.ADULT -> "adult"
        }

        val colorNum = storage.petColor + 1

        val petImagePath = remember(species, moodStr, stageStr, colorNum) {
            PetAssets.petImageAssetPath(
                context = context,
                species = species,
                mood = moodStr,
                stage = stageStr,
                color = colorNum
            )
        }

        // Маска для проверки кликов
        val petMask = remember(species, moodStr, stageStr) {
            PetAssets.loadMaskBitmap(context, species, moodStr, stageStr)
        }

        val petImageRequest = remember(petImagePath) {
            if (petImagePath != null) {
                ImageRequest.Builder(context)
                    .data(petImagePath)
                    .crossfade(500)
                    .build()
            } else null
        }

        if (petImageRequest != null) {
            AsyncImage(
                model = petImageRequest,
                contentDescription = petOptions.getOrElse(storage.petAppearance) { petOptions[0] }.name,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(petMask, showTutorial) {
                        detectTapGestures { tapOffset ->
                            if (showTutorial) return@detectTapGestures
                            val mask = petMask ?: return@detectTapGestures
                            if (isPixelOpaque(mask, tapOffset, size)) {
                                onPetMenuOpen()
                            }
                        }
                    },
                contentScale = ContentScale.FillBounds
            )
        }

        // ОСТАЛЬНЫЕ ЗОНЫ
        // ОСТАЛЬНЫЕ ЗОНЫ
        zones.forEach { zone ->

            if (zone.name == "Консоль") {
                val hasConsole = storage.getInventoryQuantity("special_console") > 0
                val bgIsFull = roomBackgroundRes(storage) == R.drawable.room_bg_3
                if (!hasConsole || !bgIsFull) return@forEach
            }

            if (zone.name == "Телескоп") {
                val hasTelescope = storage.getInventoryQuantity("special_telescope") > 0
                val bg = roomBackgroundRes(storage)
                val bgIsRight = bg == R.drawable.room_bg_2 || bg == R.drawable.room_bg_3
                if (!hasTelescope || !bgIsRight) return@forEach
            }

            val action: () -> Unit = when (zone.name) {
                "Компьютер"          -> onShopClick
                "Ящик с конвертами"  -> onCoinsClick
                "Банка"              -> onPiggyClick
                "Кровать"            -> onShowSleepConfirm
                "Полка с книгами"    -> onTasksClick
                "Консоль"            -> onConsoleGameClick
                "Телескоп"           -> onTelescopeClick
                else                 -> ({})
            }

            val hasBadge = !storage.hasSeenZone(zone.name) ||
                    (zone.name == "Полка с книгами" && hasNewTask)

            val wrappedAction: () -> Unit = {
                storage.markZoneSeen(zone.name)

                if (zone.name == "Полка с книгами") {
                    val maxUnlock = allTasks
                        .filter { it.unlockDay <= storage.daysWithPet }
                        .maxOfOrNull { it.unlockDay } ?: 0
                    storage.lastSeenTaskUnlockDay = maxUnlock
                }

                action()
            }

            val isBed = (zone.name == "Кровать")
            val zoneAllowed =
                !showTutorial || (tutorialFocusZone == zone.name && !isBed)

            if (DEBUG_ZONES) {
                DebugZone(
                    rect = zone,
                    parentWidth = w,
                    parentHeight = h,
                    onClick = { if (zoneAllowed) wrappedAction() }
                )
            } else {
                ClickableZone(
                    x = w * zone.x, y = h * zone.y,
                    width = w * zone.w, height = h * zone.h,
                    onClick = { if (zoneAllowed) wrappedAction() }
                )

                if (hasBadge && !showTutorial) {
                    val badgeSize = 22.dp
                    val badgeOffsetX = w * zone.x + (w * zone.w - badgeSize) / 2
                    val badgeOffsetY = h * zone.y + (h * zone.h - badgeSize) / 2

                    Box(
                        modifier = Modifier
                            .offset(x = badgeOffsetX, y = badgeOffsetY)
                            .size(badgeSize)
                            .background(Color(0xFFFF5252), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "!",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ОТЛАДОЧНАЯ НАДПИСЬ «ERROR»
        DebugErrorBanner(
            petImagePath = petImagePath,
            petMask = petMask,
            zones = zones
        )
    }
}


// ОТЛАДОЧНАЯ НАДПИСЬ
@Composable
private fun BoxScope.DebugErrorBanner(
    petImagePath: String?,
    petMask: Bitmap?,
    zones: List<ZoneRect>
) {
    run {
        val expectedZones = listOf("Компьютер", "Ящик с конвертами", "Банка", "Кровать", "Полка с книгами")
        val missingZones = expectedZones.filter { name -> zones.none { it.name == name } }

        val noPetImage = petImagePath == null
        val noPetMask  = petMask == null

        if (noPetImage || noPetMask || missingZones.isNotEmpty()) {
            Surface(
                color = Color.Red,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ERROR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    if (noPetImage) {
                        Text("Питомец: картинка не найдена")
                    }
                    if (noPetMask) {
                        Text("Питомец: маска не найдена")
                    }
                    if (missingZones.isNotEmpty()) {
                        Text("Зоны не найдены: ${missingZones.joinToString(", ")}")
                    }
                }
            }
        }
    }
}


// Проверяет, что в маске под тапом — непрозрачный пиксель
internal fun isPixelOpaque(
    mask: Bitmap,
    tap: Offset,
    containerSize: IntSize
): Boolean {
    if (containerSize.width == 0 || containerSize.height == 0) return false

    val xRatio = tap.x / containerSize.width
    val yRatio = tap.y / containerSize.height

    val px = (xRatio * mask.width).toInt().coerceIn(0, mask.width - 1)
    val py = (yRatio * mask.height).toInt().coerceIn(0, mask.height - 1)

    return android.graphics.Color.alpha(mask.getPixel(px, py)) > 0
}