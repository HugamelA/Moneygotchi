package com.example.app

import android.content.Context
import org.json.JSONObject

// Стадия развития питомца
data class StageConfig(
    val id: String,
    val name: String,
    val fromLevel: Int
)

// Условие для звёзд
data class StarRule(
    val id: String,
    val text: String,
    val condition: String,
    val stars: Int
)

object Economy {

    private var loaded = false

    // Стартовые параметры
    var startCoins: Int = 40
        private set
    var startSatiety: Int = 80
        private set
    var startMood: Int = 80
        private set
    var maxStat: Int = 100
        private set

    // Доход за сон
    var sleepBase: Int = 15
        private set
    var sleepBonusFed: Int = 10
        private set
    var sleepBonusHappy: Int = 10
        private set
    var careThreshold: Int = 50
        private set
    var sleepMax: Int = 35
        private set

    // Уменьшение статусов за сон
    var decaySatiety: Int = 12
        private set
    var decayMood: Int = 15
        private set

    // Влияние голода на настроение
    var hungerDecayK: Float = 0.5f
        private set
    var hungerGainK: Float = 0.15f
        private set

    // Звёзды
    var goalBonus: Int = 3
        private set
    var starsPerDay: List<StarRule> = emptyList()
        private set
    var starsMaxPerDay: Int = 3
        private set

    // Уровни
    var levelThresholds: List<Int> = listOf(0, 2, 5, 9, 14, 20, 27, 35, 44, 54)
        private set
    var levelStepAfterLast: Int = 20
        private set
    var stages: List<StageConfig> = emptyList()
        private set

    // Правила заданий
    var paidOnce: Boolean = true
        private set
    var moodForCompletion: Int = 10
        private set
    var tasksGiveStars: Boolean = false
        private set

    fun load(context: Context) {
        if (loaded) return
        try {
            val json = context.assets.open("economy.json")
                .bufferedReader().use { it.readText() }
            val root = JSONObject(json)

            root.optJSONObject("start")?.let { s ->
                startCoins = s.optInt("coins", startCoins)
                startSatiety = s.optInt("satiety", startSatiety)
                startMood = s.optInt("mood", startMood)
                maxStat = s.optInt("maxStat", maxStat)
            }

            root.optJSONObject("day")?.let { d ->
                d.optJSONObject("sleepIncome")?.let { inc ->
                    sleepBase = inc.optInt("base", sleepBase)
                    sleepBonusFed = inc.optInt("bonusFed", sleepBonusFed)
                    sleepBonusHappy = inc.optInt("bonusHappy", sleepBonusHappy)
                    careThreshold = inc.optInt("careThreshold", careThreshold)
                    sleepMax = inc.optInt("max", sleepMax)
                }
                d.optJSONObject("decay")?.let { dec ->
                    decaySatiety = dec.optInt("satiety", decaySatiety)
                    decayMood = dec.optInt("mood", decayMood)
                }
                d.optJSONObject("hungerAffectsMood")?.let { ham ->
                    hungerDecayK = ham.optDouble("decayK", hungerDecayK.toDouble()).toFloat()
                    hungerGainK = ham.optDouble("gainK", hungerGainK.toDouble()).toFloat()
                }
            }

            root.optJSONObject("stars")?.let { s ->
                goalBonus = s.optInt("goalBonus", goalBonus)
                starsMaxPerDay = s.optInt("maxPerDay", starsMaxPerDay)
                s.optJSONArray("perDay")?.let { arr ->
                    val list = mutableListOf<StarRule>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        list.add(
                            StarRule(
                                id = o.optString("id", ""),
                                text = o.optString("text", ""),
                                condition = o.optString("condition", ""),
                                stars = o.optInt("stars", 1)
                            )
                        )
                    }
                    starsPerDay = list
                }
            }

            root.optJSONObject("levels")?.let { l ->
                l.optJSONArray("thresholds")?.let { arr ->
                    val list = mutableListOf<Int>()
                    for (i in 0 until arr.length()) list.add(arr.getInt(i))
                    if (list.isNotEmpty()) levelThresholds = list
                }
                levelStepAfterLast = l.optInt("stepAfterLast", levelStepAfterLast)
                l.optJSONArray("stages")?.let { arr ->
                    val list = mutableListOf<StageConfig>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        list.add(
                            StageConfig(
                                id = o.optString("id", ""),
                                name = o.optString("name", ""),
                                fromLevel = o.optInt("fromLevel", 1)
                            )
                        )
                    }
                    stages = list
                }
            }

            root.optJSONObject("tasksRules")?.let { t ->
                paidOnce = t.optBoolean("paidOnce", paidOnce)
                moodForCompletion = t.optInt("moodForCompletion", moodForCompletion)
                tasksGiveStars = t.optBoolean("givesStars", tasksGiveStars)
            }

            loaded = true
        } catch (e: Exception) {
        }
    }
}