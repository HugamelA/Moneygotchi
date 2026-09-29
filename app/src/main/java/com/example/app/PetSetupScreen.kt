package com.example.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect

// ЭКРАН СОЗДАНИя ПИТОМЦА
@Composable
fun PetSetupScreen(storage: AppStorage, onDone: () -> Unit) {
    var name by remember { mutableStateOf(storage.petName) }
    val safeInitial = storage.petAppearance.coerceIn(0, petOptions.size - 1)

    val pagerState = rememberPagerState(
        initialPage = safeInitial,
        pageCount = { petOptions.size }
    )
    val currentPage = pagerState.currentPage

    LaunchedEffect(Unit) {
        val target = storage.petAppearance.coerceIn(0, petOptions.size - 1)
        if (pagerState.currentPage != target) {
            pagerState.scrollToPage(target)
        }
    }

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
            // Верхняя панель: заголовок + имя
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
                Text(
                    "Выбери питомца",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isLetter() }.take(12)
                        name = filtered
                    },
                    label = { Text("Имя питомца") },
                    supportingText = {
                        if (name.isNotEmpty() && name.trim().length < 2) {
                            Text(
                                "Не короче 2 букв",
                                color = Color(0xFFE53935)
                            )
                        } else {
                            Text("${name.length} / 12")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Карусель питомцев
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
                        painter = painterResource(id = petOptions[page].images[0]),
                        contentDescription = petOptions[page].name,
                        modifier = Modifier
                            .scale(if (isCurrent) 1f else 0.8f)
                            .alpha(if (isCurrent) 1f else 0.55f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Название текущего питомца
            Text(
                petOptions[currentPage].name,
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
                repeat(petOptions.size) { idx ->
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

            // Кнопка «Далее»
            val trimmedName = name.trim()
            val isNameValid = trimmedName.length >= 2

            Button(
                enabled = isNameValid,
                onClick = {
                    storage.petName = trimmedName
                    storage.petAppearance = pagerState.currentPage
                    onDone()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(60.dp)
            ) {
                Text("Далее", fontSize = 22.sp)
            }
        }
    }
}