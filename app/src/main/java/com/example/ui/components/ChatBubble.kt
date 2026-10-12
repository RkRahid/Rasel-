package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.Sender
import com.example.ui.theme.JarvisAccentGreen
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatBubble(
    message: ChatMessage,
    onSpeakClick: (String) -> Unit,
    onActionClick: (com.example.model.JarvisAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val timeFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))

    when (message.sender) {
        Sender.SYSTEM -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "◆ ${message.text} ◆",
                    color = JarvisCyan.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Sender.USER -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Box(
                        modifier = Modifier
                            .testTag("user_message_bubble")
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                            .background(JarvisSurfaceElevated)
                            .border(1.dp, JarvisCyan.copy(alpha = 0.4f), RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = message.text,
                            color = JarvisTextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "YOU // $timeFormatted",
                        color = JarvisTextSecondary.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Sender.JARVIS -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                // JARVIS Anime holographic avatar indicator
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(JarvisSurface)
                        .border(1.5.dp, if (message.isError) Color(0xFFFF5252) else JarvisCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_anime_avatar),
                        contentDescription = "Jarvis Anime Avatar",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth(0.92f)
                ) {
                    Box(
                        modifier = Modifier
                            .testTag("jarvis_message_bubble")
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                            .background(JarvisSurface)
                            .border(
                                1.dp,
                                if (message.isError) Color(0xFFFF5252).copy(alpha = 0.5f) else JarvisCyan.copy(alpha = 0.4f),
                                RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = message.text,
                                color = if (message.isError) Color(0xFFFF8A80) else JarvisTextPrimary,
                                fontSize = 14.sp,
                                lineHeight = 21.sp
                            )

                            // If there is an actionable command button
                            if (message.action != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .testTag("bubble_action_button")
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(JarvisCyan.copy(alpha = 0.2f))
                                        .border(1.dp, JarvisCyan, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "▶ ${message.action.title}",
                                        color = JarvisCyanBright,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    // Bottom info bar: Timestamp, TTS Read Aloud, and Copy buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "JARVIS // $timeFormatted",
                            color = JarvisCyan.copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onSpeakClick(message.text) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("speak_bubble_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VolumeUp,
                                    contentDescription = "Speak message",
                                    tint = JarvisCyan.copy(alpha = 0.8f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("JARVIS Response", message.text)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "কপি করা হয়েছে (Copied)", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("copy_bubble_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy message",
                                    tint = JarvisTextSecondary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
