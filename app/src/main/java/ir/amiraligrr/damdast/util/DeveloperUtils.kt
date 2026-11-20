package ir.amiraligrr.damdast.util

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.security.MessageDigest

object JsonUtils {
    fun validate(text: String): String? {
        if (text.isBlank()) return "Empty JSON"
        return try {
            val tokener = JSONTokener(text)
            tokener.nextValue()
            if (tokener.nextClean() != 0.toChar()) {
                "Unexpected content after the JSON value"
            } else {
                null
            }
        } catch (error: Exception) {
            error.message ?: "Invalid JSON"
        }
    }

    fun pretty(text: String, indent: String = "  "): String {
        validate(text)?.let { throw IllegalArgumentException(it) }
        val output = StringBuilder(text.length + 64)
        var depth = 0
        var inString = false
        var escaped = false
        for (char in text) {
            if (inString) {
                output.append(char)
                if (escaped) {
                    escaped = false
                } else if (char == '\\') {
                    escaped = true
                } else if (char == '"') {
                    inString = false
                }
                continue
            }
            when (char) {
                '"' -> {
                    inString = true
                    output.append(char)
                }

                '{', '[' -> {
                    output.append(char)
                    depth++
                    output.append('\n')
                    appendIndent(output, depth, indent)
                }

                '}', ']' -> {
                    depth = (depth - 1).coerceAtLeast(0)
                    output.append('\n')
                    appendIndent(output, depth, indent)
                    output.append(char)
                }

                ',' -> {
                    output.append(',')
                    output.append('\n')
                    appendIndent(output, depth, indent)
                }

                ':' -> output.append(": ")
                ' ', '\n', '\r', '\t' -> Unit
                else -> output.append(char)
            }
        }
        return output.toString().trim()
    }

    fun minify(text: String): String {
        validate(text)?.let { throw IllegalArgumentException(it) }
        val output = StringBuilder(text.length)
        var inString = false
        var escaped = false
        for (char in text) {
            if (inString) {
                output.append(char)
                if (escaped) {
                    escaped = false
                } else if (char == '\\') {
                    escaped = true
                } else if (char == '"') {
                    inString = false
                }
                continue
            }
            when (char) {
                '"' -> {
                    inString = true
                    output.append(char)
                }

                ' ', '\n', '\r', '\t' -> Unit
                else -> output.append(char)
            }
        }
        return output.toString()
    }

    private fun appendIndent(output: StringBuilder, depth: Int, indent: String) {
        repeat(depth) { output.append(indent) }
    }

    fun prettyValue(value: Any?, indent: String = "  "): String = when (value) {
        is JSONObject -> value.toString(2)
        is JSONArray -> value.toString(2)
        else -> value.toString()
    }
}

object CodeFormatter {
    private val markupToken = Regex(
        """<!--[\s\S]*?-->|<!\[CDATA\[[\s\S]*?]]>|<\?[\s\S]*?\?>|<![^>]*>|</?[^>]+>|[^<]+""",
    )

    fun markup(text: String, indent: String = "  "): String {
        if (text.isBlank()) return ""
        val output = StringBuilder()
        var depth = 0
        markupToken.findAll(text).forEach { match ->
            val token = match.value.trim()
            if (token.isEmpty()) return@forEach
            when {
                token.startsWith("</") -> {
                    depth = (depth - 1).coerceAtLeast(0)
                    appendLine(output, depth, token, indent)
                }

                token.startsWith("<!--") || token.startsWith("<?") || token.startsWith("<!") -> {
                    appendLine(output, depth, token, indent)
                }

                token.startsWith("<") && !token.endsWith("/>") -> {
                    appendLine(output, depth, token, indent)
                    depth++
                }

                token.startsWith("<") -> appendLine(output, depth, token, indent)
                else -> appendLine(output, depth, token, indent)
            }
        }
        return output.toString().trimEnd()
    }

    fun css(text: String, indent: String = "  "): String {
        return formatBraces(text, indent, newlineAfterSemicolon = true)
    }

    fun javascript(text: String, indent: String = "  "): String {
        return formatBraces(text, indent, newlineAfterSemicolon = true, trackParens = true)
    }

    private fun formatBraces(
        text: String,
        indent: String,
        newlineAfterSemicolon: Boolean,
        trackParens: Boolean = false,
    ): String {
        val output = StringBuilder(text.length + 64)
        var depth = 0
        var parenDepth = 0
        var buffer = StringBuilder()
        var inString = false
        var stringChar = ' '
        var escaped = false
        var inLineComment = false
        var inBlockComment = false

        fun flush(newline: Boolean = true) {
            val trimmed = buffer.toString().trim()
            buffer = StringBuilder()
            if (trimmed.isNotEmpty()) {
                repeat(depth) { output.append(indent) }
                output.append(trimmed)
                if (newline) output.append('\n')
            }
        }

        var index = 0
        while (index < text.length) {
            val char = text[index]
            val next = text.getOrNull(index + 1)
            if (inLineComment) {
                buffer.append(char)
                if (char == '\n') {
                    inLineComment = false
                    flush()
                }
                index++
                continue
            }
            if (inBlockComment) {
                buffer.append(char)
                if (char == '*' && next == '/') {
                    buffer.append('/')
                    index += 2
                    inBlockComment = false
                    flush()
                    continue
                }
                index++
                continue
            }
            if (inString) {
                buffer.append(char)
                if (escaped) {
                    escaped = false
                } else if (char == '\\') {
                    escaped = true
                } else if (char == stringChar) {
                    inString = false
                }
                index++
                continue
            }
            when {
                char == '/' && next == '/' -> {
                    inLineComment = true
                    buffer.append("//")
                    index += 2
                }

                char == '/' && next == '*' -> {
                    inBlockComment = true
                    buffer.append("/*")
                    index += 2
                }

                char == '"' || char == '\'' || char == '`' -> {
                    inString = true
                    stringChar = char
                    buffer.append(char)
                    index++
                }

                char == '{' -> {
                    buffer.append('{')
                    flush()
                    depth++
                    index++
                }

                char == '}' -> {
                    flush()
                    depth = (depth - 1).coerceAtLeast(0)
                    repeat(depth) { output.append(indent) }
                    output.append('}')
                    if (next == ';' || next == ',' || next == ')') {
                        // Punctuation is appended by the normal scanner.
                    } else {
                        output.append('\n')
                    }
                    index++
                }

                char == ';' && newlineAfterSemicolon && (!trackParens || parenDepth == 0) -> {
                    buffer.append(';')
                    flush()
                    index++
                }

                trackParens && char == '(' -> {
                    parenDepth++
                    buffer.append(char)
                    index++
                }

                trackParens && char == ')' -> {
                    parenDepth = (parenDepth - 1).coerceAtLeast(0)
                    buffer.append(char)
                    index++
                }

                char == '\n' || char == '\r' -> {
                    if (buffer.isNotBlank()) {
                        buffer.append(' ')
                    }
                    index++
                }

                else -> {
                    buffer.append(char)
                    index++
                }
            }
        }
        flush(newline = false)
        return output.toString().trim()
    }

    private fun appendLine(output: StringBuilder, depth: Int, token: String, indent: String) {
        repeat(depth) { output.append(indent) }
        output.append(token).append('\n')
    }
}

data class ArgbColor(val alpha: Int, val red: Int, val green: Int, val blue: Int) {
    fun hex(includeAlpha: Boolean = false): String =
        if (includeAlpha) {
            "#%02X%02X%02X%02X".format(alpha, red, green, blue)
        } else {
            "#%02X%02X%02X".format(red, green, blue)
        }

    fun rgbString(): String = "rgb($red, $green, $blue)"
    fun rgbaString(): String = "rgba($red, $green, $blue, ${"%.2f".format(alpha / 255.0)})"
}

object ColorUtils {
    fun parse(input: String): ArgbColor {
        val text = input.trim()
        if (text.startsWith("#")) {
            val hex = text.removePrefix("#")
            val full = when (hex.length) {
                3 -> "FF${hex[0]}${hex[0]}${hex[1]}${hex[1]}${hex[2]}${hex[2]}"
                4 -> "${hex[3]}${hex[3]}${hex[0]}${hex[0]}${hex[1]}${hex[1]}${hex[2]}${hex[2]}"
                6 -> "FF$hex"
                8 -> hex
                else -> throw IllegalArgumentException("Hex color must have 3, 4, 6 or 8 digits")
            }
            val value = full.toLong(16)
            return ArgbColor(
                alpha = ((value shr 24) and 0xFF).toInt(),
                red = ((value shr 16) and 0xFF).toInt(),
                green = ((value shr 8) and 0xFF).toInt(),
                blue = (value and 0xFF).toInt(),
            )
        }
        val rgb = Regex("""rgba?\(\s*([\d.]+)\s*,\s*([\d.]+)\s*,\s*([\d.]+)(?:\s*,\s*([\d.]+))?\s*\)""")
            .find(text)
        if (rgb != null) {
            val parts = rgb.groupValues
            val alpha = if (parts[4].isBlank()) 255 else (parts[4].toDouble().coerceIn(0.0, 1.0) * 255).toInt()
            return ArgbColor(
                alpha = alpha,
                red = parts[1].toDouble().coerceIn(0.0, 255.0).toInt(),
                green = parts[2].toDouble().coerceIn(0.0, 255.0).toInt(),
                blue = parts[3].toDouble().coerceIn(0.0, 255.0).toInt(),
            )
        }
        throw IllegalArgumentException("Use #RRGGBB, #AARRGGBB or rgb(...)")
    }

    fun fromHsv(hue: Float, saturation: Float, value: Float): ArgbColor {
        val h = ((hue % 360f) + 360f) % 360f
        val s = saturation.coerceIn(0f, 1f)
        val v = value.coerceIn(0f, 1f)
        val c = v * s
        val x = c * (1 - kotlin.math.abs((h / 60f) % 2 - 1))
        val m = v - c
        val (r1, g1, b1) = when {
            h < 60 -> Triple(c, x, 0f)
            h < 120 -> Triple(x, c, 0f)
            h < 180 -> Triple(0f, c, x)
            h < 240 -> Triple(0f, x, c)
            h < 300 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        return ArgbColor(
            255,
            ((r1 + m) * 255).toInt().coerceIn(0, 255),
            ((g1 + m) * 255).toInt().coerceIn(0, 255),
            ((b1 + m) * 255).toInt().coerceIn(0, 255),
        )
    }

    fun toHsv(color: ArgbColor): Triple<Float, Float, Float> {
        val r = color.red / 255f
        val g = color.green / 255f
        val b = color.blue / 255f
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        val hue = when {
            delta == 0f -> 0f
            max == r -> 60f * (((g - b) / delta) % 6f)
            max == g -> 60f * (((b - r) / delta) + 2f)
            else -> 60f * (((r - g) / delta) + 4f)
        }.let { if (it < 0) it + 360f else it }
        val saturation = if (max == 0f) 0f else delta / max
        return Triple(hue, saturation, max)
    }

    fun toHsl(color: ArgbColor): Triple<Float, Float, Float> {
        val (h, s, v) = toHsv(color)
        val lightness = v * (1 - s / 2)
        val saturation = if (lightness == 0f || lightness == 1f) {
            0f
        } else {
            (v - lightness) / minOf(lightness, 1 - lightness)
        }
        return Triple(h, saturation, lightness)
    }

    fun hslString(color: ArgbColor): String {
        val (h, s, l) = toHsl(color)
        return "hsl(${h.toInt()}, ${(s * 100).toInt()}%, ${(l * 100).toInt()}%)"
    }

    fun hsvString(color: ArgbColor): String {
        val (h, s, v) = toHsv(color)
        return "hsv(${h.toInt()}, ${(s * 100).toInt()}%, ${(v * 100).toInt()}%)"
    }

    fun contrastRatio(first: ArgbColor, second: ArgbColor): Double {
        val l1 = relativeLuminance(first)
        val l2 = relativeLuminance(second)
        return (maxOf(l1, l2) + 0.05) / (minOf(l1, l2) + 0.05)
    }

    fun relativeLuminance(color: ArgbColor): Double {
        fun channel(value: Int): Double {
            val c = value / 255.0
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)
    }

    fun palette(base: ArgbColor): List<ArgbColor> {
        val (h, s, l) = toHsl(base)
        val hues = listOf(h, (h + 180f) % 360f, (h + 30f) % 360f, (h + 210f) % 360f, (h + 120f) % 360f)
        return hues.mapIndexed { index, hue ->
            val lightness = when (index) {
                0 -> l.coerceIn(0.2f, 0.8f)
                1 -> (l + 0.15f).coerceIn(0.25f, 0.85f)
                2 -> (l - 0.12f).coerceIn(0.15f, 0.75f)
                3 -> (l + 0.05f).coerceIn(0.2f, 0.8f)
                else -> (l - 0.08f).coerceIn(0.15f, 0.75f)
            }
            hslToColor(hue, s.coerceIn(0.25f, 0.9f), lightness)
        }
    }

    fun hslToColor(hue: Float, saturation: Float, lightness: Float): ArgbColor {
        val h = ((hue % 360f) + 360f) % 360f
        val s = saturation.coerceIn(0f, 1f)
        val l = lightness.coerceIn(0f, 1f)
        val c = (1 - kotlin.math.abs(2 * l - 1)) * s
        val x = c * (1 - kotlin.math.abs((h / 60f) % 2 - 1))
        val m = l - c / 2
        val (r1, g1, b1) = when {
            h < 60 -> Triple(c, x, 0f)
            h < 120 -> Triple(x, c, 0f)
            h < 180 -> Triple(0f, c, x)
            h < 240 -> Triple(0f, x, c)
            h < 300 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        return ArgbColor(
            255,
            ((r1 + m) * 255).toInt().coerceIn(0, 255),
            ((g1 + m) * 255).toInt().coerceIn(0, 255),
            ((b1 + m) * 255).toInt().coerceIn(0, 255),
        )
    }

    private fun Double.pow(exponent: Double): Double = Math.pow(this, exponent)
}

object UnixPermissions {
    fun fromOctal(input: String): String {
        val value = input.trim().normalizeDigits().removePrefix("0o")
        if (value.length !in 3..4 || value.any { it !in '0'..'7' }) {
            throw IllegalArgumentException("Use 3 or 4 octal digits")
        }
        val padded = value.padStart(4, '0')
        val special = padded[0].digitToInt()
        val permissions = padded.drop(1).map { it.digitToInt() }
        val result = StringBuilder()
        permissions.forEachIndexed { index, digit ->
            result.append(if (digit and 4 != 0) 'r' else '-')
            result.append(if (digit and 2 != 0) 'w' else '-')
            val execute = digit and 1 != 0
            val flag = when (index) {
                0 -> special and 4 != 0
                1 -> special and 2 != 0
                else -> special and 1 != 0
            }
            val specialChar = if (index == 2) 't' else 's'
            val specialUpper = if (index == 2) 'T' else 'S'
            result.append(
                when {
                    flag && execute -> specialChar
                    flag -> specialUpper
                    execute -> 'x'
                    else -> '-'
                },
            )
        }
        return result.toString()
    }

    fun toOctal(rwx: String): String {
        if (rwx.length != 9) throw IllegalArgumentException("Expected 9 permission characters")
        fun triplet(value: String): Int {
            var digit = 0
            if (value[0] == 'r') digit += 4
            if (value[1] == 'w') digit += 2
            if (value[2] == 'x' || value[2] == 's' || value[2] == 't') digit += 1
            return digit
        }
        return "${triplet(rwx.substring(0, 3))}${triplet(rwx.substring(3, 6))}${triplet(rwx.substring(6, 9))}"
    }
}

object HashUtils {
    val algorithms = listOf("MD5", "SHA-1", "SHA-256", "SHA-512")

    fun hash(text: String, algorithm: String): String {
        val digest = MessageDigest.getInstance(algorithm)
        return digest.digest(text.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}

object UrlUtils {
    data class ParsedUrl(
        val scheme: String,
        val host: String,
        val port: String,
        val path: String,
        val query: String,
        val fragment: String,
        val parameters: List<Pair<String, String>>,
    )

    fun parse(input: String): ParsedUrl {
        val uri = java.net.URI(input.trim())
        val parameters = uri.rawQuery.orEmpty().split('&')
            .filter { it.isNotBlank() }
            .map { part ->
                val pieces = part.split('=', limit = 2)
                val name = TextTransforms.urlDecode(pieces[0])
                val value = if (pieces.size == 2) TextTransforms.urlDecode(pieces[1]) else ""
                name to value
            }
        return ParsedUrl(
            scheme = uri.scheme.orEmpty(),
            host = uri.host.orEmpty(),
            port = if (uri.port == -1) "" else uri.port.toString(),
            path = uri.rawPath.orEmpty(),
            query = uri.rawQuery.orEmpty(),
            fragment = uri.rawFragment.orEmpty(),
            parameters = parameters,
        )
    }
}
