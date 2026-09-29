package com.example.app

import kotlin.random.Random

object EventEngine {

    fun pickForToday(storage: AppStorage, day: Int): Event? {
        val rules = EventData.rules
        val maxDemoDay = rules.demoDays.maxOrNull() ?: 0

        // Демо-дни
        if (day <= maxDemoDay) {
            return EventData.events.firstOrNull { it.demoDay == day }
        }

        // Обычный режим
        val daysSinceDemo = day - maxDemoDay
        if (daysSinceDemo % rules.everyNDays != 0) return null
        if (storage.lastEventDay == day) return null

        // События без демо-дня, прошедшие кулдаун и не исчерпавшие лимит
        val candidates = EventData.events.filter { ev ->
            ev.demoDay == null &&
                    (ev.repeatable || storage.getEventCount(ev.id) == 0) &&
                    (ev.maxRepeats < 0 || storage.getEventCount(ev.id) < ev.maxRepeats) &&
                    (day - storage.getEventLastDay(ev.id)) >= ev.cooldownDays
        }

        if (candidates.isEmpty()) return null

        val totalWeight = candidates.sumOf { it.weight }
        var roll = Random.nextInt(totalWeight)
        for (ev in candidates) {
            roll -= ev.weight
            if (roll < 0) return ev
        }
        return candidates.last()
    }

    fun canAfford(storage: AppStorage, requires: Map<String, Int>): Boolean {
        for ((key, value) in requires) {
            val have = when (key) {
                "balance" -> storage.coins
                "savings" -> storage.piggyBalance
                "mood"    -> storage.happiness
                else -> return false
            }
            if (have < value) return false
        }
        return true
    }

    fun fillTemplate(template: String, storage: AppStorage): String = template
        .replace("{petNom}", declinePetName(storage.petName, GramCase.NOMINATIVE))
        .replace("{petDat}", declinePetName(storage.petName, GramCase.DATIVE))
        .replace("{petGen}", declinePetName(storage.petName, GramCase.GENITIVE))
        .replace("{petAcc}", declinePetName(storage.petName, GramCase.ACCUSATIVE))
        .replace("{petIns}", declinePetName(storage.petName, GramCase.INSTRUMENTAL))
        .replace("{petPrp}", aboutPet(storage.petName))

    fun applyEffects(storage: AppStorage, effects: EventEffect) {
        if (effects.balance != 0) {
            storage.coins = (storage.coins + effects.balance).coerceAtLeast(0)
        }
        if (effects.savings != 0) {
            storage.piggyBalance = (storage.piggyBalance + effects.savings).coerceAtLeast(0)
        }
        if (effects.mood != 0) {
            storage.happiness = (storage.happiness + effects.mood).coerceIn(0, 100)
        }
        if (effects.priceOverride.isNotEmpty() && effects.durationDays > 0) {
            storage.setPriceOverrides(effects.priceOverride, effects.durationDays)
        }
    }
}