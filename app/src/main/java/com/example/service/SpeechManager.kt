package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class SpeechManager(
    private val context: Context,
    private val onSpeechResult: (String) -> Unit,
    private val onSpeechPartialResult: (String) -> Unit,
    private val onRmsChanged: (Float) -> Unit,
    private val onSpeechStateChanged: (SpeechState) -> Unit,
    private val onTtsStateChanged: (Boolean) -> Unit
) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "SpeechManager"
        const val UTTERANCE_ID = "jarvis_tts_utterance"
    }

    enum class SpeechState {
        IDLE,
        LISTENING,
        ERROR
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false
    private var isBengaliTtsAvailable = false
    private var currentLanguage = "bn-BD"

    init {
        initTts()
        initSpeechRecognizer()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.let { tts ->
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        onTtsStateChanged(true)
                    }

                    override fun onDone(utteranceId: String?) {
                        onTtsStateChanged(false)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        onTtsStateChanged(false)
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        onTtsStateChanged(false)
                    }
                })

                // Try Bengali first
                val bengaliLocale = Locale.forLanguageTag("bn-BD")
                val result = tts.setLanguage(bengaliLocale)
                if (result == TextToSpeech.LANG_AVAILABLE || result == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                    isBengaliTtsAvailable = true
                    Log.d(TAG, "Bengali TTS available and selected.")
                } else {
                    // Try generic Bengali or English
                    val altBengali = Locale.forLanguageTag("bn")
                    val altResult = tts.setLanguage(altBengali)
                    if (altResult == TextToSpeech.LANG_AVAILABLE || altResult == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                        isBengaliTtsAvailable = true
                    } else {
                        Log.w(TAG, "Bengali TTS not installed on device. Falling back to English.")
                        tts.setLanguage(Locale.US)
                        isBengaliTtsAvailable = false
                    }
                }
                tts.setPitch(1.05f) // Slight futuristic clarity
                tts.setSpeechRate(1.0f)
                isTtsInitialized = true
            }
        } else {
            Log.e(TAG, "TTS initialization failed")
        }
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        onSpeechStateChanged(SpeechState.LISTENING)
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize -2dB..10dB to 0..1f
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        onRmsChanged(normalized)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        onSpeechStateChanged(SpeechState.IDLE)
                        onRmsChanged(0f)
                    }

                    override fun onError(error: Int) {
                        Log.w(TAG, "Speech recognition error code: $error")
                        onSpeechStateChanged(SpeechState.ERROR)
                        onRmsChanged(0f)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotBlank()) {
                            onSpeechResult(text)
                        }
                        onSpeechStateChanged(SpeechState.IDLE)
                        onRmsChanged(0f)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotBlank()) {
                            onSpeechPartialResult(text)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } else {
            Log.w(TAG, "SpeechRecognizer is not available on this device")
        }
    }

    fun startListening(languageCode: String = currentLanguage) {
        currentLanguage = languageCode
        stopSpeaking()

        if (speechRecognizer == null) {
            initSpeechRecognizer()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languageCode)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "JARVIS শুনছে...")
        }

        try {
            speechRecognizer?.startListening(intent)
            onSpeechStateChanged(SpeechState.LISTENING)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start speech recognition", e)
            onSpeechStateChanged(SpeechState.ERROR)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            onSpeechStateChanged(SpeechState.IDLE)
            onRmsChanged(0f)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop speech recognition", e)
        }
    }

    fun speak(text: String, isBengali: Boolean = true) {
        if (!isTtsInitialized || textToSpeech == null) {
            return
        }

        try {
            val targetLocale = if (isBengali && isBengaliTtsAvailable) {
                Locale.forLanguageTag("bn-BD")
            } else {
                Locale.US
            }
            textToSpeech?.setLanguage(targetLocale)

            // Strip markdown asterisks or code symbols for smooth vocal delivery
            val cleanText = text
                .replace("*", "")
                .replace("#", "")
                .replace("`", "")
                .replace("---", "")
                .trim()

            textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
        } catch (e: Exception) {
            Log.e(TAG, "Error in TTS speak", e)
            onTtsStateChanged(false)
        }
    }

    fun stopSpeaking() {
        try {
            if (textToSpeech?.isSpeaking == true) {
                textToSpeech?.stop()
                onTtsStateChanged(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
    }

    fun isBengaliTtsReady(): Boolean = isBengaliTtsAvailable

    fun release() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing speech resources", e)
        }
    }
}
