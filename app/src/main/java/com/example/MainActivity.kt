package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ChatBubble
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.JarvisOrb
import com.example.ui.components.OrbState
import com.example.ui.components.PermissionsInfoDialog
import com.example.ui.components.QuickActionRow
import com.example.ui.components.StatusHudBar
import com.example.ui.theme.JarvisAccentGreen
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisDeepDark
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.JarvisViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                JarvisAppScreen()
            }
        }
    }
}

@Composable
fun JarvisAppScreen(
    viewModel: JarvisViewModel = viewModel()
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val orbState by viewModel.orbState.collectAsState()
    val soundLevel by viewModel.soundLevel.collectAsState()
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsState()
    val isTtsEnabled by viewModel.isTtsEnabled.collectAsState()
    val isBengali by viewModel.isBengali.collectAsState()
    val diagnostics by viewModel.diagnostics.collectAsState()
    val pendingConfirmation by viewModel.pendingConfirmation.collectAsState()
    val partialVoice by viewModel.partialVoiceInput.collectAsState()

    var isAnimeMode by remember { mutableStateOf(true) }
    var inputText by remember { mutableStateOf("") }
    var showInfoDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Scroll to latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Permissions handled gracefully
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val needed = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground),
        containerColor = JarvisBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            JarvisDeepDark,
                            JarvisBackground,
                            JarvisDeepDark
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                // Top Telemetry HUD
                StatusHudBar(
                    diagnostics = diagnostics,
                    isBengali = isBengali,
                    isTtsEnabled = isTtsEnabled,
                    isFlashlightOn = isFlashlightOn,
                    isAnimeMode = isAnimeMode,
                    onToggleAnimeMode = { isAnimeMode = !isAnimeMode },
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onToggleTts = { viewModel.toggleTts() },
                    onToggleFlashlight = { viewModel.toggleFlashlight() },
                    onOpenInfoDialog = { showInfoDialog = true }
                )

                // Standing Anime Character or Holographic Arc Reactor Orb
                Spacer(modifier = Modifier.height(6.dp))
                if (isAnimeMode) {
                    val lastJarvisMessage = messages.lastOrNull { it.sender == com.example.model.Sender.JARVIS }?.text ?: ""
                    com.example.ui.components.AnimeCompanionView(
                        state = orbState,
                        soundLevel = soundLevel,
                        latestReply = lastJarvisMessage,
                        onClick = {
                            if (orbState == OrbState.LISTENING) {
                                viewModel.stopVoiceInput()
                            } else {
                                viewModel.startVoiceInput()
                            }
                        }
                    )
                } else {
                    JarvisOrb(
                        state = orbState,
                        soundLevel = soundLevel,
                        onClick = {
                            if (orbState == OrbState.LISTENING) {
                                viewModel.stopVoiceInput()
                            } else {
                                viewModel.startVoiceInput()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                // Quick Action Bar
                QuickActionRow(
                    isBengali = isBengali,
                    onFlashlightClick = { viewModel.toggleFlashlight() },
                    onTimeClick = { viewModel.processUserQuery(if (isBengali) "সময় কত" else "What time is it") },
                    onDateClick = { viewModel.processUserQuery(if (isBengali) "আজকের তারিখ" else "What is the date") },
                    onYouTubeClick = { viewModel.deviceControl.launchYouTube() },
                    onSettingsClick = { viewModel.deviceControl.launchSettings() },
                    onCameraClick = { viewModel.deviceControl.launchCamera() },
                    onCalculatorClick = { viewModel.deviceControl.launchCalculator() },
                    onClearChatClick = { viewModel.requestClearChat() }
                )

                // Partial voice transcription live banner
                AnimatedVisibility(
                    visible = partialVoice.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(JarvisAccentGreen.copy(alpha = 0.15f))
                            .border(1.dp, JarvisAccentGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🎙️ $partialVoice",
                            color = JarvisAccentGreen,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Chat Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        ChatBubble(
                            message = message,
                            onSpeakClick = { textToSpeak ->
                                viewModel.speakText(textToSpeak)
                            },
                            onActionClick = { action ->
                                when (action.title) {
                                    "টর্চ অন", "টর্চ অফ" -> viewModel.toggleFlashlight()
                                    "YouTube" -> viewModel.deviceControl.launchYouTube()
                                    "Settings" -> viewModel.deviceControl.launchSettings()
                                    "Camera" -> viewModel.deviceControl.launchCamera()
                                    "Calculator" -> viewModel.deviceControl.launchCalculator()
                                    "Clock" -> viewModel.deviceControl.launchClock()
                                    "WhatsApp" -> viewModel.deviceControl.launchWhatsApp()
                                }
                            }
                        )
                    }
                }

                // Futuristic Bottom Input Bar with Mic button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(JarvisDeepDark.copy(alpha = 0.95f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice Mic Floating Action Button
                    val isListening = orbState == OrbState.LISTENING
                    FloatingActionButton(
                        onClick = {
                            if (isListening) {
                                viewModel.stopVoiceInput()
                            } else {
                                viewModel.startVoiceInput()
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("microphone_button"),
                        shape = CircleShape,
                        containerColor = if (isListening) JarvisAccentGreen else JarvisCyan,
                        contentColor = Color.Black,
                        elevation = FloatingActionButtonDefaults.elevation(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Input Field
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        placeholder = {
                            Text(
                                text = if (isBengali) "প্রশ্ন করুন বা কমান্ড দিন..." else "Ask Jarvis anything...",
                                color = JarvisTextSecondary.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary,
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCyan.copy(alpha = 0.3f),
                            focusedContainerColor = JarvisSurface,
                            unfocusedContainerColor = JarvisSurface,
                            cursorColor = JarvisCyanBright
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    val query = inputText
                                    inputText = ""
                                    viewModel.processUserQuery(query)
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Send Query Button
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val query = inputText
                                inputText = ""
                                viewModel.processUserQuery(query)
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) JarvisCyan.copy(alpha = 0.2f) else Color.Transparent)
                            .testTag("send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Query",
                            tint = if (inputText.isNotBlank()) JarvisCyanBright else JarvisTextSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Sensitive Action Confirmation Dialog
            pendingConfirmation?.let { confirmData ->
                ConfirmationDialog(
                    title = confirmData.title,
                    message = confirmData.message,
                    isDestructive = confirmData.isDestructive,
                    onConfirm = confirmData.onConfirm,
                    onDismiss = { viewModel.dismissConfirmation() }
                )
            }

            // Permissions and System Info Dialog
            if (showInfoDialog) {
                PermissionsInfoDialog(
                    onDismiss = { showInfoDialog = false }
                )
            }
        }
    }
}
