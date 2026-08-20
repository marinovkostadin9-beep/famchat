package com.example.famchat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.famchat.R
import com.example.famchat.ui.theme.PrimaryBlue
import com.example.famchat.ui.theme.TextPrimary

data class AvatarOption(
    val resId: Int,
    val emoji: String,
    val name: String,
    val bgColor: Color
)

val avatarOptions = listOf(
    AvatarOption(R.drawable.avatar_dog, "🐶", "Куче", Color(0xFFFF6B6B)),
    AvatarOption(R.drawable.avatar_cat, "🐱", "Коте", Color(0xFF4ECDC4)),
    AvatarOption(R.drawable.avatar_fox, "🦊", "Лисица", Color(0xFFFF8C42)),
    AvatarOption(R.drawable.avatar_panda, "🐼", "Панда", Color(0xFF2C3E50)),
    AvatarOption(R.drawable.avatar_bear, "🐻", "Мечка", Color(0xFF8B4513)),
    AvatarOption(R.drawable.avatar_rabbit, "🐰", "Зайче", Color(0xFFFFB6C1))
)

@Composable
fun AvatarGallery(
    selectedAvatar: Int,
    onAvatarSelected: (Int) -> Unit
) {
    Column {
        Text(
            text = "Избери аватар:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            avatarOptions.forEach { avatar ->
                val isSelected = avatar.resId == selectedAvatar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isSelected) PrimaryBlue.copy(alpha = 0.15f)
                            else Color.Transparent
                        )
                        .border(
                            width = if (isSelected) 2.5.dp else 1.5.dp,
                            color = if (isSelected) PrimaryBlue else Color(0xFFE2E8F0),
                            shape = CircleShape
                        )
                        .clickable { onAvatarSelected(avatar.resId) }
                        .padding(10.dp)
                ) {
                    Text(
                        text = avatar.emoji,
                        fontSize = 26.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AvatarCircle(resId: Int, size: androidx.compose.ui.unit.Dp) {
    val option = avatarOptions.find { it.resId == resId } ?: avatarOptions[0]
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(option.bgColor.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Text(option.emoji, fontSize = (size.value * 0.55f).sp)
    }
}
