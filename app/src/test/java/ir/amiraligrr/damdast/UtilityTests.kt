package ir.amiraligrr.damdast

import ir.amiraligrr.damdast.util.CalculatorEngine
import ir.amiraligrr.damdast.util.CalculatorException
import ir.amiraligrr.damdast.util.CodeFormatter
import ir.amiraligrr.damdast.util.ColorUtils
import ir.amiraligrr.damdast.util.CsvUtils
import ir.amiraligrr.damdast.util.DateMath
import ir.amiraligrr.damdast.util.JsonUtils
import ir.amiraligrr.damdast.util.JwtUtils
import ir.amiraligrr.damdast.util.NumberBaseConverter
import ir.amiraligrr.damdast.util.PersianCalendar
import ir.amiraligrr.damdast.util.SecurityUtils
import ir.amiraligrr.damdast.util.TextTransforms
import ir.amiraligrr.damdast.util.UnitConverter
import ir.amiraligrr.damdast.util.UnixPermissions
import ir.amiraligrr.damdast.util.UrlUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorTests {
    @Test
    fun precedenceAndParentheses() {
        assertEquals(14.0, CalculatorEngine.evaluate("2+3*4"), 1e-9)
        assertEquals(20.0, CalculatorEngine.evaluate("(2+3)*4"), 1e-9)
        assertEquals(512.0, CalculatorEngine.evaluate("2^3^2"), 1e-9)
        assertEquals(-4.0, CalculatorEngine.evaluate("-2^2"), 1e-9)
        assertEquals(0.5, CalculatorEngine.evaluate("sin(30)", degrees = true), 1e-9)
        assertEquals(6.0, CalculatorEngine.evaluate("3!"), 1e-9)
        assertEquals(14.0, CalculatorEngine.evaluate("2(3+4)"), 1e-9)
        assertEquals(0.5, CalculatorEngine.evaluate("50%"), 1e-9)
    }

    @Test
    fun invalidExpressionsThrow() {
        assertThrows<CalculatorException> { CalculatorEngine.evaluate("1/0") }
        assertThrows<CalculatorException> { CalculatorEngine.evaluate("2+") }
        assertThrows<CalculatorException> { CalculatorEngine.evaluate("(1+2") }
        assertThrows<CalculatorException> { CalculatorEngine.evaluate("foo(2)") }
    }

    private inline fun <reified T : Throwable> assertThrows(block: () -> Unit) {
        var thrown = false
        try {
            block()
        } catch (error: Throwable) {
            if (error is T) thrown = true
        }
        assertTrue("Expected ${T::class.java.simpleName}", thrown)
    }
}

class PersianCalendarTests {
    @Test
    fun knownConversions() {
        assertEquals(1403, PersianCalendar.fromGregorian(2024, 3, 20).year)
        assertEquals(1, PersianCalendar.fromGregorian(2024, 3, 20).month)
        assertEquals(1, PersianCalendar.fromGregorian(2024, 3, 20).day)
        val gregorian = PersianCalendar.toGregorian(1403, 1, 1)
        assertEquals(2024, gregorian.year)
        assertEquals(3, gregorian.month)
        assertEquals(20, gregorian.day)
        assertTrue(PersianCalendar.isLeap(1403))
        assertFalse(PersianCalendar.isLeap(1402))
        assertTrue(PersianCalendar.isValid(1403, 12, 30))
        assertFalse(PersianCalendar.isValid(1402, 12, 30))
    }

    @Test
    fun roundTrips() {
        for (year in 1395..1415) {
            for (month in 1..12) {
                for (day in listOf(1, 15, PersianCalendar.daysInMonth(year, month))) {
                    val gregorian = PersianCalendar.toGregorian(year, month, day)
                    val back = PersianCalendar.fromGregorian(gregorian.year, gregorian.month, gregorian.day)
                    assertEquals(year, back.year)
                    assertEquals(month, back.month)
                    assertEquals(day, back.day)
                }
            }
        }
    }
}

class TextTransformTests {
    @Test
    fun base64RoundTrip() {
        val samples = listOf("", "hello", "دامدست", "line1\nline2", "🔐 QR")
        samples.forEach { sample ->
            assertEquals(sample, TextTransforms.base64Decode(TextTransforms.base64Encode(sample)))
        }
    }

    @Test
    fun urlAndHtmlRoundTrip() {
        val sample = "a b&c=d/سلام"
        assertEquals(sample, TextTransforms.urlDecode(TextTransforms.urlEncode(sample)))
        assertEquals(sample, TextTransforms.htmlDecode(TextTransforms.htmlEncode(sample)))
        assertEquals("© & < >", TextTransforms.htmlDecode("&copy; &amp; &lt; &gt;"))
    }

    @Test
    fun lineOperations() {
        assertEquals("a\nb", TextTransforms.removeDuplicateLines("a\nb\na"))
        assertEquals("A\nb\nC", TextTransforms.sortLines("C\nb\nA", TextTransforms.SortMode.ALPHABETICAL))
        assertEquals("c\nb\na", TextTransforms.reverseLines("a\nb\nc"))
        assertEquals("a b c", TextTransforms.cleanWhitespace("  a   b\tc  "))
    }

    @Test
    fun statsAndDiff() {
        val stats = TextTransforms.stats("Hello world.\nSecond line!")
        assertEquals(4, stats.words)
        assertEquals(2, stats.lines)
        assertEquals(2, stats.sentences)
        assertTrue(TextTransforms.diffLines("a\nb", "a\nc").any { it.kind == TextTransforms.DiffKind.ADDED })
        assertTrue(TextTransforms.diffLines("same", "same").all { it.kind == TextTransforms.DiffKind.SAME })
    }

    @Test
    fun extractionFindsValues() {
        val text = "Visit https://example.com, email me@test.com or call +98 912 123."
        assertEquals(listOf("https://example.com"), TextTransforms.extractUrls(text))
        assertEquals(listOf("me@test.com"), TextTransforms.extractEmails(text))
        assertTrue(TextTransforms.extractNumbers(text).contains("+98"))
    }
}

class SecurityTests {
    @Test
    fun passwordHonorsGroupsAndLength() {
        val result = SecurityUtils.generatePassword(24, true, true, true, true, false)
        assertEquals(24, result.value.length)
        assertTrue(result.entropyBits > 0)
        assertTrue(result.value.any { it.isUpperCase() })
        assertTrue(result.value.any { it.isLowerCase() })
        assertTrue(result.value.any { it.isDigit() })
    }

    @Test
    fun uniqueRandomNumbers() {
        val values = SecurityUtils.randomInts(1, 10, 10, true)
        assertEquals(10, values.size)
        assertEquals(10, values.toSet().size)
    }
}

class DeveloperUtilityTests {
    @Test
    fun jsonPrettyMinifyAndValidation() {
        val json = "{\"a\":1,\"b\":[true,false],\"s\":\"x y\"}"
        val pretty = JsonUtils.pretty(json)
        assertTrue(pretty.contains("\n  \"a\""))
        assertEquals(json, JsonUtils.minify(pretty))
        assertNull(JsonUtils.validate(json))
        assertNotNull(JsonUtils.validate("{\"a\":}"))
    }

    @Test
    fun codeFormatterIndentsMarkup() {
        val output = CodeFormatter.markup("<root><child>x</child></root>")
        assertTrue(output.contains("<root>"))
        assertTrue(output.contains("  <child>"))
    }

    @Test
    fun colorConversions() {
        val color = ColorUtils.parse("#7C5CFF")
        assertEquals(124, color.red)
        assertEquals(92, color.green)
        assertEquals(255, color.blue)
        assertEquals("#7C5CFF", color.hex())
        assertTrue(ColorUtils.contrastRatio(color, color.copy(red = 255, green = 255, blue = 255)) >= 1.0)
    }

    @Test
    fun unixPermissions() {
        assertEquals("rwxr-xr-x", UnixPermissions.fromOctal("755"))
        assertEquals("755", UnixPermissions.toOctal("rwxr-xr-x"))
        assertEquals("rwsr-xr-x", UnixPermissions.fromOctal("4755"))
    }

    @Test
    fun numberBasesAndUnits() {
        val converted = NumberBaseConverter.convert("FF", 16)
        assertEquals("11111111", converted[2])
        assertEquals("255", converted[10])
        assertEquals(1000.0, UnitConverter.convert(UnitConverter.length, UnitConverter.length.units[1], UnitConverter.length.units[0], 1.0), 1e-9)
        assertEquals(32.0, UnitConverter.convert(UnitConverter.temperature, UnitConverter.temperature.units[0], UnitConverter.temperature.units[1], 0.0), 1e-9)
    }

    @Test
    fun urlParsingAndJwt() {
        val parsed = UrlUtils.parse("https://example.com:8443/a/b?name=Damdast&lang=fa#top")
        assertEquals("https", parsed.scheme)
        assertEquals("example.com", parsed.host)
        assertEquals("8443", parsed.port)
        assertEquals("fa", parsed.parameters.last().second)
        val token = "eyJhbGciOiJub25lIn0.eyJzdWIiOiIxMjMifQ.signature"
        val jwt = JwtUtils.decode(token)
        assertTrue(jwt.header.contains("alg"))
        assertTrue(jwt.payload.contains("123"))
    }

    @Test
    fun csvQuotedValues() {
        val rows = CsvUtils.parse("name,note\n\"a,b\",\"line1\nline2\"")
        assertEquals(2, rows.size)
        assertEquals("a,b", rows[1][0])
        assertEquals("line1\nline2", rows[1][1])
    }

    @Test
    fun dateMath() {
        assertEquals(366L, DateMath.daysBetween(2024, 1, 1, 2025, 1, 1))
        val difference = DateMath.difference(2024, 1, 1, 2025, 1, 1)
        assertEquals(1, difference.years)
        assertEquals(366L, difference.totalDays)
        val added = DateMath.addDays(2024, 2, 28, 1)
        assertEquals(29, added.day)
        assertNotNull(DateMath.parseTimestamp("2026-11-20 14:30"))
    }
}
