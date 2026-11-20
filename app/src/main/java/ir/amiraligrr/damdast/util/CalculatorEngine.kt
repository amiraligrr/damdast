package ir.amiraligrr.damdast.util

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class CalculatorException(message: String) : IllegalArgumentException(message)

object CalculatorEngine {
    private val functions = setOf(
        "sin", "cos", "tan", "asin", "acos", "atan", "log", "ln", "sqrt", "abs", "exp",
    )

    fun evaluate(expression: String, degrees: Boolean = false): Double {
        val tokens = tokenize(expression)
        if (tokens.isEmpty()) throw CalculatorException("Empty expression")
        val rpn = toRpn(tokens)
        val result = evaluateRpn(rpn, degrees)
        if (result.isNaN() || result.isInfinite()) {
            throw CalculatorException("Result is not a finite number")
        }
        return result
    }

    private fun tokenize(raw: String): List<String> {
        val input = raw.normalizeDigits()
            .replace('×', '*')
            .replace('÷', '/')
            .replace('−', '-')
            .replace('π', 'π')
        val tokens = mutableListOf<String>()
        var index = 0
        while (index < input.length) {
            val char = input[index]
            when {
                char.isWhitespace() -> index++

                char.isDigit() || char == '.' -> {
                    val start = index
                    var dots = 0
                    while (index < input.length && (input[index].isDigit() || input[index] == '.')) {
                        if (input[index] == '.' && ++dots > 1) {
                            throw CalculatorException("Invalid number")
                        }
                        index++
                    }
                    val number = input.substring(start, index)
                    if (number == ".") throw CalculatorException("Invalid number")
                    addWithImplicitMultiply(tokens, number)
                }

                char.isLetter() -> {
                    val start = index
                    while (index < input.length && input[index].isLetter()) index++
                    val word = input.substring(start, index).lowercase()
                    when {
                        word == "pi" || word == "π" -> addWithImplicitMultiply(tokens, "3.141592653589793")
                        word == "e" -> addWithImplicitMultiply(tokens, "2.718281828459045")
                        word in functions -> {
                            if (tokens.lastOrNull()?.let { isValueEnd(it) } == true) {
                                tokens.add("*")
                            }
                            tokens.add(word)
                        }

                        else -> throw CalculatorException("Unknown name: $word")
                    }
                }

                char == '(' -> {
                    if (tokens.lastOrNull()?.let { isValueEnd(it) } == true) {
                        tokens.add("*")
                    }
                    tokens.add("(")
                    index++
                }

                char == ')' -> {
                    tokens.add(")")
                    index++
                }

                char in "+-*/^" -> {
                    val unary = char == '-' && (
                        tokens.isEmpty() ||
                            tokens.last() in setOf("+", "-", "*", "/", "^", "(", "u-") ||
                            tokens.last() in functions
                        )
                    tokens.add(if (unary) "u-" else char.toString())
                    index++
                }

                char == '%' || char == '!' -> {
                    if (tokens.isEmpty() || !isValueEnd(tokens.last())) {
                        throw CalculatorException("Unexpected $char")
                    }
                    tokens.add(char.toString())
                    index++
                }

                else -> throw CalculatorException("Unexpected character: $char")
            }
        }
        return tokens
    }

    private fun addWithImplicitMultiply(tokens: MutableList<String>, token: String) {
        if (tokens.lastOrNull()?.let { isValueEnd(it) } == true) {
            tokens.add("*")
        }
        tokens.add(token)
    }

    private fun isValueEnd(token: String): Boolean =
        token == ")" || token == "%" || token == "!" ||
            token.toDoubleOrNull() != null

    private fun toRpn(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = ArrayDeque<String>()
        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> output.add(token)
                token in functions -> stack.addLast(token)
                token == "u-" -> stack.addLast(token)

                token in setOf("+", "-", "*", "/", "^") -> {
                    while (stack.isNotEmpty() && stack.last() != "(" &&
                        (precedence(stack.last()) > precedence(token) ||
                            (precedence(stack.last()) == precedence(token) && token != "^"))
                    ) {
                        output.add(stack.removeLast())
                    }
                    stack.addLast(token)
                }

                token == "%" || token == "!" -> output.add(token)
                token == "(" -> stack.addLast(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.last() != "(") {
                        output.add(stack.removeLast())
                    }
                    if (stack.isEmpty()) throw CalculatorException("Mismatched parentheses")
                    stack.removeLast()
                    if (stack.lastOrNull() in functions) output.add(stack.removeLast())
                }

                else -> throw CalculatorException("Invalid token")
            }
        }
        while (stack.isNotEmpty()) {
            val token = stack.removeLast()
            if (token == "(") throw CalculatorException("Mismatched parentheses")
            output.add(token)
        }
        return output
    }

    private fun precedence(token: String): Int = when (token) {
        "+", "-" -> 1
        "*", "/" -> 2
        "u-" -> 3
        "^" -> 4
        in functions -> 5
        else -> 0
    }

    private fun evaluateRpn(tokens: List<String>, degrees: Boolean): Double {
        val stack = ArrayDeque<Double>()
        fun pop(): Double = stack.removeLastOrNull() ?: throw CalculatorException("Invalid expression")
        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> stack.addLast(token.toDouble())
                token == "u-" -> stack.addLast(-pop())
                token == "%" -> stack.addLast(pop() / 100.0)
                token == "!" -> {
                    val value = pop()
                    if (value < 0 || value > 170 || value != kotlin.math.floor(value)) {
                        throw CalculatorException("Factorial needs a whole number from 0 to 170")
                    }
                    var result = 1.0
                    for (number in 2..value.toInt()) result *= number
                    stack.addLast(result)
                }

                token == "+" -> {
                    val b = pop()
                    val a = pop()
                    stack.addLast(a + b)
                }

                token == "-" -> {
                    val b = pop()
                    val a = pop()
                    stack.addLast(a - b)
                }

                token == "*" -> {
                    val b = pop()
                    val a = pop()
                    stack.addLast(a * b)
                }

                token == "/" -> {
                    val b = pop()
                    val a = pop()
                    if (b == 0.0) throw CalculatorException("Division by zero")
                    stack.addLast(a / b)
                }

                token == "^" -> {
                    val b = pop()
                    val a = pop()
                    stack.addLast(a.pow(b))
                }

                token in functions -> {
                    val value = pop()
                    val radians = if (degrees) Math.toRadians(value) else value
                    val result = when (token) {
                        "sin" -> sin(radians)
                        "cos" -> cos(radians)
                        "tan" -> {
                            val cosine = cos(radians)
                            if (abs(cosine) < 1e-12) {
                                throw CalculatorException("Undefined tangent")
                            }
                            tan(radians)
                        }

                        "asin" -> Math.toDegrees(asin(value)).takeIf { degrees } ?: asin(value)
                        "acos" -> Math.toDegrees(acos(value)).takeIf { degrees } ?: acos(value)
                        "atan" -> Math.toDegrees(atan(value)).takeIf { degrees } ?: atan(value)
                        "log" -> {
                            if (value <= 0) throw CalculatorException("Logarithm needs a positive value")
                            log10(value)
                        }

                        "ln" -> {
                            if (value <= 0) throw CalculatorException("Logarithm needs a positive value")
                            ln(value)
                        }

                        "sqrt" -> {
                            if (value < 0) throw CalculatorException("Square root needs a non-negative value")
                            sqrt(value)
                        }

                        "abs" -> abs(value)
                        "exp" -> exp(value)
                        else -> throw CalculatorException("Unknown function")
                    }
                    stack.addLast(result)
                }

                else -> throw CalculatorException("Unknown token")
            }
        }
        if (stack.size != 1) throw CalculatorException("Invalid expression")
        return stack.removeLast()
    }

    fun formatResult(value: Double): String {
        if (!value.isFinite()) return "Error"
        val rounded = if (abs(value) >= 1e15 || (abs(value) > 0 && abs(value) < 1e-10)) {
            return value.toString()
        } else {
            kotlin.math.round(value * 1e10) / 1e10
        }
        return if (rounded == kotlin.math.floor(rounded) && abs(rounded) < 1e15) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
        }
    }
}
