package com.example.calculadora.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculadora.CalculatorEngine

@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier
) {
    var expression by remember { mutableStateOf("") }
    var resultPreview by remember { mutableStateOf("") }
    var isEvaluated by remember { mutableStateOf(false) }
    var isDegreeMode by remember { mutableStateOf(true) }
    var isFractionDisplay by remember { mutableStateOf(false) }

    val engine = remember(isDegreeMode) { CalculatorEngine(isDegreeMode) }

    fun updatePreview(expr: String) {
        if (expr.isNotEmpty()) {
            val eval = engine.evaluate(expr)
            if (eval != "Error" && eval != expr) {
                val displayResult = if (isFractionDisplay) {
                    CalculatorEngine.doubleToFraction(eval)
                } else {
                    eval
                }
                resultPreview = "= $displayResult"
            } else {
                resultPreview = ""
            }
        } else {
            resultPreview = ""
        }
    }

    LaunchedEffect(expression, isDegreeMode, isFractionDisplay) {
        if (!isEvaluated) {
            updatePreview(expression)
        }
    }

    fun onButtonClick(symbol: String) {
        when (symbol) {
            "C" -> {
                expression = ""
                resultPreview = ""
                isEvaluated = false
                isFractionDisplay = false
            }
            "⌫" -> {
                if (isEvaluated) {
                    expression = ""
                    resultPreview = ""
                    isEvaluated = false
                    isFractionDisplay = false
                } else if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                }
            }
            "=" -> {
                if (expression.isNotEmpty()) {
                    val finalResult = engine.evaluate(expression)
                    if (finalResult != "Error") {
                        val displayResult = if (isFractionDisplay) {
                            CalculatorEngine.doubleToFraction(finalResult)
                        } else {
                            finalResult
                        }
                        expression = displayResult
                        resultPreview = ""
                        isEvaluated = true
                    } else {
                        resultPreview = "Error"
                    }
                }
            }
            "DEG", "RAD" -> {
                isDegreeMode = !isDegreeMode
            }
            "a/b" -> {
                if (isEvaluated || (expression.isNotEmpty() && resultPreview.isNotEmpty())) {
                    isFractionDisplay = !isFractionDisplay
                    if (isEvaluated) {
                        val eval = engine.evaluate(expression)
                        if (eval != "Error") {
                            expression = if (isFractionDisplay) CalculatorEngine.doubleToFraction(eval) else eval
                        }
                    }
                } else {
                    expression += "/"
                }
            }
            "sin", "cos", "tan" -> {
                if (isEvaluated) {
                    expression = "$symbol("
                    resultPreview = ""
                    isEvaluated = false
                } else {
                    expression += "$symbol("
                }
            }
            "±" -> {
                expression = CalculatorEngine.toggleSign(expression)
                isEvaluated = false
            }
            "+", "-", "×", "÷", "^", "%" -> {
                if (isEvaluated) {
                    isEvaluated = false
                }
                if (expression.isEmpty() && symbol == "-") {
                    expression = "-"
                } else if (expression.isNotEmpty()) {
                    val lastChar = expression.last()
                    if (lastChar in listOf('+', '-', '×', '÷', '^', '%', '/')) {
                        expression = expression.dropLast(1) + symbol
                    } else {
                        expression += symbol
                    }
                }
            }
            else -> {
                if (isEvaluated) {
                    expression = symbol
                    resultPreview = ""
                    isEvaluated = false
                } else {
                    expression += symbol
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Mode & Display Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            // Mode Indicator Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { isDegreeMode = !isDegreeMode },
                    label = { Text(if (isDegreeMode) "DEG" else "RAD", fontWeight = FontWeight.Bold) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )

                if (isFractionDisplay) {
                    AssistChip(
                        onClick = { isFractionDisplay = false },
                        label = { Text("Fracción a/b", fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (expression.isEmpty()) "0" else expression,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = if (expression.length > 12) 32.sp else 44.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                ),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = resultPreview,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,
                    textAlign = TextAlign.End
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Keypad Grid (5 columns x 6 rows)
        val buttons = listOf(
            listOf(if (isDegreeMode) "DEG" else "RAD", "sin", "cos", "tan", "a/b"),
            listOf("C", "(", ")", "^", "⌫"),
            listOf("√", "7", "8", "9", "÷"),
            listOf("%", "4", "5", "6", "×"),
            listOf("±", "1", "2", "3", "-"),
            listOf("0", ".", "=", "+")
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { symbol ->
                        val weight = if (symbol == "=") 2f else 1f
                        CalculatorButton(
                            symbol = symbol,
                            modifier = Modifier
                                .weight(weight)
                                .aspectRatio(if (symbol == "=") 2.1f else 1f),
                            onClick = { onButtonClick(symbol) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculatorButton(
    symbol: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = when (symbol) {
        "=" -> MaterialTheme.colorScheme.primary
        "C", "⌫" -> MaterialTheme.colorScheme.errorContainer
        "+", "-", "×", "÷" -> MaterialTheme.colorScheme.primaryContainer
        "DEG", "RAD", "sin", "cos", "tan", "a/b", "√", "^", "%", "(", ")" -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when (symbol) {
        "=" -> MaterialTheme.colorScheme.onPrimary
        "C", "⌫" -> MaterialTheme.colorScheme.onErrorContainer
        "+", "-", "×", "÷" -> MaterialTheme.colorScheme.onPrimaryContainer
        "DEG", "RAD", "sin", "cos", "tan", "a/b", "√", "^", "%", "(", ")" -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        color = containerColor,
        onClick = onClick
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = symbol,
                fontSize = if (symbol.length > 3) 14.sp else if (symbol.length > 2) 16.sp else 20.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
