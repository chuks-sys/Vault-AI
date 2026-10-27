package com.vaultai.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null

    private var listening by mutableStateOf(false)
    private var screenPermissionGranted by mutableStateOf(false)
    private var recognizedText by mutableStateOf("Ready for your command.")
    private var assistantReply by mutableStateOf(
        "Hello. I am VaultAI. How can I help you?"
    )
    private var statusText by mutableStateOf("SYSTEM READY")

    private val microphonePermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                startListening()
            } else {
                statusText = "MICROPHONE PERMISSION DENIED"
            }
        }

    private val screenCapturePermission =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                screenPermissionGranted = true
                statusText = "SCREEN PERMISSION APPROVED"
                assistantReply =
                    "Screen permission approved. Screen capture still needs to be connected."
            } else {
                screenPermissionGranted = false
                statusText = "SCREEN SHARING NOT APPROVED"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        textToSpeech = TextToSpeech(this, this)

        setContent {
            VaultAIApp(
                listening = listening,
                screenPermissionGranted = screenPermissionGranted,
                recognizedText = recognizedText,
                assistantReply = assistantReply,
                statusText = statusText,
                onTalk = { requestMicrophonePermission() },
                onStopListening = { stopListening() },
                onShareScreen = { requestScreenPermission() }
            )
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale.US
        }
    }

    private fun requestMicrophonePermission() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            startListening()
        } else {
            microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            statusText = "SPEECH RECOGNITION UNAVAILABLE"
            return
        }

        speechRecognizer?.destroy()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {
                    listening = true
                    statusText = "LISTENING..."
                }

                override fun onBeginningOfSpeech() {
                    statusText = "VOICE DETECTED"
                }

                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    listening = false
                    statusText = "PROCESSING..."
                }

                override fun onError(error: Int) {
                    listening = false
                    statusText = "SPEECH ENDED — TAP TO TRY AGAIN"
                }

                override fun onResults(results: Bundle?) {
                    listening = false

                    val matches = results?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )

                    val spokenWords = matches?.firstOrNull()

                    if (!spokenWords.isNullOrBlank()) {
                        recognizedText = spokenWords

                        assistantReply =
                            "I heard you say: $spokenWords. " +
                            "My full AI engine is not connected yet."

                        speak(assistantReply)
                        statusText = "VOICE RECEIVED"
                    } else {
                        statusText = "NO SPEECH DETECTED"
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            }
        )

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            listening = false
            statusText = "COULD NOT START LISTENING"
        }
    }

    private fun stopListening() {
        speechRecognizer?.stopListening()
        listening = false
        statusText = "LISTENING STOPPED"
    }

    private fun requestScreenPermission() {
        val manager = getSystemService(
            MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager

        screenCapturePermission.launch(
            manager.createScreenCaptureIntent()
        )
    }

    private fun speak(message: String) {
        textToSpeech?.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "VAULTAI_RESPONSE"
        )
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onDestroy()
    }
}

@Composable
private fun VaultAIApp(
    listening: Boolean,
    screenPermissionGranted: Boolean,
    recognizedText: String,
    assistantReply: String,
    statusText: String,
    onTalk: () -> Unit,
    onStopListening: () -> Unit,
    onShareScreen: () -> Unit
) {
    val background = Color(0xFF050914)
    val cyan = Color(0xFF00E5FF)
    val purple = Color(0xFF6C63FF)

    val transition = rememberInfiniteTransition(label = "vaultai_orb")

    val orbScale by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_scale"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF101D35),
                            background,
                            Color(0xFF090D20)
                        )
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "V A U L T A I",
                color = cyan,
                fontSize = 29.sp,
                letterSpacing = 5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "LIVE VOICE + SCREEN ASSISTANT",
                color = Color.LightGray,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(38.dp))

            Box(
                modifier = Modifier
                    .size((150 * orbScale).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                cyan.copy(alpha = 0.65f),
                                purple.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(cyan, purple)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "V",
                        color = Color.White,
                        fontSize = 58.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = statusText,
                color = cyan,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "YOU SAID",
                color = Color.Gray,
                fontSize = 12.sp
            )

            Text(
                text = recognizedText,
                color = Color.White,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "VAULTAI",
                color = purple,
                fontSize = 12.sp
            )

            Text(
                text = assistantReply,
                color = Color.White,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onTalk,
                colors = ButtonDefaults.buttonColors(
                    containerColor = cyan,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .height(52.dp)
            ) {
                Text(
                    text = if (listening) {
                        "LISTENING..."
                    } else {
                        "TALK TO VAULTAI"
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onShareScreen,
                colors = ButtonDefaults.buttonColors(
                    containerColor = purple
                ),
                modifier = Modifier
                    .height(52.dp)
            ) {
                Text(
                    text = if (screenPermissionGranted) {
                        "SCREEN PERMISSION APPROVED"
                    } else {
                        "SHARE MY SCREEN"
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onStopListening,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF30394D)
                )
            ) {
                Text("STOP LISTENING")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "VOICE • VISION • INTELLIGENCE",
                color = Color.Gray,
                fontSize = 10.sp,
                letterSpacing = 2.sp
            )
        }
    }
}
