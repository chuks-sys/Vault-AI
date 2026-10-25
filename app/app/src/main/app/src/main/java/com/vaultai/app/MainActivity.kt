package com.vaultai.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

private val ComponentActivity.dataStore by preferencesDataStore(
    name = "vaultai_settings"
)

private val AI_NAME_KEY = stringPreferencesKey("ai_name")
private val VOICE_ENABLED_KEY = booleanPreferencesKey("voice_enabled")

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        setContent {
            VaultAIApp(
                activity = this,
                speak = { message -> speak(message) }
            )
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
        }
    }

    private fun speak(message: String) {
        tts?.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "vaultai_response"
        )
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun VaultAIApp(
    activity: MainActivity,
    speak: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    var aiName by remember { mutableStateOf("VaultAI") }
    var nameInput by remember { mutableStateOf("VaultAI") }
    var showSettings by remember { mutableStateOf(false) }
    var voiceEnabled by remember { mutableStateOf(false) }
    var userInput by remember { mutableStateOf("") }

    val messages = remember {
        mutableStateListOf(
            "VaultAI: Systems online. Welcome."
        )
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        val preferences = activity.dataStore.data.first()

        aiName = preferences[AI_NAME_KEY] ?: "VaultAI"
        nameInput = aiName
        voiceEnabled = preferences[VOICE_ENABLED_KEY] ?: false
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()

            if (!spokenText.isNullOrBlank()) {
                messages.add("You: $spokenText")

                val reply =
                    "I'm $aiName. Your voice was received. " +
                    "My AI brain still needs to be connected."

                messages.add("$aiName: $reply")

                if (voiceEnabled) {
                    speak(reply)
                }
            }
        }
    }

    val microphonePermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                launchSpeechRecognition(speechLauncher::launch)
            }
        }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF050914)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = aiName.uppercase(),
                        color = Color(0xFF54E8FF),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            showSettings = !showSettings
                        }
                    ) {
                        Text("Settings")
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0B1C30)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            color = Color(0xFF123E59)
                        ) {
                            BoxPlaceholder()
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "VOICE ASSISTANT",
                            color = Color(0xFF54E8FF),
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Ready when you are",
                            color = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (showSettings) {

                    Text(
                        text = "ASSISTANT SETTINGS",
                        color = Color(0xFF54E8FF),
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Choose AI name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val chosenName = nameInput.trim()

                            if (chosenName.isNotEmpty()) {
                                scope.launch {
                                    activity.dataStore.edit {
                                        it[AI_NAME_KEY] = chosenName
                                    }

                                    aiName = chosenName
                                    messages.add(
                                        "$aiName: Assistant name updated."
                                    )
                                }
                            }
                        }
                    ) {
                        Text("Save AI Name")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Speak responses",
                            color = Color.White
                        )

                        Switch(
                            checked = voiceEnabled,
                            onCheckedChange = { enabled ->
                                voiceEnabled = enabled

                                scope.launch {
                                    activity.dataStore.edit {
                                        it[VOICE_ENABLED_KEY] = enabled
                                    }
                                }
                            }
                        )
                    }

                } else {

                    Text(
                        text = "CONVERSATION",
                        color = Color(0xFF54E8FF),
                        fontWeight = FontWeight.Bold
                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        items(messages) { message ->
                            Text(
                                text = message,
                                color = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        label = { Text("Type a message") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (
                                    ContextCompat.checkSelfPermission(
                                        activity,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    launchSpeechRecognition(
                                        speechLauncher::launch
                                    )
                                } else {
                                    microphonePermissionLauncher.launch(
                                        Manifest.permission.RECORD_AUDIO
                                    )
                                }
                            }
                        ) {
                            Text("Speak")
                        }

                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (userInput.isNotBlank()) {
                                    messages.add("You: $userInput")

                                    val reply =
                                        "I'm $aiName. Your message was received. " +
                                        "My AI brain is not connected yet."

                                    messages.add("$aiName: $reply")

                                    if (voiceEnabled) {
                                        speak(reply)
                                    }

                                    userInput = ""
                                }
                            }
                        ) {
                            Text("Send")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "VAULTAI • PERSONAL ASSISTANT",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun BoxPlaceholder() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "V",
            color = Color(0xFF54E8FF),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun launchSpeechRecognition(
    launch: (Intent) -> Unit
) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Speak to VaultAI"
        )
    }

    launch(intent)
}
