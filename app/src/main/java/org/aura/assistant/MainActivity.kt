package org.aura.assistant

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.aura.assistant.actions.AndroidActions
import org.aura.assistant.ai.ChatMessage
import org.aura.assistant.ai.OpenAiCompatibleClient
import org.aura.assistant.data.ConversationStore
import org.aura.assistant.data.StoredMessage
import org.aura.assistant.security.SecureSettings
import org.aura.assistant.smarthome.HomeAssistantClient
import java.util.Locale

private val Ink = Color(0xFF080D18)
private val Panel = Color(0xFF121C2E)
private val Cyan = Color(0xFF58D7F5)
private val Violet = Color(0xFF8A7CFF)

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                background = Ink, surface = Panel, primary = Cyan, secondary = Violet,
                onBackground = Color.White, onSurface = Color.White
            )) { AuraApp(this) }
        }
    }
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts?.language = Locale.getDefault()
    }
    fun speak(text: String) { tts?.speak(text.take(3000), TextToSpeech.QUEUE_FLUSH, null, "aura-response") }
    override fun onDestroy() { tts?.stop(); tts?.shutdown(); super.onDestroy() }
}

private data class UiMessage(val role: String, val content: String)

@Composable
private fun AuraApp(activity: MainActivity) {
    val context = activity
    val secure = remember { SecureSettings(context) }
    val store = remember { ConversationStore(context) }
    val ai = remember { OpenAiCompatibleClient() }
    val ha = remember { HomeAssistantClient() }
    val actions = remember { AndroidActions(context) }
    val scope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf<UiMessage>() }
    var input by remember { mutableStateOf("") }
    var endpoint by remember { mutableStateOf(secure.get("ai_endpoint", "https://YOUR-HOST/v1/chat/completions")) }
    var model by remember { mutableStateOf(secure.get("ai_model", "your-model-name")) }
    var apiKey by remember { mutableStateOf(secure.get("ai_key")) }
    var haUrl by remember { mutableStateOf(secure.get("ha_url")) }
    var haToken by remember { mutableStateOf(secure.get("ha_token")) }
    var haEntity by remember { mutableStateOf(secure.get("ha_entity", "light.living_room")) }
    var packageName by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Configure your own HTTPS AI endpoint in Settings to start.") }
    var speakReplies by remember { mutableStateOf(true) }
    var showHaConfirm by remember { mutableStateOf(false) }
    var pendingHaService by remember { mutableStateOf("toggle") }

    LaunchedEffect(Unit) {
        store.recent().forEach { messages += UiMessage(it.role, it.content) }
    }

    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!spoken.isNullOrBlank()) input = spoken
    }

    if (settingsOpen) {
        SettingsScreen(
            endpoint, { endpoint = it }, model, { model = it }, apiKey, { apiKey = it },
            haUrl, { haUrl = it }, haToken, { haToken = it }, haEntity, { haEntity = it },
            onSave = {
                secure.put("ai_endpoint", endpoint.trim())
                secure.put("ai_model", model.trim())
                secure.put("ai_key", apiKey)
                secure.put("ha_url", haUrl.trim())
                secure.put("ha_token", haToken)
                secure.put("ha_entity", haEntity.trim())
                status = "Settings saved securely on this device."
                settingsOpen = false
            },
            onBack = { settingsOpen = false }
        )
        return
    }

    Surface(Modifier.fillMaxSize(), color = Ink) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("AURA", color = Cyan, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    Text("Personal AI assistant", color = Color.LightGray, fontSize = 12.sp)
                }
                TextButton(onClick = { settingsOpen = true }) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Text("Settings")
                }
            }
            Spacer(Modifier.height(10.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Cloud-first • Mobile-light", color = Cyan, fontWeight = FontWeight.SemiBold)
                    Text(status, color = Color.LightGray, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(messages) { message ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement =
                        if (message.role == "user") Arrangement.End else Arrangement.Start) {
                        Surface(
                            color = if (message.role == "user") Color(0xFF20344A) else Panel,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(.88f)
                        ) {
                            Text(message.content, Modifier.padding(12.dp), color = Color.White)
                        }
                    }
                }
            }
            OutlinedTextField(
                value = input, onValueChange = { input = it }, modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Message AURA…") }, maxLines = 4,
                trailingIcon = {
                    Row {
                        IconButton(onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to AURA")
                            }
                            try { voiceLauncher.launch(intent) } catch (_: Exception) {
                                status = "Speech recognition is not available on this device."
                            }
                        }) { Icon(Icons.Default.Mic, contentDescription = "Speak", tint = Cyan) }
                        IconButton(enabled = !busy && input.isNotBlank(), onClick = {
                            val prompt = input.trim()
                            input = ""
                            messages += UiMessage("user", prompt)
                            store.add("user", prompt)
                            val history = store.recent(20).map { ChatMessage(it.role, it.content) }
                            busy = true
                            status = "Connecting to your configured AI endpoint…"
                            scope.launch {
                                try {
                                    val answer = ai.stream(endpoint, model, apiKey, history, 512) { delta ->
                                        // Accumulate streaming text in a single assistant bubble.
                                        if (messages.lastOrNull()?.role == "assistant" && messages.last().content.startsWith("__STREAM__")) {
                                            val previous = messages.removeAt(messages.lastIndex)
                                            messages += previous.copy(content = previous.content.removePrefix("__STREAM__") + delta)
                                        } else {
                                            messages += UiMessage("assistant", "__STREAM__$delta")
                                        }
                                    }
                                    val cleaned = answer.ifBlank { "The model returned an empty response." }
                                    if (messages.lastOrNull()?.role == "assistant") {
                                        messages[messages.lastIndex] = UiMessage("assistant", cleaned)
                                    } else messages += UiMessage("assistant", cleaned)
                                    store.add("assistant", cleaned)
                                    status = "Connected • Conversation stored locally."
                                    if (speakReplies) activity.speak(cleaned)
                                } catch (e: Exception) {
                                    messages += UiMessage("assistant", "I couldn't reach the AI endpoint: ${e.message ?: "unknown error"}")
                                    status = "Connection failed. Check endpoint, model, network and credentials in Settings."
                                } finally { busy = false }
                            }
                        }) { Icon(Icons.Default.Send, contentDescription = "Send", tint = Cyan) }
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { speakReplies = !speakReplies }) {
                    Text(if (speakReplies) "Voice replies: on" else "Voice replies: off")
                }
                OutlinedButton(onClick = { store.clear(); messages.clear(); status = "Local conversation cleared." }) {
                    Text("Clear chat")
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Actions require your tap; AI replies cannot execute device commands.", color = Color.Gray, fontSize = 11.sp)
            var showReminder by remember { mutableStateOf(false) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { showReminder = true }, modifier = Modifier.weight(1f)) { Text("Create reminder") }
                OutlinedButton(onClick = { settingsOpen = true }, modifier = Modifier.weight(1f)) { Text("Settings") }
            }
            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Text("Smart home • Home Assistant", color = Cyan, fontWeight = FontWeight.SemiBold)
                    Text(if (haUrl.isBlank()) "Add your Home Assistant URL and token in Settings." else "Configured entity: $haEntity",
                        color = Color.LightGray, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("turn_on", "turn_off", "toggle").forEach { service ->
                            OutlinedButton(
                                enabled = haUrl.isNotBlank() && haToken.isNotBlank() && !busy,
                                onClick = { pendingHaService = service; showHaConfirm = true },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) { Text(service.replace("_", " "), fontSize = 11.sp) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = packageName, onValueChange = { packageName = it },
                    modifier = Modifier.weight(1f), singleLine = true,
                    label = { Text("App package to open") }, placeholder = { Text("com.example.app") }
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    try {
                        val opened = actions.launchApp(packageName)
                        status = if (opened) "Opened $packageName" else "No launchable app found for that package."
                    } catch (e: Exception) { status = e.message ?: "Unable to open app." }
                }, enabled = packageName.isNotBlank()) { Text("Open") }
            }
            if (showHaConfirm) {
                AlertDialog(
                    onDismissRequest = { showHaConfirm = false },
                    title = { Text("Confirm smart-home action") },
                    text = { Text("Send $pendingHaService to $haEntity on your configured Home Assistant server?") },
                    confirmButton = { TextButton(onClick = {
                        showHaConfirm = false
                        scope.launch {
                            try {
                                val domain = haEntity.substringBefore(".")
                                ha.callService(haUrl, haToken, domain, pendingHaService, haEntity)
                                status = "Home Assistant accepted $pendingHaService for $haEntity."
                            } catch (e: Exception) { status = e.message ?: "Home Assistant request failed." }
                        }
                    }) { Text("Confirm") } },
                    dismissButton = { TextButton(onClick = { showHaConfirm = false }) { Text("Cancel") } }
                )
            }
            if (showReminder) {
                AlertDialog(
                    onDismissRequest = { showReminder = false },
                    title = { Text("Create calendar reminder?") },
                    text = { Text("AURA will open your calendar app. You can review and save the event there.") },
                    confirmButton = { TextButton(onClick = {
                        showReminder = false
                        try { actions.createCalendarReminder("AURA reminder") } catch (_: Exception) { status = "No calendar app is available." }
                    }) { Text("Continue") } },
                    dismissButton = { TextButton(onClick = { showReminder = false }) { Text("Cancel") } }
                )
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    endpoint: String, onEndpoint: (String) -> Unit,
    model: String, onModel: (String) -> Unit,
    apiKey: String, onApiKey: (String) -> Unit,
    haUrl: String, onHaUrl: (String) -> Unit,
    haToken: String, onHaToken: (String) -> Unit,
    haEntity: String, onHaEntity: (String) -> Unit,
    onSave: () -> Unit, onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Ink).padding(20.dp)) {
        Text("AURA Settings", color = Cyan, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("Credentials are encrypted with Android Keystore.", color = Color.LightGray, fontSize = 12.sp)
        Spacer(Modifier.height(14.dp))
        Text("AI endpoint", color = Cyan, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(endpoint, onEndpoint, Modifier.fillMaxWidth(), label = { Text("HTTPS chat completions URL") }, singleLine = true)
        OutlinedTextField(model, onModel, Modifier.fillMaxWidth(), label = { Text("Model ID") }, singleLine = true)
        OutlinedTextField(apiKey, onApiKey, Modifier.fillMaxWidth(), label = { Text("API key (optional)") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
        Spacer(Modifier.height(16.dp))
        Text("Home Assistant (optional)", color = Cyan, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(haUrl, onHaUrl, Modifier.fillMaxWidth(), label = { Text("HTTPS base URL") }, singleLine = true)
        OutlinedTextField(haToken, onHaToken, Modifier.fillMaxWidth(), label = { Text("Long-lived access token") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
        OutlinedTextField(haEntity, onHaEntity, Modifier.fillMaxWidth(), label = { Text("Entity ID e.g. light.living_room") }, singleLine = true)
        Spacer(Modifier.height(10.dp))
        Text("Never put a shared production API key in the APK. Use a trusted token-issuing backend for public deployments.",
            color = Color.LightGray, fontSize = 12.sp)
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back") }
            Button(onClick = onSave, modifier = Modifier.weight(1f)) { Text("Save securely") }
        }
    }
}
