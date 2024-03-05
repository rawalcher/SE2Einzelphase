package com.example.se2einzelphase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.se2einzelphase.ui.theme.SE2EinzelphaseTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SE2EinzelphaseTheme{
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Green
                ) {
                    val outputState = remember { mutableStateOf("") }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutputField(outputState.value)
                        Header()
                        NumberTextField()
                        TwoButtons(outputState)
                    }
                }
            }
        }
    }
}

@Composable
fun NumberTextField() {
    val textState = remember { mutableStateOf("") }

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
    Box(
        modifier = Modifier.padding(8.dp)
    ) {
        Text(text)
    }
}

// Aufgabe 12210093 % 7 = 0
// 0 - Ziffern der Größe nach sortieren, Primzahlen werden gestrichen
fun processLocal(number: String): String {
    // array with primes because a method to calculate would be overkill

    // max heap to sort it descending

    return "Locally Processed: $number"
}

fun processServer(number: String): String {
    return "Dingdong! Server says: $number"
}

@Composable
fun StyledButton(text: String, onClick: () -> String, outputState: MutableState<String>) {
    Button(
        onClick = { outputState.value = onClick() },
        modifier = Modifier.padding(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = androidx.compose.ui.graphics.Color.Yellow,
            contentColor = androidx.compose.ui.graphics.Color.Blue)) {
        Text(text)
    }
}

@Composable
fun TwoButtons(outputState: MutableState<String>) {
    MaterialTheme {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.Center) {
                StyledButton("Process Remote", { processServer("1234") }, outputState)
                StyledButton("Process Local", { processLocal("5678") }, outputState)
            }
        }
    }
}

@Composable
fun Header() {
    Text(
        text = "Enter your Matrikelnummer",
    )
    Text(
        text = "Must be no more than 8 digits",
    )
}
