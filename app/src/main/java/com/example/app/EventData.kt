package com.example.app

import android.content.Context
import org.json.JSONObject

// МОДЕЛИ

data class EventRules(
    val everyNDays: Int,
    val demoOrderFixed: Boolean,
    val demoDays: List<Int>,
    val shuffleOutsideDemo: Boolean,
    val noHarmToPet: Boolean,
    val maxIncome: Int,
    val maxExpense: Int
)

data class EventEffect(
    val balance: Int = 0,
    val savings: Int = 0,
    val mood: Int = 0,
    val factNeeds: Int = 0,
    val factWants: Int = 0,
    val purchase: String? = null,
    val priceOverride: Map<String, Int> = emptyMap(),
    val durationDays: Int = 0
) {
    val isEmpty: Boolean
        get() = balance == 0 && savings == 0 && mood == 0 &&
                factNeeds == 0 && factWants == 0 &&
                purchase == null && priceOverride.isEmpty()
}

data class EventOption(
    val id: String,
    val text: String,
    val verdict: String? = null,        // recommended / acceptable / less_favorable
    val requires: Map<String, Int> = emptyMap(),  // balance / savings
    val confirm: String? = null,        // "withdraw"
    val effects: EventEffect = EventEffect(),
    val petReply: String? = null,
    val explanation: String? = null
)

data class EventFallback(
    val petReply: String,
    val retryNextDay: Boolean
)

data class Event(
    val id: String,
    val order: Int,
    val demoDay: Int?,
    val title: String,
    val type: String,
    val text: String,
    val effects: EventEffect,
    val petReply: String,
    val explanation: String,
    val linkedTask: String? = null,
    val options: List<EventOption>? = null,
    val fallback: EventFallback? = null,
    val repeatable: Boolean = true,
    val cooldownDays: Int = 0,
    val weight: Int = 1,
    val maxRepeats: Int = -1
)

// ЗАГРУЗКА
object EventData {

    var rules: EventRules = EventRules(
        everyNDays = 3,
        demoOrderFixed = true,
        demoDays = emptyList(),
        shuffleOutsideDemo = true,
        noHarmToPet = true,
        maxIncome = 20,
        maxExpense = 25
    )
        private set

    var events: List<Event> = emptyList()
        private set

    private var loaded = false
    private var shuffleQueue: MutableList<Event> = mutableListOf()

    fun load(context: Context) {
        if (loaded) return
        try {
            val json = context.assets.open("events.json")
                .bufferedReader().use { it.readText() }
            val root = JSONObject(json)

            // rules
            val r = root.optJSONObject("rules")
            if (r != null) {
                val demoDaysArr = r.optJSONArray("demoDays")
                val demoDays = mutableListOf<Int>()
                if (demoDaysArr != null) {
                    for (i in 0 until demoDaysArr.length()) {
                        demoDays.add(demoDaysArr.getInt(i))
                    }
                }
                rules = EventRules(
                    everyNDays = r.optInt("everyNDays", 3),
                    demoOrderFixed = r.optBoolean("demoOrderFixed", true),
                    demoDays = demoDays,
                    shuffleOutsideDemo = r.optBoolean("shuffleOutsideDemo", true),
                    noHarmToPet = r.optBoolean("noHarmToPet", true),
                    maxIncome = r.optInt("maxIncome", 20),
                    maxExpense = r.optInt("maxExpense", 25)
                )
            }

            // events
            val eventsArr = root.getJSONArray("events")
            val list = mutableListOf<Event>()
            for (i in 0 until eventsArr.length()) {
                val e = eventsArr.getJSONObject(i)
                list.add(parseEvent(e))
            }
            events = list.sortedBy { it.order }

            loaded = true
        } catch (e: Exception) {
            events = emptyList()
        }
    }

    private fun parseEvent(json: JSONObject): Event {
        val effectsObj = json.optJSONObject("effects")
        val effects = parseEffect(effectsObj)

        // options
        val optionsArr = json.optJSONArray("options")
        val options = if (optionsArr != null) {
            val list = mutableListOf<EventOption>()
            for (i in 0 until optionsArr.length()) {
                val o = optionsArr.getJSONObject(i)
                list.add(
                    EventOption(
                        id = o.optString("id", ""),
                        text = o.optString("text", ""),
                        verdict = if (o.has("verdict")) o.optString("verdict") else null,
                        requires = parseStringIntMap(o.optJSONObject("requires")),
                        confirm = if (o.has("confirm")) o.optString("confirm") else null,
                        effects = parseEffect(o.optJSONObject("effects")),
                        petReply = if (o.has("petReply")) o.optString("petReply") else null,
                        explanation = if (o.has("explanation")) o.optString("explanation") else null
                    )
                )
            }
            list
        } else null

        // fallback
        val fallbackObj = json.optJSONObject("fallback")
        val fallback = fallbackObj?.let {
            EventFallback(
                petReply = it.optString("petReply", ""),
                retryNextDay = it.optBoolean("retryNextDay", false)
            )
        }

        return Event(
            id = json.optString("id", ""),
            order = json.optInt("order", 0),
            demoDay = if (json.has("demoDay") && !json.isNull("demoDay"))
                json.getInt("demoDay") else null,
            title = json.optString("title", ""),
            type = json.optString("type", ""),
            text = json.optString("text", ""),
            effects = effects,
            petReply = json.optString("petReply", ""),
            explanation = json.optString("explanation", ""),
            linkedTask = if (json.has("linkedTask") && !json.isNull("linkedTask"))
                json.optString("linkedTask") else null,
            options = options,
            fallback = fallback,
            repeatable = json.optBoolean("repeatable", true),
            cooldownDays = json.optInt("cooldownDays", 0),
            weight = json.optInt("weight", 1).coerceAtLeast(1),
            maxRepeats = json.optInt("maxRepeats", -1)
        )
    }

    private fun parseEffect(obj: JSONObject?): EventEffect {
        if (obj == null) return EventEffect()
        val priceOverrideObj = obj.optJSONObject("priceOverride")
        val priceOverride = if (priceOverrideObj != null) {
            val map = mutableMapOf<String, Int>()
            val keys = priceOverrideObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = priceOverrideObj.optInt(k, 0)
            }
            map
        } else emptyMap()

        return EventEffect(
            balance = obj.optInt("balance", 0),
            savings = obj.optInt("savings", 0),
            mood = obj.optInt("mood", 0),
            factNeeds = obj.optInt("factNeeds", 0),
            factWants = obj.optInt("factWants", 0),
            purchase = if (obj.has("purchase")) obj.optString("purchase") else null,
            priceOverride = priceOverride,
            durationDays = obj.optInt("durationDays", 0)
        )
    }

    private fun parseStringIntMap(obj: JSONObject?): Map<String, Int> {
        if (obj == null) return emptyMap()
        val map = mutableMapOf<String, Int>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            map[k] = obj.optInt(k, 0)
        }
        return map
    }

    fun eventById(id: String): Event? = events.firstOrNull { it.id == id }

    fun reset() {
        loaded = false
        events = emptyList()
        shuffleQueue = mutableListOf()
    }
}