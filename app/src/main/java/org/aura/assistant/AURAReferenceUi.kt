package org.aura.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AuraInk = Color(0xFF050B20)
private val AuraCyan = Color(0xFF13D9F5)
private val AuraViolet = Color(0xFF9D35F5)
private val AuraTile = Color(0xFF17264F)

@Composable
internal fun AuraMicrophoneHero(onSpeak: () -> Unit, status: String) {
    Box(
        Modifier.fillMaxWidth().height(270.dp)
            .background(Brush.linearGradient(listOf(Color(0xFF061B55), Color(0xFF17104B), Color(0xFF28105C))), RoundedCornerShape(28.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Your Smart AI Assistant", color = Color(0xFFC4D9FF), fontSize = 12.sp, letterSpacing = 1.2.sp)
            Box(
                Modifier.size(164.dp).border(5.dp, Brush.linearGradient(listOf(AuraCyan, Color(0xFF3E70FF), AuraViolet)), CircleShape)
                    .padding(8.dp).background(Color(0xFF15134A), CircleShape).clickable(onClick = onSpeak),
                contentAlignment = Alignment.Center
            ) {
                Text("🎙", fontSize = 62.sp)
            }
            Text("Tap to Speak", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Text("Speech recognition • Review before sending", color = Color(0xFFC5CDEB), fontSize = 11.sp)
            if (status.isNotBlank()) Text(status, color = Color(0xFF9EDCF8), fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable
internal fun AuraQuickTile(title: String, symbol: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.weight(1f).height(70.dp),
        color = AuraTile,
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(symbol, color = AuraCyan, fontSize = 25.sp)
            Spacer(Modifier.width(10.dp))
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, lineHeight = 16.sp)
        }
    }
}

@Composable
internal fun AuraBottomNavigation(selected: String, onSelect: (String) -> Unit) {
    Surface(color = Color(0xFF0B1532), shape = RoundedCornerShape(22.dp), tonalElevation = 8.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 7.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            val items = listOf(
                Triple("home", "⌂", "Home"),
                Triple("memories", "◈", "Memories"),
                Triple("automations", "⚡", "Automations"),
                Triple("extensions", "✣", "Extensions")
            )
            items.forEach { (id, glyph, label) ->
                Column(
                    Modifier.weight(1f).clickable { onSelect(id) }.padding(vertical = 3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(glyph, color = if (selected == id) AuraCyan else Color(0xFF9AAED0), fontSize = 21.sp)
                    Text(label, color = if (selected == id) Color.White else Color(0xFF9AAED0), fontSize = 10.sp,
                        fontWeight = if (selected == id) FontWeight.Bold else FontWeight.Normal)
                    if (selected == id) Spacer(Modifier.padding(top = 2.dp).width(22.dp).height(2.dp).background(
                        Brush.horizontalGradient(listOf(AuraCyan, AuraViolet)), RoundedCornerShape(2.dp)))
                }
            }
        }
    }
}

@Composable
internal fun AuraVoicePanel(onStop: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(420.dp).background(
        Brush.linearGradient(listOf(Color(0xFF080D2B), Color(0xFF211052), Color(0xFF090E2B))), RoundedCornerShape(26.dp)
    ).padding(20.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("Listening…", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Box(Modifier.size(150.dp).border(5.dp, Brush.linearGradient(listOf(AuraCyan, Color(0xFF526BFF), AuraViolet)), CircleShape)
                .background(Color(0xFF17154D), CircleShape), contentAlignment = Alignment.Center) {
                Text("🎙", fontSize = 58.sp)
            }
            Text("Tap Stop to return to chat", color = Color(0xFFD4D8F0), fontSize = 13.sp)
            Button(onClick = onStop, colors = ButtonDefaults.buttonColors(containerColor = AuraViolet),
                shape = RoundedCornerShape(28.dp), modifier = Modifier.width(180.dp).height(52.dp)) {
                Text("■  Stop", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
