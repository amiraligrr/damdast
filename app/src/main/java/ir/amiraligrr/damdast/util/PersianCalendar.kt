package ir.amiraligrr.damdast.util

import java.util.Calendar

data class JalaliDate(val year: Int, val month: Int, val day: Int) {
    override fun toString(): String = "%04d/%02d/%02d".format(year, month, day)
}

data class GregorianDate(val year: Int, val month: Int, val day: Int) {
    override fun toString(): String = "%04d/%02d/%02d".format(year, month, day)
}

/**
 * Calendar conversion based on the well-tested jalaali-js algorithm:
 * https://github.com/jalaali/jalaali-js (MIT).
 */
object PersianCalendar {
    private val breaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
        1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178,
    )

    private data class JalCal(val leap: Int, val gy: Int, val march: Int)

    fun today(): JalaliDate {
        val calendar = Calendar.getInstance()
        return fromGregorian(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
        )
    }

    fun fromGregorian(year: Int, month: Int, day: Int): JalaliDate {
        require(isValidGregorian(year, month, day)) { "Invalid Gregorian date" }
        return d2j(g2d(year, month, day))
    }

    fun toGregorian(year: Int, month: Int, day: Int): GregorianDate {
        require(isValid(year, month, day)) { "Invalid Jalali date" }
        return d2g(j2d(year, month, day))
    }

    fun isLeap(year: Int): Boolean = jalCal(year, false).leap == 0

    fun daysInMonth(year: Int, month: Int): Int = when (month) {
        in 1..6 -> 31
        in 7..11 -> 30
        12 -> if (isLeap(year)) 30 else 29
        else -> 0
    }

    fun isValid(year: Int, month: Int, day: Int): Boolean =
        year > 0 && month in 1..12 && day in 1..daysInMonth(year, month)

    fun isGregorianLeap(year: Int): Boolean =
        year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

    fun gregorianDaysInMonth(year: Int, month: Int): Int = when (month) {
        2 -> if (isGregorianLeap(year)) 29 else 28
        4, 6, 9, 11 -> 30
        in 1..12 -> 31
        else -> 0
    }

    fun isValidGregorian(year: Int, month: Int, day: Int): Boolean =
        year > 0 && month in 1..12 && day in 1..gregorianDaysInMonth(year, month)

    private fun jalCal(jy: Int, withoutLeap: Boolean): JalCal {
        var gy = jy + 621
        var leapJ = -14
        var jp = breaks[0]
        var jump = 0
        var index = 1
        while (index < breaks.size) {
            val jm = breaks[index]
            jump = jm - jp
            if (jy < jm) break
            leapJ += (jump / 33) * 8 + ((jump % 33) / 4)
            jp = jm
            index++
        }
        val n0 = jy - jp
        leapJ += (n0 / 33) * 8 + ((n0 % 33 + 3) / 4)
        if (jump % 33 == 4 && jump - n0 == 4) leapJ++
        val leapG = gy / 4 - ((gy / 100 + 1) * 3) / 4 - 150
        val march = 20 + leapJ - leapG
        var leap = -1
        if (!withoutLeap) {
            var n = n0
            if (jump - n < 6) n = n - jump + ((jump + 4) / 33) * 33
            leap = ((n + 1) % 33 - 1) % 4
            if (leap == -1) leap = 4
        }
        return JalCal(leap, gy, march)
    }

    private fun g2d(gy: Int, gm: Int, gd: Int): Int {
        var d = ((gy + (gm - 8) / 6 + 100100) * 1461) / 4 +
            (153 * ((gm + 9) % 12) + 2) / 5 + gd - 34840408
        d -= (((gy + 100100 + (gm - 8) / 6) / 100) * 3) / 4 - 752
        return d
    }

    private fun d2g(jdn: Int): GregorianDate {
        var j = 4 * jdn + 139361631
        j += (((4 * jdn + 183187720) / 146097) * 3) / 4 * 4 - 3908
        val i = ((j % 1461) / 4) * 5 + 308
        val gd = (i % 153) / 5 + 1
        val gm = (i / 153) % 12 + 1
        val gy = j / 1461 - 100100 + (8 - gm) / 6
        return GregorianDate(gy, gm, gd)
    }

    private fun j2d(jy: Int, jm: Int, jd: Int): Int {
        val r = jalCal(jy, true)
        return g2d(r.gy, 3, r.march) + (jm - 1) * 31 - (jm / 7) * (jm - 7) + jd - 1
    }

    private fun d2j(jdn: Int): JalaliDate {
        val gy = d2g(jdn).year
        var jy = gy - 621
        val r = jalCal(jy, false)
        val jdn1f = g2d(gy, 3, r.march)
        var k = jdn - jdn1f
        var jm: Int
        var jd: Int
        if (k in 0..185) {
            jm = 1 + k / 31
            jd = k % 31 + 1
            return JalaliDate(jy, jm, jd)
        }
        if (k < 0) {
            jy -= 1
            k += 179
            if (r.leap == 1) k += 1
        } else {
            k -= 186
        }
        jm = 7 + k / 30
        jd = k % 30 + 1
        return JalaliDate(jy, jm, jd)
    }
}
