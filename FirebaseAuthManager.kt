package com.example.famchat.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.famchat.model.User
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    val currentUser get() = auth.currentUser

    suspend fun register(nickname: String, password: String, avatarResId: Int): Result<String> {
        return try {
            val snapshot = db.collection("users").whereEqualTo("nickname", nickname).get().await()
            if (!snapshot.isEmpty) {
                return Result.failure(Exception("Този никнейм вече е зает"))
            }
            val email = "$nickname@famchat.local"
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("Неуспешна регистрация")
            val user = hashMapOf(
                "userId" to userId, "nickname" to nickname, "avatarResId" to avatarResId,
                "isOnline" to true, "lastSeen" to System.currentTimeMillis(),
                "createdAt" to System.currentTimeMillis(), "fcmToken" to ""
            )
            db.collection("users").document(userId).set(user).await()
            createFamilyGroupChat(userId, nickname)
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(nickname: String, password: String): Result<String> {
        return try {
            val snapshot = db.collection("users").whereEqualTo("nickname", nickname).get().await()
            if (snapshot.isEmpty) {
                return Result.failure(Exception("Няма потребител с този никнейм"))
            }
            val email = "$nickname@famchat.local"
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("Неуспешен вход")
            db.collection("users").document(userId).update(
                mapOf("isOnline" to true, "lastSeen" to System.currentTimeMillis())
            ).await()
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        currentUser?.uid?.let { userId ->
            db.collection("users").document(userId).update(
                mapOf("isOnline" to false, "lastSeen" to System.currentTimeMillis())
            ).await()
        }
        auth.signOut()
    }

    suspend fun getCurrentUserData(): User? {
        val userId = currentUser?.uid ?: return null
        return db.collection("users").document(userId).get().await().toObject(User::class.java)
    }

    private suspend fun createFamilyGroupChat(userId: String, nickname: String) {
        val ref = db.collection("chats").document("family_group")
        val doc = ref.get().await()
        if (!doc.exists()) {
            ref.set(hashMapOf(
                "chatId" to "family_group", "type" to "group", "name" to "Семеен чат",
                "participants" to listOf(userId), "lastMessage" to "",
                "lastMessageTime" to System.currentTimeMillis(),
                "createdBy" to userId, "createdAt" to System.currentTimeMillis(),
                "deletedFor" to emptyList<String>()
            )).await()
        } else {
            val list = doc.get("participants") as? List<String> ?: emptyList()
            if (!list.contains(userId)) ref.update("participants", list + userId).await()
        }
    }
}
