package com.example.famchat.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.famchat.FAMILY_GROUP_ID
import com.example.famchat.R
import com.example.famchat.ui.components.AvatarCircle
import com.example.famchat.data.FirebaseAuthManager
import com.example.famchat.model.Message
import com.example.famchat.model.User
import com.example.famchat.navigation.Screen
import com.example.famchat.ui.theme.BackgroundLight
import com.example.famchat.ui.theme.ChatBubbleOther
import com.example.famchat.ui.theme.OnlineGreen
import com.example.famchat.ui.theme.OfflineGray
import com.example.famchat.ui.theme.PrimaryBlue
import com.example.famchat.ui.theme.PrivatePink
import com.example.famchat.ui.theme.TextSecondary
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private fun avatarResOrDefault(resId: Int): Int = if (resId != 0) resId else R.drawable.avatar_dog

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
    var myAvatarResId by remember { mutableStateOf(0) }
    var chatType by remember { mutableStateOf("group") }
    var members by remember { mutableStateOf<List<User>>(emptyList()) }
    val listState = rememberLazyListState()
    val isGroupChat = chatId == FAMILY_GROUP_ID
    val accentColor = if (chatType == "private") PrivatePink else PrimaryBlue

    LaunchedEffect(Unit) {
        val me = authManager.getCurrentUserData()
        myNickname = me?.nickname ?: ""
        myAvatarResId = me?.avatarResId ?: 0
        db.collection("chats").document(chatId).get().addOnSuccessListener { doc ->
            chatType = doc.getString("type") ?: "group"
        }
    }

    DisposableEffect(isGroupChat) {
        if (isGroupChat) {
            val registration = db.collection("users").addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    members = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
                }
            }
            onDispose { registration.remove() }
        } else {
            onDispose { }
        }
    }

    DisposableEffect(chatId) {
        val registration = db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
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
                "senderAvatarResId" to myAvatarResId,
                "text" to text,
                "timestamp" to now,
                "type" to "text",
                "imageUrl" to ""
            )
            messageRef.set(message)
            chatRef.update(mapOf("lastMessage" to text, "lastMessageTime" to now))
        }
    }

    fun openPrivateChat(other: User) {
        scope.launch {
            val pcId = if (userId < other.userId) "${userId}_${other.userId}" else "${other.userId}_${userId}"
            val chatRef = db.collection("chats").document(pcId)
            val doc = chatRef.get().await()
            if (!doc.exists()) {
                val now = System.currentTimeMillis()
                chatRef.set(
                    hashMapOf(
                        "chatId" to pcId, "type" to "private", "name" to other.nickname,
                        "participants" to listOf(userId, other.userId),
                        "lastMessage" to "", "lastMessageTime" to now,
                        "createdBy" to userId, "createdAt" to now,
                        "deletedFor" to emptyList<String>()
                    )
                ).await()
            }
            navController.navigate(Screen.Chat.createRoute(pcId, other.nickname))
        }
    }

    fun logout() {
        scope.launch {
            authManager.logout()
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ChatHeaderBar(
            title = chatName,
            subtitle = if (isGroupChat) "${members.size} членове" else null,
            showBack = !isGroupChat,
            onBack = { navController.popBackStack() },
            onLogout = if (isGroupChat) { { logout() } } else null
        )
        Row(modifier = Modifier.weight(1f)) {
            if (isGroupChat) {
                MemberRail(members = members, myUserId = userId, onMemberClick = { openPrivateChat(it) })
            }
            MessageList(
                messages = messages, userId = userId, chatType = chatType,
                listState = listState, modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
        MessageInputBar(messageText, { messageText = it }, { sendMessage() }, accentColor)
    }
}

@Composable
private fun ChatHeaderBar(
    title: String,
    subtitle: String?,
    showBack: Boolean,
    onBack: () -> Unit,
    onLogout: (() -> Unit)?
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                if (subtitle != null) Text(subtitle, fontSize = 11.sp, color = TextSecondary)
            }
            if (onLogout != null) {
                IconButton(onClick = onLogout) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Изход")
                }
            }
        }
        Divider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
    }
}

@Composable
private fun MessageList(
    messages: List<Message>,
    userId: String,
    chatType: String,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }
        items(messages) { msg ->
            MessageBubble(message = msg, isMine = msg.senderId == userId, chatType = chatType)
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun MessageInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Напиши съобщение...") },
            shape = MaterialTheme.shapes.extraLarge,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = BackgroundLight,
                focusedContainerColor = BackgroundLight,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(accentColor)
                .clickable(onClick = onSend),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Изпрати", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun MemberRail(members: List<User>, myUserId: String, onMemberClick: (User) -> Unit) {
    Column(
        modifier = Modifier
            .width(60.dp)
            .fillMaxHeight()
            .background(Color(0xFFF8FAFC))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        members.forEach { member ->
            val isMe = member.userId == myUserId
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .then(if (!isMe) Modifier.clickable { onMemberClick(member) } else Modifier)
            ) {
                Box {
                    AvatarCircle(resId = member.avatarResId, size = 38.dp)
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(if (member.isOnline) OnlineGreen else OfflineGray)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    if (isMe) "Ти" else member.nickname,
                    fontSize = 9.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Message, isMine: Boolean, chatType: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMine) {
            AvatarCircle(resId = message.senderAvatarResId, size = 26.dp)
            Spacer(modifier = Modifier.width(6.dp))
        }
        Column(
            modifier = Modifier
                .widthIn(max = 240.dp)
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
