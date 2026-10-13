package com.example.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.runtime.withFrameNanos
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.runtime.key
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset

// ПАРАМЕТРЫ
private const val PLAYER_SIZE = 50f
private const val PLAYER_HITBOX_RATIO = 0.5f
private const val ENEMY_HITBOX_RATIO = 0.5f
private const val PLAYER_HITBOX = PLAYER_SIZE * 2f * PLAYER_HITBOX_RATIO
private const val FIRE_INTERVAL = 0.15f
private const val BULLET_SPEED = 1200f
private const val BASE_SPAWN = 0.75f
private const val MIN_SPAWN = 0.22f
private const val MAX_COINS_REWARD = 20
private const val POWERUP_FALL_SPEED = 400f
private const val POWERUP_LIFETIME = 7f
private const val POWERUP_PICKUP_RADIUS = 40f
private const val POWERUP_MAX_ON_SCREEN = 4
private const val POWERUP_RADIUS = 28f

private const val SHIELD_PUSH_SPEED = 500f
private const val SHIELD_PUSH_DURATION = 0.4f

private const val SLOW_FACTOR = 0.6f

// ЭКРАН
@Composable
private fun PowerUpIcon(
    emoji: String,
    sprite: ImageBitmap?,
    ringColor: Color,
    fraction: Float
) {
    Box(
        modifier = Modifier.size(32.dp),
        contentAlignment = Alignment.Center
    ) {
        // Картинка баффа — под кольцом
        if (sprite != null) {
            androidx.compose.foundation.Image(
                bitmap = sprite,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(emoji, fontSize = 18.sp)
        }

        // Кольцо таймера — поверх
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f - 2.dp.toPx()
            val stroke = 3.dp.toPx()

            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * fraction.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun ConsoleGameScreen(
    storage: AppStorage,
    onBack: () -> Unit
) {
    var sizePx by remember { mutableStateOf(IntSize.Zero) }
    var frameTick by remember { mutableLongStateOf(0L) }
    var hudScore by remember { mutableIntStateOf(0) }
    var hudHp by remember { mutableIntStateOf(3) }
    var gameOver by remember { mutableStateOf(false) }
    var rewardGiven by remember { mutableStateOf(false) }
    var coinsEarned by remember { mutableIntStateOf(0) }

    val density = LocalDensity.current
    val bottomReservedPx = with(density) {
        WindowInsets.safeDrawing.getBottom(this).toFloat() +
                90.dp.toPx()
    }

    // Загрузка спрайтов
    val context = androidx.compose.ui.platform.LocalContext.current
    val playerSprite = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.player_ship)
            ?.asImageBitmap()
    }
    val enemySmallSprite = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.enemy_small)
            ?.asImageBitmap()
    }
    val enemyMediumSprite = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.enemy_medium)
            ?.asImageBitmap()
    }
    val enemyLargeSprite = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.enemy_large)
            ?.asImageBitmap()
    }

    // Спрайты баффов
    val buffSprites = remember {
        mapOf(
            PowerUpType.HEAL       to BitmapFactory.decodeResource(context.resources, PowerUpType.HEAL.iconRes)?.asImageBitmap(),
            PowerUpType.SHIELD     to BitmapFactory.decodeResource(context.resources, PowerUpType.SHIELD.iconRes)?.asImageBitmap(),
            PowerUpType.RAPID_FIRE to BitmapFactory.decodeResource(context.resources, PowerUpType.RAPID_FIRE.iconRes)?.asImageBitmap(),
            PowerUpType.TRIPLE     to BitmapFactory.decodeResource(context.resources, PowerUpType.TRIPLE.iconRes)?.asImageBitmap(),
            PowerUpType.PIERCE     to BitmapFactory.decodeResource(context.resources, PowerUpType.PIERCE.iconRes)?.asImageBitmap(),
            PowerUpType.SLOW       to BitmapFactory.decodeResource(context.resources, PowerUpType.SLOW.iconRes)?.asImageBitmap()
        )
    }

    val gameState = remember(sizePx, bottomReservedPx) {
        if (sizePx == IntSize.Zero) null
        else GameState(
            sizePx.width.toFloat(),
            sizePx.height.toFloat(),
            bottomReservedPx,
            playerSprite,
            enemySmallSprite,
            enemyMediumSprite,
            enemyLargeSprite,
            buffSprites
        )
    }

    // Игровой цикл
    LaunchedEffect(gameState) {
        val gs = gameState ?: return@LaunchedEffect
        var last = withFrameNanos { it }
        while (!gs.gameOver) {
            val now = withFrameNanos { it }
            val dt = ((now - last) / 1_000_000_000f).coerceAtMost(0.05f)
            last = now
            gs.update(dt)

            if (gs.score != hudScore) hudScore = gs.score
            if (gs.player.hp != hudHp) hudHp = gs.player.hp

            frameTick = now
        }
        gameOver = true
    }

    BackHandler { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A14))
            .onSizeChanged { sizePx = it }
            .pointerInput(gameState) {
                detectDragGestures { change, _ ->
                    gameState?.movePlayerTo(change.position.x)
                }
            }
            .pointerInput(gameState) {
                detectTapGestures { offset ->
                    gameState?.movePlayerTo(offset.x)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            @Suppress("UNUSED_EXPRESSION")
            frameTick
            gameState?.draw(this)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "SCORE",
                    color = Color(0xFF6A6A80),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp
                )
                Text(
                    "$hudScore",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Row {
                    val isShielded = (gameState?.shieldTimer ?: 0f) > 0f

                    repeat(3) { i ->
                        Canvas(
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .size(width = 12.dp, height = 18.dp)
                        ) {
                            val skew = size.width * 0.35f

                            val path = Path().apply {
                                moveTo(skew, 0f)
                                lineTo(size.width, 0f)
                                lineTo(size.width - skew, size.height)
                                lineTo(0f, size.height)
                                close()
                            }

                            val filledColor = if (isShielded)
                                Color(0xFF00BFFF)
                            else
                                Color(0xFFFF5A6E)

                            drawPath(
                                path = path,
                                color = if (i < hudHp) filledColor else Color(0xFF2A2A3A)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Активные баффы
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    @Suppress("UNUSED_EXPRESSION")
                    frameTick
                    val gs = gameState

                    if (gs != null && gs.shieldTimer > 0f) {
                        PowerUpIcon(
                            emoji = PowerUpType.SHIELD.emoji,
                            sprite = buffSprites[PowerUpType.SHIELD],
                            ringColor = PowerUpType.SHIELD.ringColor,
                            fraction = gs.shieldFraction()
                        )
                    }
                    if (gs != null && gs.activeSlow > 0f) {
                        PowerUpIcon(
                            emoji = PowerUpType.SLOW.emoji,
                            sprite = buffSprites[PowerUpType.SLOW],
                            ringColor = PowerUpType.SLOW.ringColor,
                            fraction = gs.slowFraction()
                        )
                    }
                    if (gs != null && gs.activeRapidFire > 0f) {
                        PowerUpIcon(
                            emoji = PowerUpType.RAPID_FIRE.emoji,
                            sprite = buffSprites[PowerUpType.RAPID_FIRE],
                            ringColor = PowerUpType.RAPID_FIRE.ringColor,
                            fraction = gs.rapidFireFraction()
                        )
                    }
                    if (gs != null && gs.activeTriple > 0f) {
                        PowerUpIcon(
                            emoji = PowerUpType.TRIPLE.emoji,
                            sprite = buffSprites[PowerUpType.TRIPLE],
                            ringColor = PowerUpType.TRIPLE.ringColor,
                            fraction = gs.tripleFraction()
                        )
                    }
                    if (gs != null && gs.activePierce > 0f) {
                        PowerUpIcon(
                            emoji = PowerUpType.PIERCE.emoji,
                            sprite = buffSprites[PowerUpType.PIERCE],
                            ringColor = PowerUpType.PIERCE.ringColor,
                            fraction = gs.pierceFraction()
                        )
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.10f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp)
                .clickable { onBack() }
        ) {
            Text(
                "Выйти",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }

        if (gameOver) {
            val score = gameState?.score ?: 0
            val coins = score.coerceAtMost(MAX_COINS_REWARD)
            LaunchedEffect(Unit) {
                if (!rewardGiven) {
                    storage.coins += coins
                    coinsEarned = coins
                    rewardGiven = true
                }
            }
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Игра окончена", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Счёт: $score", fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Монет получено: +$coinsEarned",
                            color = Color(0xFF4CAF50),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Забрать и выйти")
                    }
                }
            )
        }
    }
}

// СОСТОЯНИЕ ИГРЫ
private class GameState(
    val width: Float,
    val height: Float,
    private val bottomReservedPx: Float,
    private val playerSprite: ImageBitmap?,
    private val enemySmallSprite: ImageBitmap?,
    private val enemyMediumSprite: ImageBitmap?,
    private val enemyLargeSprite: ImageBitmap?,
    private val buffSprites: Map<PowerUpType, ImageBitmap?>
) {

    var score = 0
        private set
    var gameOver = false
        private set

    val player = Player(
        x = width / 2f,
        y = height - bottomReservedPx - PLAYER_SIZE,
        hp = 3,
        maxHp = 3
    )
    val bullets = mutableListOf<Bullet>()
    val enemies = mutableListOf<Enemy>()
    val particles = mutableListOf<Particle>()
    val stars = mutableListOf<Star>()
    val powerUps = mutableListOf<PowerUp>()

    private var elapsed = 0f
    private var fireTimer = 0f
    private var spawnTimer = 0f
    private val rng = Random(System.currentTimeMillis())

    // Время неуязвимости после удара
    private var invulnerableTimer = 0f
    private val invulnerableDuration = 1.2f

    // Активные бонусы
    var activeRapidFire = 0f
        private set
    var activeTriple = 0f
        private set
    var activePierce = 0f
        private set
    var activeSlow = 0f
        private set
    var shieldTimer = 0f
        private set

    fun rapidFireFraction(): Float =
        if (activeRapidFire <= 0f) 0f
        else activeRapidFire / PowerUpType.RAPID_FIRE.duration

    fun tripleFraction(): Float =
        if (activeTriple <= 0f) 0f
        else activeTriple / PowerUpType.TRIPLE.duration

    fun pierceFraction(): Float =
        if (activePierce <= 0f) 0f
        else activePierce / PowerUpType.PIERCE.duration

    fun slowFraction(): Float =
        if (activeSlow <= 0f) 0f
        else activeSlow / PowerUpType.SLOW.duration

    fun shieldFraction(): Float =
        if (shieldTimer <= 0f) 0f
        else shieldTimer / PowerUpType.SHIELD.duration

    init {
        repeat(70) {
            stars.add(
                Star(
                    x = rng.nextFloat() * width,
                    y = rng.nextFloat() * height,
                    size = rng.nextFloat() * 1.6f + 0.6f,
                    speed = rng.nextFloat() * 0.6f + 0.2f,
                    brightness = rng.nextFloat() * 0.5f + 0.3f
                )
            )
        }
    }

    fun update(dt: Float) {
        if (gameOver) return
        elapsed += dt

        if (invulnerableTimer > 0f) invulnerableTimer -= dt

        if (activeRapidFire > 0f) activeRapidFire = (activeRapidFire - dt).coerceAtLeast(0f)
        if (activeTriple > 0f)    activeTriple    = (activeTriple - dt).coerceAtLeast(0f)
        if (activePierce > 0f)    activePierce    = (activePierce - dt).coerceAtLeast(0f)
        if (activeSlow > 0f)      activeSlow      = (activeSlow - dt).coerceAtLeast(0f)
        if (shieldTimer > 0f)     shieldTimer     = (shieldTimer - dt).coerceAtLeast(0f)

        // Звёзды
        stars.forEach { s ->
            s.y += s.speed * 40f * dt
            if (s.y > height) {
                s.y = 0f
                s.x = rng.nextFloat() * width
            }
        }

        // Автострельба
        val fireInterval = if (activeRapidFire > 0f) FIRE_INTERVAL * 0.5f else FIRE_INTERVAL
        fireTimer += dt
        if (fireTimer >= fireInterval) {
            fireTimer = 0f
            if (activeTriple > 0f) {
                shootBullet(-20f)
                shootBullet(0f)
                shootBullet(20f)
            } else {
                shootBullet(0f)
            }
        }

        // Спавн врагов
        spawnTimer += dt
        val interval = (BASE_SPAWN - elapsed * 0.020f).coerceAtLeast(MIN_SPAWN)
        if (spawnTimer >= interval) {
            spawnTimer = 0f
            spawnEnemy()
        }

        // Прожектайлы
        bullets.forEach {
            it.x += it.vx * dt
            it.y += it.vy * dt
        }
        bullets.removeAll {
            it.y < -30f || it.x < -30f || it.x > width + 30f || it.dead
        }

        // Враги — замедление и отскок
        val enemyDt = if (activeSlow > 0f) dt * SLOW_FACTOR else dt

        enemies.forEach { e ->
            if (e.knockbackTimer > 0f) {
                // Летит по инерции отскока
                e.x += e.knockbackVx * enemyDt
                e.y += e.knockbackVy * enemyDt
                e.knockbackTimer -= dt
            } else {
                e.y += e.vy * enemyDt
                when (e.pattern) {
                    EnemyPattern.STRAIGHT -> {}
                    EnemyPattern.SINE -> e.x = e.baseX + sin(elapsed * 3f + e.phase) * 70f
                    EnemyPattern.ZIGZAG -> {
                        if (e.x < e.baseX - 80f) e.vx = abs(e.vx)
                        if (e.x > e.baseX + 80f) e.vx = -abs(e.vx)
                        e.x += e.vx * enemyDt
                    }
                    EnemyPattern.HOMING -> {
                        val dx = player.x - e.x
                        val maxDrift = 160f * enemyDt
                        e.x += dx.coerceIn(-maxDrift, maxDrift)
                    }
                }
            }
        }
        enemies.removeAll { it.y > height + 80f || it.dead }

        // Взаимодействие прожектайла с врагом
        for (b in bullets) {
            if (b.dead) continue
            for (e in enemies) {
                if (e.dead) continue
                if (abs(b.x - e.x) < e.size && abs(b.y - e.y) < e.size) {
                    val damage = if (activePierce > 0f) 2 else 1
                    val hpBefore = e.hp
                    e.hp -= damage

                    if (e.hp <= 0) {
                        e.dead = true
                        score += e.type.points
                        spawnExplosion(e.x, e.y, e.color)
                        dropPowerUpIfNeeded(e)
                    }

                    // Пробитие: только если урон строго больше исходного HP
                    val pierced = activePierce > 0f && damage > hpBefore
                    if (!pierced) {
                        b.dead = true
                        break
                    }
                }
            }
        }

        // Взаимодействие врага с персонажем игрока
        for (e in enemies) {
            if (e.dead) continue

            val eHit = e.size * ENEMY_HITBOX_RATIO

            if (abs(e.x - player.x) < eHit + PLAYER_HITBOX &&
                abs(e.y - player.y) < eHit + PLAYER_HITBOX
            ) {
                if (shieldTimer > 0f) {
                    // Щит: поглощает удар
                    shieldTimer = 0f
                    e.hp -= 1
                    spawnExplosion(e.x, e.y, Color(0xFF00BFFF))

                    // Отскок в сторону, противоположную от игрока
                    val dx = e.x - player.x
                    val dy = e.y - player.y
                    val len = kotlin.math.sqrt(dx * dx + dy * dy)
                    val dirX = if (len < 0.001f) 0f else dx / len
                    val dirY = if (len < 0.001f) -1f else dy / len

                    e.knockbackVx = dirX * SHIELD_PUSH_SPEED
                    e.knockbackVy = dirY * SHIELD_PUSH_SPEED
                    e.knockbackTimer = SHIELD_PUSH_DURATION

                    if (e.hp <= 0) {
                        e.dead = true
                        score += e.type.points
                        spawnExplosion(e.x, e.y, e.color)
                        dropPowerUpIfNeeded(e)
                    }
                } else if (invulnerableTimer <= 0f) {
                    // Обычный урон
                    e.dead = true
                    player.hp -= 1
                    invulnerableTimer = invulnerableDuration
                    spawnExplosion(e.x, e.y, e.color)

                    if (player.hp <= 0) {
                        gameOver = true
                        spawnExplosion(player.x, player.y, Color(0xFF52D6FF))
                    }
                } else {
                    // Игрок в неуязвимости
                    e.dead = true
                    spawnExplosion(e.x, e.y, e.color)
                    dropPowerUpIfNeeded(e)
                }
            }
        }

        // Частицы
        particles.forEach { p ->
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vx *= 0.96f
            p.vy *= 0.96f
            p.life -= dt
        }
        particles.removeAll { it.life <= 0f }

        // Бонусы: падают и подбираются
        powerUps.forEach { p ->
            p.y += POWERUP_FALL_SPEED * dt
            p.lifeTimer -= dt
            if (p.lifeTimer <= 0f) p.dead = true

            val dx = p.x - player.x
            val dy = p.y - player.y
            if (dx * dx + dy * dy < POWERUP_PICKUP_RADIUS * POWERUP_PICKUP_RADIUS) {
                applyPowerUp(p.type)
                p.dead = true
            }
        }
        powerUps.removeAll { it.dead || it.y > height + 50f }
    }

    fun movePlayerTo(x: Float) {
        player.x = x.coerceIn(PLAYER_SIZE, width - PLAYER_SIZE)
    }

    private fun shootBullet(angleDeg: Float) {
        val rad = Math.toRadians(angleDeg.toDouble())
        val vx = (BULLET_SPEED * kotlin.math.sin(rad)).toFloat()
        val vy = (-BULLET_SPEED * kotlin.math.cos(rad)).toFloat()
        bullets.add(Bullet(player.x, player.y - PLAYER_SIZE, vx, vy))
    }

    private fun spawnEnemy() {
        val roll = rng.nextFloat()
        val type = when {
            elapsed < 15f -> if (roll < 0.85f) EnemyType.SMALL else EnemyType.MEDIUM
            elapsed < 40f -> when {
                roll < 0.55f -> EnemyType.SMALL
                roll < 0.9f  -> EnemyType.MEDIUM
                else         -> EnemyType.LARGE
            }
            else -> when {
                roll < 0.4f  -> EnemyType.SMALL
                roll < 0.75f -> EnemyType.MEDIUM
                else         -> EnemyType.LARGE
            }
        }

        // Доля «умных» врагов
        val targetingChance = (0.25f + elapsed * 0.010f).coerceAtMost(0.75f)
        val isTargeted = rng.nextFloat() < targetingChance

        // Самонаводящиеся враги
        val canHome = elapsed > 8f
        val homingChance = if (canHome) 0.35f else 0f
        val isHoming = isTargeted && rng.nextFloat() < homingChance

        val baseX = when {
            isHoming -> {
                val side = if (rng.nextBoolean()) -1f else 1f
                (player.x + side * 120f)
                    .coerceIn(type.size, width - type.size)
            }
            isTargeted -> {
                val offset = (rng.nextFloat() - 0.5f) * 90f
                (player.x + offset).coerceIn(type.size, width - type.size)
            }
            else -> {
                // Обычный — случайное место
                type.size + rng.nextFloat() * (width - 2f * type.size)
            }
        }

        val pattern = if (isHoming) {
            EnemyPattern.HOMING
        } else {
            // Для прицельных — чаще STRAIGHT, чтобы летели именно на игрока
            if (isTargeted) {
                if (rng.nextFloat() < 0.65f) EnemyPattern.STRAIGHT
                else EnemyPattern.values().filter { it != EnemyPattern.HOMING }.random(rng)
            } else {
                EnemyPattern.values().filter { it != EnemyPattern.HOMING }.random(rng)
            }
        }

        val vxSign = if (rng.nextBoolean()) 1f else -1f

        enemies.add(
            Enemy(
                x = baseX,
                y = -type.size - 30f,
                baseX = baseX,
                vx = vxSign * type.speed * 0.6f,
                vy = type.speed,
                hp = type.hp,
                maxHp = type.hp,
                size = type.size,
                color = type.color,
                pattern = pattern,
                phase = rng.nextFloat() * 6.28f,
                type = type
            )
        )
    }

    private fun dropPowerUpIfNeeded(e: Enemy) {
        val baseChance = when (e.type) {
            EnemyType.SMALL  -> 0f
            EnemyType.MEDIUM -> 0.10f
            EnemyType.LARGE  -> 0.30f
        }
        val homingBonus = if (e.pattern == EnemyPattern.HOMING) 0.10f else 0f
        val dropChance = (baseChance + homingBonus).coerceAtMost(1f)

        if (rng.nextFloat() >= dropChance) return
        if (powerUps.size >= POWERUP_MAX_ON_SCREEN) return

        val type = rollPowerUp() ?: return
        powerUps.add(PowerUp(x = e.x, y = e.y, type = type))
    }

    private fun rollPowerUp(): PowerUpType? {
        // Исключаем то, что нельзя взять прямо сейчас
        val available = PowerUpType.values().filter { t ->
            when (t) {
                PowerUpType.HEAL   -> player.hp < player.maxHp
                PowerUpType.SHIELD -> shieldTimer <= 0f
                else               -> true
            }
        }
        if (available.isEmpty()) return null

        val totalWeight = available.sumOf { it.weight }
        var roll = rng.nextFloat() * totalWeight
        for (t in available) {
            roll -= t.weight
            if (roll <= 0f) return t
        }
        return available.last()
    }

    private fun applyPowerUp(type: PowerUpType) {
        when (type) {
            PowerUpType.HEAL -> {
                if (player.hp < player.maxHp) player.hp += 1
            }
            PowerUpType.SHIELD -> {
                if (shieldTimer <= 0f) shieldTimer = type.duration
            }
            PowerUpType.RAPID_FIRE -> {
                activeRapidFire = type.duration
                activeTriple = 0f
                activePierce = 0f
            }
            PowerUpType.TRIPLE -> {
                activeTriple = type.duration
                activeRapidFire = 0f
                activePierce = 0f
            }
            PowerUpType.PIERCE -> {
                activePierce = type.duration
                activeRapidFire = 0f
                activeTriple = 0f
            }
            PowerUpType.SLOW -> {
                activeSlow = type.duration
            }
        }
    }

    private fun spawnExplosion(x: Float, y: Float, color: Color) {
        repeat(12) {
            val a = rng.nextFloat() * 6.28f
            val sp = rng.nextFloat() * 220f + 60f
            particles.add(
                Particle(
                    x = x, y = y,
                    vx = cos(a) * sp,
                    vy = sin(a) * sp,
                    life = 0.6f,
                    maxLife = 0.6f,
                    color = color
                )
            )
        }
    }

    fun draw(scope: DrawScope) {
        scope.drawRect(
            Color(0xFF0A0A14),
            topLeft = Offset.Zero,
            size = Size(width, height)
        )

        // Звёзды
        stars.forEach { s ->
            scope.drawRect(
                Color.White.copy(alpha = s.brightness),
                topLeft = Offset(s.x, s.y),
                size = Size(s.size, s.size)
            )
        }

        // Враги
        enemies.forEach { e ->
            val sprite: ImageBitmap? = when (e.type) {
                EnemyType.SMALL  -> enemySmallSprite
                EnemyType.MEDIUM -> enemyMediumSprite
                EnemyType.LARGE  -> enemyLargeSprite
            }

            if (sprite != null) {
                scope.drawImage(
                    image = sprite,
                    dstOffset = IntOffset(
                        (e.x - e.size).toInt(),
                        (e.y - e.size).toInt()
                    ),
                    dstSize = IntSize(
                        (e.size * 2f).toInt(),
                        (e.size * 2f).toInt()
                    ),
                    filterQuality = androidx.compose.ui.graphics.FilterQuality.Low
                )
            } else {
                // Fallback: если картинка не загрузилась — рисуем как раньше
                scope.drawRect(
                    e.color,
                    topLeft = Offset(e.x - e.size, e.y - e.size),
                    size = Size(e.size * 2f, e.size * 2f)
                )
            }

            // HP-полоска
            if (e.maxHp > 1 && e.hp < e.maxHp) {
                val barW = e.size * 2f
                val hpFrac = e.hp.toFloat() / e.maxHp
                scope.drawRect(
                    Color(0x88000000),
                    topLeft = Offset(e.x - e.size, e.y - e.size - 9f),
                    size = Size(barW, 3f)
                )
                scope.drawRect(
                    Color(0xFF5DFF9E),
                    topLeft = Offset(e.x - e.size, e.y - e.size - 9f),
                    size = Size(barW * hpFrac, 3f)
                )
            }
        }

        // Прожектайлы
        val pierceActive = activePierce > 0f
        val tripleActive = activeTriple > 0f
        val rapidActive = activeRapidFire > 0f

        bullets.forEach { b ->
            when {
                // Пробивной
                pierceActive -> {
                    scope.drawRect(
                        Color(0x99FF3B30),
                        topLeft = Offset(b.x - 9f, b.y - 22f),
                        size = Size(18f, 36f)
                    )
                    scope.drawRect(
                        Color(0xFFFF5252),
                        topLeft = Offset(b.x - 4f, b.y - 16f),
                        size = Size(8f, 24f)
                    )
                    scope.drawRect(
                        Color(0xFFFFE0B2),
                        topLeft = Offset(b.x - 2f, b.y - 12f),
                        size = Size(4f, 16f)
                    )
                }

                // Тройной
                tripleActive -> {
                    scope.drawRect(
                        Color(0x99D946EF),
                        topLeft = Offset(b.x - 5f, b.y - 14f),
                        size = Size(10f, 22f)
                    )
                    scope.drawRect(
                        Color(0xFFE040FB),
                        topLeft = Offset(b.x - 2f, b.y - 10f),
                        size = Size(4f, 14f)
                    )
                }

                // Скорострельность
                rapidActive -> {
                    scope.drawRect(
                        Color(0x99FFC107),
                        topLeft = Offset(b.x - 5f, b.y - 14f),
                        size = Size(10f, 22f)
                    )
                    scope.drawRect(
                        Color(0xFFFFD54F),
                        topLeft = Offset(b.x - 2f, b.y - 10f),
                        size = Size(4f, 14f)
                    )
                }

                // Обычные
                else -> {
                    scope.drawRect(
                        Color(0x555DFF9E),
                        topLeft = Offset(b.x - 5f, b.y - 14f),
                        size = Size(10f, 22f)
                    )
                    scope.drawRect(
                        Color(0xFF9DFFBE),
                        topLeft = Offset(b.x - 2f, b.y - 10f),
                        size = Size(4f, 14f)
                    )
                }
            }
        }

        // Игрок
        if (player.hp > 0) {
            val x = player.x
            val y = player.y
            val w = PLAYER_SIZE

            // Мигание во время неуязвимости: 10 раз в секунду
            val blinking = invulnerableTimer > 0f &&
                    (invulnerableTimer * 10f).toInt() % 2 == 0

            if (playerSprite != null) {
                // Спрайт рисуем от верха до низа корабля
                if (!blinking) {
                    scope.drawImage(
                        image = playerSprite,
                        dstOffset = IntOffset(
                            (x - w).toInt(),
                            (y - w * 1.4f).toInt()
                        ),
                        dstSize = IntSize(
                            (w * 2f).toInt(),
                            (w * 2.8f).toInt()
                        ),
                        filterQuality = androidx.compose.ui.graphics.FilterQuality.Low
                    )
                }
            } else {
                val blinking = invulnerableTimer > 0f &&
                        (invulnerableTimer * 10f).toInt() % 2 == 0
                if (!blinking) {
                    val h = PLAYER_SIZE * 1.3f
                    val path = Path().apply {
                        moveTo(x, y - h)
                        lineTo(x - w, y + h * 0.6f)
                        lineTo(x - w * 0.4f, y + h * 0.3f)
                        lineTo(x - w * 0.4f, y + h * 0.7f)
                        lineTo(x + w * 0.4f, y + h * 0.7f)
                        lineTo(x + w * 0.4f, y + h * 0.3f)
                        lineTo(x + w, y + h * 0.6f)
                        close()
                    }
                    scope.drawPath(path, Color(0xFF52D6FF))
                    scope.drawPath(
                        path,
                        Color(0xFF0B4D66),
                        style = Stroke(width = 2f)
                    )
                }
            }

            // Голубой щит-купол
            if (shieldTimer > 0f) {
                val cx = player.x
                val cy = player.y
                val baseRadius = PLAYER_SIZE * 1.3f

                scope.drawCircle(
                    color = Color(0x2200E5FF),
                    radius = baseRadius + 12f,
                    center = Offset(cx, cy)
                )
                scope.drawCircle(
                    color = Color(0x3300BFFF),
                    radius = baseRadius + 6f,
                    center = Offset(cx, cy)
                )
                scope.drawCircle(
                    color = Color(0x5500BFFF),
                    radius = baseRadius,
                    center = Offset(cx, cy)
                )
                scope.drawCircle(
                    color = Color(0x3300E5FF),
                    radius = baseRadius * 0.65f,
                    center = Offset(cx, cy)
                )
                scope.drawCircle(
                    color = Color(0xCC00BFFF),
                    radius = baseRadius,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.5f)
                )
            }
        }

        // Частицы
        particles.forEach { p ->
            val a = (p.life / p.maxLife).coerceIn(0f, 1f)
            scope.drawRect(
                p.color.copy(alpha = a),
                topLeft = Offset(p.x - 2f, p.y - 2f),
                size = Size(4f, 4f)
            )
        }

        // Бонусы
        powerUps.forEach { p ->
            val sprite = buffSprites[p.type]
            if (sprite != null) {
                scope.drawImage(
                    image = sprite,
                    dstOffset = IntOffset(
                        (p.x - POWERUP_RADIUS).toInt(),
                        (p.y - POWERUP_RADIUS).toInt()
                    ),
                    dstSize = IntSize(
                        (POWERUP_RADIUS * 2f).toInt(),
                        (POWERUP_RADIUS * 2f).toInt()
                    ),
                    filterQuality = androidx.compose.ui.graphics.FilterQuality.Low
                )
            }
        }
    }
}

// ОЪЕКТЫ на экране
private class Player(var x: Float, var y: Float, var hp: Int, var maxHp: Int)
private class Bullet(
    var x: Float, var y: Float,
    var vx: Float = 0f, var vy: Float = -BULLET_SPEED,
    var dead: Boolean = false
)
private class Star(
    var x: Float, var y: Float, var size: Float,
    var speed: Float, var brightness: Float
)
private class Particle(
    var x: Float, var y: Float, var vx: Float, var vy: Float,
    var life: Float, var maxLife: Float, var color: Color
)
private class Enemy(
    var x: Float, var y: Float, var baseX: Float,
    var vx: Float, var vy: Float,
    var hp: Int, var maxHp: Int, var size: Float,
    var color: Color, var pattern: EnemyPattern, var phase: Float,
    var type: EnemyType, var dead: Boolean = false
) {
    var knockbackTimer: Float = 0f
    var knockbackVx: Float = 0f
    var knockbackVy: Float = 0f
}

private enum class EnemyPattern { STRAIGHT, SINE, ZIGZAG, HOMING }

// БОНУСЫ
private enum class PowerUpType(
    val emoji: String,
    val iconRes: Int,
    val weight: Int,
    val duration: Float,
    val ringColor: Color
) {
    HEAL      ("❤️", R.drawable.buff_heal,    25, 0f,  Color(0xFFFF3B5C)),
    SHIELD    ("🛡️", R.drawable.buff_shield,  20, 12f, Color(0xFF00BFFF)),
    RAPID_FIRE("⚡",  R.drawable.buff_rapid,   30, 8f,  Color(0xFFFFC107)),
    TRIPLE    ("🔱", R.drawable.buff_triple,  30, 8f,  Color(0xFFD946EF)),
    PIERCE    ("💥", R.drawable.buff_pierce,  12, 6f,  Color(0xFFFF3B30)),
    SLOW      ("⏳", R.drawable.buff_slow,    15, 5f,  Color(0xFF00E5FF))
}

private class PowerUp(
    var x: Float,
    var y: Float,
    val type: PowerUpType,
    var dead: Boolean = false
) {
    var lifeTimer = POWERUP_LIFETIME
}

private enum class EnemyType(
    val hp: Int,
    val size: Float,
    val speed: Float,
    val color: Color,
    val points: Int
) {
    SMALL (hp = 1, size = 30f, speed = 340f, color = Color(0xFFFF5A6E), points = 1),
    MEDIUM(hp = 3, size = 50f, speed = 260f, color = Color(0xFFFFA53D), points = 2),
    LARGE (hp = 6, size = 70f, speed = 180f, color = Color(0xFFB26BFF), points = 5)
}