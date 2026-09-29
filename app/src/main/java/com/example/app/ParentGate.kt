package com.example.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ParentGate(
    onPass: () -> Unit,
    onDismiss: () -> Unit
) {
    val question = remember { ParentQuiz.random() }

    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    fun submit() {
        if (question.check(input)) {
            onPass()
        } else {
            error = true
            input = ""
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Раздел для родителей") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Ответьте на вопрос, чтобы продолжить:",
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    question.question,
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic
                )

                if (question.imageRes != null) {
                    Spacer(Modifier.height(12.dp))
                    Image(
                        painter = painterResource(question.imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = it
                        error = false
                    },
                    isError = error,
                    singleLine = true,
                    placeholder = { Text("Ответ") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { submit() }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Неверно, попробуйте снова",
                        color = Color(0xFFB00020),
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { submit() }) {
                Text("Продолжить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}