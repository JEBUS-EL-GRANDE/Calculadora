package com.example.calculadora

class CalculatorEngine(var isDegreeMode: Boolean = true) {

    fun evaluate(expression: String): String {
        val cleanExpr = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace(" ", "")

        if (cleanExpr.isEmpty()) return ""

        return try {
            val parser = Parser(cleanExpr, isDegreeMode)
            val result = parser.parse()
            if (result.isNaN() || result.isInfinite()) {
                "Error"
            } else {
                formatResult(result)
            }
        } catch (_: Exception) {
            "Error"
        }
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"

        val longVal = Math.round(value)
        if (Math.abs(value - longVal) < 1e-10 && Math.abs(value) < 1e15) {
            return longVal.toString()
        }

        val formatted = String.format(java.util.Locale.US, "%.8f", value)
            .trimEnd('0')
            .trimEnd('.')

        return if (formatted == "-0") "0" else formatted
    }

    companion object {
        fun doubleToFraction(valueStr: String): String {
            val doubleVal = valueStr.toDoubleOrNull() ?: return valueStr
            if (doubleVal.isNaN() || doubleVal.isInfinite()) return valueStr
            val isNegative = doubleVal < 0
            val absVal = Math.abs(doubleVal)

            val intPart = absVal.toLong()
            val fracPart = absVal - intPart

            if (fracPart < 1e-9) {
                return doubleVal.toLong().toString()
            }

            var h1 = 1L
            var h2 = 0L
            var k1 = 0L
            var k2 = 1L
            var b = fracPart

            do {
                val a = b.toLong()
                var aux = h1
                h1 = a * h1 + h2
                h2 = aux
                aux = k1
                k1 = a * k1 + k2
                k2 = aux
                if (Math.abs(b - a) < 1e-6) break
                b = 1.0 / (b - a)
            } while (Math.abs(fracPart - h1.toDouble() / k1.toDouble()) > 1e-6 && k1 < 10000)

            val num = intPart * k1 + h1
            val den = k1

            val g = gcd(num, den)
            val finalNum = (if (isNegative) -num else num) / g
            val finalDen = den / g

            return if (finalDen == 1L) {
                finalNum.toString()
            } else {
                "$finalNum/$finalDen"
            }
        }

        private fun gcd(a: Long, b: Long): Long {
            var x = Math.abs(a)
            var y = Math.abs(b)
            while (y != 0L) {
                val t = y
                y = x % y
                x = t
            }
            return x
        }

        fun toggleSign(expr: String): String {
            if (expr.isEmpty()) return "-"
            if (expr.endsWith("(-")) return expr.dropLast(2)
            if (expr.endsWith("-")) return expr.dropLast(1)

            val numberRegex = Regex("""(-\d+\.?\d*|\d+\.?\d*)$""")
            val match = numberRegex.find(expr)
            return if (match != null) {
                val numStr = match.value
                val startIndex = match.range.first
                val prefix = expr.substring(0, startIndex)
                if (numStr.startsWith("-")) {
                    prefix + numStr.substring(1)
                } else {
                    prefix + "-$numStr"
                }
            } else {
                val lastChar = expr.last()
                if (lastChar in listOf('+', '-', '×', '÷', '^', '(', '√', '/')) {
                    "$expr-"
                } else {
                    "$expr-"
                }
            }
        }
    }
}

private class Parser(private val str: String, private val isDegreeMode: Boolean) {
    private var pos = -1
    private var ch = -1

    private fun nextChar() {
        ch = if (++pos < str.length) str[pos].code else -1
    }

    private fun eat(charToEat: Int): Boolean {
        while (ch == ' '.code) nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    private fun eatString(prefix: String): Boolean {
        while (ch == ' '.code) nextChar()
        if (pos + prefix.length <= str.length && str.substring(pos, pos + prefix.length) == prefix) {
            pos += prefix.length
            ch = if (pos < str.length) str[pos].code else -1
            return true
        }
        return false
    }

    fun parse(): Double {
        nextChar()
        val x = parseExpression()
        if (pos < str.length) throw RuntimeException("Character no esperado: " + ch.toChar())
        return x
    }

    private fun parseExpression(): Double {
        var x = parseTerm()
        while (true) {
            when {
                eat('+'.code) -> x += parseTerm()
                eat('-'.code) -> x -= parseTerm()
                else -> return x
            }
        }
    }

    private fun parseTerm(): Double {
        var x = parseFactor()
        while (true) {
            when {
                eat('*'.code) -> x *= parseFactor()
                eat('/'.code) -> {
                    val divisor = parseFactor()
                    if (divisor == 0.0) throw ArithmeticException("División por cero")
                    x /= divisor
                }
                canStartFactor() -> {
                    x *= parseFactor()
                }
                else -> return x
            }
        }
    }

    private fun canStartFactor(): Boolean {
        while (ch == ' '.code) nextChar()
        if (ch == -1) return false
        val char = ch.toChar()
        if (char == '(' || char == '√' || char in '0'..'9' || char == '.') return true
        if (pos + 3 <= str.length) {
            val sub = str.substring(pos, pos + 3)
            if (sub == "sin" || sub == "cos" || sub == "tan") return true
        }
        return false
    }

    private fun parseFactor(): Double {
        var x = parsePostfix()
        if (eat('^'.code)) {
            val exponent = parseFactor()
            x = Math.pow(x, exponent)
        }
        return x
    }

    private fun parsePostfix(): Double {
        var x = parseUnary()
        while (eat('%'.code)) {
            x /= 100.0
        }
        return x
    }

    private fun parseUnary(): Double {
        if (eat('+'.code)) return parseUnary()
        if (eat('-'.code)) return -parseUnary()
        if (eat('√'.code)) {
            val v = parseUnary()
            if (v < 0) throw IllegalArgumentException("Raíz de número negativo")
            return Math.sqrt(v)
        }
        if (eatString("sin")) {
            val v = parseUnary()
            val rad = if (isDegreeMode) Math.toRadians(v) else v
            val res = Math.sin(rad)
            return if (isDegreeMode && Math.abs(v % 180) < 1e-9) 0.0 else res
        }
        if (eatString("cos")) {
            val v = parseUnary()
            val rad = if (isDegreeMode) Math.toRadians(v) else v
            val res = Math.cos(rad)
            return if (isDegreeMode && Math.abs((v - 90) % 180) < 1e-9) 0.0 else res
        }
        if (eatString("tan")) {
            val v = parseUnary()
            if (isDegreeMode && Math.abs((v - 90) % 180) < 1e-9) {
                throw ArithmeticException("Tan indefinida")
            }
            val rad = if (isDegreeMode) Math.toRadians(v) else v
            return Math.tan(rad)
        }
        return parsePrimary()
    }

    private fun parsePrimary(): Double {
        val startPos = pos
        if (eat('('.code)) {
            val x = parseExpression()
            if (!eat(')'.code)) throw RuntimeException("Falta paréntesis de cierre")
            return x
        }

        if (ch in '0'.code..'9'.code || ch == '.'.code) {
            while (ch in '0'.code..'9'.code || ch == '.'.code) {
                nextChar()
            }
            return str.substring(startPos, pos).toDouble()
        }

        throw RuntimeException("Inesperado: " + if (ch != -1) ch.toChar().toString() else "fin de entrada")
    }
}
