package com.example.famchat.model

data class User(
    val userId: String = "",
    val nickname: String = "",
    val avatarResId: Int = 0,
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L,
    val fcmToken: String = ""
)
