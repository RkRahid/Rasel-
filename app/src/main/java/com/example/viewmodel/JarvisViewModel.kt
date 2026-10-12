package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ActionType
import com.example.model.ChatMessage
import com.example.model.JarvisAction
import com.example.model.Sender
import com.example.service.DeviceControlManager
import com.example.service.DeviceDiagnostics
import com.example.service.GeminiService
import com.example.service.SpeechManager
import com.example.ui.components.OrbState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConfirmationData(
    val title: String,
    val message: String,
    val isDestructive: Boolean = false,
    val onConfirm: () -> Unit
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val geminiService = GeminiService()
    val deviceControl = DeviceControlManager(context)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _orbState = MutableStateFlow(OrbState.IDLE)
    val orbState: StateFlow<OrbState> = _orbState.asStateFlow()

    private val _soundLevel = MutableStateFlow(0f)
    val soundLevel: StateFlow<Float> = _soundLevel.asStateFlow()

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _isTtsEnabled = MutableStateFlow(true)
    val isTtsEnabled: StateFlow<Boolean> = _isTtsEnabled.asStateFlow()

    private val _isBengali = MutableStateFlow(true)
    val isBengali: StateFlow<Boolean> = _isBengali.asStateFlow()

    private val _diagnostics = MutableStateFlow(deviceControl.getDeviceDiagnostics())
    val diagnostics: StateFlow<DeviceDiagnostics> = _diagnostics.asStateFlow()

    private val _pendingConfirmation = MutableStateFlow<ConfirmationData?>(null)
    val pendingConfirmation: StateFlow<ConfirmationData?> = _pendingConfirmation.asStateFlow()

    private val _partialVoiceInput = MutableStateFlow("")
    val partialVoiceInput: StateFlow<String> = _partialVoiceInput.asStateFlow()

    private var speechManager: SpeechManager? = null

    init {
        initSpeechManager()
        addInitialGreeting()
        refreshDiagnostics()
    }

    private fun initSpeechManager() {
        speechManager = SpeechManager(
            context = context,
            onSpeechResult = { recognizedText ->
                _partialVoiceInput.value = ""
                processUserQuery(recognizedText)
            },
            onSpeechPartialResult = { partial ->
                _partialVoiceInput.value = partial
            },
            onRmsChanged = { rms ->
                _soundLevel.value = rms
            },
            onSpeechStateChanged = { state ->
                when (state) {
                    SpeechManager.SpeechState.LISTENING -> _orbState.value = OrbState.LISTENING
                    SpeechManager.SpeechState.IDLE -> {
                        if (_orbState.value == OrbState.LISTENING) {
                            _orbState.value = OrbState.IDLE
                        }
                    }
                    SpeechManager.SpeechState.ERROR -> {
                        _orbState.value = OrbState.IDLE
                        _soundLevel.value = 0f
                    }
                }
            },
            onTtsStateChanged = { isSpeaking ->
                if (isSpeaking) {
                    _orbState.value = OrbState.SPEAKING
                    _soundLevel.value = 0.5f
                } else {
                    if (_orbState.value == OrbState.SPEAKING) {
                        _orbState.value = OrbState.IDLE
                        _soundLevel.value = 0f
                    }
                }
            }
        )
    }

    private fun addInitialGreeting() {
        val welcomeBn = "অনলাইনে সংযুক্ত। আমি রাহিদ জারভিস—আপনার নাথিং ফোন (২এ) প্লাস এর ব্যক্তিগত এআই অ্যাসিস্ট্যান্ট।\nবলুন, কীভাবে সাহায্য করতে পারি?"
        _messages.value = listOf(
            ChatMessage(
                sender = Sender.JARVIS,
                text = welcomeBn,
                action = JarvisAction(ActionType.INFO, "সিস্টেম রেডি // NOTHING OS")
            )
        )
    }

    fun refreshDiagnostics() {
        _diagnostics.value = deviceControl.getDeviceDiagnostics()
    }

    fun toggleLanguage() {
        _isBengali.value = !_isBengali.value
        val msg = if (_isBengali.value) "ভাষা পরিবর্তন করা হয়েছে: বাংলা (Bangla)" else "Language switched to English"
        addSystemMessage(msg)
    }

    fun toggleTts() {
        _isTtsEnabled.value = !_isTtsEnabled.value
        if (!_isTtsEnabled.value) {
            speechManager?.stopSpeaking()
        }
    }

    fun startVoiceInput() {
        speechManager?.stopSpeaking()
        val lang = if (_isBengali.value) "bn-BD" else "en-US"
        speechManager?.startListening(lang)
    }

    fun stopVoiceInput() {
        speechManager?.stopListening()
    }

    fun speakText(text: String) {
        if (_isTtsEnabled.value) {
            speechManager?.speak(text, isBengali = _isBengali.value)
        }
    }

    fun processUserQuery(query: String) {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return

        // 1. Post user message
        val userMsg = ChatMessage(sender = Sender.USER, text = cleanQuery)
        _messages.value = _messages.value + userMsg

        // 2. Check local offline phone controls first
        val lower = cleanQuery.lowercase()
        val handledOffline = handleOfflineCommand(lower, cleanQuery)
        if (handledOffline) {
            return
        }

        // 3. Otherwise dispatch to Gemini AI
        sendToGeminiAI(cleanQuery)
    }

    private fun handleOfflineCommand(lower: String, original: String): Boolean {
        // Flashlight ON
        if (lower.contains("টর্চ জ্বালাও") || lower.contains("টর্চ অন") || lower.contains("ফ্ল্যাশলাইট জ্বালাও") ||
            lower.contains("turn on flashlight") || lower.contains("torch on") || lower.contains("flashlight on")
        ) {
            val result = deviceControl.setFlashlight(true)
            _isFlashlightOn.value = result.getOrDefault(false)
            val reply = if (result.isSuccess) "টর্চ লাইট অন করা হয়েছে।" else "ফ্ল্যাশলাইট চালু করা সম্ভব হয়নি।"
            addJarvisReply(reply, JarvisAction(ActionType.FLASHLIGHT_TOGGLE, "টর্চ অন"))
            return true
        }

        // Flashlight OFF
        if (lower.contains("টর্চ বন্ধ") || lower.contains("টর্চ অফ") || lower.contains("ফ্ল্যাশলাইট বন্ধ") ||
            lower.contains("turn off flashlight") || lower.contains("torch off") || lower.contains("flashlight off")
        ) {
            val result = deviceControl.setFlashlight(false)
            _isFlashlightOn.value = false
            val reply = if (result.isSuccess) "টর্চ লাইট বন্ধ করা হয়েছে।" else "ফ্ল্যাশলাইট বন্ধ করা সম্ভব হয়নি।"
            addJarvisReply(reply, JarvisAction(ActionType.FLASHLIGHT_TOGGLE, "টর্চ অফ"))
            return true
        }

        // Time Query
        if (lower.contains("সময় কত") || lower.contains("কয়টা বাজে") || lower.contains("time") ||
            lower.contains("what time is it") || lower.contains("current time")
        ) {
            val timeString = deviceControl.getCurrentTimeFormatted(_isBengali.value)
            addJarvisReply(timeString, JarvisAction(ActionType.TELL_TIME, "বর্তমান সময়"))
            return true
        }

        // Date Query
        if (lower.contains("তারিখ") || lower.contains("কি বার") || lower.contains("date") ||
            lower.contains("what is the date") || lower.contains("today's date")
        ) {
            val dateString = deviceControl.getCurrentDateFormatted(_isBengali.value)
            addJarvisReply(dateString, JarvisAction(ActionType.TELL_DATE, "আজকের তারিখ"))
            return true
        }

        // YouTube
        if (lower.contains("ইউটিউব") || lower.contains("youtube")) {
            val res = deviceControl.launchYouTube()
            val reply = if (res.isSuccess) "ইউটিউব অ্যাপ চালু করা হচ্ছে..." else "ইউটিউব খুলতে ব্যর্থ হয়েছে।"
            addJarvisReply(reply, JarvisAction(ActionType.OPEN_APP, "YouTube"))
            return true
        }

        // Settings
        if (lower.contains("সেটিংস") || lower.contains("settings")) {
            val res = deviceControl.launchSettings()
            val reply = if (res.isSuccess) "ডিভাইস সেটিংস ওপেন করা হচ্ছে..." else "সেটিংস খুলতে ব্যর্থ হয়েছে।"
            addJarvisReply(reply, JarvisAction(ActionType.OPEN_APP, "Settings"))
            return true
        }

        // Camera
        if (lower.contains("ক্যামেরা") || lower.contains("camera")) {
            val res = deviceControl.launchCamera()
            val reply = if (res.isSuccess) "ক্যামেরা চালু করা হচ্ছে..." else "ক্যামেরা খুলতে ব্যর্থ হয়েছে।"
            addJarvisReply(reply, JarvisAction(ActionType.OPEN_APP, "Camera"))
            return true
        }

        // Calculator
        if (lower.contains("ক্যালকুলেটর") || lower.contains("calculator")) {
            val res = deviceControl.launchCalculator()
            val reply = if (res.isSuccess) "ক্যালকুলেটর খোলা হচ্ছে..." else "ক্যালকুলেটর খুলতে ব্যর্থ হয়েছে।"
            addJarvisReply(reply, JarvisAction(ActionType.OPEN_APP, "Calculator"))
            return true
        }

        // Clock / Alarm
        if (lower.contains("ঘড়ি") || lower.contains("ঘড়ি") || lower.contains("অ্যালার্ম") || lower.contains("clock") || lower.contains("alarm")) {
            val res = deviceControl.launchClock()
            val reply = if (res.isSuccess) "ঘড়ি ও অ্যালার্ম খোলা হচ্ছে..." else "ঘড়ি অ্যাপ খুলতে ব্যর্থ হয়েছে।"
            addJarvisReply(reply, JarvisAction(ActionType.OPEN_APP, "Clock"))
            return true
        }

        // WhatsApp
        if (lower.contains("হোয়াটসঅ্যাপ") || lower.contains("whatsapp")) {
            val res = deviceControl.launchWhatsApp()
            val reply = if (res.isSuccess) "হোয়াটসঅ্যাপ চালু করা হচ্ছে..." else res.exceptionOrNull()?.message ?: "ব্যর্থ হয়েছে"
            addJarvisReply(reply, JarvisAction(ActionType.OPEN_APP, "WhatsApp"))
            return true
        }

        // Sensitive action check: Phone call
        if (lower.startsWith("কল") || lower.contains("ফোন দাও") || lower.contains("call")) {
            _pendingConfirmation.value = ConfirmationData(
                title = "ফোন ডায়ালার কনফার্মেশন",
                message = "আপনি কি ফোন কল করার জন্য ডায়ালার খুলতে চান?",
                onConfirm = {
                    _pendingConfirmation.value = null
                    deviceControl.openDialer()
                    addJarvisReply("ফোন ডায়ালার ওপেন করা হয়েছে।")
                }
            )
            return true
        }

        return false
    }

    private fun sendToGeminiAI(prompt: String) {
        _orbState.value = OrbState.PROCESSING
        viewModelScope.launch {
            val history = _messages.value.mapNotNull { msg ->
                when (msg.sender) {
                    Sender.USER -> Pair("user", msg.text)
                    Sender.JARVIS -> Pair("model", msg.text)
                    else -> null
                }
            }

            val result = geminiService.generateResponse(prompt, history)
            _orbState.value = OrbState.IDLE

            result.onSuccess { aiReply ->
                addJarvisReply(aiReply)
            }.onFailure { err ->
                val errMessage = if (err.message == "API_KEY_MISSING") {
                    if (_isBengali.value) {
                        "অনলাইন জেমিনি এআই ব্যবহারের জন্য অনুগ্রহ করে AI Studio Secrets প্যানেলে আপনার GEMINI_API_KEY সেট করুন।\n(তবে টর্চ, সময়, তারিখ, ইউটিউব ও সেটিংস সহ অফলাইন কমান্ড সম্পূর্ণ সচল আছে!)"
                    } else {
                        "Please configure your GEMINI_API_KEY in the AI Studio Secrets panel for online Gemini AI queries.\n(Device commands like Torch, Time, Date, YouTube & Settings are fully functional offline!)"
                    }
                } else {
                    if (_isBengali.value) {
                        "সংযোগ ত্রুটি: ${err.message ?: "ইন্টারনেট কানেকশন চেক করুন।"}"
                    } else {
                        "Connection Error: ${err.message ?: "Check internet connection."}"
                    }
                }
                val errorMsg = ChatMessage(sender = Sender.JARVIS, text = errMessage, isError = true)
                _messages.value = _messages.value + errorMsg
                if (_isTtsEnabled.value) {
                    speakText(errMessage)
                }
            }
        }
    }

    private fun addJarvisReply(text: String, action: JarvisAction? = null) {
        val jarvisMsg = ChatMessage(sender = Sender.JARVIS, text = text, action = action)
        _messages.value = _messages.value + jarvisMsg
        if (_isTtsEnabled.value) {
            speakText(text)
        }
    }

    private fun addSystemMessage(text: String) {
        _messages.value = _messages.value + ChatMessage(sender = Sender.SYSTEM, text = text)
    }

    fun toggleFlashlight() {
        val newState = !_isFlashlightOn.value
        val res = deviceControl.setFlashlight(newState)
        if (res.isSuccess) {
            _isFlashlightOn.value = newState
            val msg = if (newState) "টর্চ অন করা হয়েছে।" else "টর্চ বন্ধ করা হয়েছে।"
            addSystemMessage(msg)
        }
    }

    fun requestClearChat() {
        _pendingConfirmation.value = ConfirmationData(
            title = "চ্যাট হিস্ট্রি ক্লিয়ার",
            message = "আপনি কি সমস্ত কনভার্সেশন মুছে ফেলতে চান?",
            isDestructive = true,
            onConfirm = {
                _pendingConfirmation.value = null
                _messages.value = emptyList()
                addInitialGreeting()
                addSystemMessage("চ্যাট ইতিহাস মুছে ফেলা হয়েছে।")
            }
        )
    }

    fun dismissConfirmation() {
        _pendingConfirmation.value = null
    }

    override fun onCleared() {
        super.onCleared()
        speechManager?.release()
    }
}
