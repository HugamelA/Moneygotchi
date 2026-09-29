package com.example.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ЭКРАН 3: КАРУСЕЛЬ ВАРИАНТОВ
@Composable
fun ColorSetupScreen(
    storage: AppStorage,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val pagerState = rememberPagerState(
        initialPage = storage.petColor,
        pageCount = { colorNames.size }
    )
    val pet = petOptions.getOrElse(storage.petAppearance) { petOptions[0] }
    val currentPage = pagerState.currentPage

    // Флаг: показывать ли диалог подтверждения создания
    var showConfirm by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        // Фон: магазин
        Image(
            painter = painterResource(id = R.drawable.shop_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Верхняя панель: кнопка «Назад» + заголовок
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(
                        Color.White.copy(alpha = 0.92f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onBack,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Назад", fontSize = 16.sp)
                    }
                    Spacer(Modifier.weight(1f))
                }

                Text(
                    "Выбери вариант",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    "Листай влево-вправо, чтобы посмотреть все.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            // Карусель вариантов
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 60.dp),
                pageSpacing = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val isCurrent = (page == currentPage)

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Image(
                        painter = painterResource(id = pet.images[page]),
                        contentDescription = colorNames[page],
                        modifier = Modifier
                            .scale(if (isCurrent) 1f else 0.8f)
                            .alpha(if (isCurrent) 1f else 0.55f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Название текущего варианта
            Text(
                colorNames[currentPage],
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.45f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(Modifier.height(10.dp))

            // Точки-индикаторы
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(colorNames.size) { idx ->
                    Box(
                        modifier = Modifier
                            .size(if (idx == currentPage) 12.dp else 8.dp)
                            .background(
                                if (idx == currentPage) Color.White
                                else Color.White.copy(alpha = 0.5f),
                                RoundedCornerShape(50)
                            )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Кнопка «Подтвердить»
            Button(
                onClick = { showConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(60.dp)
            ) {
                Text("Подтвердить", fontSize = 22.sp)
            }
        }
    }

    // Диалог подтверждения создания
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = {
                Text(
                    "Создать питомца?",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Проверь, всё ли правильно:",
                        fontSize = 14.sp, color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))

                    Image(
                        painter = painterResource(id = pet.images[currentPage]),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Имя: ${storage.petName.ifBlank { "Финни" }}",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Облик: ${pet.name}",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Вариант: ${colorNames[currentPage]}",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirm = false
                        storage.petColor = pagerState.currentPage
                        storage.isPetCreated = true
                        onDone()
                    }
                ) {
                    Text("Создать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}