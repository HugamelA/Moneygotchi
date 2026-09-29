package com.example.app

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf

// ХРАНИЛИЩЕ
class AppStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("my_pet_prefs", Context.MODE_PRIVATE)

    //  Служебные методы записи в prefs
    private fun saveInt(key: String, v: Int) = prefs.edit().putInt(key, v).apply()
    private fun saveBool(key: String, v: Boolean) = prefs.edit().putBoolean(key, v).apply()
    private fun saveString(key: String, v: String) = prefs.edit().putString(key, v).apply()
    private fun saveStringSet(key: String, v: Set<String>) =
        prefs.edit().putStringSet(key, v).apply()

    //  БАЗОВЫЕ ПОЛЯ

    private val _isPetCreated = mutableStateOf(prefs.getBoolean("pet_created", false))
    var isPetCreated: Boolean
        get() = _isPetCreated.value
        set(v) { _isPetCreated.value = v; saveBool("pet_created", v) }

    private val _petName = mutableStateOf(prefs.getString("pet_name", "") ?: "")
    var petName: String
        get() = _petName.value
        set(v) { _petName.value = v; saveString("pet_name", v) }

    private val _petAppearance = mutableStateOf(prefs.getInt("pet_appearance", 0))
    var petAppearance: Int
        get() = _petAppearance.value
        set(v) { _petAppearance.value = v; saveInt("pet_appearance", v) }

    private val _petColor = mutableStateOf(prefs.getInt("pet_color", 0))
    var petColor: Int
        get() = _petColor.value
        set(v) { _petColor.value = v; saveInt("pet_color", v) }

    private val _petLevel = mutableStateOf(prefs.getInt("pet_level", 1))
    var petLevel: Int
        get() = _petLevel.value
        set(v) { _petLevel.value = v; saveInt("pet_level", v) }

    private val _hunger = mutableStateOf(prefs.getInt("hunger", 80))
    var hunger: Int
        get() = _hunger.value
        set(v) { _hunger.value = v; saveInt("hunger", v) }

    private val _happiness = mutableStateOf(prefs.getInt("happiness", 80))
    var happiness: Int
        get() = _happiness.value
        set(v) { _happiness.value = v; saveInt("happiness", v) }

    private val _coins = mutableStateOf(prefs.getInt("coins", 0))
    var coins: Int
        get() = _coins.value
        set(v) { _coins.value = v; saveInt("coins", v) }

    private val _daysWithPet = mutableStateOf(prefs.getInt("days_with_pet", 1))
    var daysWithPet: Int
        get() = _daysWithPet.value
        set(v) { _daysWithPet.value = v; saveInt("days_with_pet", v) }

    private val _tutorialShown = mutableStateOf(prefs.getBoolean("tutorial_shown", false))
    var tutorialShown: Boolean
        get() = _tutorialShown.value
        set(v) { _tutorialShown.value = v; saveBool("tutorial_shown", v) }

    //  ЗАДАНИЯ
    private val _completedTasks = mutableStateOf(
        prefs.getStringSet("completed_tasks", emptySet())?.toSet() ?: emptySet()
    )

    // Индикаторы «нового» на главном экране
    private val _seenZonesRaw = mutableStateOf(
        prefs.getStringSet("seen_zones", emptySet())?.toSet() ?: emptySet()
    )

    fun hasSeenZone(name: String): Boolean = name in _seenZonesRaw.value

    fun markZoneSeen(name: String) {
        if (name in _seenZonesRaw.value) return
        val next = _seenZonesRaw.value + name
        _seenZonesRaw.value = next
        saveStringSet("seen_zones", next)
    }

    fun resetSeenZones() {
        _seenZonesRaw.value = emptySet()
        saveStringSet("seen_zones", emptySet())
    }

    private val _lastSeenTaskUnlockDay = mutableStateOf(
        prefs.getInt("last_seen_task_unlock_day", 0)
    )
    var lastSeenTaskUnlockDay: Int
        get() = _lastSeenTaskUnlockDay.value
        set(v) {
            _lastSeenTaskUnlockDay.value = v
            saveInt("last_seen_task_unlock_day", v)
        }

    fun isTaskCompleted(taskId: String): Boolean = taskId in _completedTasks.value

    fun markTaskCompleted(taskId: String) {
        val next = _completedTasks.value + taskId
        _completedTasks.value = next
        saveStringSet("completed_tasks", next)
    }

    fun resetCompletedTasks() {
        _completedTasks.value = emptySet()
        saveStringSet("completed_tasks", emptySet())
        _chosenOptionsRaw.value = emptySet()
        saveStringSet("chosen_options", emptySet())
        correctTasksToday = 0
    }

    fun getCompletedTasksCount(): Int = _completedTasks.value.size

    //  КОНВЕРТЫ БЮДЖЕТА
    private val _coinsNeeds = mutableStateOf(prefs.getInt("coins_needs", 0))
    var coinsNeeds: Int
        get() = _coinsNeeds.value
        set(v) { _coinsNeeds.value = v; saveInt("coins_needs", v) }

    private val _coinsWants = mutableStateOf(prefs.getInt("coins_wants", 0))
    var coinsWants: Int
        get() = _coinsWants.value
        set(v) { _coinsWants.value = v; saveInt("coins_wants", v) }

    //  КОПИЛКА
    private val _piggyBalance = mutableStateOf(prefs.getInt("piggy_balance", 0))
    var piggyBalance: Int
        get() = _piggyBalance.value
        set(v) { _piggyBalance.value = v; saveInt("piggy_balance", v) }

    //  ЦЕЛЬ
    private val _goalName = mutableStateOf(prefs.getString("goal_name", "") ?: "")
    var goalName: String
        get() = _goalName.value
        set(v) { _goalName.value = v; saveString("goal_name", v) }

    private val _goalPrice = mutableStateOf(prefs.getInt("goal_price", 0))
    var goalPrice: Int
        get() = _goalPrice.value
        set(v) { _goalPrice.value = v; saveInt("goal_price", v) }

    private val _goalPurchased = mutableStateOf(prefs.getBoolean("goal_purchased", false))
    var goalPurchased: Boolean
        get() = _goalPurchased.value
        set(v) { _goalPurchased.value = v; saveBool("goal_purchased", v) }

    fun clearGoal() {
        goalName = ""
        goalPrice = 0
        goalPurchased = false
    }

    //  ВЫБРАННЫЕ ОТВЕТЫ В ЗАДАНИЯХ
    private val _chosenOptionsRaw = mutableStateOf(
        prefs.getStringSet("chosen_options", emptySet())?.toSet() ?: emptySet()
    )

    fun getChosenOption(taskId: String): String? =
        _chosenOptionsRaw.value.firstOrNull { it.startsWith("$taskId=") }
            ?.substringAfter("=")

    fun setChosenOption(taskId: String, optionId: String) {
        val filtered = _chosenOptionsRaw.value
            .filter { !it.startsWith("$taskId=") }
            .toMutableSet()
        filtered.add("$taskId=$optionId")
        _chosenOptionsRaw.value = filtered
        saveStringSet("chosen_options", filtered)
    }

    //  ОБУЧЕНИЕ
    private val _tutorialStep = mutableStateOf(prefs.getInt("tutorial_step", 0))
    var tutorialStep: Int
        get() = _tutorialStep.value
        set(v) { _tutorialStep.value = v; saveInt("tutorial_step", v) }

    private val _tutorialActive = mutableStateOf(prefs.getBoolean("tutorial_active", true))
    var tutorialActive: Boolean
        get() = _tutorialActive.value
        set(v) { _tutorialActive.value = v; saveBool("tutorial_active", v) }

    //  ЗВЁЗДЫ И УРОВНИ
    private val _stars = mutableStateOf(prefs.getInt("stars", 0))
    var stars: Int
        get() = _stars.value
        set(v) { _stars.value = v; saveInt("stars", v) }

    fun starsToNextLevel(): Int {
        val thresholds = Economy.levelThresholds
        val lvl = petLevel
        return if (lvl < thresholds.size) {
            thresholds[lvl]
        } else {
            thresholds.last() + (lvl - (thresholds.size - 1)) * Economy.levelStepAfterLast
        }
    }

    fun addStars(count: Int) {
        if (count <= 0) return
        stars += count
        while (stars >= starsToNextLevel()) {
            stars -= starsToNextLevel()
            petLevel += 1
        }
    }

    fun starsProgress(): Float {
        val need = starsToNextLevel().coerceAtLeast(1)
        return (stars.toFloat() / need).coerceIn(0f, 1f)
    }

    private val sortedStages = Economy.stages.sortedByDescending { it.fromLevel }

    fun petStage(): PetStage {
        val lvl = petLevel
        val match = sortedStages.firstOrNull { lvl >= it.fromLevel }
        return when (match?.id) {
            "teen"  -> PetStage.TEEN
            "adult" -> PetStage.ADULT
            else    -> PetStage.BABY
        }
    }

    //  ДНЕВНЫЕ ФЛАГИ
    private val _budgetSpreadToday = mutableStateOf(prefs.getBoolean("budget_spread_today", false))
    var budgetSpreadToday: Boolean
        get() = _budgetSpreadToday.value
        set(v) { _budgetSpreadToday.value = v; saveBool("budget_spread_today", v) }

    private val _piggyAddedToday = mutableStateOf(prefs.getBoolean("piggy_added_today", false))
    var piggyAddedToday: Boolean
        get() = _piggyAddedToday.value
        set(v) { _piggyAddedToday.value = v; saveBool("piggy_added_today", v) }

    private val _correctTasksToday = mutableStateOf(prefs.getInt("correct_tasks_today", 0))
    var correctTasksToday: Int
        get() = _correctTasksToday.value
        set(v) { _correctTasksToday.value = v; saveInt("correct_tasks_today", v) }

    fun resetDailyFlags() {
        budgetSpreadToday = false
        piggyAddedToday = false
        correctTasksToday = 0
    }

    //  КОРЗИНА
    private val _cartRaw = mutableStateOf(
        prefs.getStringSet("shop_cart", emptySet())?.toSet() ?: emptySet()
    )

    fun getCart(): Map<String, Int> =
        _cartRaw.value.mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val qty = parts[1].toIntOrNull() ?: 0
                if (qty > 0) parts[0] to qty else null
            } else null
        }.toMap()

    fun getCartQuantity(productId: String): Int =
        getCart()[productId] ?: 0

    fun setCartQuantity(productId: String, quantity: Int) {
        val current = _cartRaw.value.toMutableSet()
        current.removeAll { it.startsWith("$productId:") }
        if (quantity > 0) {
            current.add("$productId:$quantity")
        }
        _cartRaw.value = current
        saveStringSet("shop_cart", current)
    }

    fun clearCart() {
        _cartRaw.value = emptySet()
        saveStringSet("shop_cart", emptySet())
    }

    //  ИНВЕНТАРЬ
    private val _inventoryRaw = mutableStateOf(
        prefs.getStringSet("inventory", emptySet())?.toSet() ?: emptySet()
    )

    private val _inventoryCache = mutableStateOf(parseInventory(_inventoryRaw.value))

    private fun parseInventory(raw: Set<String>): Map<String, Int> =
        raw.mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val qty = parts[1].toIntOrNull() ?: 0
                if (qty > 0) parts[0] to qty else null
            } else null
        }.toMap()

    fun getInventory(): Map<String, Int> = _inventoryCache.value

    fun getInventoryQuantity(productId: String): Int =
        _inventoryCache.value[productId] ?: 0

    fun addToInventory(productId: String, quantity: Int) {
        if (quantity <= 0) return
        val current = _inventoryCache.value[productId] ?: 0
        setInventoryQuantity(productId, current + quantity)
    }

    fun setInventoryQuantity(productId: String, quantity: Int) {
        val newMap = _inventoryCache.value.toMutableMap()
        if (quantity > 0) {
            newMap[productId] = quantity
        } else {
            newMap.remove(productId)
        }
        _inventoryCache.value = newMap
        val raw = newMap.map { "${it.key}:${it.value}" }.toSet()
        _inventoryRaw.value = raw
        saveStringSet("inventory", raw)
    }

    fun consumeFromInventory(productId: String): Boolean {
        val current = _inventoryCache.value[productId] ?: 0
        if (current <= 0) return false
        setInventoryQuantity(productId, current - 1)
        return true
    }

    fun clearInventory() {
        _inventoryCache.value = emptyMap()
        _inventoryRaw.value = emptySet()
        saveStringSet("inventory", emptySet())
    }

    //  ЛЕЖАНКА
    private val _bedOwned = mutableStateOf(prefs.getBoolean("bed_owned", false))
    var bedOwned: Boolean
        get() = _bedOwned.value
        set(v) { _bedOwned.value = v; saveBool("bed_owned", v) }

    //  ИГРОВАЯ ЛОГИКА (без изменений)

    fun applyDailyDecay(): Pair<Int, Int> {
        val hungerBefore = hunger
        val happinessBefore = happiness

        val threshold = Economy.careThreshold
        val decayK = Economy.hungerDecayK
        val factor = if (hungerBefore >= threshold) {
            1f
        } else {
            val k = (threshold - hungerBefore).toFloat() / threshold
            1f + k * decayK
        }

        val bedModifier = if (bedOwned) bedDecayMultiplier() else 1f

        val happinessLoss = (Economy.decayMood * factor * bedModifier)
            .toInt().coerceAtLeast(1)

        hunger = (hunger - Economy.decaySatiety).coerceAtLeast(0)
        happiness = (happiness - happinessLoss).coerceAtLeast(0)

        return (hungerBefore - hunger) to (happinessBefore - happiness)
    }

    fun gainHappinessWithPenalty(amount: Int): Int {
        if (amount <= 0) return 0

        val threshold = Economy.careThreshold
        val gainK = Economy.hungerGainK
        val penalty = if (hunger >= threshold) {
            1f
        } else {
            val k = (threshold - hunger).toFloat() / threshold
            1f - k * gainK
        }
        val bedBonus = if (bedOwned) bedGainMultiplier() else 1f

        val actual = (amount * penalty * bedBonus).toInt().coerceAtLeast(1)
        val before = happiness
        happiness = (happiness + actual).coerceAtMost(100)
        return happiness - before
    }

    fun applyPurchaseEffects(hungerBonus: Int, happinessBonus: Int): Pair<Int, Int> {
        val hungerBefore = hunger
        val happinessBefore = happiness

        if (hungerBonus > 0) {
            hunger = (hunger + hungerBonus).coerceAtMost(100)
        }
        if (happinessBonus > 0) {
            gainHappinessWithPenalty(happinessBonus)
        }

        return (hunger - hungerBefore) to (happiness - happinessBefore)
    }

    private fun bedDecayMultiplier(): Float {
        val perk = ShopCatalog.productById("special_bed")?.perk
        return perk?.moodDecayMult ?: 1f
    }

    private fun bedGainMultiplier(): Float {
        val perk = ShopCatalog.productById("special_bed")?.perk
        return perk?.moodGainMult ?: 1f
    }

    //  СОБЫТИЯ
    private val _lastEventDay = mutableStateOf(prefs.getInt("last_event_day", 0))
    var lastEventDay: Int
        get() = _lastEventDay.value
        set(v) { _lastEventDay.value = v; saveInt("last_event_day", v) }

    private val _pendingEventId = mutableStateOf(prefs.getString("pending_event_id", "") ?: "")
    var pendingEventId: String
        get() = _pendingEventId.value
        set(v) { _pendingEventId.value = v; saveString("pending_event_id", v) }

    private val _priceOverrideRaw = mutableStateOf(
        prefs.getStringSet("price_override", emptySet())?.toSet() ?: emptySet()
    )
    private val _priceOverrideUntilDay = mutableStateOf(prefs.getInt("price_override_until_day", 0))

    fun getPriceOverride(productId: String): Int? {
        if (daysWithPet >= _priceOverrideUntilDay.value) return null
        val prefix = "$productId="
        val entry = _priceOverrideRaw.value.firstOrNull { it.startsWith(prefix) } ?: return null
        return entry.substringAfter("=").toIntOrNull()
    }

    fun setPriceOverrides(map: Map<String, Int>, durationDays: Int) {
        val raw = map.map { "${it.key}=${it.value}" }.toSet()
        _priceOverrideRaw.value = raw
        saveStringSet("price_override", raw)
        val until = daysWithPet + durationDays
        _priceOverrideUntilDay.value = until
        saveInt("price_override_until_day", until)
    }

    private val eventLastDayCache = mutableMapOf<String, Int>()
    private val eventCountCache = mutableMapOf<String, Int>()

    fun getEventLastDay(id: String): Int =
        eventLastDayCache.getOrPut(id) { prefs.getInt("event_last_day_$id", -999) }

    fun setEventLastDay(id: String, day: Int) {
        eventLastDayCache[id] = day
        saveInt("event_last_day_$id", day)
    }

    fun getEventCount(id: String): Int =
        eventCountCache.getOrPut(id) { prefs.getInt("event_count_$id", 0) }

    fun incrementEventCount(id: String) {
        val next = getEventCount(id) + 1
        eventCountCache[id] = next
        saveInt("event_count_$id", next)
    }

    //  СТАТИСТИКА ДЛЯ РОДИТЕЛЕЙ
    private val _hungerSum = mutableStateOf(prefs.getInt("stats_hunger_sum", 0))
    var hungerSum: Int
        get() = _hungerSum.value
        set(v) { _hungerSum.value = v; saveInt("stats_hunger_sum", v) }

    private val _happinessSum = mutableStateOf(prefs.getInt("stats_happiness_sum", 0))
    var happinessSum: Int
        get() = _happinessSum.value
        set(v) { _happinessSum.value = v; saveInt("stats_happiness_sum", v) }

    private val _statsDays = mutableStateOf(prefs.getInt("stats_days", 0))
    var statsDays: Int
        get() = _statsDays.value
        set(v) { _statsDays.value = v; saveInt("stats_days", v) }

    fun recordDailyStats(hunger: Int, happiness: Int) {
        hungerSum += hunger
        happinessSum += happiness
        statsDays += 1
    }

    fun avgHunger(): Int =
        if (statsDays == 0) 0 else hungerSum / statsDays

    fun avgHappiness(): Int =
        if (statsDays == 0) 0 else happinessSum / statsDays

    //  ЦЕЛИ
    private val _goalStartDay = mutableStateOf(prefs.getInt("goal_start_day", 0))
    var goalStartDay: Int
        get() = _goalStartDay.value
        set(v) { _goalStartDay.value = v; saveInt("goal_start_day", v) }

    private val _goalsCompleted = mutableStateOf(prefs.getInt("goals_completed", 0))
    var goalsCompleted: Int
        get() = _goalsCompleted.value
        set(v) { _goalsCompleted.value = v; saveInt("goals_completed", v) }

    //  ЗАДАНИЯ (счётчики)
    private val _tasksTotal = mutableStateOf(prefs.getInt("tasks_total", 0))
    var tasksTotal: Int
        get() = _tasksTotal.value
        set(v) { _tasksTotal.value = v; saveInt("tasks_total", v) }

    private val _tasksCorrect = mutableStateOf(prefs.getInt("tasks_correct", 0))
    var tasksCorrect: Int
        get() = _tasksCorrect.value
        set(v) { _tasksCorrect.value = v; saveInt("tasks_correct", v) }

    //  РОДИТЕЛЬСКАЯ НАГРАДА
    fun addParentBonus(amount: Int) {
        if (amount <= 0) return
        coins = (coins + amount).coerceAtLeast(0)
    }

    //  ИСТОРИЯ ЗАВЕРШЁННЫХ ЦЕЛЕЙ
    private val _completedGoalsRaw = mutableStateOf(
        prefs.getStringSet("completed_goals", emptySet())?.toSet() ?: emptySet()
    )

    fun recordCompletedGoal(name: String, days: Int) {
        val encoded = "$days|${name.replace("|", "/")}"
        val next = _completedGoalsRaw.value + encoded
        _completedGoalsRaw.value = next
        saveStringSet("completed_goals", next)
    }

    fun getCompletedGoalHistory(): List<Pair<Int, String>> =
        _completedGoalsRaw.value.mapNotNull { entry ->
            val parts = entry.split("|", limit = 2)
            if (parts.size < 2) return@mapNotNull null
            val days = parts[0].toIntOrNull() ?: return@mapNotNull null
            days to parts[1]
        }.sortedByDescending { it.first }

    fun allSpecialProductsBought(): Boolean {
        val specials = listOf("special_bed", "special_telescope", "special_console")
        return specials.all { getInventoryQuantity(it) > 0 }
    }

    private val _allDoneShown = mutableStateOf(prefs.getBoolean("all_done_shown", false))
    var allDoneShown: Boolean
        get() = _allDoneShown.value
        set(v) { _allDoneShown.value = v; saveBool("all_done_shown", v) }

    //  СБРОС
    fun reset() {
        prefs.edit().clear().apply()

        // Сбрасываем все состояния на исходные
        _isPetCreated.value = false
        _petName.value = ""
        _petAppearance.value = 0
        _petColor.value = 0
        _petLevel.value = 1
        _hunger.value = 80
        _happiness.value = 80
        _coins.value = 0
        _daysWithPet.value = 1
        _tutorialShown.value = false
        _completedTasks.value = emptySet()
        _coinsNeeds.value = 0
        _coinsWants.value = 0
        _piggyBalance.value = 0
        _goalName.value = ""
        _goalPrice.value = 0
        _goalPurchased.value = false
        _chosenOptionsRaw.value = emptySet()
        _tutorialStep.value = 0
        _tutorialActive.value = true
        _stars.value = 0
        _budgetSpreadToday.value = false
        _piggyAddedToday.value = false
        _correctTasksToday.value = 0
        _cartRaw.value = emptySet()
        _inventoryRaw.value = emptySet()
        _inventoryCache.value = emptyMap()
        _bedOwned.value = false
        _lastEventDay.value = 0
        _pendingEventId.value = ""
        _priceOverrideRaw.value = emptySet()
        _priceOverrideUntilDay.value = 0
        _hungerSum.value = 0
        _happinessSum.value = 0
        _statsDays.value = 0
        _goalStartDay.value = 0
        _goalsCompleted.value = 0
        _tasksTotal.value = 0
        _tasksCorrect.value = 0
        _completedGoalsRaw.value = emptySet()
        _allDoneShown.value = false
        eventLastDayCache.clear()
        eventCountCache.clear()
        _seenZonesRaw.value = emptySet()
        _lastSeenTaskUnlockDay.value = 0
    }
}