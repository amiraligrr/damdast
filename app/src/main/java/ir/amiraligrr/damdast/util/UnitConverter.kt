package ir.amiraligrr.damdast.util

import java.math.BigInteger

data class ConversionUnit(
    val symbol: String,
    val factor: Double,
)

data class ConversionCategory(
    val key: String,
    val units: List<ConversionUnit>,
)

object UnitConverter {
    val length = ConversionCategory(
        "length",
        listOf(
            ConversionUnit("m", 1.0),
            ConversionUnit("km", 1000.0),
            ConversionUnit("cm", 0.01),
            ConversionUnit("mm", 0.001),
            ConversionUnit("mi", 1609.344),
            ConversionUnit("yd", 0.9144),
            ConversionUnit("ft", 0.3048),
            ConversionUnit("in", 0.0254),
            ConversionUnit("nmi", 1852.0),
        ),
    )
    val weight = ConversionCategory(
        "weight",
        listOf(
            ConversionUnit("kg", 1.0),
            ConversionUnit("g", 0.001),
            ConversionUnit("mg", 0.000001),
            ConversionUnit("t", 1000.0),
            ConversionUnit("lb", 0.45359237),
            ConversionUnit("oz", 0.028349523125),
            ConversionUnit("st", 6.35029318),
        ),
    )
    val temperature = ConversionCategory(
        "temperature",
        listOf(
            ConversionUnit("°C", 1.0),
            ConversionUnit("°F", 1.0),
            ConversionUnit("K", 1.0),
        ),
    )
    val area = ConversionCategory(
        "area",
        listOf(
            ConversionUnit("m²", 1.0),
            ConversionUnit("km²", 1_000_000.0),
            ConversionUnit("cm²", 0.0001),
            ConversionUnit("ha", 10_000.0),
            ConversionUnit("acre", 4046.8564224),
            ConversionUnit("ft²", 0.09290304),
            ConversionUnit("in²", 0.00064516),
            ConversionUnit("yd²", 0.83612736),
        ),
    )
    val volume = ConversionCategory(
        "volume",
        listOf(
            ConversionUnit("L", 1.0),
            ConversionUnit("mL", 0.001),
            ConversionUnit("m³", 1000.0),
            ConversionUnit("cm³", 0.001),
            ConversionUnit("gal (US)", 3.785411784),
            ConversionUnit("qt (US)", 0.946352946),
            ConversionUnit("pt (US)", 0.473176473),
            ConversionUnit("cup (US)", 0.2365882365),
            ConversionUnit("fl oz (US)", 0.0295735295625),
            ConversionUnit("tbsp", 0.01478676478125),
            ConversionUnit("tsp", 0.00492892159375),
        ),
    )
    val speed = ConversionCategory(
        "speed",
        listOf(
            ConversionUnit("m/s", 1.0),
            ConversionUnit("km/h", 1.0 / 3.6),
            ConversionUnit("mph", 0.44704),
            ConversionUnit("knot", 0.514444444444),
            ConversionUnit("ft/s", 0.3048),
        ),
    )
    val time = ConversionCategory(
        "time",
        listOf(
            ConversionUnit("s", 1.0),
            ConversionUnit("ms", 0.001),
            ConversionUnit("min", 60.0),
            ConversionUnit("h", 3600.0),
            ConversionUnit("day", 86_400.0),
            ConversionUnit("week", 604_800.0),
        ),
    )
    val data = ConversionCategory(
        "data",
        listOf(
            ConversionUnit("B", 1.0),
            ConversionUnit("KB", 1024.0),
            ConversionUnit("MB", 1024.0 * 1024),
            ConversionUnit("GB", 1024.0 * 1024 * 1024),
            ConversionUnit("TB", 1024.0 * 1024 * 1024 * 1024),
            ConversionUnit("bit", 0.125),
            ConversionUnit("KiB", 1024.0),
            ConversionUnit("MiB", 1024.0 * 1024),
        ),
    )

    val categories = listOf(length, weight, temperature, area, volume, speed, time, data)

    fun convert(category: ConversionCategory, from: ConversionUnit, to: ConversionUnit, value: Double): Double {
        if (category.key == "temperature") {
            val celsius = when (from.symbol) {
                "°C" -> value
                "°F" -> (value - 32.0) * 5.0 / 9.0
                "K" -> value - 273.15
                else -> value
            }
            return when (to.symbol) {
                "°C" -> celsius
                "°F" -> celsius * 9.0 / 5.0 + 32.0
                "K" -> celsius + 273.15
                else -> celsius
            }
        }
        return value * from.factor / to.factor
    }
}

object NumberBaseConverter {
    val bases = listOf(2, 8, 10, 16, 32)

    fun convert(value: String, fromBase: Int): Map<Int, String> {
        val normalized = value.normalizeDigits().trim().replace("_", "").replace(" ", "")
        if (normalized.isEmpty()) throw NumberFormatException("Empty value")
        if (fromBase !in 2..36) throw NumberFormatException("Unsupported base")
        val number = BigInteger(normalized, fromBase)
        return bases.associateWith { number.toString(it).uppercase() }
    }
}

object CalculatorMoney {
    fun percentageOf(percent: Double, value: Double): Double = percent / 100.0 * value
    fun whatPercent(part: Double, whole: Double): Double {
        if (whole == 0.0) throw ArithmeticException("Whole is zero")
        return part / whole * 100.0
    }
    fun percentChange(from: Double, to: Double): Double {
        if (from == 0.0) throw ArithmeticException("Original is zero")
        return (to - from) / from * 100.0
    }

    fun discount(price: Double, discountPercent: Double): Pair<Double, Double> {
        val safePercent = discountPercent.coerceIn(0.0, 100.0)
        val savings = price * safePercent / 100.0
        return (price - savings) to savings
    }

    fun profit(cost: Double, sell: Double, quantity: Int = 1): ProfitResult {
        val totalCost = cost * quantity
        val totalSell = sell * quantity
        val profit = totalSell - totalCost
        val margin = if (totalSell == 0.0) 0.0 else profit / totalSell * 100.0
        val markup = if (totalCost == 0.0) 0.0 else profit / totalCost * 100.0
        return ProfitResult(profit, margin, markup, totalCost, totalSell)
    }
}

data class ProfitResult(
    val profit: Double,
    val marginPercent: Double,
    val markupPercent: Double,
    val totalCost: Double,
    val totalSell: Double,
)
