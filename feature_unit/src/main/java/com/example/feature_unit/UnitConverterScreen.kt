package com.example.feature_unit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unit Converter") },
                navigationIcon = {
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.textButtonColors(),
                    ) { Text("← Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            ConverterSection(
                title = "Length",
                labelA = "Metres (m)",
                labelB = "Feet (ft)",
                convert = { m -> m * 3.28084 },
                convertBack = { ft -> ft / 3.28084 },
            )
            ConverterSection(
                title = "Weight",
                labelA = "Kilograms (kg)",
                labelB = "Pounds (lb)",
                convert = { kg -> kg * 2.20462 },
                convertBack = { lb -> lb / 2.20462 },
            )
            TemperatureSection()
        }
    }
}

@Composable
private fun ConverterSection(
    title: String,
    labelA: String,
    labelB: String,
    convert: (Double) -> Double,
    convertBack: (Double) -> Double,
) {
    var inputA by remember { mutableStateOf("") }
    var inputB by remember { mutableStateOf("") }

    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = inputA,
                onValueChange = { v ->
                    inputA = v
                    inputB = v.toDoubleOrNull()?.let { "%.4f".format(convert(it)) } ?: ""
                },
                label = { Text(labelA) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            Text("=", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = inputB,
                onValueChange = { v ->
                    inputB = v
                    inputA = v.toDoubleOrNull()?.let { "%.4f".format(convertBack(it)) } ?: ""
                },
                label = { Text(labelB) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
        }
    }
}

@Composable
private fun TemperatureSection() {
    var celsius by remember { mutableStateOf("") }
    var fahrenheit by remember { mutableStateOf("") }

    Column {
        Text("Temperature", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = celsius,
                onValueChange = { v ->
                    celsius = v
                    fahrenheit = v.toDoubleOrNull()?.let { c -> "%.2f".format(c * 9.0 / 5.0 + 32.0) } ?: ""
                },
                label = { Text("Celsius (°C)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            Text("=", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = fahrenheit,
                onValueChange = { v ->
                    fahrenheit = v
                    celsius = v.toDoubleOrNull()?.let { f -> "%.2f".format((f - 32.0) * 5.0 / 9.0) } ?: ""
                },
                label = { Text("Fahrenheit (°F)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
        }
    }
}
