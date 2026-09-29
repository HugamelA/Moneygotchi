package com.example.myapplication

// СКЛОНЕНИЯ
// Возвращает правильную форму для числа.
fun pluralRu(count: Int, forms: List<String>): String {
    val n = kotlin.math.abs(count) % 100
    if (n in 11..14) return forms[2]
    return when (n % 10) {
        1 -> forms[0]
        2, 3, 4 -> forms[1]
        else -> forms[2]
    }
}

fun coinsWord(count: Int) = pluralRu(count, listOf("монета", "монеты", "монет"))
fun starsWord(count: Int) = pluralRu(count, listOf("звезда", "звезды", "звёзд"))
fun daysWord(count: Int) = pluralRu(count, listOf("день", "дня", "дней"))

// ФОРМАТ МОНЕТ
fun formatCoins(value: Int): String = when {
    value < 10_000 -> "$value ${coinsWord(value)}"
    value < 1_000_000 -> "${value / 1_000}K монет"
    value < 1_000_000_000 -> "${value / 1_000_000}M монет"
    value < 1_000_000_000_000 -> "${value / 1_000_000}B монет"
    else -> "💰💰💰"
}

// ПОДСТАНОВКА ЗНАЧЕНИЙ В ТЕКСТАХ ЗАДАНИЙ
// Заменяет в тексте:
/*   • {pet}        → имя питомца в именительном
   • {petGen}     → в родительном
   • {petDat}     → в дательном
   • {petAcc}     → в винительном
   • {petInstr}   → в творительном
   • {petPrep}    → в предложном (с «о»)
   • {goalLeft}   → сколько монет осталось до цели */
fun interpolateTaskText(
    text: String,
    petName: String,
    goalPrice: Int,
    piggyBalance: Int
): String {
    val safeName = petName.ifBlank { "Питомец" }
    val goalLeft = (goalPrice - piggyBalance).coerceAtLeast(0)

    return text
        .replace("{petNom}",   declinePetName(safeName, GramCase.NOMINATIVE))
        .replace("{petGen}",   declinePetName(safeName, GramCase.GENITIVE))
        .replace("{petDat}",   declinePetName(safeName, GramCase.DATIVE))
        .replace("{petAcc}",   declinePetName(safeName, GramCase.ACCUSATIVE))
        .replace("{petInstr}", declinePetName(safeName, GramCase.INSTRUMENTAL))
        .replace("{petPrep}",  declinePetName(safeName, GramCase.PREPOSITIONAL))
        .replace("{goalLeft}", goalLeft.toString())
}