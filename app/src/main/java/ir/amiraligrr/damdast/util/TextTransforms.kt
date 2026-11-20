package ir.amiraligrr.damdast.util

import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.random.Random

data class TextStats(
    val characters: Int,
    val charactersNoSpaces: Int,
    val words: Int,
    val lines: Int,
    val paragraphs: Int,
    val sentences: Int,
    val readingMinutes: Int,
)

object TextTransforms {
    private val whitespace = Regex("\\s+")
    private val sentenceEnd = Regex("[.!?؟।]+")
    private val numberRegex = Regex("""[-+]?\d+(?:[.,]\d+)?""")
    private val urlRegex = Regex("""(?i)\b(?:https?://|www\.)[^\s<>"']+""")
    private val emailRegex = Regex("""(?i)\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}\b""")

    fun stats(text: String): TextStats {
        val trimmed = text.trim()
        val words = if (trimmed.isEmpty()) 0 else trimmed.split(whitespace).size
        val lines = if (text.isEmpty()) 0 else text.lines().size
        val paragraphs = if (trimmed.isEmpty()) 0 else {
            text.split(Regex("""(?:\r?\n){2,}""")).count { it.isNotBlank() }
        }
        val sentences = if (trimmed.isEmpty()) 0 else {
            sentenceEnd.split(trimmed).count { it.isNotBlank() }.coerceAtLeast(1)
        }
        return TextStats(
            characters = text.length,
            charactersNoSpaces = text.count { !it.isWhitespace() },
            words = words,
            lines = lines,
            paragraphs = paragraphs,
            sentences = sentences,
            readingMinutes = if (words == 0) 0 else ((words + 199) / 200).coerceAtLeast(1),
        )
    }

    fun upper(text: String): String = text.uppercase()
    fun lower(text: String): String = text.lowercase()

    fun titleCase(text: String): String =
        text.split(whitespace).joinToString(" ") { word ->
            if (word.isEmpty()) word else word[0].uppercase() + word.drop(1)
        }

    fun sentenceCase(text: String): String {
        if (text.isEmpty()) return text
        val builder = StringBuilder(text.length)
        var capitalizeNext = true
        text.forEach { char ->
            if (capitalizeNext && char.isLetter()) {
                builder.append(char.uppercaseChar())
                capitalizeNext = false
            } else {
                builder.append(if (capitalizeNext) char else char.lowercaseChar())
            }
            if (char == '.' || char == '!' || char == '?' || char == '؟' || char == '\n') {
                capitalizeNext = true
            }
        }
        return builder.toString()
    }

    fun removeDuplicateLines(text: String, trim: Boolean = true, ignoreCase: Boolean = false): String {
        val seen = HashSet<String>()
        return text.lineSequence().filter { line ->
            val key = (if (trim) line.trim() else line).let { if (ignoreCase) it.lowercase() else it }
            seen.add(key)
        }.joinToString("\n")
    }

    fun removeEmptyLines(text: String): String =
        text.lineSequence().filter { it.isNotBlank() }.joinToString("\n")

    enum class SortMode { ALPHABETICAL, ALPHABETICAL_DESC, NUMERIC, LENGTH }

    fun sortLines(
        text: String,
        mode: SortMode,
        ignoreCase: Boolean = true,
        removeDuplicates: Boolean = false,
        removeEmpty: Boolean = false,
    ): String {
        var lines = text.lines()
        if (removeEmpty) lines = lines.filter { it.isNotBlank() }
        if (removeDuplicates) {
            val seen = HashSet<String>()
            lines = lines.filter { seen.add(if (ignoreCase) it.lowercase() else it) }
        }
        val comparator: Comparator<String> = when (mode) {
            SortMode.ALPHABETICAL -> if (ignoreCase) {
                compareBy<String> { it.lowercase() }
            } else {
                naturalOrder<String>()
            }

            SortMode.ALPHABETICAL_DESC -> if (ignoreCase) {
                compareByDescending<String> { it.lowercase() }
            } else {
                naturalOrder<String>().reversed()
            }

            SortMode.NUMERIC -> compareBy<String> { line ->
                line.toDoubleOrNull() ?: Double.POSITIVE_INFINITY
            }

            SortMode.LENGTH -> compareBy<String> { it.length }
        }
        return lines.sortedWith(comparator).joinToString("\n")
    }

    fun reverse(text: String): String = text.reversed()

    fun reverseLines(text: String): String = text.lines().reversed().joinToString("\n")

    fun cleanWhitespace(text: String): String =
        text.lineSequence()
            .joinToString("\n") { line ->
                line.trim().replace(Regex("[\\t\\u00A0]+"), " ").replace(Regex(" {2,}"), " ")
            }
            .replace(Regex("\\n{3,}"), "\n\n")

    fun base64Encode(text: String): String {
        val bytes = text.toByteArray(Charsets.UTF_8)
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        val output = StringBuilder(((bytes.size + 2) / 3) * 4)
        var index = 0
        while (index < bytes.size) {
            val b0 = bytes[index].toInt() and 0xFF
            val b1 = if (index + 1 < bytes.size) bytes[index + 1].toInt() and 0xFF else 0
            val b2 = if (index + 2 < bytes.size) bytes[index + 2].toInt() and 0xFF else 0
            val combined = (b0 shl 16) or (b1 shl 8) or b2
            output.append(alphabet[(combined shr 18) and 0x3F])
            output.append(alphabet[(combined shr 12) and 0x3F])
            output.append(if (index + 1 < bytes.size) alphabet[(combined shr 6) and 0x3F] else '=')
            output.append(if (index + 2 < bytes.size) alphabet[combined and 0x3F] else '=')
            index += 3
        }
        return output.toString()
    }

    fun base64Decode(text: String): String {
        val cleaned = text.filterNot { it.isWhitespace() }
            .replace('-', '+')
            .replace('_', '/')
            .trimEnd('=')
        if (cleaned.length % 4 == 1) throw IllegalArgumentException("Invalid Base64 length")
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        val lookup = IntArray(128) { -1 }
        alphabet.forEachIndexed { index, char -> lookup[char.code] = index }
        val output = ByteArrayOutputStream(cleaned.length * 3 / 4)
        var index = 0
        while (index < cleaned.length) {
            val c0 = decodeChar(cleaned[index], lookup, index)
            val c1 = decodeChar(cleaned[index + 1], lookup, index + 1)
            val c2 = if (index + 2 < cleaned.length) {
                decodeChar(cleaned[index + 2], lookup, index + 2)
            } else {
                0
            }
            val c3 = if (index + 3 < cleaned.length) {
                decodeChar(cleaned[index + 3], lookup, index + 3)
            } else {
                0
            }
            val combined = (c0 shl 18) or (c1 shl 12) or (c2 shl 6) or c3
            output.write((combined shr 16) and 0xFF)
            if (index + 2 < cleaned.length) output.write((combined shr 8) and 0xFF)
            if (index + 3 < cleaned.length) output.write(combined and 0xFF)
            index += 4
        }
        return output.toByteArray().toString(Charsets.UTF_8)
    }

    private fun decodeChar(char: Char, lookup: IntArray, position: Int): Int {
        val code = char.code
        if (code >= lookup.size || lookup[code] < 0) {
            throw IllegalArgumentException("Invalid Base64 character at $position")
        }
        return lookup[code]
    }

    fun urlEncode(text: String): String = URLEncoder.encode(text, "UTF-8")

    fun urlDecode(text: String): String = URLDecoder.decode(text, "UTF-8")

    fun htmlEncode(text: String): String = buildString(text.length) {
        text.forEach { char ->
            when (char) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&#39;")
                else -> append(char)
            }
        }
    }

    fun htmlDecode(text: String): String {
        val named = mapOf(
            "amp" to "&",
            "lt" to "<",
            "gt" to ">",
            "quot" to "\"",
            "apos" to "'",
            "nbsp" to "\u00A0",
            "copy" to "©",
            "reg" to "®",
            "hellip" to "…",
            "mdash" to "—",
            "ndash" to "–",
            "laquo" to "«",
            "raquo" to "»",
            "ldquo" to "“",
            "rdquo" to "”",
            "lsquo" to "‘",
            "rsquo" to "’",
            "times" to "×",
            "divide" to "÷",
            "euro" to "€",
        )
        return Regex("&(#[xX]?[0-9a-fA-F]+|[a-zA-Z]+);").replace(text) { match ->
            val body = match.groupValues[1]
            when {
                body.startsWith("#x", ignoreCase = true) || body.startsWith("#X", ignoreCase = true) -> {
                    body.drop(2).toIntOrNull(16)?.let { codePoint ->
                        String(Character.toChars(codePoint))
                    } ?: match.value
                }

                body.startsWith("#") -> {
                    body.drop(1).toIntOrNull()?.let { codePoint ->
                        String(Character.toChars(codePoint))
                    } ?: match.value
                }

                else -> named[body.lowercase()] ?: match.value
            }
        }
    }

    fun extractUrls(text: String): List<String> =
        urlRegex.findAll(text).map { it.value.trimEnd('.', ',', ';', ')', ']') }.toList()

    fun extractEmails(text: String): List<String> = emailRegex.findAll(text).map { it.value }.toList()

    fun extractNumbers(text: String): List<String> = numberRegex.findAll(text).map { it.value }.toList()

    enum class DiffKind { SAME, ADDED, REMOVED }

    data class DiffLine(
        val kind: DiffKind,
        val text: String,
        val leftNumber: Int?,
        val rightNumber: Int?,
    )

    fun diffLines(left: String, right: String, maxLines: Int = 700): List<DiffLine> {
        val a = left.lines()
        val b = right.lines()
        if (a.size > maxLines || b.size > maxLines) {
            return buildList {
                a.forEachIndexed { index, line ->
                    add(DiffLine(DiffKind.REMOVED, line, index + 1, null))
                }
                b.forEachIndexed { index, line ->
                    add(DiffLine(DiffKind.ADDED, line, null, index + 1))
                }
            }
        }
        val dp = Array(a.size + 1) { IntArray(b.size + 1) }
        for (i in a.indices.reversed()) {
            for (j in b.indices.reversed()) {
                dp[i][j] = if (a[i] == b[j]) {
                    dp[i + 1][j + 1] + 1
                } else {
                    maxOf(dp[i + 1][j], dp[i][j + 1])
                }
            }
        }
        val result = mutableListOf<DiffLine>()
        var i = 0
        var j = 0
        while (i < a.size && j < b.size) {
            if (a[i] == b[j]) {
                result.add(DiffLine(DiffKind.SAME, a[i], i + 1, j + 1))
                i++
                j++
            } else if (dp[i + 1][j] >= dp[i][j + 1]) {
                result.add(DiffLine(DiffKind.REMOVED, a[i], i + 1, null))
                i++
            } else {
                result.add(DiffLine(DiffKind.ADDED, b[j], null, j + 1))
                j++
            }
        }
        while (i < a.size) {
            result.add(DiffLine(DiffKind.REMOVED, a[i], i + 1, null))
            i++
        }
        while (j < b.size) {
            result.add(DiffLine(DiffKind.ADDED, b[j], null, j + 1))
            j++
        }
        return result
    }

    fun loremIpsum(paragraphs: Int, sentencesPerParagraph: Int, random: Random = Random.Default): String {
        val words = ("lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor " +
            "incididunt ut labore et dolore magna aliqua enim ad minim veniam quis nostrud " +
            "exercitation ullamco laboris nisi aliquip ex ea commodo consequat duis aute irure " +
            "in reprehenderit voluptate velit esse cillum dolore fugiat nulla pariatur excepteur " +
            "sint occaecat cupidatat non proident sunt culpa qui officia deserunt mollit anim id est laborum")
            .split(" ")
        val paragraphCount = paragraphs.coerceIn(1, 50)
        val sentenceCount = sentencesPerParagraph.coerceIn(1, 30)
        return buildString {
            repeat(paragraphCount) { paragraphIndex ->
                if (paragraphIndex > 0) append("\n\n")
                repeat(sentenceCount) { sentenceIndex ->
                    if (sentenceIndex > 0) append(' ')
                    val wordCount = random.nextInt(8, 18)
                    repeat(wordCount) { wordIndex ->
                        if (wordIndex > 0) append(' ')
                        var word = words[random.nextInt(words.size)]
                        if (wordIndex == 0) word = word.replaceFirstChar { it.uppercase() }
                        append(word)
                    }
                    append('.')
                }
            }
        }
    }
}
