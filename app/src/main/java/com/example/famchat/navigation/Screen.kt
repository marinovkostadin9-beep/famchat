package com.example.famchat.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ChatList : Screen("chat_list")
    object NewChat : Screen("new_chat")
    object Chat : Screen("chat/{chatId}/{chatName}") {
        fun createRoute(chatId: String, chatName: String) =
            "chat/$chatId/${Uri.encode(chatName)}"
    }
}
