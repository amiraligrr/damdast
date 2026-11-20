package ir.amiraligrr.damdast.util

import java.security.SecureRandom
import kotlin.math.log2

data class PasswordResult(
    val value: String,
    val entropyBits: Double,
    val poolSize: Int,
)

object SecurityUtils {
    private val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val lower = "abcdefghijklmnopqrstuvwxyz"
    private val digits = "0123456789"
    private val symbols = "!@#$%^&*()-_=+[]{};:,.?/|~"
    private val ambiguous = "Il1O0o"

    fun generatePassword(
        length: Int,
        useUpper: Boolean,
        useLower: Boolean,
        useDigits: Boolean,
        useSymbols: Boolean,
        excludeAmbiguous: Boolean,
    ): PasswordResult {
        require(length in 4..128) { "Password length must be between 4 and 128" }
        val groups = buildList {
            if (useUpper) add(upper)
            if (useLower) add(lower)
            if (useDigits) add(digits)
            if (useSymbols) add(symbols)
        }
        require(groups.isNotEmpty()) { "Choose at least one character group" }
        val filteredGroups = groups.map { group ->
            if (excludeAmbiguous) group.filterNot { it in ambiguous } else group
        }.filter { it.isNotEmpty() }
        require(filteredGroups.isNotEmpty()) { "No characters remain after filtering" }
        require(length >= filteredGroups.size) { "Length is too short for the selected groups" }
        val random = SecureRandom()
        val output = MutableList(length) { ' ' }
        filteredGroups.forEachIndexed { index, group ->
            output[index] = group[random.nextInt(group.length)]
        }
        val all = filteredGroups.joinToString("")
        for (index in filteredGroups.size until length) {
            output[index] = all[random.nextInt(all.length)]
        }
        // Fisher-Yates with SecureRandom.
        for (index in output.lastIndex downTo 1) {
            val swap = random.nextInt(index + 1)
            val temp = output[index]
            output[index] = output[swap]
            output[swap] = temp
        }
        val entropy = length * log2(all.length.toDouble())
        return PasswordResult(output.joinToString(""), entropy, all.length)
    }

    fun randomInts(min: Int, max: Int, count: Int, unique: Boolean): List<Int> {
        require(min <= max) { "Minimum must not exceed maximum" }
        require(count in 1..1000) { "Count must be between 1 and 1000" }
        val random = SecureRandom()
        val rangeSize = max.toLong() - min.toLong() + 1
        if (unique) {
            require(count <= rangeSize) { "Not enough unique values in this range" }
            val values = mutableSetOf<Int>()
            while (values.size < count) {
                values.add((min.toLong() + random.nextBounded(rangeSize)).toInt())
            }
            return values.toList()
        }
        return List(count) { (min.toLong() + random.nextBounded(rangeSize)).toInt() }
    }

    fun rollDice(sides: Int, count: Int): List<Int> {
        require(sides in 2..100)
        require(count in 1..12)
        val random = SecureRandom()
        return List(count) { random.nextInt(sides) + 1 }
    }

    private fun SecureRandom.nextBounded(bound: Long): Long {
        require(bound > 0)
        var bits: Long
        var value: Long
        do {
            bits = nextLong() ushr 1
            value = bits % bound
        } while (bits - value + (bound - 1) < 0L)
        return value
    }

    fun pick(random: SecureRandom = SecureRandom(), items: List<String>, count: Int, unique: Boolean): List<String> {
        require(items.isNotEmpty()) { "Add at least one item" }
        require(count in 1..100)
        if (unique) {
            require(count <= items.size) { "Not enough unique items" }
            val copy = items.toMutableList()
            for (index in copy.lastIndex downTo 1) {
                val swap = random.nextInt(index + 1)
                val temp = copy[index]
                copy[index] = copy[swap]
                copy[swap] = temp
            }
            return copy.take(count)
        }
        return List(count) { items[random.nextInt(items.size)] }
    }
}

object CsvUtils {
    fun detectDelimiter(text: String): Char {
        val firstLine = text.lineSequence().firstOrNull().orEmpty()
        val candidates = listOf(',', ';', '\t', '|')
        return candidates.maxByOrNull { delimiter -> firstLine.count { it == delimiter } }
            ?.takeIf { firstLine.count { char -> char == it } > 0 }
            ?: ','
    }

    fun parse(text: String, delimiter: Char = detectDelimiter(text), maxRows: Int = 1000): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val cell = StringBuilder()
        var inQuotes = false
        var index = 0
        while (index < text.length && rows.size < maxRows) {
            val char = text[index]
            when {
                inQuotes && char == '"' && text.getOrNull(index + 1) == '"' -> {
                    cell.append('"')
                    index += 2
                    continue
                }

                char == '"' -> inQuotes = !inQuotes
                !inQuotes && char == delimiter -> {
                    row.add(cell.toString())
                    cell.clear()
                }

                !inQuotes && char == '\n' -> {
                    row.add(cell.toString().trimEnd('\r'))
                    cell.clear()
                    if (row.any { it.isNotEmpty() } || rows.isEmpty()) rows.add(row)
                    row = mutableListOf()
                }

                else -> cell.append(char)
            }
            index++
        }
        if (cell.isNotEmpty() || row.isNotEmpty()) {
            row.add(cell.toString().trimEnd('\r'))
            if (row.any { it.isNotEmpty() }) rows.add(row)
        }
        return rows
    }
}
