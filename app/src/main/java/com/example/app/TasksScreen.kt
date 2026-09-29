package com.example.myapplication

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource


private fun isCorrectVerdict(verdict: String): Boolean = verdict == "recommended"

private data class TaskResult(
    val chosenOption: TaskOption,
    val correctOption: TaskOption?,
    val rewardGiven: Int,
    val moodGiven: Int,
    val isCorrect: Boolean
)

private enum class TaskState { NOT_ANSWERED, CORRECT, WRONG }

private fun taskStateOf(task: Task, storage: AppStorage): TaskState {
    val chosenId = storage.getChosenOption(task.id) ?: return TaskState.NOT_ANSWERED
    val chosen = task.options.firstOrNull { it.id == chosenId } ?: return TaskState.NOT_ANSWERED
    return if (isCorrectVerdict(chosen.verdict)) TaskState.CORRECT else TaskState.WRONG
}

@Composable
fun TasksScreen(
    storage: AppStorage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allTasks = remember { loadTasksFromAssets(context) }

    if (allTasks.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Задания не загрузились", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Проверь, что файл tasks.json лежит в папке assets.",
                fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            TextButton(onClick = onBack) { Text("Назад") }
        }
        return
    }

    // Данные для подстановки в тексты
    val petName = storage.petName
    val goalPrice = storage.goalPrice
    val piggyBalance = storage.piggyBalance

    fun sub(text: String) = interpolateTaskText(text, petName, goalPrice, piggyBalance)

    val pageCount = (allTasks.size + 1) / 2
    val pagerState = rememberPagerState(pageCount = { pageCount })

    var selectedTaskId by remember { mutableStateOf<String?>(null) }

    val pendingOptions = remember { mutableStateMapOf<String, String>() }
    var version by remember { mutableStateOf(0) }
    var resultDialog by remember { mutableStateOf<TaskResult?>(null) }

    LaunchedEffect(pagerState.currentPage) {
        selectedTaskId = null
    }

    val selectedTask = selectedTaskId?.let { id ->
        allTasks.firstOrNull { it.id == id }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.tasks_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // Верхняя область: книжка
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .padding(top = 205.dp, start = 14.dp, end = 14.dp, bottom = 25.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                if (DEBUG_TASKS_LAYOUT) {
                    DebugZone(
                        rect = BOOK_LEFT_PAGE,
                        parentWidth = maxWidth,
                        parentHeight = maxHeight,
                        onClick = {}
                    )
                    DebugZone(
                        rect = BOOK_RIGHT_PAGE,
                        parentWidth = maxWidth,
                        parentHeight = maxHeight,
                        onClick = {}
                    )
                    return@BoxWithConstraints
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val leftIndex = page * 2
                    val rightIndex = page * 2 + 1

                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val pw = maxWidth.value
                        val ph = maxHeight.value

                        // Левая страница
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = (BOOK_LEFT_PAGE.x * pw).dp,
                                    y = (BOOK_LEFT_PAGE.y * ph).dp
                                )
                                .size(
                                    width = (BOOK_LEFT_PAGE.w * pw).dp,
                                    height = (BOOK_LEFT_PAGE.h * ph).dp
                                )
                        ) {
                            allTasks.getOrNull(leftIndex)?.let { task ->
                                val isLocked = storage.daysWithPet < task.unlockDay
                                TaskOnBookPage(
                                    task = task,
                                    state = taskStateOf(task, storage),
                                    isSelected = task.id == selectedTaskId,
                                    isLocked = isLocked,
                                    displayTitle = sub(task.title),
                                    onClick = {
                                        selectedTaskId = if (selectedTaskId == task.id) null else task.id
                                    }
                                )
                            }
                        }

                        // Правая страница
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = (BOOK_RIGHT_PAGE.x * pw).dp,
                                    y = (BOOK_RIGHT_PAGE.y * ph).dp
                                )
                                .size(
                                    width = (BOOK_RIGHT_PAGE.w * pw).dp,
                                    height = (BOOK_RIGHT_PAGE.h * ph).dp
                                )
                        ) {
                            allTasks.getOrNull(rightIndex)?.let { task ->
                                val isLocked = storage.daysWithPet < task.unlockDay
                                TaskOnBookPage(
                                    task = task,
                                    state = taskStateOf(task, storage),
                                    isSelected = task.id == selectedTaskId,
                                    isLocked = isLocked,
                                    displayTitle = sub(task.title),
                                    onClick = {
                                        selectedTaskId = if (selectedTaskId == task.id) null else task.id
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Нижняя область: листок
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.50f)
                .align(Alignment.BottomCenter)
                .padding(start = 28.dp, end = 28.dp, top = 20.dp, bottom = 150.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val pw = maxWidth.value
                val ph = maxHeight.value

                if (DEBUG_TASKS_LAYOUT) {
                    DebugZone(
                        rect = PAPER_ZONE,
                        parentWidth = maxWidth,
                        parentHeight = maxHeight,
                        onClick = {}
                    )
                    return@BoxWithConstraints
                }

                Box(
                    modifier = Modifier
                        .offset(
                            x = (PAPER_ZONE.x * pw).dp,
                            y = (PAPER_ZONE.y * ph).dp
                        )
                        .size(
                            width = (PAPER_ZONE.w * pw).dp,
                            height = (PAPER_ZONE.h * ph).dp
                        )
                ) {
                    if (selectedTask == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Нажми на задание,\nчтобы увидеть ответ",
                                fontSize = 16.sp,
                                color = Color(0xFF8A8A8A),
                                textAlign = TextAlign.Center,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    } else if (storage.daysWithPet < selectedTask.unlockDay) {
                        // Задание заблокировано
                        val daysLeft = selectedTask.unlockDay - storage.daysWithPet
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔒", fontSize = 56.sp)
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Задание откроется через $daysLeft ${
                                        daysWord(daysLeft)
                                    }",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF888888),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        key(version) {
                            val confirmedId = storage.getChosenOption(selectedTask.id)
                            val pending = pendingOptions[selectedTask.id]
                            val correctOptionId = selectedTask.options
                                .firstOrNull { isCorrectVerdict(it.verdict) }?.id

                            val scrollState = rememberScrollState()
                            val isScrollable = scrollState.maxValue > 0
                            val showAnswerButton =
                                (confirmedId == null && pending != null)

                            Column(modifier = Modifier.fillMaxSize()) {

                                // Перематываемая часть
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(scrollState)
                                            .padding(
                                                start = 4.dp,
                                                end = if (isScrollable) 12.dp else 4.dp
                                            )
                                    ) {
                                        // Текст задания с подстановкой
                                        Text(
                                            sub(selectedTask.situation),
                                            fontSize = 14.sp,
                                            lineHeight = 19.sp,
                                            color = Color(0xFF333333),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(Modifier.height(14.dp))
                                        HorizontalDivider(color = Color(0xFFEEEEEE))
                                        Spacer(Modifier.height(10.dp))

                                        // Варианты ответов
                                        Column(
                                            modifier = Modifier.offset(y = (-25).dp)
                                        ) {
                                            selectedTask.options.forEach { option ->
                                                val isConfirmed = (confirmedId == option.id)
                                                val isRevealedCorrect =
                                                    (confirmedId != null) &&
                                                            (confirmedId != correctOptionId) &&
                                                            (option.id == correctOptionId)

                                                OptionItem(
                                                    option = option,
                                                    displayText = sub(option.text),
                                                    displayPetReply = sub(option.petReply),
                                                    displayExplanation = sub(option.explanation),
                                                    isPending = (pending == option.id && confirmedId == null),
                                                    isConfirmed = isConfirmed,
                                                    isRevealedCorrect = isRevealedCorrect,
                                                    isLocked = (confirmedId != null),
                                                    onClick = {
                                                        if (confirmedId == null) {
                                                            pendingOptions[selectedTask.id] = option.id
                                                        }
                                                    }
                                                )
                                                Spacer(Modifier.height(8.dp))
                                            }
                                        }
                                    }

                                    // Ползунок прокрутки
                                    if (isScrollable) {
                                        ScrollBarIndicator(
                                            scrollValue = scrollState.value,
                                            scrollMax = scrollState.maxValue,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .width(6.dp)
                                                .fillMaxHeight()
                                                .padding(vertical = 4.dp)
                                        )
                                    }
                                }

                                // Кнопка Ответить
                                if (showAnswerButton) {
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            val chosenOption = selectedTask.options
                                                .firstOrNull { it.id == pending } ?: return@Button
                                            val correct = isCorrectVerdict(chosenOption.verdict)

                                            val firstAnswer = storage.getChosenOption(selectedTask.id) == null

                                            storage.setChosenOption(selectedTask.id, chosenOption.id)
                                            pendingOptions.remove(selectedTask.id)

                                            if (firstAnswer) {
                                                storage.tasksTotal += 1
                                                if (correct) storage.tasksCorrect += 1
                                            }

                                            var reward = 0
                                            var moodGain = 0
                                            if (correct && !storage.isTaskCompleted(selectedTask.id)) {
                                                storage.markTaskCompleted(selectedTask.id)
                                                storage.coins += selectedTask.reward
                                                moodGain = storage.gainHappinessWithPenalty(Economy.moodForCompletion)
                                                reward = selectedTask.reward
                                            }

                                            val correctOption = selectedTask.options
                                                .firstOrNull { isCorrectVerdict(it.verdict) }

                                            resultDialog = TaskResult(
                                                chosenOption = chosenOption,
                                                correctOption = correctOption,
                                                rewardGiven = reward,
                                                moodGiven = moodGain,
                                                isCorrect = correct
                                            )

                                            version++
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    ) {
                                        Text("Ответить", fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        BackHandler { onBack() }

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onBack() },
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 1f),
            border = BorderStroke(2.dp, Color.White.copy(alpha = 1f))
        ) {
            Text(
                "Назад",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }

    // Диалог результата
    resultDialog?.let { result ->
        ResultDialog(
            result = result,
            displayChosenPetReply = sub(result.chosenOption.petReply),
            displayChosenExplanation = sub(result.chosenOption.explanation),
            displayCorrectText = result.correctOption?.let { sub(it.text) },
            displayCorrectPetReply = result.correctOption?.let { sub(it.petReply) },
            displayCorrectExplanation = result.correctOption?.let { sub(it.explanation) },
            onDismiss = { resultDialog = null }
        )
    }
}

// ПОЛЗУНОК ПРОКРУТКИ
@Composable
private fun ScrollBarIndicator(
    scrollValue: Int,
    scrollMax: Int,
    modifier: Modifier = Modifier
) {
    val fraction = if (scrollMax > 0) scrollValue.toFloat() / scrollMax else 0f

    Canvas(modifier = modifier) {
        val trackWidth = size.width
        val trackHeight = size.height

        val minThumbPx = 32.dp.toPx()
        val thumbHeight = (trackHeight * 0.35f).coerceAtLeast(minThumbPx)
            .coerceAtMost(trackHeight)

        val maxThumbTop = (trackHeight - thumbHeight).coerceAtLeast(0f)
        val thumbTop = maxThumbTop * fraction

        val radius = CornerRadius(trackWidth / 2f, trackWidth / 2f)

        drawRoundRect(
            color = Color(0x22000000),
            topLeft = Offset.Zero,
            size = Size(trackWidth, trackHeight),
            cornerRadius = radius
        )

        drawRoundRect(
            color = Color(0x99333333),
            topLeft = Offset(0f, thumbTop),
            size = Size(trackWidth, thumbHeight),
            cornerRadius = radius
        )
    }
}

// ДИАЛОГ РЕЗУЛЬТАТА
@Composable
private fun ResultDialog(
    result: TaskResult,
    displayChosenPetReply: String,
    displayChosenExplanation: String,
    displayCorrectText: String?,
    displayCorrectPetReply: String?,
    displayCorrectExplanation: String?,
    onDismiss: () -> Unit
) {
    val correct = result.isCorrect
    val title = if (correct) "Правильно!" else "Неправильно"
    val titleColor = if (correct) Color(0xFF4CAF50) else Color(0xFFE53935)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column {
                if (displayChosenPetReply.isNotBlank()) {
                    Text(
                        displayChosenPetReply,
                        fontSize = 15.sp,
                        color = Color(0xFF333333)
                    )
                    Spacer(Modifier.height(10.dp))
                }
                if (displayChosenExplanation.isNotBlank()) {
                    Text(
                        displayChosenExplanation,
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        fontStyle = FontStyle.Italic
                    )
                }

                if (!correct && result.correctOption != null) {
                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Правильный ответ:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(Modifier.height(4.dp))
                    if (!displayCorrectText.isNullOrBlank()) {
                        Text(
                            displayCorrectText,
                            fontSize = 14.sp,
                            color = Color(0xFF222222),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (!displayCorrectPetReply.isNullOrBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            displayCorrectPetReply,
                            fontSize = 12.sp,
                            color = Color(0xFF555555)
                        )
                    }
                    if (!displayCorrectExplanation.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            displayCorrectExplanation,
                            fontSize = 12.sp,
                            color = Color(0xFF777777),
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(Modifier.height(10.dp))
                if (result.rewardGiven > 0) {
                    Text(
                        "Начислено на баланс: +${result.rewardGiven} ${coinsWord(result.rewardGiven)}",
                        fontSize = 13.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (result.moodGiven > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Настроение питомца: +${result.moodGiven}",
                            fontSize = 13.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Text(
                        text = if (correct)
                            "Награда уже была получена ранее"
                        else
                            "Монеты начисляются только за правильный ответ",
                        fontSize = 13.sp,
                        color = Color(0xFF888888),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Понятно", fontSize = 16.sp)
            }
        }
    )
}

@Composable
private fun TaskOnBookPage(
    task: Task,
    state: TaskState,
    isSelected: Boolean,
    isLocked: Boolean,
    displayTitle: String,
    onClick: () -> Unit
) {
    val textColor = when {
        isLocked -> Color(0xFFAAAAAA)
        state == TaskState.CORRECT -> Color(0xFF4CAF50)
        state == TaskState.WRONG -> Color(0xFFE53935)
        state == TaskState.NOT_ANSWERED ->
            if (isSelected) Color(0xFF7E57C2) else Color(0xFF222222)
        else -> Color(0xFF222222)
    }
    val mark = when {
        isLocked -> null
        state == TaskState.CORRECT -> "✓"
        state == TaskState.WRONG -> "✕"
        else -> null
    }
    val markColor = when {
        state == TaskState.CORRECT -> Color(0xFF4CAF50)
        state == TaskState.WRONG -> Color(0xFFE53935)
        else -> Color.Transparent
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() }
            .padding(14.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLocked) {
            Text("🔒", fontSize = 24.sp)
            Spacer(Modifier.height(6.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Задание №${task.order}",
                fontSize = 13.sp,
                color = textColor.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            if (mark != null) {
                Spacer(Modifier.width(6.dp))
                Text(
                    mark,
                    fontSize = 14.sp,
                    color = markColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            displayTitle,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

// ОДИН ВАРИАНТ ОТВЕТА
@Composable
private fun OptionItem(
    option: TaskOption,
    displayText: String,
    displayPetReply: String,
    displayExplanation: String,
    isPending: Boolean,
    isConfirmed: Boolean,
    isRevealedCorrect: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    val correct = isCorrectVerdict(option.verdict)
    val verdictColor = if (correct) Color(0xFF4CAF50) else Color(0xFFE53935)

    val alpha = if (isLocked && !isConfirmed && !isRevealedCorrect) 0.45f else 1f

    val circleFill = when {
        isConfirmed -> verdictColor
        isRevealedCorrect -> Color(0xFF4CAF50)
        isPending -> Color(0xFF7E57C2)
        else -> Color.Transparent
    }
    val circleBorder = when {
        isConfirmed -> verdictColor
        isRevealedCorrect -> Color(0xFF4CAF50)
        isPending -> Color(0xFF7E57C2)
        else -> Color(0xFFBDBDBD)
    }
    val circleSymbol: String? = when {
        isConfirmed && correct -> "✓"
        isConfirmed && !correct -> "✕"
        isRevealedCorrect -> "✓"
        isPending -> "✓"
        else -> null
    }
    val bgColor = when {
        isConfirmed -> verdictColor.copy(alpha = 0.12f)
        isRevealedCorrect -> Color(0xFF4CAF50).copy(alpha = 0.12f)
        isPending -> Color(0xFF7E57C2).copy(alpha = 0.10f)
        else -> Color.Transparent
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLocked) { onClick() }
            .background(bgColor, RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(circleFill, CircleShape)
                    .border(2.dp, circleBorder.copy(alpha = alpha), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (circleSymbol != null) {
                    Text(
                        circleSymbol,
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Text(
                displayText,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF222222).copy(alpha = alpha)
            )
        }

        if (isConfirmed || isRevealedCorrect) {
            Column(modifier = Modifier.padding(start = 34.dp, top = 6.dp)) {
                if (isRevealedCorrect && !isConfirmed) {
                    Text(
                        "Правильный ответ",
                        fontSize = 11.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                }
                if (displayPetReply.isNotBlank()) {
                    Text(
                        displayPetReply,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFF555555)
                    )
                }
                if (displayExplanation.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        displayExplanation,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Color(0xFF777777),
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}