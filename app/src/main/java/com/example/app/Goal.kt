package com.example.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ПЛАШКА ЦЕЛИ НА ГЛАВНОМ
@Composable
fun GoalChip(
    hasGoal: Boolean,
    saved: Int,
    price: Int,
    onClick: () -> Unit
) {
    val reached = hasGoal && saved >= price

    Surface(
        color = Color.White.copy(alpha = 0.85f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                "Цель:",
                fontSize = 11.sp,
                color = Color.Gray
            )
            Spacer(Modifier.height(2.dp))
            Text(
                if (hasGoal) "$saved / $price" else "— / —",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    !hasGoal -> Color(0xFF888888)
                    reached -> Color(0xFF4CAF50)
                    else -> Color(0xFF333333)
                }
            )
        }
    }
}

// КРУЖОК-ИНДИКАТОР ЦЕЛИ
@Composable
fun GoalStatusCircle(
    purchased: Boolean,
    reached: Boolean = false,
    size: Dp = 36.dp
) {
    val fill = when {
        purchased -> Color(0xFF4CAF50)
        reached -> Color(0xFFFFB74D)
        else -> Color.Transparent
    }
    val borderColor = when {
        purchased -> Color(0xFF4CAF50)
        reached -> Color(0xFFFF9800)
        else -> Color(0xFFBDBDBD)
    }

    Box(
        modifier = Modifier
            .size(size)
            .background(fill, shape = CircleShape)
            .border(2.dp, borderColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (purchased) {
            Text(
                "✓",
                color = Color.White,
                fontSize = (size.value * 0.55f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}