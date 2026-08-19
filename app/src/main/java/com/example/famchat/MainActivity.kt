package com.example.famchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.famchat.data.FirebaseAuthManager
import com.example.famchat.navigation.Screen
import com.example.famchat.ui.screens.ChatListScreen
import com.example.famchat.ui.screens.ChatScreen
import com.example.famchat.ui.screens.LoginScreen
import com.example.famchat.ui.screens.NewChatScreen
import com.example.famchat.ui.screens.RegisterScreen
import com.example.famchat.ui.theme.FamChatTheme

const val FAMILY_GROUP_ID = "family_group"
const val FAMILY_GROUP_NAME = "Семеен чат"

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
    val start = if (authManager.currentUser != null) {
        Screen.Chat.createRoute(FAMILY_GROUP_ID, FAMILY_GROUP_NAME)
    } else {
        Screen.Login.route
    }
    NavHost(
        navController,
        start,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(Screen.Login.route) { LoginScreen(navController, authManager) }
        composable(Screen.Register.route) { RegisterScreen(navController, authManager) }
        composable(Screen.ChatList.route) { ChatListScreen(navController, authManager) }
        composable(Screen.NewChat.route) { NewChatScreen(navController, authManager) }
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("chatName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val chatName = backStackEntry.arguments?.getString("chatName") ?: ""
            ChatScreen(navController, authManager, chatId, chatName)
        }
    }
}
