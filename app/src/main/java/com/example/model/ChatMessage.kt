package com.example.model

enum class Sender {
    USER,
    JARVIS,
    SYSTEM
}

enum class ActionType {
    FLASHLIGHT_TOGGLE,
    OPEN_APP,
    TELL_TIME,
    TELL_DATE,
    CONFIRM_DIAL,
    INFO
}

data class JarvisAction(
    val type: ActionType,
    val title: String,
    val payload: String = ""
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: Sender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val action: JarvisAction? = null
)
