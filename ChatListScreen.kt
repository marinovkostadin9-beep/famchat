package com.example.famchat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.famchat.data.FirebaseAuthManager
import com.example.famchat.navigation.Screen
import com.example.famchat.ui.theme.PrimaryBlue
import com.example.famchat.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(navController: NavController, authManager: FirebaseAuthManager) {
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FamChat", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            authManager.logout()
                            navController.navigate(Screen.Login.route) { popUpTo(Screen.ChatList.route) { inclusive = true } }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Изход")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🎉", fontSize = 48.sp, modifier = Modifier.padding(bottom = 16.dp))
            Text("Добре дошъл в FamChat!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sprint 2 ще добави чат функционалността", fontSize = 14.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Тук ще видиш груповия чат и личните съобщения", fontSize = 12.sp, color = TextSecondary.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))
            Text("👨‍👩‍👧 Семеен чат", fontSize = 14.sp, color = PrimaryBlue, fontWeight = FontWeight.Medium)
        }
    }
}
