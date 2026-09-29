package com.example.myapplication

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect

// ОБУЧАЮЩЕЕ МЕНЮ
data class TutorialStep(
    val emoji: String,
    val title: String,
    val text: String,
    val focus: ZoneRect? = null,
    val onLeave: (() -> Unit)? = null
)

@Composable
fun TutorialOverlay(
    canSkip: Boolean,
    storage: AppStorage,
    onCoinsChanged: () -> Unit = {},
    onFocusChanged: (String?) -> Unit = {},
    onFinish: () -> Unit
) {
    var step by remember { mutableStateOf(storage.tutorialStep) }
    var showSkipConfirm by remember { mutableStateOf(false) }

    // Блокируем кнопку/жест «Назад» во время обучения
    BackHandler {

    }

    // Список шагов (чтобы зоны не пересоздавались при рекомпозиции)
    val steps = remember(canSkip) {
        val isFirstTime = !canSkip

        // Текст шага про монеты
        val coinsText = if (isFirstTime) {
            "Это твои монеты. Их дают за задания и за каждый новый день, проведённый с питомцем.\n\n" +
                    "Вот тебе ${Economy.startCoins} монет для старта!"
        } else {
            "Это твои монеты. Их дают за задания и за каждый новый день, проведённый с питомцем."
        }

        val giveCoinsIfFirstTime: (() -> Unit)? = if (isFirstTime) {
            {
                storage.coins = Economy.startCoins
                onCoinsChanged()
            }
        } else null

        // Имя питомца в разных падежах
        val petNom = declinePetName(storage.petName, GramCase.NOMINATIVE)
        val petGen = declinePetName(storage.petName, GramCase.GENITIVE)
        val petIns = declinePetName(storage.petName, GramCase.INSTRUMENTAL)
        val petAbout = aboutPet(storage.petName)

        val baseSteps = listOf(
            TutorialStep(
                "🐾", "Знакомься!",
                "Это твой питомец $petNom. Его нужно кормить, мыть и играть с ним. После обучения нажми на него, чтобы начать ухаживать.",
                ZoneRect("Питомец", 0.119f, 0.478f, 0.769f, 0.436f)
            ),
            TutorialStep(
                "", "Уровень",
                "Это уровень $petGen. Когда уровень повысится, питомец вырастет. Уровень повышается за звёзды. Звёзды ты получаешь за заботу $petAbout и грамотное распоряжение своими финансами.",
                ZoneRect("Уровень", 0.019f, 0.010f, 0.234f, 0.102f)
            ),
            TutorialStep(
                "", "Сытость",
                "Это шкала сытости. Если её не пополнять, $petNom проголодается и загрустит.",
                ZoneRect("Сытость", 0.019f, 0.110f, 0.423f, 0.061f)
            ),
            TutorialStep(
                "", "Настроение",
                "Это шкала настроения. Играй с $petIns и мой его — тогда он не загрустит.",
                ZoneRect("Настроение", 0.019f, 0.170f, 0.423f, 0.061f)
            ),
            TutorialStep(
                "", "Монеты",
                coinsText,
                ZoneRect("Монеты", 0.495f, 0.070f, 0.476f, 0.073f),
                onLeave = giveCoinsIfFirstTime
            ),
            TutorialStep(
                "", "Дни",
                "Это счётчик дней. Каждый новый день ты получаешь звёзды — если $petNom сыт, доволен, и ты положил что-нибудь в копилку.",
                ZoneRect("Дни", 0.570f, 0.140f, 0.400f, 0.064f)
            ),
            TutorialStep(
                "", "Цель",
                "Это твоя цель. Здесь видно, сколько тебе надо накопить монет. Когда накопишь — сможешь купить то, о чём мечтал!",
                ZoneRect("Цель", 0.672f, 0.202f, 0.306f, 0.096f)
            ),
            TutorialStep(
                "", "Компьютер",
                "Нажми на компьютер — откроется магазин. В нём есть еда, игрушки и всё для ухода за $petIns. Присмотрись — возможно, найдёшь то, на что захочешь накопить.",
                ZoneRect("Компьютер", 0.726f, 0.390f, 0.271f, 0.106f)
            ),
            TutorialStep(
                "", "Ящик с конвертами",
                "В выдвинутом ящике — конверты. В них ты раскладываешь монеты: на нужное, на хотелки и в копилку. Из них ты и платишь в магазине.",
                ZoneRect("Ящик с конвертами", 0.717f, 0.487f, 0.245f, 0.070f)
            ),
            TutorialStep(
                "", "Банка с накоплениями",
                "Это копилка. Сюда попадают монеты, которые ты откладываешь на цель.",
                ZoneRect("Банка", 0.850f, 0.540f, 0.150f, 0.110f)
            ),
            TutorialStep(
                "", "Кровать",
                "Нажми на кровать — начнётся новый день. Но сначала закончим обучение!",
                ZoneRect("Кровать", 0.000f, 0.410f, 0.360f, 0.200f)
            ),
            TutorialStep(
                "", "Полка с книгами",
                "На полке — задания. Выполняй их и получай монеты, чтобы $petNom радовался.",
                ZoneRect("Полка с книгами", 0.000f, 0.270f, 0.240f, 0.120f)
            ),
            TutorialStep(
                "", "Настройки",
                "Это настройки. Здесь можно пройти обучение снова или начать игру заново.",
                ZoneRect("Настройки", 0.815f, 0.010f, 0.156f, 0.068f)
            )
        )

        val repeatStep = TutorialStep(
            "", "Повтор обучения",
            "Если что-то забудешь — зайди в раздел \"Настройки\" и выбери «Пройти обучение снова»."
        )

        if (canSkip) baseSteps else baseSteps + repeatStep
    }

    // РЕЖИМ ОТЛАДКИ
    if (DEBUG_TUTORIAL) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val w = maxWidth
            val h = maxHeight

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )

            // Отладочные зоны для всех шагов
            steps.forEach { s ->
                s.focus?.let { rect ->
                    DebugZone(
                        rect = rect,
                        parentWidth = w,
                        parentHeight = h,
                        onClick = { }
                    )
                }
            }
        }
        return
    }

    // ОБЫЧНЫЙ РЕЖИМ ОБУЧЕНИЯ
    val current = steps[step]
    LaunchedEffect(current.focus?.name) {
        onFocusChanged(current.focus?.name)
    }
    val isLast = step == steps.size - 1
    val focus = current.focus

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val w = maxWidth.value
        val h = maxHeight.value

        if (focus == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
            )
        } else {
            val fx = focus.x * w
            val fy = focus.y * h
            val fw = focus.w * w
            val fh = focus.h * h

            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRect(Rect(Offset.Zero, size))
                    addRoundRect(
                        RoundRect(
                            left = fx.dp.toPx(),
                            top = fy.dp.toPx(),
                            right = (fx + fw).dp.toPx(),
                            bottom = (fy + fh).dp.toPx(),
                            cornerRadius = CornerRadius(12.dp.toPx())
                        )
                    )
                }
                drawPath(path, color = Color.Black.copy(alpha = 0.8f))
            }

            Box(
                modifier = Modifier
                    .offset(x = (fx - 4).dp, y = (fy - 4).dp)
                    .size((fw + 8).dp, (fh + 8).dp)
                    .border(3.dp, Color.White, RoundedCornerShape(16.dp))
            )
        }

        val cardAtTop = focus != null && (focus.y + focus.h) > 0.6
        val cardAlignment = if (cardAtTop) Alignment.TopCenter else Alignment.BottomCenter
        val cardShape = if (cardAtTop) {
            RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        } else {
            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        }

        Column(
            modifier = Modifier
                .align(cardAlignment)
                .fillMaxWidth()
                .background(Color(0xFF2C2C2C).copy(alpha = 0.95f), cardShape)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(current.emoji, fontSize = 24.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    current.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                current.text,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(steps.size) { idx ->
                    Box(
                        modifier = Modifier
                            .size(if (idx == step) 10.dp else 6.dp)
                            .background(
                                if (idx == step) Color.White
                                else Color.White.copy(alpha = 0.4f),
                                RoundedCornerShape(50)
                            )
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (canSkip && !isLast) {
                    OutlinedButton(
                        onClick = { showSkipConfirm = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text("Пропустить", fontSize = 16.sp)
                    }
                }
                Button(
                    onClick = {
                        current.onLeave?.invoke()
                        if (isLast) {
                            storage.tutorialShown = true
                            storage.tutorialActive = false
                            storage.tutorialStep = 0
                            onFinish()
                        } else {
                            val next = step + 1
                            step = next
                            storage.tutorialStep = next
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isLast) "Начать заботу о питомце!" else "Далее",
                        fontSize = 18.sp
                    )
                }
            }
        }
    }

    // Подтверждение пропуска
    if (showSkipConfirm) {
        AlertDialog(
            onDismissRequest = { showSkipConfirm = false },
            title = {
                Text(
                    "Пропустить обучение?",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Вы уверены, что хотите пропустить обучение? " +
                            "Пройти его заново можно будет в настройках.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSkipConfirm = false
                    storage.tutorialShown = true
                    storage.tutorialActive = false
                    storage.tutorialStep = 0
                    onFinish()
                }) {
                    Text("Пропустить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSkipConfirm = false }) {
                    Text("Продолжить")
                }
            }
        )
    }
}