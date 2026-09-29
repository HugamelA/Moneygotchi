package com.example.myapplication

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.appendInlineContent

// Общая константа для inline-иконки монеты в диалоге сна
internal const val INLINE_COIN = "inline_coin"


// ГЛАВНЫЙ ЭКРАН
@Composable
fun MainScreen(
    storage: AppStorage,
    onSettingsClick: () -> Unit,
    onShopClick: () -> Unit = {},
    onCoinsClick: () -> Unit = {},
    onPiggyClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onGoalClick: () -> Unit = {},
    onPetClick: () -> Unit = {},
    onTelescopeClick: () -> Unit = {},
    onConsoleGameClick: () -> Unit = {},
    showTutorial: Boolean = false,
    canSkipTutorial: Boolean = false,
    onTutorialFinished: () -> Unit = {}
) {
    val context = LocalContext.current

    remember { ShopCatalog.load(context); 0 }
    remember { EventData.load(context); 0 }

    val allTasks = remember { loadTasksFromAssets(context) }

    val hasNewTask by remember {
        androidx.compose.runtime.derivedStateOf {
            val maxAvail = allTasks
                .filter { it.unlockDay <= storage.daysWithPet }
                .maxOfOrNull { it.unlockDay } ?: 0
            maxAvail > storage.lastSeenTaskUnlockDay
        }
    }

    var showExitConfirm by remember { mutableStateOf(false) }

    var isSleeping by remember { mutableStateOf(false) }
    var sleepMessage by remember { mutableStateOf(AnnotatedString("")) }
    var sleepTextVisible by remember { mutableStateOf(false) }
    var sleepShowContinue by remember { mutableStateOf(false) }
    var showSleepConfirm by remember { mutableStateOf(false) }

    var currentEvent by remember { mutableStateOf<Event?>(null) }

    var tutorialFocusZone by remember { mutableStateOf<String?>(null) }

    var petMenuStage by remember { mutableStateOf("none") }
    var petInventory by remember { mutableStateOf(storage.getInventory()) }
    var hungerDelta by remember { mutableStateOf<Int?>(null) }
    var happinessDelta by remember { mutableStateOf<Int?>(null) }
    var moveLeftoversToPiggy by remember { mutableStateOf(false) }



    val sleepAlpha by animateFloatAsState(
        targetValue = if (isSleeping) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "sleepFade"
    )

    val scope = rememberCoroutineScope()

    BackHandler(enabled = !showTutorial) {
        showExitConfirm = true
    }

    BackHandler(enabled = petMenuStage != "none") {
        petMenuStage = when (petMenuStage) {
            "feed", "play", "wash" -> "bubbles"
            else -> "none"
        }
    }

    LaunchedEffect(hungerDelta, happinessDelta) {
        if (hungerDelta != null || happinessDelta != null) {
            delay(1500)
            hungerDelta = null
            happinessDelta = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Фон: комната + питомец + зоны
        RoomSceneLayer(
            storage = storage,
            context = context,
            showTutorial = showTutorial,
            tutorialFocusZone = tutorialFocusZone,
            hasNewTask = hasNewTask,
            allTasks = allTasks,
            onShopClick = onShopClick,
            onCoinsClick = onCoinsClick,
            onPiggyClick = onPiggyClick,
            onTasksClick = onTasksClick,
            onTelescopeClick = onTelescopeClick,
            onConsoleGameClick = onConsoleGameClick,
            onShowSleepConfirm = { if (!isSleeping) showSleepConfirm = true },
            onPetMenuOpen = {
                petInventory = storage.getInventory()
                petMenuStage = "bubbles"
            }
        )

        // Верхняя зона с панелями + подсказки
        MainHud(
            storage = storage,
            hungerDelta = hungerDelta,
            happinessDelta = happinessDelta,
            showTutorial = showTutorial,
            tutorialFocusZone = tutorialFocusZone,
            onSettingsClick = onSettingsClick,
            onGoalClick = onGoalClick
        )

        // Оверлей затемнения при завершении дня
        SleepOverlayLayer(
            sleepAlpha = sleepAlpha,
            sleepTextVisible = sleepTextVisible,
            sleepShowContinue = sleepShowContinue,
            sleepMessage = sleepMessage,
            onContinue = {
                scope.launch {
                    sleepShowContinue = false
                    sleepTextVisible = false
                    delay(600)
                    isSleeping = false

                    // Показать событие, если оно есть
                    if (storage.pendingEventId.isNotEmpty()) {
                        val ev = EventData.eventById(storage.pendingEventId)
                        if (ev != null) {
                            currentEvent = ev
                        }
                        storage.pendingEventId = ""
                    }
                }
            }
        )

        if (showTutorial) {
            TutorialOverlay(
                canSkip = canSkipTutorial,
                storage = storage,
                onFocusChanged = { tutorialFocusZone = it },
                onFinish = onTutorialFinished
            )
        }

        // Слой закрытия по тапу вне
        if (petMenuStage != "none") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { petMenuStage = "none" }
                    }
            )
        }

        // Кружки и карусели питомца
        PetMenuLayer(
            stage = petMenuStage,
            storage = storage,
            petInventory = petInventory,
            onStageChange = { petMenuStage = it },
            onInventoryRefresh = { petInventory = storage.getInventory() },
            onUse = { product ->
                val beforeHunger = storage.hunger
                val beforeHappiness = storage.happiness

                val real = storage.applyPurchaseEffects(
                    product.hungerEffect, product.happinessEffect
                )

                if (real.first > 0 || real.second > 0) {
                    storage.consumeFromInventory(product.id)
                    petInventory = storage.getInventory()

                    if (real.first > 0) hungerDelta = real.first
                    if (real.second > 0) happinessDelta = real.second
                }
            }
        )

        // Диалог события
        val ev = currentEvent
        if (ev != null) {
            EventDialog(
                event = ev,
                storage = storage,
                onFinish = {
                    currentEvent = null
                }
            )
        }
    }

    // Диалог «Выйти из игры?»
    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text("Выйти из игры?") },
            text = { Text("Прогресс сохранится, вы сможете продолжить позже.") },
            confirmButton = {
                TextButton(onClick = {
                    showExitConfirm = false
                    (context as? android.app.Activity)?.finish()
                }) {
                    Text("Выйти")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) {
                    Text("Остаться")
                }
            }
        )
    }

    // Диалог подтверждения завершения дня
    if (showSleepConfirm) {
        SleepConfirmDialog(
            storage = storage,
            moveLeftoversToPiggy = moveLeftoversToPiggy,
            onToggleMoveLeftovers = { moveLeftoversToPiggy = !moveLeftoversToPiggy },
            onDismiss = { showSleepConfirm = false },
            onConfirm = {
                showSleepConfirm = false
                scope.launch {
                    // Флаги за день
                    val hadPiggyAdded = storage.piggyAddedToday

                    // Сытость и настроение до сна
                    val satietyBefore = storage.hunger
                    val moodBefore = storage.happiness

                    isSleeping = true
                    sleepShowContinue = false
                    sleepMessage = AnnotatedString("День завершён")

                    delay(2000)
                    sleepTextVisible = true

                    delay(2000)

                    // Доход за день
                    val incomeBase = Economy.sleepBase
                    val fedBonus = if (satietyBefore >= Economy.careThreshold)
                        Economy.sleepBonusFed else 0
                    val happyBonus = if (moodBefore >= Economy.careThreshold)
                        Economy.sleepBonusHappy else 0
                    val totalIncome = (incomeBase + fedBonus + happyBonus)
                        .coerceAtMost(Economy.sleepMax)

                    // Перенос остатков (только если выбрано)
                    val leftoverNeeds = storage.coinsNeeds
                    val leftoverWants = storage.coinsWants
                    val leftoverTotal = leftoverNeeds + leftoverWants
                    val hasLeftovers = leftoverTotal > 0

                    val willTransfer = hasLeftovers && moveLeftoversToPiggy
                    if (willTransfer) {
                        storage.piggyBalance += leftoverTotal
                        storage.coinsNeeds = 0
                        storage.coinsWants = 0
                    }

                    // Звёзды за день
                    var starsEarned = 0
                    val details = mutableListOf<String>()

                    // 1) Питомец сыт
                    if (satietyBefore >= Economy.careThreshold) {
                        starsEarned += 1
                        details.add("Питомец был сыт в конце дня (+1 ⭐)")
                    } else {
                        details.add("Питомец не был сыт в конце дня (+0 ⭐)")
                    }

                    // 2) Питомец доволен
                    if (moodBefore >= Economy.careThreshold) {
                        starsEarned += 1
                        details.add("Питомец был доволен в конце дня (+1 ⭐)")
                    } else {
                        details.add("Питомец не был доволен в конце дня (+0 ⭐)")
                    }

                    // 3) Отложено в копилку
                    val savedToday = hadPiggyAdded || willTransfer
                    if (savedToday) {
                        starsEarned += 1
                        details.add("В копилку отложено за день (+1 ⭐)")
                    } else {
                        details.add("В копилку ничего не отложено (+0 ⭐)")
                    }

                    // Начисление
                    val levelBefore = storage.petLevel

                    storage.recordDailyStats(
                        hunger = satietyBefore,
                        happiness = moodBefore
                    )
                    val (hungerLost, happinessLost) = storage.applyDailyDecay()

                    storage.coins += totalIncome
                    storage.daysWithPet += 1

                    if (starsEarned > 0) storage.addStars(starsEarned)

                    val levelUpText =
                        if (storage.petLevel > levelBefore)
                            "\n\n🎉 Уровень питомца ${storage.petLevel}!"
                        else ""

                    val incomeLine = buildAnnotatedString {
                        append("+$totalIncome ${coinsWord(totalIncome)} ")
                        appendInlineContent(INLINE_COIN, "[coin]")
                        if (fedBonus > 0 || happyBonus > 0) {
                            append("\n(")
                            if (fedBonus > 0) append("+$fedBonus за сытость")
                            if (fedBonus > 0 && happyBonus > 0) append(", ")
                            if (happyBonus > 0) append("+$happyBonus за настроение")
                            append(")")
                        }
                    }

                    val starsLine =
                        if (starsEarned > 0)
                            "+$starsEarned ${starsWord(starsEarned)} ⭐"
                        else ""

                    sleepMessage = buildAnnotatedString {
                        append("Новый день!\n")
                        append(incomeLine)
                        append("\n\n")
                        append(details.joinToString("\n"))
                        if (starsLine.isNotBlank()) {
                            append("\n\n")
                            append(starsLine)
                        }
                        if (levelUpText.isNotBlank()) {
                            append(levelUpText)
                        }
                    }

                    storage.resetDailyFlags()
                    sleepShowContinue = true

                    val event = EventEngine.pickForToday(storage, storage.daysWithPet)
                    if (event != null) {
                        storage.pendingEventId = event.id
                        storage.lastEventDay = storage.daysWithPet
                        storage.setEventLastDay(event.id, storage.daysWithPet)
                        storage.incrementEventCount(event.id)
                    }
                }
            }
        )
    }
}