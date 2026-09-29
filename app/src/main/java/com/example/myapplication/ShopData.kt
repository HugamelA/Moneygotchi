package com.example.myapplication

import android.content.Context
import org.json.JSONObject

enum class PaymentSource { NEEDS, WANTS }

data class ShopCategory(
    val id: String,
    val name: String,
    val emoji: String,
    val paymentSource: PaymentSource,
    val description: String = ""
)

data class ShopProduct(
    val id: String,
    val categoryId: String,
    val name: String,
    val price: Int,
    val emoji: String,
    val iconRes: Int? = null,
    val hungerEffect: Int = 0,
    val happinessEffect: Int = 0,
    val petType: String = "any",
    val nameTemplate: String? = null,
    val description: String? = null,
    val perk: ProductPerk? = null
)

data class ProductPerk(
    val moodDecayMult: Float = 1f,
    val moodGainMult: Float = 1f
)

// ЗАГРУЗКА КАТАЛОГА ИЗ assets
object ShopCatalog {

    var categories: List<ShopCategory> = emptyList()
        private set

    var products: List<ShopProduct> = emptyList()
        private set

    private var loaded = false

    // Загружает каталог
    fun load(context: Context) {
        if (loaded) return
        try {
            val json = context.assets.open("shop.json")
                .bufferedReader().use { it.readText() }
            val root = JSONObject(json)

            // Категории
            val catsArr = root.getJSONArray("categories")
            val cats = mutableListOf<ShopCategory>()
            for (i in 0 until catsArr.length()) {
                val c = catsArr.getJSONObject(i)
                val src = when (c.optString("paymentSource", "wants")) {
                    "needs" -> PaymentSource.NEEDS
                    else    -> PaymentSource.WANTS
                }
                cats.add(
                    ShopCategory(
                        id = c.optString("id", ""),
                        name = c.optString("name", ""),
                        emoji = c.optString("emoji", ""),
                        paymentSource = src,
                        description = c.optString("description", "")
                    )
                )
            }
            categories = cats

            // Товары
            val prodsArr = root.getJSONArray("products")
            val prods = mutableListOf<ShopProduct>()
            for (i in 0 until prodsArr.length()) {
                val p = prodsArr.getJSONObject(i)
                // perk, если он есть
                val perkObj = p.optJSONObject("perk")
                val perk = if (perkObj != null) {
                    ProductPerk(
                        moodDecayMult = perkObj.optDouble("moodDecayMult", 1.0).toFloat(),
                        moodGainMult = perkObj.optDouble("moodGainMult", 1.0).toFloat()
                    )
                } else null

                prods.add(
                    ShopProduct(
                        id = p.optString("id", ""),
                        categoryId = p.optString("categoryId", ""),
                        name = p.optString("name", ""),
                        price = p.optInt("price", 0),
                        emoji = p.optString("emoji", ""),
                        iconRes = shopProductIconRes(p.optString("id", "")),
                        hungerEffect = p.optInt("hungerEffect", 0),
                        happinessEffect = p.optInt("happinessEffect", 0),
                        petType = p.optString("petType", "any"),
                        nameTemplate = if (p.has("nameTemplate")) p.optString("nameTemplate") else null,
                        description = if (p.has("description")) p.optString("description") else null,
                        perk = perk
                    )
                )
            }
            products = prods

            loaded = true
        } catch (e: Exception) {
            // Файл не найден или сломан
            categories = emptyList()
            products = emptyList()
        }
    }

    fun categoryById(id: String): ShopCategory? =
        categories.firstOrNull { it.id == id }

    fun productById(id: String): ShopProduct? =
        products.firstOrNull { it.id == id }

    fun reset() {
        loaded = false
        categories = emptyList()
        products = emptyList()
    }
}

// ИКОНКИ ТОВАРОВ
fun shopProductIconRes(productId: String): Int? = when (productId) {
    // Еда
    "feed_small"          -> R.drawable.item_feed_small
    "feed_medium"         -> R.drawable.item_feed_medium
    "feed_large"          -> R.drawable.item_feed_large
    "vitamins"            -> R.drawable.item_vitamins

    // Лакомства дракона
    "treat_dragon_meat"   -> R.drawable.item_treat_dragon_meat
    "treat_dragon_pepper" -> R.drawable.item_treat_dragon_pepper
    "treat_dragon_steak"  -> R.drawable.item_treat_dragon_steak

    // Лакомства собачки
    "treat_dog_bone"      -> R.drawable.item_treat_dog_bone
    "treat_dog_sausage"   -> R.drawable.item_treat_dog_sausage
    "treat_dog_cheese"    -> R.drawable.item_treat_dog_cheese

    // Лакомства кошечки
    "treat_cat_fish"      -> R.drawable.item_treat_cat_fish
    "treat_cat_shrimp"    -> R.drawable.item_treat_cat_shrimp
    "treat_cat_grass"     -> R.drawable.item_treat_cat_grass

    // Игрушки
    "ball"                -> R.drawable.item_ball
    "rope"                -> R.drawable.item_rope
    "plush"               -> R.drawable.item_plush
    "ring"             -> R.drawable.item_ring

    // Уход
    "shampoo"             -> R.drawable.item_shampoo
    "brush"               -> R.drawable.item_brush
    "soap"                -> R.drawable.item_soap

    // Особые
    "special_bed"         -> R.drawable.item_special_bed
    "special_telescope"   -> R.drawable.item_special_telescope
    "special_console"     -> R.drawable.item_special_console

    else -> null
}

// Возвращает имя товара с подстановкой вида питомца
fun ShopProduct.displayName(petGenitive: String): String =
    nameTemplate?.replace("{pet}", petGenitive) ?: name

fun ShopProduct.fitsPet(petTypeId: String): Boolean =
    petType == "any" || petType == petTypeId

fun ShopProduct.effectivePrice(storage: AppStorage): Int =
    storage.getPriceOverride(id) ?: price