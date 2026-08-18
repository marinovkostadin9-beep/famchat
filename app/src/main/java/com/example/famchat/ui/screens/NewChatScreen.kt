package com.example.famchat.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.famchat.data.FirebaseAuthManager
import com.example.famchat.model.User
import com.example.famchat.navigation.Screen
import com.example.famchat.ui.theme.TextSecondary
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatScreen(navController: NavController, authManager: FirebaseAuthManager) {
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }
    val myUserId = authManager.currentUser?.uid ?: ""
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("users").get()
            .addOnSuccessListener { snapshot ->
                users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
                    .filter { it.userId != myUserId }
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }
    }

    fun openPrivateChat(otherUser: User) {
        scope.launch {
            val chatId = if (myUserId < otherUser.userId) {
                "${myUserId}_${otherUser.userId}"
            } else {
                "${otherUser.userId}_${myUserId}"
            }
            val chatRef = db.collection("chats").document(chatId)
            val doc = chatRef.get().await()
            if (!doc.exists()) {
                val now = System.currentTimeMillis()
                chatRef.set(
                    hashMapOf(
                        "chatId" to chatId,
                        "type" to "private",
                        "name" to otherUser.nickname,
                        "participants" to listOf(myUserId, otherUser.userId),
                        "lastMessage" to "",
                        "lastMessageTime" to now,
                        "createdBy" to myUserId,
                        "createdAt" to now,
                        "deletedFor" to emptyList<String>()
                    )
                ).await()
            }
            navController.navigate(Screen.Chat.createRoute(chatId, otherUser.nickname)) {
                popUpTo(Screen.ChatList.route)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Нов чат", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Няма други потребители все още", color = TextSecondary)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(users) { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { openPrivateChat(user) }
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👤", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(user.nickname, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                    Divider()
                }
            }
        }
    }
}
