package com.example.famchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.famchat.data.FirebaseAuthManager
import com.example.famchat.navigation.Screen
import com.example.famchat.ui.screens.ChatListScreen
import com.example.famchat.ui.screens.LoginScreen
import com.example.famchat.ui.screens.RegisterScreen
import com.example.famchat.ui.theme.FamChatTheme

class MainActivity : ComponentActivity() {
    private val authManager = FirebaseAuthManager()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FamChatTheme { FamChatApp(authManager) } }
    }
}

@Composable
fun FamChatApp(authManager: FirebaseAuthManager) {
    val navController = rememberNavController()
    val start = if (authManager.currentUser != null) Screen.ChatList.route else Screen.Login.route
    NavHost(navController, start) {
        composable(Screen.Login.route) { LoginScreen(navController, authManager) }
        composable(Screen.Register.route) { RegisterScreen(navController, authManager) }
        composable(Screen.ChatList.route) { ChatListScreen(navController, authManager) }
    }
}
