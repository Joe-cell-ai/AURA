package org.aura.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF080D18)
private val Panel = Color(0xFF121C2E)
private val Cyan = Color(0xFF58D7F5)
private val Violet = Color(0xFF8A7CFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                background = Ink, surface = Panel, primary = Cyan,
                secondary = Violet, onBackground = Color.White, onSurface = Color.White
            )) { AuraHome() }
        }
    }
}

@Composable
private fun AuraHome() {
    Surface(Modifier.fillMaxSize(), color = Ink) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("AURA", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Cyan)
                    Text("Your personal AI assistant", color = Color.LightGray, fontSize = 13.sp)
                }
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
            }
            Spacer(Modifier.height(64.dp))
            Box(
                Modifier.size(190.dp).background(
                    Brush.linearGradient(listOf(Cyan.copy(alpha = .28f), Violet.copy(alpha = .35f))),
                    CircleShape
                ),
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.size(142.dp).background(Panel, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Mic, contentDescription = "Microphone", tint = Cyan, modifier = Modifier.size(54.dp))
                }
            }
            Spacer(Modifier.height(30.dp))
            Text("How can I help?", fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text("Cloud-first AI is being connected. Voice and actions are not active yet.",
                color = Color.LightGray, fontSize = 14.sp)
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = { },
                enabled = false,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(18.dp)
            ) { Text("Talk — coming in a later batch") }
            Spacer(Modifier.weight(1f))
            Text("Private by design • You control your AI endpoint", color = Color.Gray, fontSize = 12.sp)
        }
    }
}
