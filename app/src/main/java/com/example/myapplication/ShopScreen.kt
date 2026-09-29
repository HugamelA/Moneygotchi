package com.example.myapplication

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration

// ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ
@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interaction,
        indication = null,
        onClick = onClick
    )
}

@Composable
private fun PlainButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF81C784),
    textColor: Color = Color.White,
    fontSize: Int = 15,
    cornerRadius: Int = 12
) {
    val bg = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.4f)
    Surface(
        shape = RoundedCornerShape(cornerRadius.dp),
        color = bg,
        shadowElevation = if (enabled) 2.dp else 0.dp,
        modifier = modifier.noRippleClickable { if (enabled) onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Text(
                text,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

// ЭКРАН МАГАЗИНА
@Composable
fun ShopScreen(
    storage: AppStorage,
    onBack: () -> Unit,
    onCartClick: () -> Unit
) {
    val currentPetType = currentPetTypeId(storage.petAppearance)
    val petGenitive = currentPetTypeGenitive(storage.petAppearance)
    // Загрузка каталог
    val context = LocalContext.current
    remember { ShopCatalog.load(context); 0 }

    var cart by remember { mutableStateOf(storage.getCart()) }
    var selectedCategory by remember {
        mutableStateOf(ShopCatalog.categories.firstOrNull()?.id ?: "")
    }
    var lockedMessage by remember { mutableStateOf<String?>(null) }
    var goalConfirmProduct by remember { mutableStateOf<ShopProduct?>(null) }
    var goalMessage by remember { mutableStateOf<String?>(null) }

    BackHandler { onBack() }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE8F5E9))) {

        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .noRippleClickable { onBack() }
                ) {
                    Text(
                        "Назад",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                Text(
                    "Магазин",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Категории
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShopCatalog.categories.forEach { category ->
                    val selected = category.id == selectedCategory
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) Color(0xFF81C784) else Color.White,
                        shadowElevation = if (selected) 3.dp else 1.dp,
                        modifier = Modifier.noRippleClickable {
                            selectedCategory = category.id
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(category.emoji, fontSize = 18.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                category.name,
                                fontSize = 15.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) Color.White else Color(0xFF333333)
                            )
                        }
                    }
                }
            }

            // Описание выбранной категории
            val selectedDescription = ShopCatalog
                .categoryById(selectedCategory)?.description
            if (!selectedDescription.isNullOrBlank()) {
                Text(
                    selectedDescription,
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Список товаров
            val allInCategory = ShopCatalog.products
                .filter { it.categoryId == selectedCategory }
                .filter { it.fitsPet(currentPetType) }

            // Особые товары
            val productsInCategory = if (selectedCategory == "special") {
                allInCategory.filter { product ->
                    val inInventory = storage.getInventoryQuantity(product.id) > 0
                    val isCurrentGoal =
                        storage.goalName == product.displayName(petGenitive) &&
                                storage.goalPrice == product.price
                    !inInventory && !isCurrentGoal
                }
            } else {
                allInCategory
            }

            val specials = ShopCatalog.products
                .filter { it.categoryId == "special" }
                .sortedBy { it.price }

            fun isLocked(product: ShopProduct): Boolean {
                if (selectedCategory != "special") return false
                val idx = specials.indexOfFirst { it.id == product.id }
                if (idx <= 0) return false
                return (0 until idx).any { i ->
                    storage.getInventoryQuantity(specials[i].id) == 0
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Подсказка для категории «Особые товары»
                if (selectedCategory == "special") {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFF8E1),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                "Товары из этой категории необязательны. " +
                                        "Не все из них влияют на питомца. " +
                                        "Копи монеты, чтобы их купить — это твои маленькие мечты.",
                                fontSize = 13.sp,
                                color = Color(0xFF6D4C41),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (productsInCategory.isEmpty()) {
                    // Пустая категория
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📦", fontSize = 64.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Скоро товары данной категории\nпоступят в продажу",
                                fontSize = 16.sp,
                                color = Color(0xFF888888),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(productsInCategory, key = { it.id }) { product ->
                            val locked = isLocked(product)
                            ProductCard(
                                product = product,
                                petGenitive = petGenitive,
                                quantity = cart[product.id] ?: 0,
                                locked = locked,
                                storage = storage,
                                onAdd = {
                                    val current = cart[product.id] ?: 0
                                    // Особые — только по одной штуке
                                    val maxQty = if (product.categoryId == "special") 1 else 99
                                    val newQty = (current + 1).coerceAtMost(maxQty)
                                    cart = cart.toMutableMap().apply { put(product.id, newQty) }
                                    storage.setCartQuantity(product.id, newQty)
                                },
                                onRemove = {
                                    val current = cart[product.id] ?: 0
                                    val newQty = (current - 1).coerceAtLeast(0)
                                    val newCart = cart.toMutableMap()
                                    if (newQty == 0) newCart.remove(product.id)
                                    else newCart[product.id] = newQty
                                    cart = newCart
                                    storage.setCartQuantity(product.id, newQty)
                                },
                                onLockedClick = {
                                    lockedMessage = "Сначала купи предыдущие особые товары."
                                },
                                onSetGoal = { goalConfirmProduct = product }
                            )
                        }
                    }
                }
            }

            // Нижняя панель корзины
            CartBar(
                cart = cart,
                storage = storage,
                onClick = onCartClick
            )
        }
    }

    // Сообщение о заблокированном товаре
    val lockMsg = lockedMessage
    if (lockMsg != null) {
        AlertDialog(
            onDismissRequest = { lockedMessage = null },
            title = { Text("Закрыто") },
            text = { Text(lockMsg) },
            confirmButton = {
                TextButton(onClick = { lockedMessage = null }) { Text("Понятно") }
            }
        )
    }

    // Диалог: сделать особым товаром целью
    val confirmProduct = goalConfirmProduct
    if (confirmProduct != null) {
        val displayName = confirmProduct.displayName(petGenitive)
        AlertDialog(
            onDismissRequest = { goalConfirmProduct = null },
            title = { Text("Создать цель?") },
            text = {
                Column {
                    Text("Сделать товар «$displayName» твоей целью?", fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "На главном экране появится цель. Копи на неё монеты в копилку.",
                        fontSize = 13.sp,
                        color = Color(0xFF666666)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    storage.goalName = displayName
                    storage.goalPrice = confirmProduct.price
                    storage.goalStartDay = storage.daysWithPet
                    goalConfirmProduct = null
                    goalMessage = "Цель создана! Копи на товар «$displayName»."
                }) { Text("Да, создать") }
            },
            dismissButton = {
                TextButton(onClick = { goalConfirmProduct = null }) { Text("Нет") }
            }
        )
    }

    // Сообщение после создания цели
    val goalMsg = goalMessage
    if (goalMsg != null) {
        AlertDialog(
            onDismissRequest = { goalMessage = null },
            title = { Text("Готово!") },
            text = { Text(goalMsg) },
            confirmButton = {
                TextButton(onClick = { goalMessage = null }) { Text("Отлично") }
            }
        )
    }
}

// КАРТОЧКА ТОВАРА
@Composable
private fun ProductCard(
    product: ShopProduct,
    petGenitive: String,
    quantity: Int,
    locked: Boolean,
    storage: AppStorage,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onLockedClick: () -> Unit,
    onSetGoal: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            if (product.iconRes != null) {
                Image(
                    painter = painterResource(id = product.iconRes),
                    contentDescription = product.name,
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(product.emoji, fontSize = 40.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.displayName(petGenitive),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Spacer(Modifier.height(2.dp))
                val displayPrice = product.effectivePrice(storage)
                val hasDiscount = displayPrice < product.price

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasDiscount) {
                        Text(
                            "${product.price}",
                            fontSize = 13.sp,
                            color = Color(0xFF999999),
                            textDecoration = TextDecoration.LineThrough
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        "${displayPrice} ${coinsWord(displayPrice)}",
                        fontSize = 13.sp,
                        color = if (hasDiscount) Color(0xFF4CAF50) else Color(0xFF888888),
                        fontWeight = if (hasDiscount) FontWeight.Medium else FontWeight.Normal
                    )
                }

                // Эффекты применения
                val hasHunger = product.hungerEffect > 0
                val hasHappiness = product.happinessEffect > 0
                if (hasHunger || hasHappiness) {
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (hasHunger) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFFF3E0)
                            ) {
                                Text(
                                    "🍽️ +${product.hungerEffect}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE65100),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (hasHappiness) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFFEBEE)
                            ) {
                                Text(
                                    "😊 +${product.happinessEffect}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFC62828),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Описание для особых товаров
                if (!product.description.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        product.description,
                        fontSize = 11.sp,
                        color = Color(0xFF777777),
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            when {
                locked -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE0E0E0),
                        modifier = Modifier
                            .size(44.dp)
                            .noRippleClickable { onLockedClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🔒", fontSize = 20.sp)
                        }
                    }
                }

                product.categoryId == "special" -> {
                    PlainButton(
                        text = "Сделать целью",
                        onClick = onSetGoal,
                        fontSize = 13,
                        cornerRadius = 12,
                        backgroundColor = Color(0xFFFF9800)
                    )
                }

                quantity == 0 -> {
                    PlainButton(
                        text = "Добавить",
                        onClick = onAdd,
                        fontSize = 14,
                        cornerRadius = 12
                    )
                }

                else -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFC8E6C9),
                            modifier = Modifier
                                .size(36.dp)
                                .noRippleClickable { onRemove() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "−",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                        Text(
                            "$quantity",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.widthIn(min = 28.dp),
                            textAlign = TextAlign.Center
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFC8E6C9),
                            modifier = Modifier
                                .size(36.dp)
                                .noRippleClickable { onAdd() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "+",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// НИЖНЯЯ ПАНЕЛЬ КОРЗИНЫ
@Composable
private fun CartBar(
    cart: Map<String, Int>,
    storage: AppStorage,
    onClick: () -> Unit
) {
    val totalItems = cart.values.sum()
    val totalPrice = cart.entries.sumOf { (id, qty) ->
        (ShopCatalog.productById(id)?.effectivePrice(storage) ?: 0) * qty
    }

    Surface(
        color = Color.White,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("🛒", fontSize = 26.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (totalItems == 0) "Корзина пуста"
                    else "В корзине: $totalItems",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                if (totalPrice > 0) {
                    Text(
                        "Всего: $totalPrice ${coinsWord(totalPrice)}",
                        fontSize = 13.sp,
                        color = Color(0xFF888888)
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (totalItems > 0) Color(0xFF81C784)
                else Color(0xFF81C784).copy(alpha = 0.4f),
                shadowElevation = if (totalItems > 0) 2.dp else 0.dp
            ) {
                Text(
                    "Открыть",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }
    }
}