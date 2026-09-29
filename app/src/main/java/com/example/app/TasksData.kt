package com.example.app

import android.content.Context
import org.json.JSONObject

// МОДЕЛИ
data class TaskOption(
    val id: String,
    val text: String,
    val verdict: String,
    val petReply: String,
    val explanation: String
)

data class Task(
    val id: String,
    val order: Int,
    val title: String,
    val topic: String,
    val difficulty: String,
    val skill: String,
    val reward: Int,
    val situation: String,
    val hint: String,
    val options: List<TaskOption>,
    val unlockDay: Int = 1
)

// ЗАГРУЗКА ИЗ assets
fun loadTasksFromAssets(context: Context): List<Task> {
    return try {
        val json = context.assets.open("tasks.json")
            .bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val arr = root.getJSONArray("tasks")
        val result = mutableListOf<Task>()

        for (i in 0 until arr.length()) {
            val t = arr.getJSONObject(i)
            val optsArr = t.getJSONArray("options")
            val opts = mutableListOf<TaskOption>()
            for (j in 0 until optsArr.length()) {
                val o = optsArr.getJSONObject(j)
                opts.add(
                    TaskOption(
                        id = o.optString("id", ""),
                        text = o.optString("text", ""),
                        verdict = o.optString("verdict", ""),
                        petReply = o.optString("petReply", ""),
                        explanation = o.optString("explanation", "")
                    )
                )
            }
            result.add(
                Task(
                    id = t.optString("id", "T$i"),
                    order = t.optInt("order", i + 1),
                    title = t.optString("title", "Задание"),
                    topic = t.optString("topic", ""),
                    difficulty = t.optString("difficulty", ""),
                    skill = t.optString("skill", ""),
                    reward = t.optInt("reward", 0),
                    situation = t.optString("situation", ""),
                    hint = t.optString("hint", ""),
                    options = opts,
                    unlockDay = t.optInt("unlockDay", 1)
                )
            )
        }
        result.sortedBy { it.order }
    } catch (e: Exception) {
        emptyList()
    }
}