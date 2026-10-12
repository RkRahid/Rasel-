package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.DeviceDiagnostics
import com.example.ui.theme.JarvisAccentGreen
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisDeepDark
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun StatusHudBar(
    diagnostics: DeviceDiagnostics,
    isBengali: Boolean,
    isTtsEnabled: Boolean,
    isFlashlightOn: Boolean,
    isAnimeMode: Boolean = true,
    onToggleAnimeMode: () -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleTts: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onOpenInfoDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .background(JarvisDeepDark.copy(alpha = 0.95f))
            .border(
                width = 1.dp,
                color = JarvisCyan.copy(alpha = 0.25f),
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Top Row: Brand & Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(JarvisAccentGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RAHID JARVIS AI",
                        color = JarvisTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "${diagnostics.model} // 8GB RAM",
                    color = JarvisCyan.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Quick Control Action Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Anime / Orb Mode Toggle Pill
                Box(
                    modifier = Modifier
                        .testTag("avatar_mode_toggle_button")
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAnimeMode) JarvisCyan.copy(alpha = 0.25f) else JarvisSurfaceElevated)
                        .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onToggleAnimeMode)
                        .padding(horizontal = 7.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isAnimeMode) "👤 অ্যানিমে" else "🤖 অর্ব",
                        color = if (isAnimeMode) JarvisCyanBright else JarvisTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(5.dp))

                // Language Pill (বাংলা / EN)
                Box(
                    modifier = Modifier
                        .testTag("language_toggle_button")
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isBengali) JarvisCyan.copy(alpha = 0.2f) else JarvisSurfaceElevated)
                        .border(1.dp, JarvisCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onToggleLanguage)
                        .padding(horizontal = 7.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBengali) "বাংলা" else "ENG",
                        color = if (isBengali) JarvisCyanBright else JarvisTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Flashlight toggle button
                IconButton(
                    onClick = onToggleFlashlight,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("flashlight_hud_button")
                ) {
                    Icon(
                        imageVector = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = "Flashlight Toggle",
                        tint = if (isFlashlightOn) JarvisCyanBright else JarvisTextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // TTS Mute / Unmute Button
                IconButton(
                    onClick = onToggleTts,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("tts_mute_button")
                ) {
                    Icon(
                        imageVector = if (isTtsEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeMute,
                        contentDescription = "Text To Speech Toggle",
                        tint = if (isTtsEnabled) JarvisCyan else JarvisTextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Info Dialog Button
                IconButton(
                    onClick = onOpenInfoDialog,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("info_dialog_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "App Information",
                        tint = JarvisCyan.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom Telemetry Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ BATTERY ${diagnostics.batteryPercent}%${if (diagnostics.isCharging) " [CHARGING]" else ""}",
                color = JarvisTextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "RAM FREE: ${diagnostics.ramAvailable}",
                color = JarvisTextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "STORAGE: ${diagnostics.storage}",
                color = JarvisTextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
