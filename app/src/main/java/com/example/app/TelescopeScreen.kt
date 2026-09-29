package com.example.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val CIRCLE_RADIUS_FRACTION = 0.32f

private const val IMAGE_SIZE_MULTIPLIER = 1.5f

@Composable
fun TelescopeScreen(onBack: () -> Unit) {

    var sizePx by remember { mutableStateOf(IntSize.Zero) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val painter = painterResource(R.drawable.telescope_view)

    BackHandler { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { sizePx = it }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()

                    val screenW = size.width.toFloat()
                    val screenH = size.height.toFloat()
                    val baseSide = minOf(screenW, screenH)
                    val scaledSide = baseSide * IMAGE_SIZE_MULTIPLIER
                    val radius = minOf(screenW, screenH) * CIRCLE_RADIUS_FRACTION

                    val maxOffset = (scaledSide / 2f - radius).coerceAtLeast(0f)

                    offsetX = (offsetX + dragAmount.x).coerceIn(-maxOffset, maxOffset)
                    offsetY = (offsetY + dragAmount.y).coerceIn(-maxOffset, maxOffset)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = IMAGE_SIZE_MULTIPLIER
                    scaleY = IMAGE_SIZE_MULTIPLIER
                    translationX = offsetX
                    translationY = offsetY
                },
            contentScale = ContentScale.Fit
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawTelescopeMask()
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.10f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .clickable { onBack() }
        ) {
            Text(
                "Выйти",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

private fun DrawScope.drawTelescopeMask() {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val radius = minOf(w, h) * CIRCLE_RADIUS_FRACTION

    val maskPath = Path().apply {
        fillType = PathFillType.EvenOdd
        addRect(Rect(0f, 0f, w, h))
        addOval(
            Rect(
                left = cx - radius,
                top = cy - radius,
                right = cx + radius,
                bottom = cy + radius
            )
        )
    }
    drawPath(maskPath, color = Color.Black)

    drawCircle(
        color = Color.Black,
        radius = radius + 140f,
        center = Offset(cx, cy),
        style = Stroke(width = 280f)
    )
    drawCircle(
        color = Color(0x553A4660),
        radius = radius + 2f,
        center = Offset(cx, cy),
        style = Stroke(width = 4f)
    )
    drawCircle(
        color = Color(0x22000000),
        radius = radius - 6f,
        center = Offset(cx, cy),
        style = Stroke(width = 14f)
    )
}