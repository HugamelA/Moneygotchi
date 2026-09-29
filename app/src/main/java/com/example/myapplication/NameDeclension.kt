package com.example.myapplication

// Падежи
enum class GramCase {
    NOMINATIVE,     // Кто? Что?
    GENITIVE,       // Кого? Чего?
    DATIVE,         // Кому? Чему?
    ACCUSATIVE,     // Кого? Что?
    INSTRUMENTAL,   // Кем? Чем?
    PREPOSITIONAL   // О ком? О чём?
}

private val hushers = setOf('ш', 'ж', 'ч', 'щ')
private val noYAfter = setOf('ш', 'ж', 'ч', 'щ', 'г', 'к', 'х')

fun declinePetName(rawName: String, case: GramCase): String {
    val name = rawName.trim()

    if (name.isEmpty() || name.none { it.isLetter() }) {
        return when (case) {
            GramCase.NOMINATIVE    -> "питомец"
            GramCase.GENITIVE      -> "питомца"
            GramCase.DATIVE        -> "питомцу"
            GramCase.ACCUSATIVE    -> "питомца"
            GramCase.INSTRUMENTAL  -> "питомцем"
            GramCase.PREPOSITIONAL -> "питомце"
        }
    }

    val last = name.last()
    val stem = name.dropLast(1)
    val stemLast = stem.lastOrNull()?.lowercaseChar()
    val afterHusher = stemLast in hushers
    val afterNoY = stemLast in noYAfter

    return when (last) {
        // Женский тип на -а
        'а' -> when (case) {
            GramCase.NOMINATIVE -> name

            // Родительный
            GramCase.GENITIVE ->
                if (afterNoY) stem + "и" else stem + "ы"

            GramCase.DATIVE        -> stem + "е"
            GramCase.ACCUSATIVE    -> stem + "у"

            // Творительный
            GramCase.INSTRUMENTAL ->
                if (afterHusher) stem + "ей" else stem + "ой"

            GramCase.PREPOSITIONAL -> stem + "е"
        }

        // Женский тип на -я
        'я' -> when (case) {
            GramCase.NOMINATIVE    -> name
            GramCase.GENITIVE      -> stem + "и"
            GramCase.DATIVE        -> stem + "е"
            GramCase.ACCUSATIVE    -> stem + "ю"
            GramCase.INSTRUMENTAL  -> stem + "ей"
            GramCase.PREPOSITIONAL -> stem + "е"
        }

        // Тип на -о
        'о' -> when (case) {
            GramCase.NOMINATIVE    -> name
            GramCase.GENITIVE      -> stem + "ы"
            GramCase.DATIVE        -> stem + "е"
            GramCase.ACCUSATIVE    -> stem + "у"
            GramCase.INSTRUMENTAL  -> stem + "ой"
            GramCase.PREPOSITIONAL -> stem + "е"
        }

        // Тип на -е
        'е' -> when (case) {
            GramCase.NOMINATIVE    -> name
            GramCase.GENITIVE      -> stem + "ы"
            GramCase.DATIVE        -> stem + "е"
            GramCase.ACCUSATIVE    -> stem + "у"
            GramCase.INSTRUMENTAL  -> stem + "ей"
            GramCase.PREPOSITIONAL -> stem + "е"
        }

        // Мужской на -й
        'й' -> when (case) {
            GramCase.NOMINATIVE    -> name
            GramCase.GENITIVE      -> stem + "я"
            GramCase.DATIVE        -> stem + "ю"
            GramCase.ACCUSATIVE    -> stem + "я"
            GramCase.INSTRUMENTAL  -> stem + "ем"
            GramCase.PREPOSITIONAL -> stem + "е"
        }

        // Мужской на -ь
        'ь' -> when (case) {
            GramCase.NOMINATIVE    -> name
            GramCase.GENITIVE      -> stem + "я"
            GramCase.DATIVE        -> stem + "ю"
            GramCase.ACCUSATIVE    -> stem + "я"
            GramCase.INSTRUMENTAL  -> stem + "ем"
            GramCase.PREPOSITIONAL -> stem + "е"
        }

        // Мужской на согласную
        else -> when (case) {
            GramCase.NOMINATIVE    -> name
            GramCase.GENITIVE      -> name + "а"
            GramCase.DATIVE        -> name + "у"
            GramCase.ACCUSATIVE    -> name + "а"
            GramCase.INSTRUMENTAL  -> name + "ом"
            GramCase.PREPOSITIONAL -> name + "е"
        }
    }
}

fun aboutPet(rawName: String): String {
    val nameOnly = if (rawName.isBlank())
        "питомце"
    else
        declinePetName(rawName, GramCase.PREPOSITIONAL)

    val first = nameOnly.firstOrNull()?.lowercaseChar()
    val prep = when (first) {
        'а', 'о', 'и', 'у', 'э' -> "об"
        else -> "о"
    }
    return "$prep $nameOnly"
}