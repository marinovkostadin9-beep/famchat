package com.example.famchat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.famchat.data.FirebaseAuthManager
import com.example.famchat.model.Message
import com.example.famchat.ui.theme.PrimaryBlue
import com.example.famchat.ui.theme.ChatBubbleOther
import com.example.famchat.ui.theme.PrivatePink
import com.example.famchat.ui.theme.TextSecondary
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    authManager: FirebaseAuthManager,
    chatId: String,
    chatName: String
) {
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }
    val userId = authManager.currentUser?.uid ?: ""
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var messageText by remember { mutableStateOf("") }
    var myNickname by remember { mutableStateOf("") }
    var chatType by remember { mutableStateOf("group") }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        myNickname = authManager.getCurrentUserData()?.nickname ?: ""
        db.collection("chats").document(chatId).get().addOnSuccessListener { doc ->
            chatType = doc.getString("type") ?: "group"
        }
    }

    DisposableEffect(chatId) {
        val registration = db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    messages = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
                }
            }
        onDispose { registration.remove() }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    fun sendMessage() {
        val text = messageText.trim()
        if (text.isEmpty() || userId.isEmpty()) return
        messageText = ""
        scope.launch {
            val chatRef = db.collection("chats").document(chatId)
            val messageRef = chatRef.collection("messages").document()
            val now = System.currentTimeMillis()
            val message = hashMapOf(
                "messageId" to messageRef.id,
                "chatId" to chatId,
                "senderId" to userId,
                "senderName" to myNickname,
                "text" to text,
                "timestamp" to now,
                "type" to "text",
                "imageUrl" to ""
            )
            messageRef.set(message)
            chatRef.update(mapOf("lastMessage" to text, "lastMessageTime" to now))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chatName, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Съобщение...") },
                    shape = MaterialTheme.shapes.extraLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { sendMessage() }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Изпрати", tint = PrimaryBlue)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(messages) { msg ->
                MessageBubble(message = msg, isMine = msg.senderId == userId, chatType = chatType)
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun MessageBubble(message: Message, isMine: Boolean, chatType: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .background(
                    color = if (isMine) (if (chatType == "private") PrivatePink else PrimaryBlue) else ChatBubbleOther,
                    shape = MaterialTheme.shapes.large
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            if (!isMine) {
                Text(
                    message.senderName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }
            Text(
                message.text,
                fontSize = 15.sp,
                color = if (isMine) Color.White else Color.Black
            )
        }
    }
}
