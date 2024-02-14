package com.femi.wordbank

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.femi.wordbank.ui.theme.WordBankTheme
import java.util.Locale

private lateinit var speechRecognizer: SpeechRecognizer
private lateinit var recordAudioPermission: ManagedActivityResultLauncher<String, Boolean>

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WordBankTheme {

                var text by remember { mutableStateOf("") }
                var isRecording by remember { mutableStateOf(false) }

                recordAudioPermission = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    if (isGranted) {
                        // Permission Accepted: Do something
                        Toast.makeText(this, "PERMISSION GRANTED", Toast.LENGTH_SHORT).show()
                        startRecording()

                    } else {
                        // Permission Denied: Do something
                        Toast.makeText(this, "PERMISSION NOT GRANTED", Toast.LENGTH_SHORT).show()
                    }
                }


                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

                speechRecognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isRecording = true
                        text = ""
                    }

                    override fun onBeginningOfSpeech() {
                        text = ""
                    }

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isRecording = false
                    }

                    override fun onError(error: Int) {
                        if (isRecording && error == SpeechRecognizer.ERROR_NO_MATCH)
                            text =
                                "Error - It seems you might be playing music or a really loud sound"
                        isRecording = false
                    }

                    override fun onResults(results: Bundle?) {
                        val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        text = data?.get(0) ?: "Couldn't recognize speech"
                        isRecording = false
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            val data =
                                partialResults?.getStringArrayList(SpeechRecognizer.RECOGNITION_PARTS)
                            text = data?.get(0) ?: "Couldn't recognize speech"
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}

                })


                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        content = { paddingValues ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues),
                                contentAlignment = Alignment.Center
                            ) {
                                SpeechToTextScreen(this@MainActivity, text, isRecording) {
                                    isRecording = it
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}

@Composable
fun SpeechToTextScreen(
    context: Context?,
    text: String,
    isRecording: Boolean,
    setRecording: (Boolean) -> Unit,
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            text = text,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(5.dp))
        Button(
            onClick = {
                context?.let { context ->
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ),
                        -> {

                            if (isRecording) {
                                speechRecognizer.stopListening()
                                setRecording(false)
                            } else {
                                startRecording()
                            }

                        }

                        else -> {
                            // Asking for permission
                            recordAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }


            }
        ) {
            Row {
                Icon(
                    painter = painterResource(id = R.drawable.ic_mic),
                    contentDescription = "Record Icon",
                    modifier = Modifier.padding(end = 8.dp),
                    tint = if (isRecording) Color.Red else Color.Green
                )
                Text(
                    text = "Record",
                )
            }
        }

    }

}

fun startRecording() {
    val speechRecognizerIntent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
//    speechRecognizerIntent.putExtra(
//        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
//    )
    speechRecognizerIntent.putExtra(
        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        Locale("yo_NG")
    )
    speechRecognizerIntent.putExtra(
        RecognizerIntent.EXTRA_LANGUAGE,
        Locale("yo_NG")
    )
    speechRecognizerIntent.putExtra(
        RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
        "yo_NG"
    )
    speechRecognizerIntent.putExtra(
        RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE,
        "yo_NG"
    )

    speechRecognizer.startListening(speechRecognizerIntent)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WordBankTheme {
        SpeechToTextScreen(null, "", false) { }
    }
}