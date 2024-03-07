package com.example.se2einzelphase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.se2einzelphase.ui.theme.SE2EinzelphaseTheme
import java.net.Socket
import android.util.Log
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SE2EinzelphaseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val outputState = remember { mutableStateOf("") }
                    val textState = remember { mutableStateOf("") }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Header()
                        NumberTextField(textState)
                        TwoButtons(textState, outputState)
                        OutputField(outputState.value, outputState.value.isNotEmpty())

                    }
                }
            }
        }
    }
}



@Composable
fun NumberTextField(textState: MutableState<String>) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = textState.value,
            onValueChange = { newValue ->
                if (newValue.length <= 8) {
                    textState.value = newValue.filter { it.isDigit() }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}


@Composable
fun OutputField(text: String, isVisible: Boolean) {
    if (isVisible) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 2.dp,
                color = Color.Black,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            color = Color.Black
        )
    }
    }
}



// Aufgabe 12210093 % 7 = 0
// 0 - Ziffern der Größe nach sortieren, Primzahlen werden gestrichen
// input is String because the InputField provides a String, converting that back and forward would be unnecessary
fun processLocal(numberStr: String): String {
    if (numberStr.length <= 7) {
        return "This is not a valid Matrikelnummer"
    }
    // making a isPrime() for digits from 1-9 would be unnecessary, as we can just use a set
    val primeDigits = setOf('2', '3', '5', '7')

    val result = numberStr.filterNot { it in primeDigits }
        .map { it.toString().toInt() }
        .sortedDescending()
        .joinToString(separator = "")

    return "Removed Primes and Sorted Descending: $result"
}

// lot of debugging to get it fixed, forgot to add the newline to the matrikelnummer
suspend fun processRemote(matrikelnummer: String): String = withContext(Dispatchers.IO) {
    try {
        Log.d("ServerConnection", "Attempting to connect to server")
        Socket("se2-submission.aau.at", 20080).use { socket ->
            Log.d("ServerConnection", "Connection to server succeeded")

            val outputStream = socket.getOutputStream()
            val inputStream = socket.getInputStream()

            Log.d("ServerRequest", "Sending matrikelnummer: $matrikelnummer")
            val matrikelnummerWithNewline = "$matrikelnummer\n"
            outputStream.write(matrikelnummerWithNewline.toByteArray(Charsets.UTF_8))
            outputStream.flush()
            Log.d("ServerRequest", "Matrikelnummer sent")

            Log.d("ServerResponse", "Reading server response")
            val responseBytes = inputStream.readBytes()
            if (responseBytes.isEmpty()) {
                Log.d("ServerResponse", "Server response was empty")
            }
            Log.d("ServerResponse", "Read ${responseBytes.size} bytes from server")
            val response = String(responseBytes, Charsets.UTF_8)
            Log.d("ServerResponse", "Received response: $response")

            response
        }
    } catch (e: Exception) {
        Log.e("ServerConnectionError", "Error connecting to server: ${e.message}", e)
        "Error connecting to server: ${e.message}"
    }
}



@Composable
fun StyledButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick, // onClick now just triggers actions and doesn't need to return a String
        modifier = Modifier
            .padding(8.dp)
            .size(160.dp, 48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            // contentColor = Color.Blue
        )
    ) {
        Text(text)
    }
}


@Composable
fun TwoButtons(textState: MutableState<String>, outputState: MutableState<String>) {
    val coroutineScope = rememberCoroutineScope()

    MaterialTheme {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.Center) {
                StyledButton("Process Remote") {
                    coroutineScope.launch {
                        val response = processRemote(textState.value)
                        outputState.value = response
                    }
                }
                StyledButton("Process Local") {
                    outputState.value = processLocal(textState.value)
                }
            }
        }
    }
}

@Composable
fun Header() {
    Text(
        text = "Enter your Matrikelnummer",
    )
}
