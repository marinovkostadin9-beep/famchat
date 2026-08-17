package com.example.famchat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.famchat.R
import com.example.famchat.data.FirebaseAuthManager
import com.example.famchat.navigation.Screen
import com.example.famchat.ui.components.AvatarGallery
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, authManager: FirebaseAuthManager) {
    var nickname by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf(R.drawable.avatar_dog) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Създай акаунт", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 28.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = nickname, onValueChange = { nickname = it; errorMessage = null },
                label = { Text("Никнейм (мин. 5 символа)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(), isError = errorMessage != null,
                shape = MaterialTheme.shapes.large
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it; errorMessage = null },
                label = { Text("Парола") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmPassword, onValueChange = { confirmPassword = it; errorMessage = null },
                label = { Text("Потвърди парола") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large
            )
            Spacer(modifier = Modifier.height(24.dp))
            AvatarGallery(selectedAvatar = selectedAvatar, onAvatarSelected = { selectedAvatar = it })
            Spacer(modifier = Modifier.height(24.dp))
            if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
            }
            Button(
                onClick = {
                    when {
                        nickname.length < 5 -> { errorMessage = "Никнеймът трябва да е минимум 5 символа"; return@Button }
                        password.length < 6 -> { errorMessage = "Паролата трябва да е минимум 6 символа"; return@Button }
                        password != confirmPassword -> { errorMessage = "Паролите не съвпадат"; return@Button }
                    }
                    isLoading = true
                    scope.launch {
                        authManager.register(nickname, password, selectedAvatar).onSuccess {
                            navController.navigate(Screen.ChatList.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                        }.onFailure { errorMessage = it.message ?: "Грешка при регистрация" }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp), enabled = !isLoading,
                shape = MaterialTheme.shapes.large
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                else Text("Регистрирай се", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
