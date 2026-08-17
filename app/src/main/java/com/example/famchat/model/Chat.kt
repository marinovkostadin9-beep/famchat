package com.example.famchat.model

data class Chat(
    val chatId: String = "",
    val type: String = "group",
    val name: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val createdBy: String = "",
    val createdAt: Long = 0L,
    val deletedFor: List<String> = emptyList()
)
