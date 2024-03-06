package com.example.se2einzelphase

import android.app.Activity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.se2einzelphase.ui.theme.SE2EinzelphaseTheme
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import android.util.Log


interface ServerResponseCallback {
    fun onResponse(response: String)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SE2EinzelphaseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Green
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
                        OutputField(outputState.value)
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
fun OutputField(text: String) {
    Surface(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(2.dp, Color.Black, RoundedCornerShape(8.dp)),
        color = Color.LightGray,
        shadowElevation = 4.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            color = Color.Black
        )
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

    return "Locally Processed: $result"
}

fun processRemote(matrikelnummer: String, callback: ServerResponseCallback) {
    Thread {
        if (matrikelnummer.length != 8) {
            callback.onResponse("This is not a valid Matrikelnummer")
            return@Thread
        }

        try {
            Socket("se2-submission.aau.at", 20080).use { socket ->
                DataOutputStream(socket.getOutputStream()).use { dos ->
                    DataInputStream(socket.getInputStream()).use { dis ->

                        dos.write(matrikelnummer.toByteArray())
                        dos.flush()
                        Log.d("NetworkCall", "Sending Matrikelnummer: $matrikelnummer")

                        // Read the response from the server
                        val responseBytes = ByteArray(1024)
                        val bytesRead = dis.read(responseBytes)

                        // Convert the response bytes to a String
                        val response = String(responseBytes, 0, bytesRead)
                        Log.d("Answer", response)
                        callback.onResponse(response)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onResponse("Failed to communicate with the server: ${e.message}")
        }
    }.start()
}


@Composable
fun StyledButton(text: String, onClick: () -> Unit, outputState: MutableState<String>) {
    Button(
        onClick = onClick, // onClick now just triggers actions and doesn't need to return a String
        modifier = Modifier
            .padding(8.dp)
            .size(160.dp, 48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Yellow,
            contentColor = Color.Blue
        )
    ) {
        Text(text)
    }
}


@Composable
fun TwoButtons(textState: MutableState<String>, outputState: MutableState<String>) {
    val context = LocalContext.current

    MaterialTheme {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.Center) {
                StyledButton("Process Remote", {
                    processRemote(textState.value, object : ServerResponseCallback {
                        override fun onResponse(response: String) {
                            (context as Activity).runOnUiThread {
                                outputState.value = response
                            }
                        }
                    })
                }, outputState)
                StyledButton("Process Local", {
                    outputState.value = processLocal(textState.value)
                }, outputState)
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
