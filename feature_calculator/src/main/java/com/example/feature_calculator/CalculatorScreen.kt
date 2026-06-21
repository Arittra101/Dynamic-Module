package com.example.feature_calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class Op { NONE, ADD, SUB, MUL, DIV }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(onBack: () -> Unit) {
    var display by remember { mutableStateOf("0") }
    var accumulator by remember { mutableStateOf(0.0) }
    var pendingOp by remember { mutableStateOf(Op.NONE) }
    var freshEntry by remember { mutableStateOf(true) }

    fun appendDigit(d: String) {
        if (freshEntry) {
            display = if (d == ".") "0." else d
            freshEntry = false
        } else {
            if (d == "." && display.contains(".")) return
            display = if (display == "0" && d != ".") d else display + d
        }
    }

    fun applyOp(op: Op) {
        val current = display.toDoubleOrNull() ?: 0.0
        if (pendingOp != Op.NONE) {
            accumulator = when (pendingOp) {
                Op.ADD -> accumulator + current
                Op.SUB -> accumulator - current
                Op.MUL -> accumulator * current
                Op.DIV -> if (current != 0.0) accumulator / current else Double.NaN
                Op.NONE -> current
            }
        } else {
            accumulator = current
        }
        pendingOp = op
        display = formatResult(accumulator)
        freshEntry = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculator") },
                navigationIcon = {
                    Button(onClick = onBack, colors = ButtonDefaults.textButtonColors()) {
                        Text("← Back")
                    }
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = display,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.displaySmall,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                )
            }

            Spacer(Modifier.height(8.dp))

            val rows = listOf(
                listOf("7", "8", "9", "÷"),
                listOf("4", "5", "6", "×"),
                listOf("1", "2", "3", "−"),
                listOf("0", ".", "=", "+"),
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { label ->
                        val isOp = label in listOf("+", "−", "×", "÷", "=")
                        if (isOp) {
                            Button(
                                onClick = {
                                    when (label) {
                                        "+" -> applyOp(Op.ADD)
                                        "−" -> applyOp(Op.SUB)
                                        "×" -> applyOp(Op.MUL)
                                        "÷" -> applyOp(Op.DIV)
                                        "=" -> applyOp(Op.NONE).also { pendingOp = Op.NONE }
                                    }
                                },
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                ),
                            ) { Text(label, fontSize = 20.sp) }
                        } else {
                            OutlinedButton(
                                onClick = { appendDigit(label) },
                                modifier = Modifier.weight(1f).height(56.dp),
                            ) { Text(label, fontSize = 20.sp) }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = {
                        display = "0"
                        accumulator = 0.0
                        pendingOp = Op.NONE
                        freshEntry = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) { Text("C — Clear") }
            }
        }
    }
}

private fun formatResult(value: Double): String {
    if (value.isNaN()) return "Error"
    return if (value == kotlin.math.floor(value) && !value.isInfinite())
        value.toLong().toString()
    else
        "%.6g".format(value).trimEnd('0').trimEnd('.')
}
