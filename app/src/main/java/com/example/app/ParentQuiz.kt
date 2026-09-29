package com.example.app

import kotlin.random.Random
import androidx.annotation.DrawableRes

data class ParentQuestion(
    val question: String,
    val acceptedAnswers: List<String>,
    @DrawableRes val imageRes: Int? = null
) {
    fun check(input: String): Boolean {
        val cleaned = input.trim().lowercase().replace("ё", "е")
        return acceptedAnswers.any {
            it.trim().lowercase().replace("ё", "е") == cleaned
        }
    }
}

object ParentQuiz {

    private val questions = listOf(
        ParentQuestion(
            "Как называется график функции y = x²?",
            listOf("парабола", "Парабола"),
            imageRes = R.drawable.quiz_parabola
        ),
        ParentQuestion(
            "Как называется график функции y = kx + b?",
            listOf("прямая", "прямая линия", "Прямая", "Прямая линия"),
            imageRes = R.drawable.quiz_line
        ),
        ParentQuestion(
            "Чему равно число π с точностью до сотых?",
            listOf("3.14", "3,14"),
            imageRes = R.drawable.quiz_pi
        ),
        ParentQuestion(
            "Как называется теорема: a² + b² = c²?",
            listOf("пифагора", "теорема пифагора", "Пифагора", "Теорема Пифагора", "теорема Пифагора"),
            imageRes = R.drawable.quiz_pythagoras
        ),
        ParentQuestion(
            "Какая химическая формула воды?",
            listOf("h2o", "h₂o", "н2о", "H2O"),
            imageRes = R.drawable.quiz_water
        ),
        ParentQuestion(
            "Как называется химический элемент с символом O?",
            listOf("кислород", "Кислород"),
            imageRes = R.drawable.quiz_oxygen
        ),
        ParentQuestion(
            "Столица Франции?",
            listOf("париж", "Париж"),
            imageRes = R.drawable.quiz_paris
        ),
        ParentQuestion(
            "Кто написал роман «Война и мир»?",
            listOf("толстой", "лев толстой", "л. толстой", "л.н. толстой", "Толстой", "Лев Толстой", "Л. Толстой", "Л.Н. Толстой"),
            imageRes = R.drawable.quiz_war_and_peace
        ),
        ParentQuestion(
            "Кто написал «Евгения Онегина»?",
            listOf("пушкин", "а.с. пушкин", "александр пушкин", "Пушкин", "Александр Пушкин", "А.С. Пушкин"),
            imageRes = R.drawable.quiz_onegin
        )
    )

    fun random(): ParentQuestion = questions[Random.nextInt(questions.size)]
}