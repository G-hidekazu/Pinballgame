package com.example.pinball

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val FRAME_DELAY_MS = 16L
private const val GRAVITY = 1800f

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF001F4A)) {
                    PinballScreen()
                }
            }
        }
    }
}

data class Ball(var position: Offset, var velocity: Offset, val radius: Float = 14f)

data class Flipper(
    val pivot: Offset,
    val length: Float,
    val restAngle: Float,
    val activeAngle: Float,
    val clockwise: Boolean
)

@Composable
fun PinballScreen() {
    var ball by remember { mutableStateOf(Ball(position = Offset(180f, 300f), velocity = Offset.Zero)) }
    var plungerPull by remember { mutableStateOf(0f) }
    var leftActive by remember { mutableStateOf(false) }
    var rightActive by remember { mutableStateOf(false) }
    val plungerAnim = remember { Animatable(0f) }

    val flipperLength = 120f
    val leftFlipper = remember {
        Flipper(pivot = Offset(170f, 1100f), length = flipperLength, restAngle = 25f, activeAngle = -40f, clockwise = true)
    }
    val rightFlipper = remember {
        Flipper(pivot = Offset(420f, 1100f), length = flipperLength, restAngle = 155f, activeAngle = 220f, clockwise = false)
    }

    LaunchedEffect(plungerPull) {
        plungerAnim.animateTo(plungerPull, animationSpec = tween(durationMillis = 120))
    }

    LaunchedEffect(Unit) {
        while (true) {
            ball = stepGame(ball, leftFlipper, rightFlipper, leftActive, rightActive)
            delay(FRAME_DELAY_MS)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mini Pinball",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            GameTable(
                ball = ball,
                leftFlipper = leftFlipper,
                rightFlipper = rightFlipper,
                leftActive = leftActive,
                rightActive = rightActive,
                plungerAmount = plungerAnim.value,
                onTapLeft = { leftActive = true },
                onReleaseLeft = { leftActive = false },
                onTapRight = { rightActive = true },
                onReleaseRight = { rightActive = false }
            )
        }
        ControlRow(
            plungerAmount = plungerPull,
            onPlungerChange = { plungerPull = min(1f, it) },
            onLaunch = {
                ball = ball.copy(velocity = ball.velocity.copy(y = ball.velocity.y - 1200f * (0.5f + plungerPull)))
                plungerPull = 0f
            },
            onLeftPressed = { leftActive = true },
            onLeftReleased = { leftActive = false },
            onRightPressed = { rightActive = true },
            onRightReleased = { rightActive = false }
        )
    }
}

private fun stepGame(
    ball: Ball,
    leftFlipper: Flipper,
    rightFlipper: Flipper,
    leftActive: Boolean,
    rightActive: Boolean,
): Ball {
    var newBall = ball.copy()
    val dt = FRAME_DELAY_MS / 1000f

    newBall.velocity = newBall.velocity.copy(y = newBall.velocity.y + GRAVITY * dt)
    newBall.position += newBall.velocity * dt

    // Walls
    val boundsLeft = 60f
    val boundsRight = 520f
    val boundsTop = 120f
    val boundsBottom = 1220f

    if (newBall.position.x - newBall.radius < boundsLeft) {
        newBall = newBall.copy(
            position = Offset(boundsLeft + newBall.radius, newBall.position.y),
            velocity = newBall.velocity.copy(x = newBall.velocity.x.absoluteBounce())
        )
    }
    if (newBall.position.x + newBall.radius > boundsRight) {
        newBall = newBall.copy(
            position = Offset(boundsRight - newBall.radius, newBall.position.y),
            velocity = newBall.velocity.copy(x = -newBall.velocity.x.absoluteBounce())
        )
    }
    if (newBall.position.y - newBall.radius < boundsTop) {
        newBall = newBall.copy(
            position = Offset(newBall.position.x, boundsTop + newBall.radius),
            velocity = newBall.velocity.copy(y = newBall.velocity.y.absoluteBounce())
        )
    }

    // Floor reset
    if (newBall.position.y - newBall.radius > boundsBottom) {
        newBall = Ball(position = Offset(420f, 1080f), velocity = Offset.Zero)
    }

    // Flipper collisions
    val leftAngle = if (leftActive) leftFlipper.activeAngle else leftFlipper.restAngle
    val rightAngle = if (rightActive) rightFlipper.activeAngle else rightFlipper.restAngle

    newBall = collideFlipper(newBall, leftFlipper, leftAngle)
    newBall = collideFlipper(newBall, rightFlipper, rightAngle)

    return newBall
}

private fun collideFlipper(ball: Ball, flipper: Flipper, angle: Float): Ball {
    val rad = angle / 180f * PI.toFloat()
    val end = flipper.pivot + Offset(cos(rad) * flipper.length, sin(rad) * flipper.length)
    val closest = closestPointOnSegment(ball.position, flipper.pivot, end)
    val dist = (ball.position - closest).getDistance()
    if (dist < ball.radius + 6f) {
        val normal = (ball.position - closest).normalize()
        val projected = ball.velocity.dot(normal)
        val newVelocity = ball.velocity - normal * (projected * 1.6f)
        val kick = normal * 450f
        return ball.copy(
            position = closest + normal * (ball.radius + 8f),
            velocity = newVelocity + kick
        )
    }
    return ball
}

@Composable
private fun GameTable(
    ball: Ball,
    leftFlipper: Flipper,
    rightFlipper: Flipper,
    leftActive: Boolean,
    rightActive: Boolean,
    plungerAmount: Float,
    onTapLeft: () -> Unit,
    onReleaseLeft: () -> Unit,
    onTapRight: () -> Unit,
    onReleaseRight: () -> Unit,
) {
    val tableWidth = 600.dp
    val tableHeight = 1300.dp

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001F4A))
            .pointerInput(Unit) {
                detectTapGestures(onPress = { offset ->
                    if (offset.x < size.width / 2) {
                        onTapLeft()
                        tryAwaitRelease()
                        onReleaseLeft()
                    } else {
                        onTapRight()
                        tryAwaitRelease()
                        onReleaseRight()
                    }
                })
            }
            .align(Alignment.Center)
    ) {
        val scaleX = size.width / tableWidth.toPx()
        val scaleY = size.height / tableHeight.toPx()
        val scale = min(scaleX, scaleY)

        with(drawContext.canvas) {
            save()
            scale(scale, scale)

            // Playfield border
            drawRoundRect(
                color = Color(0xFF1F9CF0),
                size = androidx.compose.ui.geometry.Size(tableWidth.toPx(), tableHeight.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f),
                style = Stroke(width = 12f)
            )

            // Plunger lane
            drawRoundRect(
                color = Color(0xFF1565C0),
                topLeft = Offset(540f, 100f),
                size = androidx.compose.ui.geometry.Size(40f, 1100f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
            )

            // Bumpers
            val bumperPositions = listOf(
                Offset(180f, 300f), Offset(260f, 240f), Offset(340f, 300f)
            )
            bumperPositions.forEach { bumper ->
                drawCircle(color = Color(0xFFFFB300), radius = 26f, center = bumper, style = Stroke(width = 8f))
            }

            // Drop targets
            val targetPositions = listOf(Offset(120f, 480f), Offset(220f, 520f), Offset(320f, 480f))
            targetPositions.forEach {
                drawRoundRect(
                    color = Color(0xFF66BB6A),
                    topLeft = it - Offset(18f, 12f),
                    size = androidx.compose.ui.geometry.Size(36f, 24f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
            }

            // Plunger indicator
            val plungerTop = 1120f - plungerAmount * 160f
            drawRoundRect(
                color = Color(0xFFEF6C00),
                topLeft = Offset(546f, plungerTop),
                size = androidx.compose.ui.geometry.Size(28f, 120f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
            )

            // Flippers
            val leftAngle = if (leftActive) leftFlipper.activeAngle else leftFlipper.restAngle
            val rightAngle = if (rightActive) rightFlipper.activeAngle else rightFlipper.restAngle
            drawFlipper(leftFlipper, leftAngle)
            drawFlipper(rightFlipper, rightAngle)

            // Ball
            drawCircle(color = Color.White, radius = ball.radius, center = ball.position)

            restore()
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlipper(
    flipper: Flipper,
    angle: Float,
) {
    rotate(degrees = angle, pivot = flipper.pivot) {
        drawRoundRect(
            color = Color.White,
            topLeft = flipper.pivot,
            size = androidx.compose.ui.geometry.Size(flipper.length, 24f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
        )
    }
}

@Composable
private fun ControlRow(
    plungerAmount: Float,
    onPlungerChange: (Float) -> Unit,
    onLaunch: () -> Unit,
    onLeftPressed: () -> Unit,
    onLeftReleased: () -> Unit,
    onRightPressed: () -> Unit,
    onRightReleased: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Controls", color = Color.White, fontWeight = FontWeight.SemiBold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ControlButton(label = "Left Paddle", modifier = Modifier.weight(1f), onPress = onLeftPressed, onRelease = onLeftReleased)
            ControlButton(label = "Right Paddle", modifier = Modifier.weight(1f), onPress = onRightPressed, onRelease = onRightReleased)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Pull: ${(plungerAmount * 100).toInt()}%", color = Color.White)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onPlungerChange(plungerAmount + 0.1f) }) { Text("+5%") }
                    Button(onClick = { onPlungerChange(plungerAmount + 0.25f) }) { Text("+25%") }
                }
            }
            Button(onClick = onLaunch, modifier = Modifier.align(Alignment.CenterVertically)) { Text("Launch") }
        }
    }
}

@Composable
private fun ControlButton(label: String, modifier: Modifier = Modifier, onPress: () -> Unit, onRelease: () -> Unit) {
    Button(
        onClick = {},
        modifier = modifier
            .padding(horizontal = 8.dp)
            .height(56.dp)
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    onPress()
                    tryAwaitRelease()
                    onRelease()
                })
            },
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(label)
    }
}

private operator fun Offset.plus(other: Offset) = Offset(x + other.x, y + other.y)
private operator fun Offset.minus(other: Offset) = Offset(x - other.x, y - other.y)
private operator fun Offset.times(value: Float) = Offset(x * value, y * value)
private fun Offset.getDistance(): Float = kotlin.math.sqrt(x * x + y * y)
private fun Offset.normalize(): Offset = if (getDistance() == 0f) Offset.Zero else Offset(x / getDistance(), y / getDistance())
private fun Offset.dot(other: Offset): Float = x * other.x + y * other.y
private fun Float.absoluteBounce(): Float = kotlin.math.abs(this) * 0.9f

private fun closestPointOnSegment(p: Offset, a: Offset, b: Offset): Offset {
    val ab = b - a
    val t = ((p - a).dot(ab) / ab.dot(ab)).coerceIn(0f, 1f)
    return a + ab * t
}
