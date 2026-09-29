package com.example.myapplication

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// HUD И ПОДСКАЗКИ
@Composable
internal fun BoxScope.MainHud(
    storage: AppStorage,
    hungerDelta: Int?,
    happinessDelta: Int?,
    showTutorial: Boolean,
    tutorialFocusZone: String?,
    onSettingsClick: () -> Unit,
    onGoalClick: () -> Unit
) {
    // Локальные состояния подсказок
    var starsPopupVisible by remember { mutableStateOf(false) }
    var hungerTooltipVisible by remember { mutableStateOf(false) }
    var moodTooltipVisible by remember { mutableStateOf(false) }

    // Верхняя зона с панелями
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(start = 16.dp, top = 16.dp, end = 16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopStart),
            horizontalAlignment = Alignment.Start
        ) {
            LevelCircle(
                level = storage.petLevel,
                progress = storage.starsProgress(),
                onPressChange = { starsPopupVisible = it }
            )
            Spacer(Modifier.height(10.dp))
            StatBar(
                iconRes = hungerIconRes(storage.hunger),
                value = storage.hunger,
                barColorFor = ::hungerBarColor,
                delta = hungerDelta,
                onPressChange = { hungerTooltipVisible = it }
            )
            Spacer(Modifier.height(6.dp))
            StatBar(
                iconRes = moodIconRes(storage.happiness),
                value = storage.happiness,
                barColorFor = ::moodBarColor,
                delta = happinessDelta,
                onPressChange = { moodTooltipVisible = it }
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomEnd),
            horizontalAlignment = Alignment.End
        ) {
            SmallInfoChip(iconRes = R.drawable.coin, text = formatCoins(storage.coins))
            Spacer(Modifier.height(10.dp))
            SmallInfoChip(text = "День: ${storage.daysWithPet}")
            Spacer(Modifier.height(10.dp))
            GoalChip(
                hasGoal = storage.goalName.isNotBlank() && storage.goalPrice > 0,
                saved = storage.piggyBalance,
                price = storage.goalPrice,
                onClick = {
                    if (!showTutorial || tutorialFocusZone == "Цель") onGoalClick()
                }
            )
        }

        IconButton(
            onClick = { if (!showTutorial) onSettingsClick() },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Text("⚙️", fontSize = 26.sp)
        }
    }

    // Stars tooltip
    if (starsPopupVisible) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 96.dp, top = 34.dp)
                .background(
                    Color(0xEE2C2C2C),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                "⭐ ${storage.stars} / ${storage.starsToNextLevel()}",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    // Hunger tooltip
    if (hungerTooltipVisible) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 96.dp, top = 92.dp)
                .background(
                    Color(0xEE2C2C2C),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(hungerIconRes(100)),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Unspecified
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "${storage.hunger} / 100",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // Mood tooltip
    if (moodTooltipVisible) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 96.dp, top = 122.dp)
                .background(
                    Color(0xEE2C2C2C),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(moodIconRes(100)),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Unspecified
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "${storage.happiness} / 100",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


// ОВЕРЛЕЙ СНА
@Composable
internal fun SleepOverlayLayer(
    sleepAlpha: Float,
    sleepTextVisible: Boolean,
    sleepShowContinue: Boolean,
    sleepMessage: AnnotatedString,
    onContinue: () -> Unit
) {
    if (sleepAlpha > 0.01f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = sleepAlpha))
                .pointerInput(Unit) {
                    detectTapGestures {  }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                AnimatedVisibility(
                    visible = sleepTextVisible,
                    enter = fadeIn(animationSpec = tween(durationMillis = 700)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 600))
                ) {
                    AnimatedContent(
                        targetState = sleepMessage,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(durationMillis = 700)) togetherWith
                                    fadeOut(animationSpec = tween(durationMillis = 500))
                        },
                        label = "sleepMessage"
                    ) { message ->
                        Text(
                            text = message,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            inlineContent = mapOf(
                                INLINE_COIN to InlineTextContent(
                                    Placeholder(
                                        width = 22.sp,
                                        height = 22.sp,
                                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                                    )
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.coin),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            )
                        )
                    }
                }

                // Кнопка «Продолжить» после итогового сообщения
                if (sleepShowContinue) {
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = onContinue,
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(56.dp)
                    ) {
                        Text("Продолжить", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}


// ДИАЛОГ «ЗАКОНЧИТЬ ДЕНЬ?»
@Composable
internal fun SleepConfirmDialog(
    storage: AppStorage,
    moveLeftoversToPiggy: Boolean,
    onToggleMoveLeftovers: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val leftoverNeeds = storage.coinsNeeds
    val leftoverWants = storage.coinsWants
    val leftoverTotal = leftoverNeeds + leftoverWants
    val hasLeftovers = leftoverTotal > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Закончить день?") },
        text = {
            Column {
                if (hasLeftovers) {
                    Text(
                        "В конвертах осталось $leftoverTotal " +
                                "${coinsWord(leftoverTotal)}.",
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(10.dp))

                    // Переключатель перекладывания монет из конвертов в копилку
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onToggleMoveLeftovers() }
                    ) {
                        Checkbox(
                            checked = moveLeftoversToPiggy,
                            onCheckedChange = { onToggleMoveLeftovers() }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Перенести их в копилку",
                            fontSize = 14.sp,
                            color = Color(0xFF333333)
                        )
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (moveLeftoversToPiggy) {
                            "Монеты перейдут в копилку и пойдут на цель."
                        } else {
                            "Монеты останутся в конвертах на завтра."
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF888888)
                    )
                } else {
                    Text("Начнётся новый день.", fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Закончить день")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Остаться")
            }
        }
    )
}