package com.example.famchat.model

data class Message(
    val messageId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatarResId: Int = 0,
    val text: String = "",
    val timestamp: Long = 0L,
    val type: String = "text",
    val imageUrl: String = ""
)
