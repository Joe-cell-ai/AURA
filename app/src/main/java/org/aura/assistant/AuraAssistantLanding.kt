package org.aura.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime

private val AssistantInk = Color(0xFF080D1C)
private val AssistantMuted = Color(0xFFAAB7D0)
private val AssistantCyan = Color(0xFF72D9F4)
private val AssistantViolet = Color(0xFF9D8BFF)

@Composable
internal fun AuraAssistantLanding(
    status: String,
    onVoice: () -> Unit,
    onAsk: (String) -> Unit,
    onReminder: () -> Unit,
    onSmartHome: () -> Unit,
    onSettings: () -> Unit
) {
    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Hello"
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(greeting, color = AssistantMuted, fontSize = 15.sp)
                Text("How can I help?", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Assistant settings", tint = AssistantMuted)
            }
        }
        Spacer(Modifier.height(22.dp))
        Box(
            Modifier.fillMaxWidth().weight(1f).padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier.size(244.dp)
                    .background(Brush.radialGradient(listOf(Color(0x553A8DFF), Color(0x222B3F8D), Color.Transparent)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier.size(174.dp)
                        .shadow(28.dp, CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF173C69), Color(0xFF29235F), Color(0xFF151B3C))), CircleShape)
                        .clickable(onClick = onVoice),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier.size(132.dp)
                            .background(Brush.linearGradient(listOf(AssistantCyan, Color(0xFF6388FF), AssistantViolet)), CircleShape)
                            .padding(3.dp)
                            .background(Brush.linearGradient(listOf(Color(0xFF152D56), Color(0xFF241D50))), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Talk to AURA", tint = Color.White, modifier = Modifier.size(54.dp))
                    }
                }
            }
        }
        Text("Talk to AURA", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Text("Ask naturally. I'll help you get things done.", color = AssistantMuted, fontSize = 12.sp)
        if (status.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(status, color = AssistantCyan, fontSize = 11.sp, maxLines = 2)
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AssistantSuggestion("Set a reminder", "◷", Modifier.weight(1f), onReminder)
            AssistantSuggestion("Smart home", "⌂", Modifier.weight(1f), onSmartHome)
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AssistantSuggestion("Find something", "⌕", Modifier.weight(1f)) { onAsk("Search the web for ") }
            AssistantSuggestion("Open an app", "▦", Modifier.weight(1f)) { onAsk("Open ") }
        }
        Spacer(Modifier.height(18.dp))
        Surface(
            onClick = { onAsk("") },
            modifier = Modifier.fillMaxWidth().height(58.dp),
            color = Color(0xFF18233A),
            shape = RoundedCornerShape(30.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF34435F))
        ) {
            Row(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AssistantCyan)
                Spacer(Modifier.width(12.dp))
                Text("Ask AURA anything", color = AssistantMuted, modifier = Modifier.weight(1f), fontSize = 14.sp)
                Icon(Icons.Default.Mic, contentDescription = "Voice input", tint = AssistantCyan)
            }
        }
    }
}

@Composable
private fun AssistantSuggestion(label: String, glyph: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        color = Color(0xFF151F35),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2B3B59))
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(glyph, color = AssistantCyan, fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}
