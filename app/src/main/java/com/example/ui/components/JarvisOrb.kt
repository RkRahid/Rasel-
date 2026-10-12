package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisAccentGreen
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisNeonGlow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class OrbState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

@Composable
fun JarvisOrb(
    state: OrbState,
    soundLevel: Float = 0f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_orb_transition")

    // Slow continuous outer rotation
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rotation"
    )

    // Inner counter-rotation
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rotation"
    )

    // Fast rotation when processing
    val processingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "processing_rotation"
    )

    // Breathing pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_pulse"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = size.minDimension / 2.6f

                // Dynamic scale based on state and microphone/TTS audio
                val dynamicScale = when (state) {
                    OrbState.LISTENING -> (1.0f + (soundLevel * 0.35f))
                    OrbState.SPEAKING -> (1.0f + (breathingPulse - 1f) * 0.5f + (soundLevel * 0.25f))
                    OrbState.PROCESSING -> 1.05f
                    OrbState.IDLE -> breathingPulse
                    OrbState.ERROR -> 0.95f
                }

                val primaryColor = when (state) {
                    OrbState.LISTENING -> JarvisAccentGreen
                    OrbState.ERROR -> Color(0xFFFF5252)
                    OrbState.SPEAKING -> JarvisNeonGlow
                    OrbState.PROCESSING -> JarvisCyanBright
                    OrbState.IDLE -> JarvisCyan
                }

                // 1. Outermost soft radial ambient glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.35f),
                            primaryColor.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius * 1.6f * dynamicScale
                    ),
                    radius = baseRadius * 1.6f * dynamicScale,
                    center = center
                )

                // 2. Outer tech ring with notches
                val currentOuterRot = if (state == OrbState.PROCESSING) processingRotation else outerRotation
                rotate(currentOuterRot, pivot = center) {
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.3f),
                        radius = baseRadius * 1.25f,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Draw 12 peripheral tick marks
                    val numTicks = 12
                    for (i in 0 until numTicks) {
                        val angle = (i * 360f / numTicks) * (PI / 180f)
                        val r1 = baseRadius * 1.2f
                        val r2 = baseRadius * 1.32f
                        val start = Offset(center.x + (r1 * cos(angle)).toFloat(), center.y + (r1 * sin(angle)).toFloat())
                        val end = Offset(center.x + (r2 * cos(angle)).toFloat(), center.y + (r2 * sin(angle)).toFloat())
                        drawLine(
                            color = primaryColor.copy(alpha = 0.6f),
                            start = start,
                            end = end,
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }

                // 3. Middle segmented holographic arc reactor ring
                val currentInnerRot = if (state == OrbState.PROCESSING) -processingRotation * 1.5f else innerRotation
                rotate(currentInnerRot, pivot = center) {
                    val arcRadius = baseRadius * 0.95f
                    val arcRect = Size(arcRadius * 2, arcRadius * 2)
                    val arcTopLeft = Offset(center.x - arcRadius, center.y - arcRadius)

                    // Draw 3 sci-fi curved shields
                    for (i in 0..2) {
                        drawArc(
                            color = primaryColor.copy(alpha = 0.85f),
                            startAngle = (i * 120f) + 15f,
                            sweepAngle = 75f,
                            useCenter = false,
                            topLeft = arcTopLeft,
                            size = arcRect,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // 4. Core energy pulse ring
                drawCircle(
                    color = primaryColor.copy(alpha = 0.5f),
                    radius = baseRadius * 0.65f * dynamicScale,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // 5. Central glowing reactor orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            primaryColor,
                            primaryColor.copy(alpha = 0.7f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius * 0.45f * dynamicScale
                    ),
                    radius = baseRadius * 0.45f * dynamicScale,
                    center = center
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // State indicator text
        val statusText = when (state) {
            OrbState.IDLE -> "SYSTEM READY // AWAITING COMMAND"
            OrbState.LISTENING -> "JARVIS LISTENING... [বাংলা / EN]"
            OrbState.PROCESSING -> "PROCESSING QUERY // GEMINI 3.5"
            OrbState.SPEAKING -> "TRANSMITTING VOCAL RESPONSE..."
            OrbState.ERROR -> "SYSTEM NOTICE // CHECK INPUT"
        }

        val statusColor = when (state) {
            OrbState.LISTENING -> JarvisAccentGreen
            OrbState.ERROR -> Color(0xFFFF5252)
            OrbState.SPEAKING -> JarvisNeonGlow
            OrbState.PROCESSING -> JarvisCyanBright
            OrbState.IDLE -> JarvisCyan.copy(alpha = 0.7f)
        }

        Text(
            text = statusText,
            color = statusColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
