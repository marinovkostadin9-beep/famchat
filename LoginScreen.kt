package com.example.famchat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.famchat.data.FirebaseAuthManager
import com.example.famchat.navigation.Screen
import com.example.famchat.ui.theme.PrimaryBlue
import com.example.famchat.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController, authManager: FirebaseAuthManager) {
    var nickname by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("💬", fontSize = 64.sp, modifier = Modifier.padding(bottom = 12.dp))
            Text("FamChat", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            Text("Семеен чат", fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 40.dp))

            OutlinedTextField(
                value = nickname, onValueChange = { nickname = it; errorMessage = null },
                label = { Text("Никнейм") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(), isError = errorMessage != null,
                shape = MaterialTheme.shapes.large
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it; errorMessage = null },
                label = { Text("Парола") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(), isError = errorMessage != null,
                shape = MaterialTheme.shapes.large
            )

            if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (nickname.length < 5) { errorMessage = "Никнеймът трябва да е минимум 5 символа"; return@Button }
                    if (password.isBlank()) { errorMessage = "Въведи парола"; return@Button }
                    isLoading = true
                    scope.launch {
                        authManager.login(nickname, password).onSuccess {
                            navController.navigate(Screen.ChatList.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                        }.onFailure { errorMessage = it.message ?: "Грешка при вход" }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp), enabled = !isLoading,
                shape = MaterialTheme.shapes.large
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                else Text("Вход", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                Text("Нямаш акаунт? Регистрирай се", color = PrimaryBlue, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("FamChat v1.0", fontSize = 10.sp, color = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 16.dp))
        }
    }
}
