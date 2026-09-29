package com.example.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun EventDialog(
    event: Event,
    storage: AppStorage,
    onFinish: () -> Unit
) {
    var chosenOption by remember(event.id) { mutableStateOf<EventOption?>(null) }
    var resolved by remember(event.id) { mutableStateOf(false) }

    val title = event.title
    val text = EventEngine.fillTemplate(event.text, storage)
    val petReply = EventEngine.fillTemplate(event.petReply, storage)

    val optionsAvailable = event.options?.filter {
        EventEngine.canAfford(storage, it.requires)
    } ?: emptyList()

    val showFallback = event.options != null &&
            optionsAvailable.isEmpty() &&
            event.fallback != null

    Dialog(onDismissRequest = {  }) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Заголовок
                Text(
                    title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(Modifier.height(12.dp))

                // Текст события
                Text(
                    text,
                    fontSize = 16.sp,
                    color = Color(0xFF333333),
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    petReply,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    fontStyle = FontStyle.Italic,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(16.dp))

                when {
                    showFallback -> {
                        Text(
                            event.fallback!!.petReply,
                            fontSize = 14.sp,
                            color = Color(0xFF888888)
                        )
                    }

                    event.options != null && !resolved -> {
                        event.options.forEach { opt ->
                            val enabled = EventEngine.canAfford(storage, opt.requires)
                            EventOptionButton(
                                text = opt.text,
                                enabled = enabled,
                                verdict = opt.verdict,
                                onClick = {
                                    chosenOption = opt
                                    resolved = true
                                }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    resolved && chosenOption != null -> {
                        val reply = chosenOption!!.petReply
                            ?.let { EventEngine.fillTemplate(it, storage) }
                            ?: petReply
                        Text(
                            reply,
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            fontStyle = FontStyle.Italic
                        )
                        val exp = chosenOption!!.explanation
                        if (!exp.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                exp,
                                fontSize = 13.sp,
                                color = Color(0xFF888888),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                when {
                    showFallback -> {
                        EventActionButton("Хорошо") {
                            onFinish()
                        }
                    }

                    event.options != null && resolved -> {
                        EventActionButton("Продолжить") {
                            chosenOption?.let {
                                EventEngine.applyEffects(storage, it.effects)
                            }
                            onFinish()
                        }
                    }

                    event.options == null -> {
                        EventActionButton("Хорошо") {
                            EventEngine.applyEffects(storage, event.effects)
                            onFinish()
                        }
                    }

                    else -> { }
                }
            }
        }
    }
}

@Composable
private fun EventOptionButton(
    text: String,
    enabled: Boolean,
    verdict: String?,
    onClick: () -> Unit
) {
    val bg = when (verdict) {
        "recommended" -> Color(0xFF81C784)
        "acceptable"  -> Color(0xFF64B5F6)
        else          -> Color(0xFFBDBDBD)
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (enabled) bg else bg.copy(alpha = 0.35f),
        shadowElevation = if (enabled) 2.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp)
        ) {
            Text(
                text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun EventActionButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}