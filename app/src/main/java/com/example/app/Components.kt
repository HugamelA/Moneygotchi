package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue

@Composable
fun LevelCircle(
    level: Int,
    progress: Float = 0f,
    onPressChange: (Boolean) -> Unit = {}
) {
    val sizeDp = 72.dp
    val strokeDp = 6.dp
    val trackColor = Color(0x22000000)
    val progressColor = Color(0xFF64B5F6)

    Box(
        modifier = Modifier
            .size(sizeDp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPressChange(true)
                        tryAwaitRelease()
                        onPressChange(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeDp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)

            // Дорожка
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Заполнение
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        // Внутренний белый кружок
        Box(
            modifier = Modifier
                .size(sizeDp - strokeDp * 3)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ур.", fontSize = 10.sp, color = Color.Gray)
                Text(
                    "$level",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222)
                )
            }
        }
    }
}

@Composable
fun StatBar(
    iconRes: Int,
    value: Int,
    barColorFor: (Int) -> Color,
    delta: Int? = null,
    onPressChange: (Boolean) -> Unit = {}
) {
    val barColor by animateColorAsState(
        targetValue = barColorFor(value),
        animationSpec = tween(durationMillis = 400),
        label = "barColor"
    )

    Box(
        modifier = Modifier
            .height(44.dp)
            .width(220.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPressChange(true)
                        tryAwaitRelease()
                        onPressChange(false)
                    }
                )
            }
    ) {
        // Белая полоска — сзади, начинается из-под кружка
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 20.dp)
                .height(12.dp)
                .width(120.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(
                        topEnd = 11.dp,
                        bottomEnd = 11.dp
                    )
                )
                .padding(start = 15.dp, top = 3.dp, end = 4.dp, bottom = 3.dp)
        ) {
            // Цветная шкала
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((value / 100f).coerceIn(0f, 1f))
                    .background(
                        color = barColor,
                        shape = RoundedCornerShape(10.dp)
                    )
            )
        }

        // Кружок с иконкой
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = -5.dp)
                .offset(y = -2.dp)
                .size(40.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (delta != null && delta > 0) {
                Text(
                    "+$delta",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            } else {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(35.dp),
                    tint = Color.Unspecified
                )
            }
        }
    }
}

// КОМПАКТНЫЙ ИНДИКАТОР

@Composable
fun SmallInfoChip(
    text: String,
    iconRes: Int? = null
) {
    val hasLeading = iconRes != null
    val startPadding = if (iconRes != null) 8.dp else 16.dp
    Row(
        modifier = Modifier
            .background(Color.White.copy(alpha = 1f), RoundedCornerShape(16.dp))
            .padding(
                start = startPadding,
                end = 16.dp,
                top = 10.dp,
                bottom = 10.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
    }
}