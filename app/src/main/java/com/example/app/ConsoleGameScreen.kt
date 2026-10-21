package com.example.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.foundation.gestures.awaitFirstDown
import android.graphics.Bitmap

// БОСС
private const val BOSS_TRIGGER_SCORE = 1500
private const val BOSS_HP = 700
private const val BOSS_HALF_HEIGHT = 350f
private const val BOSS_ASPECT = 656f / 476f
private const val BOSS_Y_FRACTION = 0.20f
private const val BOSS_RECOVERY_TIME = 1.5f
private const val BOSS_ENTRY_DURATION = 1.5f
private const val BOSS_DEATH_DURATION = 3f
private const val BOSS_DEATH_EXPLOSION_INTERVAL = 0.12f
private const val BOSS_VICTORY_DELAY = 2f

// Ракеты
private const val MISSILE_SPEED = 220f
private const val MISSILE_TURN = 1.4f
private const val MISSILE_HP = 2
private const val MISSILE_DROP_CHANCE = 0.35f
private const val MISSILE_HALF_HEIGHT = 64f
private const val MISSILE_ASPECT = 53f / 169f

// Веер выстрелов
private const val SPREAD_SPEED = 420f
private const val SPREAD_BULLET_RADIUS = 20f
private const val SPREAD_SALVOS = 10
private const val SPREAD_DELAY_BETWEEN = 0.7f
private const val SPREAD_FINISH_DELAY = 0.6f

// Приспешники
private const val MINION_COUNT = 4
private const val MINION_FLY_TIME = 2.2f
private const val MINION_FIRE_INTERVAL_START = 0.9f
private const val MINION_FIRE_INTERVAL_MIN = 0.15f
private const val MINION_ACCEL_START_TIME = 4f
private const val MINION_ACCEL_DURATION = 3f
private const val MINION_BULLET_SPEED = 300f
private const val MINION_BULLET_RADIUS = 12f
private const val MINION_Y_OFFSET = 60f

// Прицельные выстрелы
private const val AIMED_INTERVAL = 0.2f
private const val AIMED_DURATION = 6f
private const val AIMED_BULLET_SPEED = 350f
private const val AIMED_BULLET_RADIUS = 20f

// Цвета пуль
private val BULLET_COLOR_HALO = Color(0x66FF6F00)
private val BULLET_COLOR_MID = Color(0xAAFF3B30)
private val BULLET_COLOR_CORE = Color(0xFFFFE0B2)

// Награда за победу
private const val BOSS_VICTORY_BONUS_SCORE = 1000
private const val BOSS_VICTORY_COINS = 20

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
private const val POWERUP_PICKUP_RADIUS = 68f
private const val POWERUP_MAX_ON_SCREEN = 4
private const val POWERUP_RADIUS = 34f

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
    LaunchedEffect(Unit) {
        SoundManager.stopBgm()
    }

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
    fun loadScaled(id: Int, maxSide: Int = 128): ImageBitmap? {
        val original = BitmapFactory.decodeResource(context.resources, id) ?: return null
        if (original.width <= maxSide && original.height <= maxSide) {
            return original.asImageBitmap()
        }
        val ratio = maxSide.toFloat() / maxOf(original.width, original.height)
        val w = (original.width * ratio).toInt().coerceAtLeast(1)
        val h = (original.height * ratio).toInt().coerceAtLeast(1)
        val scaled = android.graphics.Bitmap.createScaledBitmap(original, w, h, true)
        if (scaled !== original) original.recycle()
        return scaled.asImageBitmap()
    }

    val playerSprite = remember {
        loadScaled(R.drawable.player_ship, maxSide = 256)
    }
    val enemySmallSprite = remember {
        loadScaled(R.drawable.enemy_small, maxSide = 128)
    }
    val enemyMediumSprite = remember {
        loadScaled(R.drawable.enemy_medium, maxSide = 192)
    }
    val enemyLargeSprite = remember {
        loadScaled(R.drawable.enemy_large, maxSide = 256)
    }

    val bossSprite = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.boss_ship)
            ?.asImageBitmap()
    }
    val missileSprite = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.boss_missile)
            ?.asImageBitmap()
    }

    val bossMaskBitmap: Bitmap? = remember {
        val resId = context.resources.getIdentifier(
            "boss_ship_mask", "drawable", context.packageName
        )
        if (resId != 0) BitmapFactory.decodeResource(context.resources, resId) else null
    }

    // Спрайты баффов
    val buffSprites = remember {
        fun loadScaled(id: Int, maxSide: Int = 128): ImageBitmap? {
            val original = BitmapFactory.decodeResource(context.resources, id) ?: return null
            if (original.width <= maxSide && original.height <= maxSide) {
                return original.asImageBitmap()
            }
            val ratio = maxSide.toFloat() / maxOf(original.width, original.height)
            val w = (original.width * ratio).toInt().coerceAtLeast(1)
            val h = (original.height * ratio).toInt().coerceAtLeast(1)
            val scaled = android.graphics.Bitmap.createScaledBitmap(original, w, h, true)
            if (scaled !== original) original.recycle()
            return scaled.asImageBitmap()
        }

        mapOf(
            PowerUpType.HEAL       to loadScaled(PowerUpType.HEAL.iconRes),
            PowerUpType.SHIELD     to loadScaled(PowerUpType.SHIELD.iconRes),
            PowerUpType.RAPID_FIRE to loadScaled(PowerUpType.RAPID_FIRE.iconRes),
            PowerUpType.TRIPLE     to loadScaled(PowerUpType.TRIPLE.iconRes),
            PowerUpType.PIERCE     to loadScaled(PowerUpType.PIERCE.iconRes),
            PowerUpType.SLOW       to loadScaled(PowerUpType.SLOW.iconRes)
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
            buffSprites,
            bossSprite,
            missileSprite,
            bossMaskBitmap
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
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        gameState?.isTouching = true
                        down.consume()

                        var stillPressed = true
                        while (stillPressed) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                            if (change == null || !change.pressed) {
                                stillPressed = false
                            } else {
                                if (change.position != change.previousPosition) {
                                    gameState?.movePlayerTo(change.position.x)
                                }
                                change.consume()
                            }
                        }
                        gameState?.isTouching = false
                    }
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
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Левая колонка — SCORE
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

            // Правая колонка — HP + баффы (баффы СТРОГО под HP)
            Column(horizontalAlignment = Alignment.End) {

                // HP
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

                // Баффы под HP
                Spacer(Modifier.height(8.dp))

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
                .clickable {
                    SoundManager.playSfx(R.raw.sfx_ui_tap)
                    onBack()
                }
        ) {
            Text(
                "Выйти",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }

        val shouldShowDialog = gameOver || (gameState?.showVictoryDialog == true)
        if (shouldShowDialog) {
            val baseScore = gameState?.score ?: 0
            val isVictory = gameState?.bossDefeated == true
            val finalScore = if (isVictory) baseScore + BOSS_VICTORY_BONUS_SCORE else baseScore
            val coins = if (isVictory) BOSS_VICTORY_COINS
            else baseScore.coerceAtMost(MAX_COINS_REWARD)
            LaunchedEffect(Unit) {
                if (!rewardGiven) {
                    storage.coins += coins
                    coinsEarned = coins
                    rewardGiven = true
                }
            }
            AlertDialog(
                onDismissRequest = { },
                title = {
                    Text(
                        if (isVictory) "🎉 Босс побеждён!" else "Игра окончена",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        if (isVictory) {
                            Text(
                                "Ты победил босса!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Бонус за победу: +$BOSS_VICTORY_BONUS_SCORE очков",
                                fontSize = 14.sp,
                                color = Color(0xFF4CAF50)
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        Text("Счёт: $finalScore", fontSize = 16.sp)
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
    private val buffSprites: Map<PowerUpType, ImageBitmap?>,
    private val bossSprite: ImageBitmap?,
    private val missileSprite: ImageBitmap?,
    private val bossMaskBitmap: Bitmap?
) {

    // Пиксели маски для быстрого доступа за O(1)
    private val bossMaskPixels: IntArray? = bossMaskBitmap?.let { bmp ->
        val pixels = IntArray(bmp.width * bmp.height)
        bmp.getPixels(pixels, 0, bmp.width, 0, 0, bmp.width, bmp.height)
        pixels
    }

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
    val shockwaves = mutableListOf<Shockwave>()
    val stars = mutableListOf<Star>()
    val powerUps = mutableListOf<PowerUp>()
    val bossMissiles = mutableListOf<BossMissile>()
    val bossMinions = mutableListOf<BossMinion>()
    val bossBullets = mutableListOf<BossBullet>()

    var boss: Boss? = null
        private set
    var bossDefeated = false
        private set
    private var bossSpawned = false

    private var victoryDelayTimer = 0f
    var showVictoryDialog = false
        private set

    private var elapsed = 0f
    private var fireTimer = 0f
    private var spawnTimer = 0f

    var isTouching = false
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

        if (victoryDelayTimer > 0f) {
            victoryDelayTimer -= dt
            if (victoryDelayTimer <= 0f) {
                showVictoryDialog = true
                gameOver = true
            }
        }

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

        // Стрельба
        if (isTouching) {
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
        } else {
            fireTimer = 0f
        }

        // Спавн врагов
        if (!bossSpawned) {
            if (score < BOSS_TRIGGER_SCORE) {
                spawnTimer += dt
                val interval = (BASE_SPAWN - elapsed * 0.020f).coerceAtLeast(MIN_SPAWN)
                if (spawnTimer >= interval) {
                    spawnTimer = 0f
                    spawnEnemy()
                }
            } else {
                if (enemies.isEmpty()) {
                    bossSpawned = true
                    val targetY = height * BOSS_Y_FRACTION
                    boss = Boss(
                        x = width / 2f,
                        y = -BOSS_HALF_HEIGHT - 50f,   // стартует за экраном
                        hp = BOSS_HP,
                        maxHp = BOSS_HP
                    ).also {
                        it.entryTimer = BOSS_ENTRY_DURATION
                        it.targetY = targetY
                    }
                }
            }
        } else {
            updateBoss(dt)
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

        // Ударные волны
        shockwaves.forEach { sw ->
            sw.life -= dt
            val t = 1f - (sw.life / sw.maxLife).coerceIn(0f, 1f)
            sw.radius = 10f + (sw.maxRadius - 10f) * t
        }
        shockwaves.removeAll { it.life <= 0f }

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

    // Точная проверка попадания по маске босса.
    // Если маски нет — возвращает false, тогда используется fallback-прямоугольник.
    private fun bossHitAt(bx: Float, by: Float): Boolean {
        val mask = bossMaskBitmap ?: return false
        val pixels = bossMaskPixels ?: return false
        val b = boss ?: return false
        val halfW = BOSS_HALF_HEIGHT * BOSS_ASPECT

        val xRatio = (bx - (b.x - halfW)) / (halfW * 2f)
        val yRatio = (by - (b.y - BOSS_HALF_HEIGHT)) / (BOSS_HALF_HEIGHT * 2f)

        if (xRatio < 0f || xRatio > 1f || yRatio < 0f || yRatio > 1f) return false

        val px = (xRatio * mask.width).toInt().coerceIn(0, mask.width - 1)
        val py = (yRatio * mask.height).toInt().coerceIn(0, mask.height - 1)
        return android.graphics.Color.alpha(pixels[py * mask.width + px]) > 0
    }

    private fun bulletHitsBoss(bul: Bullet): Boolean {
        val b = boss ?: return false
        if (bossMaskPixels != null) {
            // Пульс летит вверх — проверяем верхнюю часть снаряда
            if (bossHitAt(bul.x, bul.y)) return true
            if (bossHitAt(bul.x, bul.y - 10f)) return true
            if (bossHitAt(bul.x, bul.y - 20f)) return true
            return false
        }
        // Fallback: прямоугольник
        val halfW = BOSS_HALF_HEIGHT * BOSS_ASPECT
        return abs(bul.x - b.x) < halfW * 0.9f &&
                abs(bul.y - b.y) < BOSS_HALF_HEIGHT * 0.9f
    }

    // БОСС
    private fun updateBoss(dt: Float) {
        val b = boss ?: return
        if (b.dead) return

        if (!b.entered) {
            b.entryTimer -= dt
            val t = 1f - (b.entryTimer / BOSS_ENTRY_DURATION).coerceIn(0f, 1f)
            // Плавная кривая: slow start → fast → slow end
            val easeT = t * t * (3f - 2f * t)
            val startY = -BOSS_HALF_HEIGHT - 50f
            b.y = startY + (b.targetY - startY) * easeT

            if (b.entryTimer <= 0f) {
                b.y = b.targetY
                b.entered = true
                b.phase = BossPhase.RECOVERY
                b.phaseTimer = 0f
            }
            return
        }

        if (b.dying) {
            b.deathTimer -= dt
            b.deathExplosionTimer -= dt

            if (b.deathExplosionTimer <= 0f) {
                b.deathExplosionTimer = BOSS_DEATH_EXPLOSION_INTERVAL
                val halfW = BOSS_HALF_HEIGHT * BOSS_ASPECT
                repeat(3) {
                    val ex = b.x + (rng.nextFloat() - 0.5f) * halfW * 2f
                    val ey = b.y + (rng.nextFloat() - 0.5f) * BOSS_HALF_HEIGHT * 2f
                    spawnExplosion(ex, ey, Color(0xFFFFA53D))
                }
            }

            if (b.deathTimer <= 0f) {
                val halfW = BOSS_HALF_HEIGHT * BOSS_ASPECT

                repeat(12) {
                    val ex = b.x + (rng.nextFloat() - 0.5f) * halfW * 2f
                    val ey = b.y + (rng.nextFloat() - 0.5f) * BOSS_HALF_HEIGHT * 2f
                    spawnExplosion(ex, ey, Color(0xFFFFA53D))
                }

                spawnBigExplosion(b.x, b.y)

                spawnShockwave(b.x, b.y)

                b.dead = true
                bossDefeated = true
                victoryDelayTimer = BOSS_VICTORY_DELAY
            }

            bossMissiles.forEach { m ->
                m.x += m.vx * dt; m.y += m.vy * dt
            }
            bossBullets.forEach { bb ->
                bb.x += bb.vx * dt; bb.y += bb.vy * dt
            }
            return
        }

        when (b.phase) {
            BossPhase.RECOVERY -> {
                b.phaseTimer += dt
                if (b.phaseTimer >= BOSS_RECOVERY_TIME) {
                    b.phase = BossPhase.ATTACK
                    b.phaseTimer = 0f
                    b.attackElapsed = 0f
                    b.shotTimer = 0f
                    b.shotsFired = 0
                    b.currentAttack = pickNextAttack(b.lastAttack)
                    b.lastAttack = b.currentAttack
                    startAttack(b)
                }
            }
            BossPhase.ATTACK -> {
                b.attackElapsed += dt
                when (b.currentAttack) {
                    BossAttack.MISSILES -> tickMissilesAttack(b)
                    BossAttack.SPREAD   -> tickSpreadAttack(b, dt)
                    BossAttack.MINIONS  -> tickMinionsAttack(b)
                    BossAttack.AIMED    -> tickAimedAttack(b, dt)
                }
            }
        }

        // Ракеты самонаводящиеся
        bossMissiles.forEach { m ->
            val dx = player.x - m.x
            val dy = player.y - m.y
            val len = kotlin.math.sqrt(dx * dx + dy * dy)
            if (len > 0.01f) {
                val tvx = dx / len * MISSILE_SPEED
                val tvy = dy / len * MISSILE_SPEED
                m.vx += (tvx - m.vx) * MISSILE_TURN * dt
                m.vy += (tvy - m.vy) * MISSILE_TURN * dt
            }
            m.x += m.vx * dt
            m.y += m.vy * dt
        }
        bossMissiles.removeAll {
            it.dead || it.y > height + 100f || it.y < -100f ||
                    it.x < -100f || it.x > width + 100f
        }

        // Снаряды
        bossBullets.forEach { bb ->
            bb.x += bb.vx * dt
            bb.y += bb.vy * dt
        }
        bossBullets.removeAll {
            it.dead || it.y > height + 60f || it.y < -60f ||
                    it.x < -60f || it.x > width + 60f
        }

        // Миньоны
        bossMinions.forEach { m ->
            if (m.dead) return@forEach
            m.ageTimer += dt
            if (!m.landed) {
                val t = (m.ageTimer / MINION_FLY_TIME).coerceIn(0f, 1f)
                m.y = -50f + (m.targetY - (-50f)) * t
                if (t >= 1f) {
                    m.landed = true
                    m.shootTimer = 0f
                }
            } else {
                val accelStart = MINION_ACCEL_START_TIME
                val interval = if (m.ageTimer < accelStart) {
                    MINION_FIRE_INTERVAL_START
                } else {
                    val k = ((m.ageTimer - accelStart) / MINION_ACCEL_DURATION)
                        .coerceIn(0f, 1f)
                    MINION_FIRE_INTERVAL_START +
                            (MINION_FIRE_INTERVAL_MIN - MINION_FIRE_INTERVAL_START) * k
                }
                m.shootTimer += dt
                if (m.shootTimer >= interval) {
                    m.shootTimer = 0f
                    fireMinionBullet(m)
                }
            }
        }

        // Пули игрока по ракетам
        for (bul in bullets) {
            if (bul.dead) continue
            for (m in bossMissiles) {
                if (m.dead) continue
                if (abs(bul.x - m.x) < 22f && abs(bul.y - m.y) < 22f) {
                    bul.dead = true
                    m.hp -= 1
                    if (m.hp <= 0) {
                        m.dead = true
                        spawnExplosion(m.x, m.y, Color(0xFFFF5252))
                        if (rng.nextFloat() < MISSILE_DROP_CHANCE) {
                            val t = rollPowerUp()
                            if (t != null && powerUps.size < POWERUP_MAX_ON_SCREEN) {
                                powerUps.add(PowerUp(x = m.x, y = m.y, type = t))
                            }
                        }
                    }
                    break
                }
            }
        }

        // Пули игрока по приспешникам
        for (bul in bullets) {
            if (bul.dead) continue
            for (m in bossMinions) {
                if (m.dead || !m.landed) continue
                if (abs(bul.x - m.x) < EnemyType.SMALL.size &&
                    abs(bul.y - m.y) < EnemyType.SMALL.size) {
                    bul.dead = true
                    m.dead = true
                    score += EnemyType.SMALL.points
                    spawnExplosion(m.x, m.y, EnemyType.SMALL.color)
                    break
                }
            }
        }

        // Пули игрока по боссу
        for (bul in bullets) {
            if (bul.dead || bul.hitBoss) continue
            if (bulletHitsBoss(bul)) {
                bul.hitBoss = true

                val damage = if (activePierce > 0f) 2 else 1
                b.hp -= damage
                if (b.hp <= 0 && !b.dying) {
                    b.dying = true
                    b.deathTimer = BOSS_DEATH_DURATION
                    b.deathExplosionTimer = 0f
                }
                if (activePierce <= 0f) bul.dead = true
            }
        }

        // Ракеты в игрока
        for (m in bossMissiles) {
            if (m.dead) continue
            val dx = m.x - player.x
            val dy = m.y - player.y
            if (dx * dx + dy * dy < PLAYER_HITBOX * PLAYER_HITBOX * 4f) {
                m.dead = true
                spawnExplosion(m.x, m.y, Color(0xFFFF5252))
                damagePlayer()
            }
        }

        // Снаряды в игрока
        for (bb in bossBullets) {
            if (bb.dead) continue
            val dx = bb.x - player.x
            val dy = bb.y - player.y
            val r = bb.radius + PLAYER_HITBOX
            if (dx * dx + dy * dy < r * r) {
                bb.dead = true
                spawnExplosion(bb.x, bb.y, Color(0xFFFFA53D))
                damagePlayer()
            }
        }

        // Приспешники
        for (m in bossMinions) {
            if (m.dead || !m.landed) continue
            val dx = m.x - player.x
            val dy = m.y - player.y
            val r = EnemyType.SMALL.size + PLAYER_HITBOX
            if (dx * dx + dy * dy < r * r) {
                m.dead = true
                spawnExplosion(m.x, m.y, EnemyType.SMALL.color)
                damagePlayer()
            }
        }
    }

    private fun damagePlayer() {
        if (shieldTimer > 0f) {
            shieldTimer = 0f
            spawnExplosion(player.x, player.y, Color(0xFF00BFFF))
        } else if (invulnerableTimer <= 0f) {
            player.hp -= 1
            invulnerableTimer = invulnerableDuration
            if (player.hp <= 0) {
                gameOver = true
                spawnExplosion(player.x, player.y, Color(0xFF52D6FF))
            }
        }
    }

    private fun pickNextAttack(last: BossAttack?): BossAttack {
        val all = BossAttack.values().toList()
        val filtered = if (last != null) all.filter { it != last } else all
        return filtered.random(rng)
    }

    private fun startAttack(b: Boss) {
        when (b.currentAttack) {
            BossAttack.MISSILES -> {
                repeat(4) { i ->
                    val angleRad = Math.toRadians((i * 90f).toDouble())
                    val spawnR = BOSS_HALF_HEIGHT * BOSS_ASPECT * 0.6f
                    val sx = b.x + kotlin.math.cos(angleRad).toFloat() * spawnR
                    val sy = b.y + kotlin.math.sin(angleRad).toFloat() * BOSS_HALF_HEIGHT * 0.6f
                    bossMissiles.add(
                        BossMissile(x = sx, y = sy, vx = 0f, vy = 80f, hp = MISSILE_HP)
                    )
                }
            }
            BossAttack.SPREAD -> {
                fireSpreadSalvo(b)
                b.shotsFired = 1
                b.shotTimer = 0f
            }
            BossAttack.MINIONS -> {
                bossMinions.clear()
                val positions = (0 until MINION_COUNT).map { i ->
                    (i + 0.5f) / MINION_COUNT * width
                }
                val targetY = b.y + BOSS_HALF_HEIGHT + MINION_Y_OFFSET
                positions.forEach { px ->
                    bossMinions.add(
                        BossMinion(x = px, targetY = targetY)
                    )
                }
            }
            BossAttack.AIMED -> {
                b.shotTimer = AIMED_INTERVAL
            }
        }
    }

    private fun tickMissilesAttack(b: Boss) {
        if (bossMissiles.isEmpty()) finishAttack(b)
    }

    private fun tickSpreadAttack(b: Boss, dt: Float) {
        b.shotTimer += dt

        if (b.shotsFired >= SPREAD_SALVOS) {
            if (b.shotTimer >= SPREAD_FINISH_DELAY) {
                finishAttack(b)
            }
            return
        }

        if (b.shotTimer >= SPREAD_DELAY_BETWEEN) {
            fireSpreadSalvo(b)
            b.shotsFired++
            b.shotTimer = 0f
        }
    }

    private fun tickMinionsAttack(b: Boss) {
        if (bossMinions.all { it.dead }) {
            bossMinions.clear()
            finishAttack(b)
        }
    }

    private fun tickAimedAttack(b: Boss, dt: Float) {
        b.shotTimer += dt
        if (b.shotTimer >= AIMED_INTERVAL) {
            b.shotTimer = 0f
            fireAimedShot(b)
        }
        if (b.attackElapsed >= AIMED_DURATION) finishAttack(b)
    }

    private fun finishAttack(b: Boss) {
        b.phase = BossPhase.RECOVERY
        b.phaseTimer = 0f
        b.shotTimer = 0f
        b.shotsFired = 0
        b.attackElapsed = 0f
    }

    private fun fireSpreadSalvo(b: Boss) {
        val angles = listOf(-25f, -12f, 0f, 12f, 25f)
        for (deg in angles) {
            val rad = Math.toRadians(deg.toDouble())
            bossBullets.add(
                BossBullet(
                    x = b.x,
                    y = b.y + BOSS_HALF_HEIGHT * 0.5f,
                    vx = (SPREAD_SPEED * kotlin.math.sin(rad)).toFloat(),
                    vy = (SPREAD_SPEED * kotlin.math.cos(rad)).toFloat(),
                    radius = SPREAD_BULLET_RADIUS
                )
            )
        }
    }

    private fun fireAimedShot(b: Boss) {
        val dx = player.x - b.x
        val dy = player.y - b.y
        val len = kotlin.math.sqrt(dx * dx + dy * dy)
        if (len < 0.01f) return
        bossBullets.add(
            BossBullet(
                x = b.x,
                y = b.y + BOSS_HALF_HEIGHT * 0.5f,
                vx = dx / len * AIMED_BULLET_SPEED,
                vy = dy / len * AIMED_BULLET_SPEED,
                radius = AIMED_BULLET_RADIUS
            )
        )
    }

    private fun fireMinionBullet(m: BossMinion) {
        val dx = player.x - m.x
        val dy = player.y - m.y
        val len = kotlin.math.sqrt(dx * dx + dy * dy)
        if (len < 0.01f) return
        bossBullets.add(
            BossBullet(
                x = m.x,
                y = m.y,
                vx = dx / len * MINION_BULLET_SPEED,
                vy = dy / len * MINION_BULLET_SPEED,
                radius = MINION_BULLET_RADIUS
            )
        )
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

    private fun spawnBigExplosion(x: Float, y: Float) {
        val colors = listOf(
            Color(0xFFFFF3E0),
            Color(0xFFFFD54F),
            Color(0xFFFFA53D),
            Color(0xFFFF5252),
            Color(0xFFFF3B30)
        )

        repeat(30) {
            val a = rng.nextFloat() * 6.28f
            val sp = rng.nextFloat() * 100f + 350f    // быстрые
            particles.add(
                Particle(
                    x = x, y = y,
                    vx = cos(a) * sp,
                    vy = sin(a) * sp,
                    life = 0.9f, maxLife = 0.9f,
                    color = colors[rng.nextInt(colors.size)],
                    size = rng.nextFloat() * 8f + 8f   // крупные 8–16
                )
            )
        }

        repeat(50) {
            val a = rng.nextFloat() * 6.28f
            val sp = rng.nextFloat() * 200f + 150f
            particles.add(
                Particle(
                    x = x, y = y,
                    vx = cos(a) * sp,
                    vy = sin(a) * sp,
                    life = 0.7f, maxLife = 0.7f,
                    color = colors[rng.nextInt(colors.size)],
                    size = rng.nextFloat() * 5f + 3f    // средние 3–8
                )
            )
        }

        repeat(80) {
            val a = rng.nextFloat() * 6.28f
            val sp = rng.nextFloat() * 400f + 100f
            particles.add(
                Particle(
                    x = x, y = y,
                    vx = cos(a) * sp,
                    vy = sin(a) * sp,
                    life = 0.5f, maxLife = 0.5f,
                    color = colors[rng.nextInt(colors.size)],
                    size = rng.nextFloat() * 2f + 1.5f  // мелкие 1.5–3.5
                )
            )
        }

        repeat(40) {
            val a = rng.nextFloat() * 6.28f
            val sp = rng.nextFloat() * 250f + 80f
            particles.add(
                Particle(
                    x = x, y = y,
                    vx = cos(a) * sp,
                    vy = sin(a) * sp,
                    life = 1.4f, maxLife = 1.4f,
                    color = Color(0xFFFFEB3B),
                    size = 2f
                )
            )
        }
    }

    private fun spawnShockwave(x: Float, y: Float) {
        shockwaves.add(Shockwave(x = x, y = y))
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

        // БОСС
        boss?.let { b ->
            if (!b.dead) {
                val halfW = BOSS_HALF_HEIGHT * BOSS_ASPECT
                val deathProgress = if (b.dying)
                    (1f - b.deathTimer / BOSS_DEATH_DURATION).coerceIn(0f, 1f)
                else 0f
                val scale = if (b.dying) (1f - deathProgress * 0.4f) else 1f
                val alpha = if (b.dying) (1f - deathProgress * 0.5f) else 1f
                val blink = if (b.dying)
                    (b.deathTimer * 15f).toInt() % 2 == 0
                else false

                val drawW = halfW * scale
                val drawH = BOSS_HALF_HEIGHT * scale

                if (bossSprite != null && !blink) {
                    scope.drawImage(
                        image = bossSprite,
                        dstOffset = IntOffset(
                            (b.x - drawW).toInt(),
                            (b.y - drawH).toInt()
                        ),
                        dstSize = IntSize(
                            (drawW * 2f).toInt(),
                            (drawH * 2f).toInt()
                        ),
                        alpha = alpha,
                        filterQuality = androidx.compose.ui.graphics.FilterQuality.Low
                    )
                } else if (bossSprite == null) {
                    scope.drawRect(
                        Color(0xFFB26BFF),
                        topLeft = Offset(b.x - halfW, b.y - BOSS_HALF_HEIGHT),
                        size = Size(halfW * 2f, BOSS_HALF_HEIGHT * 2f)
                    )
                }

                val barW = width * 0.7f
                val barH = 12f
                val barX = (width - barW) / 2f
                val barY = 20f
                scope.drawRect(
                    Color(0x88000000),
                    topLeft = Offset(barX, barY),
                    size = Size(barW, barH)
                )
                val hpFrac = (b.hp.toFloat() / b.maxHp).coerceIn(0f, 1f)
                scope.drawRect(
                    Color(0xFFE53935),
                    topLeft = Offset(barX, barY),
                    size = Size(barW * hpFrac, barH)
                )
                scope.drawRect(
                    Color.White,
                    topLeft = Offset(barX, barY),
                    size = Size(barW, barH),
                    style = Stroke(width = 2f)
                )
            }
        }

        // Ракеты
        bossMissiles.forEach { m ->
            if (m.dead) return@forEach
            val halfH = MISSILE_HALF_HEIGHT
            val halfW = halfH * MISSILE_ASPECT
            val speed = kotlin.math.sqrt(m.vx * m.vx + m.vy * m.vy)
            if (missileSprite != null && speed > 0.01f) {
                val angleRad = kotlin.math.atan2(m.vy.toDouble(), m.vx.toDouble())
                val angleDeg = Math.toDegrees(angleRad).toFloat() - 90f
                scope.rotate(
                    degrees = angleDeg,
                    pivot = Offset(m.x, m.y)
                ) {
                    drawImage(
                        image = missileSprite,
                        dstOffset = IntOffset(
                            (m.x - halfW).toInt(),
                            (m.y - halfH).toInt()
                        ),
                        dstSize = IntSize(
                            (halfW * 2f).toInt(),
                            (halfH * 2f).toInt()
                        ),
                        filterQuality = androidx.compose.ui.graphics.FilterQuality.Low
                    )
                }
            } else {
                scope.drawRect(
                    Color(0xFFFF5252),
                    topLeft = Offset(m.x - 10f, m.y - 10f),
                    size = Size(20f, 20f)
                )
            }
        }

        // Приспешники
        bossMinions.forEach { m ->
            if (m.dead) return@forEach
            if (enemySmallSprite != null) {
                scope.drawImage(
                    image = enemySmallSprite,
                    dstOffset = IntOffset(
                        (m.x - EnemyType.SMALL.size).toInt(),
                        (m.y - EnemyType.SMALL.size).toInt()
                    ),
                    dstSize = IntSize(
                        (EnemyType.SMALL.size * 2f).toInt(),
                        (EnemyType.SMALL.size * 2f).toInt()
                    ),
                    filterQuality = androidx.compose.ui.graphics.FilterQuality.Low
                )
            } else {
                scope.drawRect(
                    EnemyType.SMALL.color,
                    topLeft = Offset(m.x - EnemyType.SMALL.size, m.y - EnemyType.SMALL.size),
                    size = Size(EnemyType.SMALL.size * 2f, EnemyType.SMALL.size * 2f)
                )
            }
        }

        // Снаряды босса и приспешников
        bossBullets.forEach { bb ->
            if (bb.dead) return@forEach
            val r = bb.radius
            scope.drawCircle(
                color = BULLET_COLOR_HALO,
                radius = r * 1.6f,
                center = Offset(bb.x, bb.y)
            )
            scope.drawCircle(
                color = BULLET_COLOR_MID,
                radius = r,
                center = Offset(bb.x, bb.y)
            )
            scope.drawCircle(
                color = BULLET_COLOR_CORE,
                radius = r * 0.4f,
                center = Offset(bb.x, bb.y)
            )
        }

        // Частицы
        particles.forEach { p ->
            val a = (p.life / p.maxLife).coerceIn(0f, 1f)
            val s = p.size
            scope.drawRect(
                p.color.copy(alpha = a),
                topLeft = Offset(p.x - s / 2f, p.y - s / 2f),
                size = Size(s, s)
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

        // Ударные волны
        shockwaves.forEach { sw ->
            val lifeT = (sw.life / sw.maxLife).coerceIn(0f, 1f)
            val alpha = (lifeT * 0.9f).coerceIn(0f, 1f)
            val strokeW = 6f * lifeT + 2f

            scope.drawCircle(
                color = sw.color.copy(alpha = alpha),
                radius = sw.radius,
                center = Offset(sw.x, sw.y),
                style = Stroke(width = strokeW)
            )
            scope.drawCircle(
                color = Color(0xFFFFF3E0).copy(alpha = (alpha * 0.5f)),
                radius = sw.radius + 8f,
                center = Offset(sw.x, sw.y),
                style = Stroke(width = strokeW * 0.5f)
            )
        }
    }
}

// ОЪЕКТЫ на экране
private class Player(var x: Float, var y: Float, var hp: Int, var maxHp: Int)
private class Bullet(
    var x: Float, var y: Float,
    var vx: Float = 0f, var vy: Float = -BULLET_SPEED,
    var dead: Boolean = false,
    var hitBoss: Boolean = false
)
private class Star(
    var x: Float, var y: Float, var size: Float,
    var speed: Float, var brightness: Float
)
private class Particle(
    var x: Float, var y: Float, var vx: Float, var vy: Float,
    var life: Float, var maxLife: Float, var color: Color,
    var size: Float = 4f
)

private class Shockwave(
    var x: Float, var y: Float,
    var radius: Float = 10f,
    var maxRadius: Float = 600f,
    var life: Float = 0.8f,
    var maxLife: Float = 0.8f,
    var color: Color = Color(0xFFFFA53D)
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

// ─── БОСС ───
private enum class BossAttack { MISSILES, SPREAD, MINIONS, AIMED }

private enum class BossPhase { RECOVERY, ATTACK }

private class Boss(
    var x: Float,
    var y: Float,
    var hp: Int,
    var maxHp: Int,
    var dead: Boolean = false
) {
    var entryTimer: Float = 0f
    var targetY: Float = y
    var entered: Boolean = false

    var phase: BossPhase = BossPhase.RECOVERY
    var phaseTimer: Float = 0f
    var currentAttack: BossAttack = BossAttack.MISSILES
    var lastAttack: BossAttack? = null
    var shotTimer: Float = 0f
    var shotsFired: Int = 0
    var attackElapsed: Float = 0f
    var dying: Boolean = false
    var deathTimer: Float = 0f
    var deathExplosionTimer: Float = 0f
}

private class BossMissile(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var hp: Int,
    var dead: Boolean = false
)

private class BossMinion(
    var x: Float,
    var targetY: Float,
    var y: Float = -50f,
    var landed: Boolean = false,
    var ageTimer: Float = 0f,
    var shootTimer: Float = 0f,
    var dead: Boolean = false
)

private class BossBullet(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float,
    var dead: Boolean = false
)

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