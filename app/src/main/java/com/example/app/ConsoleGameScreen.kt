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

// ПАРАМЕТРЫ
private const val PLAYER_SIZE = 22f
private const val PLAYER_HITBOX = 16f
private const val FIRE_INTERVAL = 0.15f
private const val BULLET_SPEED = 1200f
private const val BASE_SPAWN = 1.0f
private const val MIN_SPAWN = 0.32f
private const val MAX_COINS_REWARD = 20

// ЭКРАН

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

    val gameState = remember(sizePx) {
        if (sizePx == IntSize.Zero) null
        else GameState(sizePx.width.toFloat(), sizePx.height.toFloat())
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
            Row {
                repeat(3) { i ->
                    Box(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(14.dp)
                            .background(
                                color = if (i < hudHp) Color(0xFFFF5A6E)
                                else Color(0xFF2A2A3A),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.10f),
            modifier = Modifier
                .align(Alignment.BottomStart)
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
private class GameState(val width: Float, val height: Float) {

    var score = 0
        private set
    var gameOver = false
        private set

    val player = Player(width / 2f, height - 180f, hp = 3, maxHp = 3)
    val bullets = mutableListOf<Bullet>()
    val enemies = mutableListOf<Enemy>()
    val particles = mutableListOf<Particle>()
    val stars = mutableListOf<Star>()

    private var elapsed = 0f
    private var fireTimer = 0f
    private var spawnTimer = 0f
    private val rng = Random(System.currentTimeMillis())

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

        // Звёзды
        stars.forEach { s ->
            s.y += s.speed * 40f * dt
            if (s.y > height) {
                s.y = 0f
                s.x = rng.nextFloat() * width
            }
        }

        // Автострельба
        fireTimer += dt
        if (fireTimer >= FIRE_INTERVAL) {
            fireTimer = 0f
            bullets.add(Bullet(player.x, player.y - PLAYER_SIZE))
        }

        // Спавн врагов
        spawnTimer += dt
        val interval = (BASE_SPAWN - elapsed * 0.015f).coerceAtLeast(MIN_SPAWN)
        if (spawnTimer >= interval) {
            spawnTimer = 0f
            spawnEnemy()
        }

        // Прожектайлы
        bullets.forEach { it.y -= BULLET_SPEED * dt }
        bullets.removeAll { it.y < -30f || it.dead }

        // Враги
        enemies.forEach { e ->
            e.y += e.vy * dt
            when (e.pattern) {
                EnemyPattern.STRAIGHT -> {}
                EnemyPattern.SINE -> e.x = e.baseX + sin(elapsed * 3f + e.phase) * 70f
                EnemyPattern.ZIGZAG -> {
                    if (e.x < e.baseX - 80f) e.vx = abs(e.vx)
                    if (e.x > e.baseX + 80f) e.vx = -abs(e.vx)
                    e.x += e.vx * dt
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
                    b.dead = true
                    e.hp -= 1
                    if (e.hp <= 0) {
                        e.dead = true
                        score += e.type.points
                        spawnExplosion(e.x, e.y, e.color)
                    }
                    break
                }
            }
        }

        // Взаимодействие врага с персонажем игрока
        for (e in enemies) {
            if (e.dead) continue
            if (abs(e.x - player.x) < e.size + PLAYER_HITBOX &&
                abs(e.y - player.y) < e.size + PLAYER_HITBOX
            ) {
                e.dead = true
                player.hp -= 1
                spawnExplosion(e.x, e.y, e.color)
                if (player.hp <= 0) {
                    gameOver = true
                    spawnExplosion(player.x, player.y, Color(0xFF52D6FF))
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
    }

    fun movePlayerTo(x: Float) {
        player.x = x.coerceIn(PLAYER_SIZE, width - PLAYER_SIZE)
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

        val pattern = EnemyPattern.values().random(rng)
        val baseX = type.size + rng.nextFloat() * (width - 2f * type.size)
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
            scope.drawRect(
                e.color,
                topLeft = Offset(e.x - e.size, e.y - e.size),
                size = Size(e.size * 2f, e.size * 2f)
            )
            scope.drawRect(
                Color(0xFF1A0A1A),
                topLeft = Offset(e.x - e.size, e.y - e.size),
                size = Size(e.size * 2f, e.size * 2f),
                style = Stroke(width = 2f)
            )
            scope.drawRect(
                Color.White,
                topLeft = Offset(e.x - e.size * 0.55f, e.y - e.size * 0.35f),
                size = Size(e.size * 0.35f, e.size * 0.35f)
            )
            scope.drawRect(
                Color.White,
                topLeft = Offset(e.x + e.size * 0.20f, e.y - e.size * 0.35f),
                size = Size(e.size * 0.35f, e.size * 0.35f)
            )
            // HP
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
        bullets.forEach { b ->
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

        // Игрок
        if (player.hp > 0) {
            val x = player.x
            val y = player.y
            val w = PLAYER_SIZE
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

            scope.drawRect(
                Color(0xFFB9F4FF),
                topLeft = Offset(x - 4f, y - h * 0.25f),
                size = Size(8f, 10f)
            )
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
    }
}

// ОЪЕКТЫ на экране
private class Player(var x: Float, var y: Float, var hp: Int, var maxHp: Int)
private class Bullet(var x: Float, var y: Float, var dead: Boolean = false)
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
)

private enum class EnemyPattern { STRAIGHT, SINE, ZIGZAG }

private enum class EnemyType(
    val hp: Int,
    val size: Float,
    val speed: Float,
    val color: Color,
    val points: Int
) {
    SMALL (hp = 1, size = 22f, speed = 260f, color = Color(0xFFFF5A6E), points = 1),
    MEDIUM(hp = 3, size = 32f, speed = 200f, color = Color(0xFFFFA53D), points = 2),
    LARGE (hp = 6, size = 46f, speed = 140f, color = Color(0xFFB26BFF), points = 5)
}