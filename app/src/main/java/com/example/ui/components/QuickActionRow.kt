package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextPrimary

@Composable
fun QuickActionRow(
    isBengali: Boolean,
    onFlashlightClick: () -> Unit,
    onTimeClick: () -> Unit,
    onDateClick: () -> Unit,
    onYouTubeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCameraClick: () -> Unit,
    onCalculatorClick: () -> Unit,
    onClearChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionChip(
            icon = Icons.Default.FlashlightOn,
            label = if (isBengali) "টর্চ লাইট" else "Torch",
            testTag = "chip_flashlight",
            onClick = onFlashlightClick
        )
        ActionChip(
            icon = Icons.Default.AccessTime,
            label = if (isBengali) "বর্তমান সময়" else "Time",
            testTag = "chip_time",
            onClick = onTimeClick
        )
        ActionChip(
            icon = Icons.Default.CalendarToday,
            label = if (isBengali) "আজকের তারিখ" else "Date",
            testTag = "chip_date",
            onClick = onDateClick
        )
        ActionChip(
            icon = Icons.Default.PlayArrow,
            label = if (isBengali) "ইউটিউব" else "YouTube",
            testTag = "chip_youtube",
            onClick = onYouTubeClick
        )
        ActionChip(
            icon = Icons.Default.Settings,
            label = if (isBengali) "সেটিংস" else "Settings",
            testTag = "chip_settings",
            onClick = onSettingsClick
        )
        ActionChip(
            icon = Icons.Default.PhotoCamera,
            label = if (isBengali) "ক্যামেরা" else "Camera",
            testTag = "chip_camera",
            onClick = onCameraClick
        )
        ActionChip(
            icon = Icons.Default.Calculate,
            label = if (isBengali) "ক্যালকুলেটর" else "Calc",
            testTag = "chip_calculator",
            onClick = onCalculatorClick
        )
        ActionChip(
            icon = Icons.Default.Delete,
            label = if (isBengali) "ক্লিয়ার চ্যাট" else "Clear",
            testTag = "chip_clear_chat",
            onClick = onClearChatClick
        )
    }
}

@Composable
private fun ActionChip(
    icon: ImageVector,
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .background(JarvisSurfaceElevated)
            .border(1.dp, JarvisCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = JarvisCyan,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            color = JarvisTextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}
