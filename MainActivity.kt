package com.vaultai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navy = Color(0xFF050B18)
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Color(0xFF00BFFF),
                background = navy,
                surface = Color(0xFF0B1427)
            )) {
                Surface(modifier = Modifier.fillMaxSize(), color = navy) {
                    Column(
                        modifier = Modifier.fillMaxSize().background(navy).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("VAULT", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                        Text("AI", color = Color(0xFF00BFFF), fontSize = 48.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Your on-device assistant", color = Color(0xFFB8C8E0), fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("PRIVATE  •  SECURE  •  OFFLINE-READY", color = Color(0xFF00BFFF), fontSize = 11.sp, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(28.dp))
                        Text("Welcome to VaultAI", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("This starter build is the app shell. Offline AI chat and vault features need to be connected to your app code and an on-device model.", color = Color(0xFFB8C8E0), fontSize = 14.sp, lineHeight = 21.sp)
                    }
                }
            }
        }
    }
}
