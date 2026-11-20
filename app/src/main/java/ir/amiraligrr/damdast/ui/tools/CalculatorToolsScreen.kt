package ir.amiraligrr.damdast.ui.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.amiraligrr.damdast.R
import ir.amiraligrr.damdast.data.AppLanguage
import ir.amiraligrr.damdast.tools.ToolDefinition
import ir.amiraligrr.damdast.ui.components.DamdastButton
import ir.amiraligrr.damdast.ui.components.LocalSnackbarHostState
import ir.amiraligrr.damdast.ui.components.ToolPage
import ir.amiraligrr.damdast.ui.theme.LocalAppLanguage
import ir.amiraligrr.damdast.util.CalculatorEngine
import ir.amiraligrr.damdast.util.CalculatorMoney
import ir.amiraligrr.damdast.util.ConversionCategory
import ir.amiraligrr.damdast.util.ConversionUnit
import ir.amiraligrr.damdast.util.NumberBaseConverter
import ir.amiraligrr.damdast.util.UnitConverter
import ir.amiraligrr.damdast.util.copyToClipboard
import ir.amiraligrr.damdast.util.formatDecimal
import ir.amiraligrr.damdast.util.formatInt
import ir.amiraligrr.damdast.util.localizeDigits
import ir.amiraligrr.damdast.util.normalizeDigits
import kotlinx.coroutines.launch

@Composable
fun CalculatorToolsScreen(tool: ToolDefinition, onBack: () -> Unit) {
    ToolPage(title = stringResource(tool.nameRes), onBack = onBack) {
        when (tool.id) {
            "calculator" -> CalculatorTool()
            "percentage" -> PercentageTool()
            "discount" -> DiscountTool()
            "profit" -> ProfitTool()
            "unit_converter" -> UnitConverterTool()
            "number_base" -> NumberBaseTool()
        }
    }
}

@Composable
private fun CalculatorTool() {
    val language = LocalAppLanguage.current
    var expression by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf(false) }
    var scientific by rememberSaveable { mutableStateOf(false) }
    var degrees by rememberSaveable { mutableStateOf(true) }
    var history by rememberSaveable { mutableStateOf(listOf<String>()) }

    fun evaluate() {
        if (expression.isBlank()) return
        runCatching {
            val value = CalculatorEngine.evaluate(expression, degrees)
            result = formatDecimal(value, language, maxDecimals = 10)
            error = false
            history = (listOf("${localizeDigits(expression, language)} = $result") + history).take(5)
        }.onFailure {
            result = ""
            error = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = !scientific,
                onClick = { scientific = false },
                label = { Text(stringResource(R.string.calc_standard)) },
            )
            FilterChip(
                selected = scientific,
                onClick = { scientific = true },
                label = { Text(stringResource(R.string.calc_scientific)) },
            )
            if (scientific) {
                FilterChip(
                    selected = degrees,
                    onClick = { degrees = !degrees },
                    label = {
                        Text(
                            stringResource(
                                if (degrees) R.string.calc_degrees else R.string.calc_radians,
                            ),
                        )
                    },
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = expression.ifEmpty { stringResource(R.string.calc_expression_label) },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (expression.isEmpty()) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (error) {
                        stringResource(R.string.calc_invalid_expression)
                    } else {
                        result.ifEmpty { "0" }
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                )
            }
        }
        if (history.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                history.forEach { entry ->
                    Surface(
                        modifier = Modifier.clickable {
                            expression = entry.substringBefore(" = ")
                            result = entry.substringAfter(" = ")
                            error = false
                        },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    ) {
                        Text(
                            text = entry,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        if (scientific) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                listOf(
                    "sin" to "sin(", "cos" to "cos(", "tan" to "tan(",
                    "√" to "sqrt(", "ln" to "ln(", "log" to "log(",
                    "^" to "^", "π" to "π", "e" to "e", "!" to "!",
                ).forEach { (label, value) ->
                    SmallKey(label) { expression += value }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        val rows = listOf(
            listOf("AC" to "clear", "⌫" to "back", "(" to "(", ")" to ")"),
            listOf("7" to "7", "8" to "8", "9" to "9", "÷" to "/"),
            listOf("4" to "4", "5" to "5", "6" to "6", "×" to "*"),
            listOf("1" to "1", "2" to "2", "3" to "3", "−" to "-"),
            listOf("0" to "0", "." to ".", "%" to "%", "+" to "+"),
        )
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { (label, value) ->
                    val danger = label == "AC"
                    val accent = value in setOf("/", "*", "-", "+")
                    KeyButton(
                        label = if (label == "AC") stringResource(R.string.calc_clear_all) else label,
                        modifier = Modifier.weight(1f),
                        accent = accent,
                        danger = danger,
                        onClick = {
                            when (value) {
                                "clear" -> {
                                    expression = ""
                                    result = ""
                                    error = false
                                }

                                "back" -> {
                                    expression = expression.dropLast(1)
                                    error = false
                                }

                                else -> {
                                    expression += value
                                    error = false
                                }
                            }
                        },
                    )
                }
            }
            Spacer(Modifier.height(7.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KeyButton(
                label = "=",
                modifier = Modifier.weight(1f),
                accent = true,
                danger = false,
                primary = true,
                onClick = { evaluate() },
            )
        }
        Spacer(Modifier.height(14.dp))
    }
}

@Composable
private fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    danger: Boolean = false,
    primary: Boolean = false,
    onClick: () -> Unit,
) {
    val background = when {
        primary -> MaterialTheme.colorScheme.primary
        danger -> MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
        accent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)
        else -> MaterialTheme.colorScheme.surface
    }
    val content = when {
        primary -> MaterialTheme.colorScheme.onPrimary
        danger -> MaterialTheme.colorScheme.error
        accent -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(MaterialTheme.shapes.small)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = content,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun SmallKey(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun PercentageTool() {
    val language = LocalAppLanguage.current
    var mode by rememberSaveable { mutableIntStateOf(0) }
    var x by rememberSaveable { mutableStateOf("") }
    var y by rememberSaveable { mutableStateOf("") }
    val xValue = x.normalizeDigits().toDoubleOrNull()
    val yValue = y.normalizeDigits().toDoubleOrNull()
    val calculated = if (xValue != null && yValue != null) {
        runCatching {
            when (mode) {
                0 -> CalculatorMoney.percentageOf(xValue, yValue)
                1 -> CalculatorMoney.whatPercent(xValue, yValue)
                else -> CalculatorMoney.percentChange(xValue, yValue)
            }
        }.getOrNull()
    } else {
        null
    }
    val resultText = if (xValue != null && yValue != null && calculated != null) {
        when (mode) {
            0 -> stringResource(
                R.string.calc_percent_of_result,
                formatDecimal(xValue, language),
                formatDecimal(yValue, language),
                formatDecimal(calculated, language),
            )

            1 -> stringResource(
                R.string.calc_what_percent_result,
                formatDecimal(xValue, language),
                formatDecimal(calculated, language),
                formatDecimal(yValue, language),
            )

            else -> {
                val direction = stringResource(
                    if (calculated >= 0) R.string.calc_percent_increase else R.string.calc_percent_decrease,
                )
                stringResource(
                    R.string.calc_percent_change_result,
                    "${formatDecimal(kotlin.math.abs(calculated), language)} ($direction)",
                )
            }
        }
    } else {
        null
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        ChoiceChips(
            options = listOf(
                stringResource(R.string.calc_percent_of),
                stringResource(R.string.calc_what_percent),
                stringResource(R.string.calc_percent_change),
            ),
            selected = mode,
            onSelect = { mode = it },
        )
        Spacer(Modifier.height(16.dp))
        NumericField(
            value = x,
            onValueChange = { x = it },
            label = stringResource(if (mode == 0) R.string.calc_value_x else R.string.calc_value_x),
        )
        Spacer(Modifier.height(10.dp))
        NumericField(
            value = y,
            onValueChange = { y = it },
            label = stringResource(R.string.calc_value_y),
        )
        Spacer(Modifier.height(16.dp))
        ResultBox(
            title = stringResource(R.string.common_result),
            text = resultText ?: stringResource(R.string.calc_enter_values),
        )
    }
}

@Composable
private fun DiscountTool() {
    val language = LocalAppLanguage.current
    var price by rememberSaveable { mutableStateOf("") }
    var percent by rememberSaveable { mutableStateOf("") }
    val priceValue = price.normalizeDigits().toDoubleOrNull()
    val percentValue = percent.normalizeDigits().toDoubleOrNull()
    val calculated = if (priceValue != null && percentValue != null) {
        CalculatorMoney.discount(priceValue, percentValue)
    } else {
        null
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        NumericField(price, { price = it }, stringResource(R.string.calc_original_price))
        Spacer(Modifier.height(10.dp))
        NumericField(percent, { percent = it }, stringResource(R.string.calc_discount_percent))
        Spacer(Modifier.height(16.dp))
        ResultBox(
            title = stringResource(R.string.common_result),
            text = if (calculated != null) {
                stringResource(
                    R.string.calc_discount_result,
                    formatDecimal(calculated.first, language),
                    formatDecimal(calculated.second, language),
                )
            } else {
                stringResource(R.string.calc_enter_values)
            },
        )
    }
}

@Composable
private fun ProfitTool() {
    val language = LocalAppLanguage.current
    var cost by rememberSaveable { mutableStateOf("") }
    var sell by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("1") }
    val costValue = cost.normalizeDigits().toDoubleOrNull()
    val sellValue = sell.normalizeDigits().toDoubleOrNull()
    val quantityValue = quantity.normalizeDigits().toIntOrNull() ?: 1
    val result = if (costValue != null && sellValue != null) {
        CalculatorMoney.profit(costValue, sellValue, quantityValue.coerceAtLeast(1))
    } else {
        null
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        NumericField(cost, { cost = it }, stringResource(R.string.calc_cost))
        Spacer(Modifier.height(10.dp))
        NumericField(sell, { sell = it }, stringResource(R.string.calc_sell_price))
        Spacer(Modifier.height(10.dp))
        NumericField(
            quantity,
            { quantity = it.filter { char -> char.isDigit() } },
            stringResource(R.string.calc_quantity),
            keyboardType = KeyboardType.Number,
        )
        Spacer(Modifier.height(16.dp))
        ResultBox(
            title = stringResource(R.string.common_result),
            text = if (result != null) {
                stringResource(
                    R.string.calc_profit_result,
                    formatDecimal(result.profit, language),
                    "${formatDecimal(result.marginPercent, language)}%",
                    "${formatDecimal(result.markupPercent, language)}%",
                ) + "\n" +
                    stringResource(R.string.calc_total_cost) + ": " +
                    formatDecimal(result.totalCost, language) + "\n" +
                    stringResource(R.string.calc_total_revenue) + ": " +
                    formatDecimal(result.totalSell, language)
            } else {
                stringResource(R.string.calc_enter_values)
            },
        )
    }
}

@Composable
private fun UnitConverterTool() {
    val language = LocalAppLanguage.current
    val categories = UnitConverter.categories
    var categoryIndex by rememberSaveable { mutableIntStateOf(0) }
    var fromIndex by rememberSaveable { mutableIntStateOf(0) }
    var toIndex by rememberSaveable { mutableIntStateOf(1) }
    var input by rememberSaveable { mutableStateOf("1") }
    val category = categories[categoryIndex]
    val from = category.units[fromIndex.coerceIn(category.units.indices)]
    val to = category.units[toIndex.coerceIn(category.units.indices)]
    val value = input.normalizeDigits().toDoubleOrNull()
    val output = value?.let { UnitConverter.convert(category, from, to, it) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.calc_category),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(6.dp))
        ChoiceChips(
            options = categories.map { categoryLabel(it) },
            selected = categoryIndex,
            onSelect = {
                categoryIndex = it
                fromIndex = 0
                toIndex = 1.coerceAtMost(categories[it].units.lastIndex)
            },
        )
        Spacer(Modifier.height(14.dp))
        NumericField(
            value = input,
            onValueChange = { input = it },
            label = stringResource(R.string.calc_value),
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            UnitSelector(
                label = stringResource(R.string.calc_from_unit),
                units = category.units,
                selected = from,
                onSelect = { fromIndex = category.units.indexOf(it) },
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = {
                    val temp = fromIndex
                    fromIndex = toIndex
                    toIndex = temp
                },
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            ) {
                Icon(
                    Icons.Outlined.SwapVert,
                    contentDescription = stringResource(R.string.calc_swap_units),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            UnitSelector(
                label = stringResource(R.string.calc_to_unit),
                units = category.units,
                selected = to,
                onSelect = { toIndex = category.units.indexOf(it) },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(16.dp))
        ResultBox(
            title = stringResource(R.string.common_result),
            text = if (output != null) {
                "${formatDecimal(output, language)} ${to.symbol}"
            } else {
                stringResource(R.string.calc_enter_values)
            },
        )
    }
}

@Composable
private fun NumberBaseTool() {
    val language = LocalAppLanguage.current
    var input by rememberSaveable { mutableStateOf("") }
    var fromBase by rememberSaveable { mutableIntStateOf(10) }
    val converted = remember(input, fromBase) {
        runCatching { NumberBaseConverter.convert(input, fromBase) }.getOrNull()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        NumericField(
            value = input,
            onValueChange = { input = it },
            label = stringResource(R.string.calc_value),
            keyboardType = KeyboardType.Text,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.calc_base_from),
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(Modifier.height(6.dp))
        ChoiceChips(
            options = NumberBaseConverter.bases.map { it.toString() },
            selected = NumberBaseConverter.bases.indexOf(fromBase),
            onSelect = { fromBase = NumberBaseConverter.bases[it] },
        )
        Spacer(Modifier.height(16.dp))
        if (converted == null) {
            Text(
                text = if (input.isBlank()) {
                    stringResource(R.string.calc_enter_values)
                } else {
                    stringResource(R.string.calc_invalid_base)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (input.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        } else {
            BaseResultRow(stringResource(R.string.calc_binary), converted[2].orEmpty())
            BaseResultRow(stringResource(R.string.calc_octal), converted[8].orEmpty())
            BaseResultRow(stringResource(R.string.calc_decimal), converted[10].orEmpty())
            BaseResultRow(stringResource(R.string.calc_hexadecimal), converted[16].orEmpty())
            BaseResultRow(stringResource(R.string.calc_base32), converted[32].orEmpty())
        }
    }
}

@Composable
private fun BaseResultRow(label: String, value: String) {
    val language = LocalAppLanguage.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(104.dp),
            )
            Text(
                text = localizeDigits(value, language),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun UnitSelector(
    label: String,
    units: List<ConversionUnit>,
    selected: ConversionUnit,
    onSelect: (ConversionUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 9.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = selected.symbol,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Outlined.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.symbol) },
                    onClick = {
                        onSelect(unit)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun categoryLabel(category: ConversionCategory): String = when (category.key) {
    "length" -> stringResource(R.string.unit_length)
    "weight" -> stringResource(R.string.unit_weight)
    "temperature" -> stringResource(R.string.unit_temperature)
    "area" -> stringResource(R.string.unit_area)
    "volume" -> stringResource(R.string.unit_volume)
    "speed" -> stringResource(R.string.unit_speed)
    "time" -> stringResource(R.string.unit_time)
    else -> stringResource(R.string.unit_data)
}

@Composable
private fun NumericField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Decimal,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
    )
}

@Composable
private fun ResultBox(title: String, text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ChoiceChips(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEachIndexed { index, label ->
            FilterChip(
                selected = selected == index,
                onClick = { onSelect(index) },
                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
            )
        }
    }
}
