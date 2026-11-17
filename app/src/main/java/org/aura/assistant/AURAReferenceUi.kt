package org.aura.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import org.aura.assistant.data.MemoryRecord
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
        Canvas(Modifier.fillMaxSize()) {
            val cyanWave = Path().apply {
                moveTo(0f, size.height * 0.70f)
                cubicTo(size.width * 0.22f, size.height * 0.35f, size.width * 0.34f, size.height * 0.95f, size.width * 0.56f, size.height * 0.60f)
                cubicTo(size.width * 0.76f, size.height * 0.28f, size.width * 0.84f, size.height * 0.80f, size.width, size.height * 0.42f)
            }
            val violetWave = Path().apply {
                moveTo(0f, size.height * 0.83f)
                cubicTo(size.width * 0.25f, size.height * 0.55f, size.width * 0.34f, size.height * 0.98f, size.width * 0.58f, size.height * 0.78f)
                cubicTo(size.width * 0.78f, size.height * 0.54f, size.width * 0.90f, size.height * 0.92f, size.width, size.height * 0.68f)
            }
            drawPath(cyanWave, AuraCyan.copy(alpha = 0.34f), style = Stroke(width = 24f))
            drawPath(violetWave, AuraViolet.copy(alpha = 0.42f), style = Stroke(width = 18f))
        }
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
internal fun RowScope.AuraQuickTile(title: String, symbol: String, onClick: () -> Unit) {
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
                Triple("actions", "⚙", "Actions"),
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
internal fun AuraVoicePanel(listening: Boolean, onStart: () -> Unit, onStop: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(420.dp).background(
        Brush.linearGradient(listOf(Color(0xFF080D2B), Color(0xFF211052), Color(0xFF090E2B))), RoundedCornerShape(26.dp)
    ).padding(20.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(if (listening) "Voice session active" else "Voice input", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Box(Modifier.size(150.dp).border(5.dp, Brush.linearGradient(listOf(AuraCyan, Color(0xFF526BFF), AuraViolet)), CircleShape)
                .background(Color(0xFF17154D), CircleShape), contentAlignment = Alignment.Center) {
                Text("🎙", fontSize = 58.sp)
            }
            Text(if (listening) "Speak a phone command. AURA will listen again after it finishes." else "Start a hands-free foreground session, or use one-shot voice input.", color = Color(0xFFD4D8F0), fontSize = 13.sp)
            Button(onClick = onStart, enabled = !listening, colors = ButtonDefaults.buttonColors(containerColor = AuraViolet),
                shape = RoundedCornerShape(28.dp), modifier = Modifier.width(200.dp).height(52.dp)) {
                Text(if (listening) "Listening…" else "🎙  Start voice session", color = Color.White, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onStop) { Text(if (listening) "Stop voice session" else "Back to Home", color = Color(0xFFD4D8F0)) }
        }
    }
}


@Composable
internal fun ColumnScope.AuraMemoriesPage(
    memories: List<MemoryRecord>,
    enabled: Boolean,
    onEnabled: (Boolean) -> Unit,
    onAdd: () -> Unit,
    onEdit: (MemoryRecord) -> Unit,
    onDelete: (Long) -> Unit,
    onExport: () -> Unit,
    onDeleteAll: () -> Unit
) {
    Column(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Memories", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text("Your personal memories help AURA provide more relevant answers.", color = Color(0xFFB7C8E8), fontSize = 12.sp)
            }
            TextButton(onClick = onAdd) { Text("+", color = AuraCyan, fontSize = 26.sp) }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Use approved memories in AI chats", color = Color.White, modifier = Modifier.weight(1f), fontSize = 12.sp)
            Switch(checked = enabled, onCheckedChange = onEnabled)
        }
        if (memories.isEmpty()) {
            Surface(color = AuraTile, shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No saved memories yet", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("Add only the details you want AURA to remember.", color = Color(0xFFB7C8E8), fontSize = 12.sp)
                }
            }
        } else {
            memories.forEachIndexed { index, item ->
                Surface(color = AuraTile, shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(listOf("⌂", "▣", "♡", "✈")[index % 4], color = AuraCyan, fontSize = 22.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(item.content, color = Color.White, modifier = Modifier.weight(1f), fontSize = 13.sp)
                        TextButton(onClick = { onEdit(item) }) { Text("Edit") }
                        TextButton(onClick = { onDelete(item.id) }) { Text("×", color = Color(0xFFFF8A9A)) }
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Surface(color = Color(0xFF102D49), shape = RoundedCornerShape(18.dp)) {
            Text("🔒  Memories are encrypted and stored securely on your device.", color = Color(0xFFC6EFFF),
                modifier = Modifier.fillMaxWidth().padding(16.dp), fontSize = 12.sp)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onExport, enabled = memories.isNotEmpty()) { Text("Export") }
            TextButton(onClick = onDeleteAll, enabled = memories.isNotEmpty()) { Text("Delete all") }
            Button(onClick = onAdd, colors = ButtonDefaults.buttonColors(containerColor = AuraViolet)) { Text("+ Add Memory") }
        }
    }
}

@Composable
internal fun ColumnScope.AuraAutomationsPage(onCreate: () -> Unit, onCancelAll: () -> Unit) {
    Column(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Automations", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text("Set up smart reminders to save time and stay productive.", color = Color(0xFFB7C8E8), fontSize = 13.sp)
        listOf(
            Triple("One-time reminder", "Schedule a notification on this device", "◷"),
            Triple("Daily reminder", "Repeat every day using Android background work", "▦"),
            Triple("Weekly reminder", "Repeat every week using Android background work", "♧")
        ).forEach { (title, description, symbol) ->
            Surface(color = AuraTile, shape = RoundedCornerShape(18.dp), onClick = onCreate) {
                Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(symbol, color = AuraCyan, fontSize = 26.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(description, color = Color(0xFFB7C8E8), fontSize = 11.sp)
                    }
                    Text("›", color = AuraCyan, fontSize = 25.sp)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = onCreate, modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AuraViolet), shape = RoundedCornerShape(26.dp)) {
            Text("+  Add Automation", color = Color.White, fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onCancelAll, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Cancel all pending reminders", color = Color(0xFFFFA3B0))
        }
    }
}

@Composable
internal fun ColumnScope.AuraExtensionsPage(
    extensions: List<Triple<String, String, Boolean>>,
    onGrantChange: (String, Boolean) -> Unit
) {
    Column(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Extensions", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text("Manage optional AURA capabilities. Each permission is controlled by you.", color = Color(0xFFB7C8E8), fontSize = 13.sp)
        extensions.forEach { (name, description, granted) ->
            Surface(color = AuraTile, shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(name, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(description, color = Color(0xFFB7C8E8), fontSize = 11.sp)
                    }
                    Switch(checked = granted, onCheckedChange = { onGrantChange(name, it) })
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Text("The current catalogue is declarative: it does not download or execute third-party code.", color = Color(0xFFB7C8E8), fontSize = 11.sp)
    }
}


@Composable
internal fun AuraDeviceActionsPage(
    onDial: (String) -> Unit,
    onSms: (String, String) -> Unit,
    onSettings: (org.aura.assistant.actions.SettingsDestination) -> Unit,
    onHistory: () -> Unit
) {
    var phone by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var sms by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    Column(Modifier.fillMaxWidth().verticalScroll(androidx.compose.foundation.rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Device actions", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("AURA opens Android's own apps and settings. You review calls and messages before sending.", color = Color(0xFFB7C8E8), fontSize = 12.sp)
        Card(colors = CardDefaults.cardColors(containerColor = AuraTile)) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Phone & messages", color = Color.White, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Phone number") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onDial(phone) }, modifier = Modifier.weight(1f)) { Text("Open dialler") }
                    OutlinedButton(onClick = { onSms(phone, sms) }, modifier = Modifier.weight(1f)) { Text("Compose SMS") }
                }
                OutlinedTextField(value = sms, onValueChange = { sms = it }, modifier = Modifier.fillMaxWidth(), label = { Text("SMS message") }, minLines = 3)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = AuraTile)) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Android settings", color = Color.White, fontWeight = FontWeight.SemiBold)
                val destinations = listOf(
                    "Wi-Fi" to org.aura.assistant.actions.SettingsDestination.WIFI,
                    "Bluetooth" to org.aura.assistant.actions.SettingsDestination.BLUETOOTH,
                    "Sound" to org.aura.assistant.actions.SettingsDestination.SOUND,
                    "Display" to org.aura.assistant.actions.SettingsDestination.DISPLAY,
                    "Apps" to org.aura.assistant.actions.SettingsDestination.APPLICATIONS,
                    "Security" to org.aura.assistant.actions.SettingsDestination.SECURITY,
                    "Location" to org.aura.assistant.actions.SettingsDestination.LOCATION
                )
                destinations.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEach { (label, destination) ->
                            OutlinedButton(onClick = { onSettings(destination) }, modifier = Modifier.weight(1f)) { Text(label) }
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        OutlinedButton(onClick = onHistory, modifier = Modifier.fillMaxWidth()) { Text("View action history") }
    }
}
