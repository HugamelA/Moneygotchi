package com.example.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration

// ВСПОМОГАТЕЛЬНОЕ
@Composable
private fun Modifier.noRipple(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interaction,
        indication = null,
        onClick = onClick
    )
}

// ЭКРАН КОРЗИНЫ
@Composable
fun CartScreen(
    storage: AppStorage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    remember { ShopCatalog.load(context); 0 }

    var cart by remember { mutableStateOf(storage.getCart()) }
    var showCheckout by remember { mutableStateOf(false) }
    val petGenitive = currentPetTypeGenitive(storage.petAppearance)
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

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
                        .noRipple { onBack() }
                ) {
                    Text(
                        "Назад",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                Text(
                    "Корзина",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (cart.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🛒", fontSize = 72.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Корзина пуста",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Добавь товары из магазина",
                            fontSize = 14.sp,
                            color = Color(0xFF888888)
                        )
                    }
                }
            } else {
                // Список товаров
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = cart.entries.toList().filter { entry ->
                            ShopCatalog.productById(entry.key)?.categoryId != "special"
                        },
                        key = { it.key }
                    ) { entry ->
                        val productId = entry.key
                        val quantity = entry.value
                        val product = ShopCatalog.productById(productId)
                        val category = product?.let { ShopCatalog.categoryById(it.categoryId) }

                        if (product != null && category != null) {
                            val displayName = product.displayName(petGenitive)
                            CartItemCard(
                                product = product,
                                category = category,
                                displayName = displayName,
                                quantity = quantity,
                                storage = storage,
                                onChangeQuantity = { newQty: Int ->
                                    val newCart = cart.toMutableMap()
                                    if (newQty <= 0) newCart.remove(productId)
                                    else newCart[productId] = newQty
                                    cart = newCart
                                    storage.setCartQuantity(productId, newQty)
                                }
                            )
                        }
                    }
                }

                // Итоговая панель
                CartSummaryBar(
                    cart = cart,
                    storage = storage,
                    onCheckout = { showCheckout = true }
                )
            }
        }

        // Сообщение об ошибке
        val errMsg = errorMessage
        if (errMsg != null) {
            AlertDialog(
                onDismissRequest = { errorMessage = null },
                title = { Text("Недостаточно средств") },
                text = { Text(errMsg) },
                confirmButton = {
                    TextButton(onClick = { errorMessage = null }) { Text("Понятно") }
                }
            )
        }

        // Успешная покпка
        val okMsg = successMessage
        if (okMsg != null) {
            AlertDialog(
                onDismissRequest = {
                    successMessage = null
                    onBack()
                },
                title = { Text("Покупка совершена!") },
                text = { Text(okMsg) },
                confirmButton = {
                    TextButton(onClick = {
                        successMessage = null
                        onBack()
                    }) { Text("Отлично") }
                }
            )
        }
    }

    // Диалог оплаты
    if (showCheckout) {
        CheckoutDialog(
            cart = cart,
            storage = storage,
            onDismiss = { showCheckout = false },
            onSuccess = { msg: String ->
                showCheckout = false
                cart = emptyMap()
                storage.clearCart()
                successMessage = msg
            },
            onError = { msg: String ->
                showCheckout = false
                errorMessage = msg
            }
        )
    }
}

@Composable
private fun CartItemCard(
    product: ShopProduct,
    category: ShopCategory,
    displayName: String,
    quantity: Int,
    storage: AppStorage,
    onChangeQuantity: (Int) -> Unit
) {
    val unitPrice = product.effectivePrice(storage)
    val lineTotal = unitPrice * quantity
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (product.iconRes != null) {
                    Image(
                        painter = painterResource(id = product.iconRes),
                        contentDescription = product.name,
                        modifier = Modifier.size(56.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(product.emoji, fontSize = 36.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        displayName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Spacer(Modifier.height(2.dp))


                    val hasDiscount = unitPrice < product.price

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (hasDiscount) {
                            Text(
                                "${product.price}",
                                fontSize = 12.sp,
                                color = Color(0xFF999999),
                                textDecoration = TextDecoration.LineThrough
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(
                            "${unitPrice} ${coinsWord(unitPrice)}",
                            fontSize = 12.sp,
                            color = if (hasDiscount) Color(0xFF4CAF50) else Color(0xFF888888),
                            fontWeight = if (hasDiscount) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }

            // Обычный товар: количество + оплата
            Spacer(Modifier.height(6.dp))

            val sourceLabel = when (category.paymentSource) {
                PaymentSource.NEEDS -> "Обязательного"
                PaymentSource.WANTS -> "Необязательного"
            }
            val sourceColor = when (category.paymentSource) {
                PaymentSource.NEEDS -> Color(0xFF64B5F6)
                PaymentSource.WANTS -> Color(0xFFBA68C8)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFC8E6C9),
                    modifier = Modifier
                        .size(36.dp)
                        .noRipple { onChangeQuantity(quantity - 1) }
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
                        .noRipple { onChangeQuantity(quantity + 1) }
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
                Spacer(Modifier.weight(1f))
                Text(
                    "Итого: $lineTotal ${coinsWord(lineTotal)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            Spacer(Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = sourceColor.copy(alpha = 0.15f)
            ) {
                Text(
                    "Оплата из: $sourceLabel",
                    fontSize = 12.sp,
                    color = sourceColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ИТОГОВАЯ ПАНЕЛЬКА
@Composable
private fun CartSummaryBar(
    cart: Map<String, Int>,
    storage: AppStorage,
    onCheckout: () -> Unit
) {
    var totalNeeds = 0
    var totalWants = 0

    cart.forEach { (id, qty) ->
        val product = ShopCatalog.productById(id)
        val category = product?.let { ShopCatalog.categoryById(it.categoryId) }
        if (product != null && category != null) {
            if (product.categoryId == "special") return@forEach
            val cost = product.effectivePrice(storage) * qty
            when (category.paymentSource) {
                PaymentSource.NEEDS -> totalNeeds += cost
                PaymentSource.WANTS -> totalWants += cost
            }
        }
    }

    val totalCost = totalNeeds + totalWants
    val enough = storage.coinsNeeds >= totalNeeds && storage.coinsWants >= totalWants

    Surface(
        color = Color.White,
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (totalNeeds > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Из «Обязательного»",
                        fontSize = 14.sp,
                        color = Color(0xFF333333)
                    )
                    Text(
                        "$totalNeeds ${coinsWord(totalNeeds)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (storage.coinsNeeds >= totalNeeds)
                            Color(0xFF4CAF50) else Color(0xFFE53935)
                    )
                }
            }
            if (totalWants > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Из «Необязательного»",
                        fontSize = 14.sp,
                        color = Color(0xFF333333)
                    )
                    Text(
                        "$totalWants ${coinsWord(totalWants)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (storage.coinsWants >= totalWants)
                            Color(0xFF4CAF50) else Color(0xFFE53935)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Итого:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Text(
                    "$totalCost ${coinsWord(totalCost)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            Spacer(Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (enough) Color(0xFF81C784)
                else Color(0xFF81C784).copy(alpha = 0.4f),
                shadowElevation = if (enough) 2.dp else 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .noRipple { if (enough) onCheckout() }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 14.dp)
                ) {
                    Text(
                        if (enough) "Оплатить" else "Недостаточно средств",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ДИАЛОГ ОПЛАТЫ
@Composable
private fun CheckoutDialog(
    cart: Map<String, Int>,
    storage: AppStorage,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    var totalNeeds = 0
    var totalWants = 0

    cart.forEach { (id, qty) ->
        val product = ShopCatalog.productById(id)
        val category = product?.let { ShopCatalog.categoryById(it.categoryId) }
        if (product != null && category != null) {
            if (product.categoryId == "special") return@forEach
            val cost = product.effectivePrice(storage) * qty
            when (category.paymentSource) {
                PaymentSource.NEEDS -> totalNeeds += cost
                PaymentSource.WANTS -> totalWants += cost
            }
        }
    }

    val totalCost = totalNeeds + totalWants

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Подтвердить покупку?") },
        text = {
            Column {
                if (totalNeeds > 0) {
                    Text(
                        "Из «Обязательного»: $totalNeeds ${coinsWord(totalNeeds)}",
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(4.dp))
                }
                if (totalWants > 0) {
                    Text(
                        "Из «Необязательного»: $totalWants ${coinsWord(totalWants)}",
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Итого: $totalCost ${coinsWord(totalCost)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        storage.coinsNeeds < totalNeeds && storage.coinsWants < totalWants ->
                            onError(
                                "Не хватает монет и в «Обязательном» " +
                                        "(нужно $totalNeeds, есть ${storage.coinsNeeds}), " +
                                        "и в «Необязательном» " +
                                        "(нужно $totalWants, есть ${storage.coinsWants})."
                            )
                        storage.coinsNeeds < totalNeeds ->
                            onError(
                                "В конверте «Обязательное» не хватает " +
                                        "${totalNeeds - storage.coinsNeeds} " +
                                        "${coinsWord(totalNeeds - storage.coinsNeeds)}."
                            )
                        storage.coinsWants < totalWants ->
                            onError(
                                "В конверте «Необязательное» не хватает " +
                                        "${totalWants - storage.coinsWants} " +
                                        "${coinsWord(totalWants - storage.coinsWants)}."
                            )
                        else -> {
                            storage.coinsNeeds -= totalNeeds
                            storage.coinsWants -= totalWants

                            cart.forEach { (id, qty) ->
                                storage.addToInventory(id, qty)
                            }

                            // Если куплена лежанка — нужно включить перк
                            if (cart.containsKey("special_bed")) {
                                storage.bedOwned = true
                            }

                            onSuccess("Куплено на $totalCost ${coinsWord(totalCost)}.")
                        }
                    }
                }
            ) {
                Text("Оплатить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}