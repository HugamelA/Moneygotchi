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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only

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

        //Фон магазина
        Image(
            painter = painterResource(id = R.drawable.shop_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        //Карусель
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val isCurrent = (page == currentPage)
            Image(
                painter = painterResource(id = petOptions[page].images[0]),
                contentDescription = petOptions[page].name,
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = -30.dp)
                    .alpha(if (isCurrent) 1f else 0.55f),
                contentScale = ContentScale.FillBounds
            )
        }

        //Интерфейс (Панель, кнопки)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                    )
                )
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Верхняя панель
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
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
                        val cyr = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя"
                        val lat = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
                        val hasCyr = input.any { it in cyr }
                        val hasLat = input.any { it in lat }
                        val filtered = when {
                            hasCyr && hasLat -> {
                                val firstLetter = input.firstOrNull { it.isLetter() }
                                if (firstLetter != null && firstLetter in cyr) input.filter { it in cyr }
                                else input.filter { it in lat }
                            }
                            else -> input
                        }
                        name = filtered.filter { it.isLetter() }.take(12)
                    },
                    label = { Text("Имя питомца") },
                    supportingText = {
                        if (name.isNotEmpty() && name.trim().length < 2) {
                            Text("Не короче 2 букв", color = Color(0xFFE53935))
                        } else {
                            Text("${name.length} / 12")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(Modifier.weight(1f))

            // Название текущего питомца
            Text(
                petOptions[currentPage].name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
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
                                if (idx == currentPage) Color.White else Color.White.copy(alpha = 0.5f),
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
                    SoundManager.playSfx(R.raw.sfx_ui_tap)
                    storage.petName = trimmedName
                    storage.petAppearance = pagerState.currentPage
                    onDone()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A9DB8)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .height(60.dp)
            ) {
                Text("Далее", fontSize = 22.sp)
            }
        }
    }
}