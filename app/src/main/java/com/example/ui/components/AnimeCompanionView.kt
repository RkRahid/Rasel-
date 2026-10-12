package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.JarvisAccentGreen
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisDeepDark
import com.example.ui.theme.JarvisNeonGlow
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextPrimary

@Composable
fun AnimeCompanionView(
    state: OrbState,
    soundLevel: Float = 0f,
    latestReply: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "anime_companion_transition")

    // Gentle floating idle breathing movement
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_offset"
    )

    // Holographic pedestal glow rotation
    val pedestalRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pedestal_rotation"
    )

    // Glowing aura pulse
    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_pulse"
    )

    val interactionSource = remember { MutableInteractionSource() }

    val statusColor = when (state) {
        OrbState.LISTENING -> JarvisAccentGreen
        OrbState.ERROR -> Color(0xFFFF5252)
        OrbState.SPEAKING -> JarvisNeonGlow
        OrbState.PROCESSING -> JarvisCyanBright
        OrbState.IDLE -> JarvisCyan
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Holographic Speech Bubble above character
        AnimatedVisibility(
            visible = latestReply.isNotBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(JarvisSurfaceElevated.copy(alpha = 0.95f))
                    .border(1.5.dp, statusColor.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = latestReply.take(120) + if (latestReply.length > 120) "..." else "",
                        color = JarvisTextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        maxLines = 3
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Standing Character Card
        Box(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .height(280.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            JarvisDeepDark.copy(alpha = 0.8f),
                            Color(0xFF071224).copy(alpha = 0.95f),
                            JarvisDeepDark
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            statusColor.copy(alpha = 0.8f),
                            JarvisCyan.copy(alpha = 0.3f),
                            statusColor.copy(alpha = 0.6f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .testTag("anime_character_card"),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Standing anime figure with subtle floating breathing motion
            Image(
                painter = painterResource(id = R.drawable.ic_anime_avatar),
                contentDescription = "Standing Anime AI Companion Character",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = floatingOffset.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            // Holographic Pedestal at character's feet
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                val center = Offset(size.width / 2f, size.height * 0.75f)
                val rx = size.width * 0.42f
                val ry = size.height * 0.35f

                // Glowing base ellipse
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            statusColor.copy(alpha = auraPulse * 0.7f),
                            statusColor.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = rx
                    ),
                    topLeft = Offset(center.x - rx, center.y - ry),
                    size = Size(rx * 2, ry * 2)
                )

                // Rotating holographic ring
                rotate(pedestalRotation, pivot = center) {
                    drawOval(
                        color = statusColor.copy(alpha = 0.6f),
                        topLeft = Offset(center.x - rx * 0.85f, center.y - ry * 0.85f),
                        size = Size(rx * 1.7f, ry * 1.7f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // Top Status Badge Overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(JarvisDeepDark.copy(alpha = 0.85f))
                    .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "UNIT-01 // ANIME AI",
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Bottom Tap Prompt Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                JarvisDeepDark.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (state) {
                        OrbState.LISTENING -> "● শুনছি... (LISTENING)"
                        OrbState.PROCESSING -> "● প্রসেস হচ্ছে... (PROCESSING)"
                        OrbState.SPEAKING -> "● কথা বলছে... (SPEAKING)"
                        else -> "কথা বলতে স্পর্শ করুন (TAP TO TALK)"
                    },
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
