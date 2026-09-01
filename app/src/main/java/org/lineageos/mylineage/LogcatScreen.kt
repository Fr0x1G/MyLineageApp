package org.lineageos.mylineage

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogcatScreen(onBack: () -> Unit) {
    val lineageColor = Color(0xFF167C80)
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var logs by remember { mutableStateOf(listOf("Fetching logs...")) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val process = Runtime.getRuntime().exec(
                    arrayOf("logcat", "-d", "-v", "threadtime", "--pid=${android.os.Process.myPid()}")
                )
                val logText = process.inputStream.bufferedReader().readText()
                if (logText.isNotEmpty()) {
                    logs = logText.split("\n")
                } else {
                    logs = listOf("No logs found.")
                }
            } catch (e: Exception) {
                logs = listOf("Error fetching logs: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("LOGCAT", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp, color = lineageColor)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Button(
                    onClick = {
                        val fullLog = logs.joinToString("\n")
                        clipboardManager.setText(AnnotatedString(fullLog))
                        android.widget.Toast.makeText(context, "Logs copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Fr0x1G/MyLineageApp/issues/new"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = lineageColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Submit to GitHub", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFF121212))
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                items(logs.size) { index ->
                    val line = logs[index]
                    val textColor = if (line.contains(" E ")) Color(0xFFE57373) else lineageColor
                    Text(
                        text = line,
                        color = textColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}