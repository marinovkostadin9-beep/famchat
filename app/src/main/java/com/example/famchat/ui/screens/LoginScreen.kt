package com.example.famchat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.famchat.data.FirebaseAuthManager
import com.example.famchat.navigation.Screen
import com.example.famchat.ui.theme.BackgroundLight
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

    val fieldColors = TextFieldDefaults.colors(
        unfocusedContainerColor = BackgroundLight,
        focusedContainerColor = BackgroundLight,
        unfocusedIndicatorColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        errorIndicatorColor = MaterialTheme.colorScheme.error
    )

    Scaffold { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(PrimaryBlue, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💬", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("FamChat", fontSize = 22.sp, fontWeight = FontWeight.Medium)
                    Text("Семеен чат", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 24.dp))

                    TextField(
                        value = nickname, onValueChange = { nickname = it; errorMessage = null },
                        placeholder = { Text("Никнейм") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(), isError = errorMessage != null,
                        shape = RoundedCornerShape(14.dp), colors = fieldColors
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TextField(
                        value = password, onValueChange = { password = it; errorMessage = null },
                        placeholder = { Text("Парола") }, singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(), isError = errorMessage != null,
                        shape = RoundedCornerShape(14.dp), colors = fieldColors
                    )

                    if (errorMessage != null) {
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (nickname.length < 5) { errorMessage = "Никнеймът трябва да е минимум 5 символа"; return@Button }
                            if (password.isBlank()) { errorMessage = "Въведи парола"; return@Button }
                            isLoading = true
                            scope.launch {
                                authManager.login(nickname, password).onSuccess {
                                    navController.navigate(Screen.Chat.createRoute(com.example.famchat.FAMILY_GROUP_ID, com.example.famchat.FAMILY_GROUP_NAME)) { popUpTo(Screen.Login.route) { inclusive = true } }
                                }.onFailure { errorMessage = it.message ?: "Грешка при вход" }
                                isLoading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp), enabled = !isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Вход", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                        Text("Нямаш акаунт? Регистрирай се", color = PrimaryBlue, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
