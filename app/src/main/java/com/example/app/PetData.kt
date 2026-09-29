package com.example.app

import androidx.compose.ui.graphics.Color

// СТАДИЯ РАЗВИТИЯ ПИТОМЦА
enum class PetStage(val displayName: String) {
    BABY("Малыш"),
    TEEN("Подросток"),
    ADULT("Взрослый")
}

enum class VisualStage { BABY, TEEN, ADULT }

// Конвертер между PetStage (логика) и VisualStage (картинки)
fun PetStage.toVisualStage(): VisualStage = when (this) {
    PetStage.BABY  -> VisualStage.BABY
    PetStage.TEEN  -> VisualStage.TEEN
    PetStage.ADULT -> VisualStage.ADULT
}

// ПИТОМЦЫ (3 варианта облика)
data class PetOption(val name: String, val images: List<Int>)

val petOptions = listOf(
    PetOption("Дракончик", listOf(
        R.drawable.pet_dragon_1, R.drawable.pet_dragon_2, R.drawable.pet_dragon_3
    )),
    PetOption("Собачка", listOf(
        R.drawable.pet_dog_1, R.drawable.pet_dog_2, R.drawable.pet_dog_3
    )),
    PetOption("Кошечка", listOf(
        R.drawable.pet_cat_1, R.drawable.pet_cat_2, R.drawable.pet_cat_3
    ))

    // PetOption("Черепашка", listOf()),
    // PetOption("Пингвин", listOf()),
    // PetOption("Паучок", listOf())
)

val colorNames = listOf("Вариант 1", "Вариант 2", "Вариант 3")

fun petImage(petIndex: Int, colorIndex: Int): Int {
    val pet = petOptions.getOrElse(petIndex) { petOptions[0] }
    return pet.images.getOrElse(colorIndex) { pet.images[0] }
}

// ИКОНКИ НАСТРОЕНИЯ
fun moodIconRes(value: Int): Int = when {
    value < 33 -> R.drawable.mood_sad
    value < 66 -> R.drawable.mood_normal
    else       -> R.drawable.mood_happy
}

// ИКОНКИ СЫТОСТИ
fun hungerIconRes(value: Int): Int = when {
    value < 33 -> R.drawable.hunger_low
    value < 66 -> R.drawable.hunger_normal
    else       -> R.drawable.hunger_full
}

// Цвет шкалы сытости
fun hungerBarColor(value: Int): Color = when {
    value < 33 -> Color(0xFFEF5350)
    value < 66 -> Color(0xFFFFC107)
    else       -> Color(0xFF7CB342)
}

// Цвет шкалы настроения
fun moodBarColor(value: Int): Color = when {
    value < 33 -> Color(0xFF9575CD)
    value < 66 -> Color(0xFFFFC107)
    else       -> Color(0xFF7CB342)
}

// ТИПЫ ПИТОМЦЕВ
val petTypeIds = listOf("dragon", "dog", "cat", "turtle", "penguin", "spider")
val petTypeGenitive = listOf(
    "дракончика",
    "собачки",
    "кошечки",
    "черепашки",
    "пингвина",
    "паучка"
)

fun currentPetTypeId(appearance: Int): String =
    petTypeIds.getOrElse(appearance) { "dragon" }

fun currentPetTypeGenitive(appearance: Int): String =
    petTypeGenitive.getOrElse(appearance) { "питомца" }


fun moodIndex(happiness: Int): Int = when {
    happiness < 33 -> 0
    happiness < 66 -> 1
    else -> 2
}

// ФОН КОМНАТЫ ПО КОЛИЧЕСТВУ ОСОБЫХ ПОКУПОК
fun roomBackgroundRes(storage: AppStorage): Int {
    val specials = listOf("special_bed", "special_telescope", "special_console")
    val count = specials.count { storage.getInventoryQuantity(it) > 0 }
    return when (count) {
        0 -> R.drawable.room_bg
        1 -> R.drawable.room_bg_1
        2 -> R.drawable.room_bg_2
        else -> R.drawable.room_bg_3
    }
}