package com.example.myapplication

data class ParentMetric(
    val label: String,
    val value: String,
    val hint: String = ""
)

object ParentData {

    fun petMetrics(storage: AppStorage): List<ParentMetric> {
        val avgH = storage.avgHunger()
        val avgM = storage.avgHappiness()
        val days = storage.statsDays

        return listOf(
            ParentMetric(
                "Средняя сытость",
                "$avgH%",
                if (days == 0) "Данных пока нет" else "За $days дней"
            ),
            ParentMetric(
                "Среднее настроение",
                "$avgM%",
                if (days == 0) "Данных пока нет" else "За $days дней"
            )
        )
    }

    fun goalProgress(storage: AppStorage): List<ParentMetric> {
        val list = mutableListOf<ParentMetric>()

        list.add(
            ParentMetric(
                "Поставлено целей",
                "${storage.goalsCompleted + if (storage.goalName.isNotBlank()) 1 else 0}"
            )
        )
        list.add(
            ParentMetric(
                "Достигнуто целей",
                "${storage.goalsCompleted}"
            )
        )

        // История завершённых
        val history = storage.getCompletedGoalHistory()
        history.forEachIndexed { i, (days, name) ->
            list.add(
                ParentMetric(
                    "«$name»",
                    "$days ${daysWord(days)}"
                )
            )
        }

        // Текущая
        val hasGoal = storage.goalName.isNotBlank() && storage.goalPrice > 0
        if (hasGoal) {
            val startDay = storage.goalStartDay
            val daysInWork = if (startDay > 0) {
                storage.daysWithPet - startDay
            } else 0

            val reached = storage.piggyBalance >= storage.goalPrice

            list.add(ParentMetric("Текущая цель", "«${storage.goalName}»"))
            list.add(
                ParentMetric(
                    "Прогресс",
                    "${storage.piggyBalance} / ${storage.goalPrice} монет"
                )
            )
            list.add(
                ParentMetric(
                    if (reached) "Готова к покупке" else "В работе",
                    "$daysInWork ${daysWord(daysInWork)}"
                )
            )
        } else if (history.isEmpty()) {
            list.add(ParentMetric("Текущая цель", "Ещё не поставлена"))
        }

        return list
    }

    fun taskMetrics(storage: AppStorage): List<ParentMetric> {
        val total = storage.getCompletedTasksCount()
        val correct = storage.tasksCorrect

        return listOf(
            ParentMetric(
                "Всего заданий пройдено",
                "$total"
            ),
            ParentMetric(
                "Решено правильно",
                "$correct" + if (total > 0) " из $total" else ""
            ),
            ParentMetric(
                "Процент правильных",
                if (total == 0) "—" else "${correct * 100 / total}%"
            )
        )
    }

    fun overallProgress(storage: AppStorage): List<ParentMetric> = listOf(
        ParentMetric("Дней с питомцем", "${storage.daysWithPet}"),
        ParentMetric("Уровень питомца", "${storage.petLevel}"),
        ParentMetric("Стадия", storage.petStage().displayName),
        ParentMetric("Звёзды", "${storage.stars} / ${storage.starsToNextLevel()}")
    )

    private fun daysWord(n: Int): String = when {
        n % 10 == 1 && n % 100 != 11 -> "день"
        n % 10 in 2..4 && n % 100 !in 12..14 -> "дня"
        else -> "дней"
    }
}