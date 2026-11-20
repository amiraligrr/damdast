package ir.amiraligrr.damdast.tools

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.Gradient
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.AddToHomeScreen
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.PhotoSizeSelectLarge
import androidx.compose.material.icons.outlined.Rotate90DegreesCcw
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.FilterVintage
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.RotateRight
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Html
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.ManageSearch
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.SpaceBar
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ir.amiraligrr.damdast.R
import ir.amiraligrr.damdast.ui.theme.Brand
import java.util.Locale

enum class ToolCategory(
    @StringRes val titleRes: Int,
    val icon: ImageVector,
) {
    QR(R.string.category_qr, Icons.Outlined.QrCode2),
    PDF(R.string.category_pdf, Icons.Outlined.PictureAsPdf),
    IMAGE(R.string.category_image, Icons.Outlined.Image),
    TEXT(R.string.category_text, Icons.Outlined.TextFields),
    CALCULATOR(R.string.category_calculator, Icons.Outlined.Calculate),
    FILE(R.string.category_file, Icons.Outlined.FolderOpen),
    DEVELOPER(R.string.category_developer, Icons.Outlined.Terminal),
    DATETIME(R.string.category_datetime, Icons.Outlined.Schedule),
    COLOR(R.string.category_color, Icons.Outlined.Palette),
    EVERYDAY(R.string.category_everyday, Icons.Outlined.Checklist),
    ;

    val accent: Color
        get() = Brand.categoryAccents[ordinal % Brand.categoryAccents.size]
}

data class ToolDefinition(
    val id: String,
    val category: ToolCategory,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val icon: ImageVector,
    val keywords: List<String> = emptyList(),
    val premium: Boolean = false,
    val recommended: Boolean = false,
) {
    val accent: Color get() = category.accent
}

object ToolCatalog {
    private val tools = listOf(
        ToolDefinition(
            id = "calculator",
            category = ToolCategory.CALCULATOR,
            nameRes = R.string.tool_calculator,
            descriptionRes = R.string.tool_calculator_desc,
            icon = Icons.Outlined.Calculate,
            keywords = listOf("calculator", "scientific", "math", "sin", "cos", "ماشین", "حساب", "علمی", "ریاضی"),
            recommended = true,
        ),
        ToolDefinition(
            id = "percentage",
            category = ToolCategory.CALCULATOR,
            nameRes = R.string.tool_percentage,
            descriptionRes = R.string.tool_percentage_desc,
            icon = Icons.Outlined.Percent,
            keywords = listOf("percent", "percentage", "change", "درصد", "تغییر"),
        ),
        ToolDefinition(
            id = "discount",
            category = ToolCategory.CALCULATOR,
            nameRes = R.string.tool_discount,
            descriptionRes = R.string.tool_discount_desc,
            icon = Icons.Outlined.LocalOffer,
            keywords = listOf("discount", "sale", "price", "off", "تخفیف", "قیمت", "حراج"),
        ),
        ToolDefinition(
            id = "profit",
            category = ToolCategory.CALCULATOR,
            nameRes = R.string.tool_profit,
            descriptionRes = R.string.tool_profit_desc,
            icon = Icons.Outlined.TrendingUp,
            keywords = listOf("profit", "margin", "markup", "سود", "حاشیه", "فروش"),
        ),
        ToolDefinition(
            id = "unit_converter",
            category = ToolCategory.CALCULATOR,
            nameRes = R.string.tool_unit_converter,
            descriptionRes = R.string.tool_unit_converter_desc,
            icon = Icons.Outlined.Straighten,
            keywords = listOf("unit", "convert", "length", "weight", "temperature", "مبدل", "واحد", "طول", "وزن", "دما"),
            recommended = true,
        ),
        ToolDefinition(
            id = "number_base",
            category = ToolCategory.CALCULATOR,
            nameRes = R.string.tool_number_base,
            descriptionRes = R.string.tool_number_base_desc,
            icon = Icons.Outlined.Numbers,
            keywords = listOf("base", "binary", "hex", "octal", "decimal", "پایه", "دودویی", "هگز"),
        ),
        ToolDefinition(
            id = "json_formatter",
            category = ToolCategory.DEVELOPER,
            nameRes = R.string.tool_json_formatter,
            descriptionRes = R.string.tool_json_formatter_desc,
            icon = Icons.Outlined.DataObject,
            keywords = listOf("json", "format", "pretty", "beautify", "قالب", "مرتب"),
            recommended = true,
        ),
        ToolDefinition(
            id = "json_validator",
            category = ToolCategory.DEVELOPER,
            nameRes = R.string.tool_json_validator,
            descriptionRes = R.string.tool_json_validator_desc,
            icon = Icons.Outlined.DataObject,
            keywords = listOf("json", "validate", "check", "error", "اعتبار", "بررسی", "خطا"),
        ),
        ToolDefinition(
            id = "json_minifier",
            category = ToolCategory.DEVELOPER,
            nameRes = R.string.tool_json_minifier,
            descriptionRes = R.string.tool_json_minifier_desc,
            icon = Icons.Outlined.DataObject,
            keywords = listOf("json", "minify", "compact", "فشرده", "کوتاه"),
        ),
        ToolDefinition(
            id = "xml_formatter",
            category = ToolCategory.DEVELOPER,
            nameRes = R.string.tool_xml_formatter,
            descriptionRes = R.string.tool_xml_formatter_desc,
            icon = Icons.Outlined.Code,
            keywords = listOf("xml", "format", "indent", "قالب", "تگ"),
        ),
        ToolDefinition(
            id = "html_formatter",
            category = ToolCategory.DEVELOPER,
            nameRes = R.string.tool_html_formatter,
            descriptionRes = R.string.tool_html_formatter_desc,
            icon = Icons.Outlined.Html,
            keywords = listOf("html", "format", "markup", "قالب", "صفحه"),
        ),
        ToolDefinition(
            id = "css_formatter",
            category = ToolCategory.DEVELOPER,
            nameRes = R.string.tool_css_formatter,
            descriptionRes = R.string.tool_css_formatter_desc,
            icon = Icons.Outlined.Style,
            keywords = listOf("css", "style", "format", "قالب", "استایل"),
        ),
        ToolDefinition(
            id = "js_formatter",
            category = ToolCategory.DEVELOPER,
            nameRes = R.string.tool_js_formatter,
            descriptionRes = R.string.tool_js_formatter_desc,
            icon = Icons.Outlined.Terminal,
            keywords = listOf("javascript", "js", "format", "قالب", "جاوااسکریپت"),
        ),
        ToolDefinition(
            id = "uuid_generator",
            category = ToolCategory.DEVELOPER,
            nameRes = R.string.tool_uuid,
            descriptionRes = R.string.tool_uuid_desc,
            icon = Icons.Outlined.Fingerprint,
            keywords = listOf("uuid", "guid", "random", "شناسه", "تصادفی"),
            recommended = true,
        ),
        ToolDefinition(
            id = "hash_calculator",
            category = ToolCategory.DEVELOPER,
            nameRes = R.string.tool_hash,
            descriptionRes = R.string.tool_hash_desc,
            icon = Icons.Outlined.Tag,
            keywords = listOf("hash", "md5", "sha", "checksum", "هش", "چک‌سام"),
        ),
        ToolDefinition(
            id = "timestamp_converter",
            category = ToolCategory.DEVELOPER,
            nameRes = R.string.tool_timestamp,
            descriptionRes = R.string.tool_timestamp_desc,
            icon = Icons.Outlined.Schedule,
            keywords = listOf("timestamp", "unix", "time", "date", "زمان", "تاریخ"),
            recommended = true,
        ),
        ToolDefinition(
            id = "regex_tester",
            category = ToolCategory.DEVELOPER,
            nameRes = R.string.tool_regex,
            descriptionRes = R.string.tool_regex_desc,
            icon = Icons.Outlined.ManageSearch,
            keywords = listOf("regex", "regular", "pattern", "match", "عبارت", "باقاعده"),
        ),
        ToolDefinition(
            id = "url_parser",
            category = ToolCategory.DEVELOPER,
            nameRes = R.string.tool_url_parser,
            descriptionRes = R.string.tool_url_parser_desc,
            icon = Icons.Outlined.Link,
            keywords = listOf("url", "uri", "parse", "query", "لینک", "تحلیل"),
        ),
        ToolDefinition(
            id = "jwt_decoder",
            category = ToolCategory.DEVELOPER,
            nameRes = R.string.tool_jwt,
            descriptionRes = R.string.tool_jwt_desc,
            icon = Icons.Outlined.Key,
            keywords = listOf("jwt", "token", "decode", "bearer", "توکن", "رمز"),
        ),
        ToolDefinition(
            id = "unix_permissions",
            category = ToolCategory.DEVELOPER,
            nameRes = R.string.tool_unix_permissions,
            descriptionRes = R.string.tool_unix_permissions_desc,
            icon = Icons.Outlined.Lock,
            keywords = listOf("unix", "chmod", "permission", "rwx", "octal", "مجوز", "دسترسی"),
        ),
        ToolDefinition(
            id = "dev_color_converter",
            category = ToolCategory.DEVELOPER,
            nameRes = R.string.tool_color_converter,
            descriptionRes = R.string.tool_color_converter_desc,
            icon = Icons.Outlined.Palette,
            keywords = listOf("color", "hex", "rgb", "hsl", "hsv", "رنگ", "تبدیل"),
        ),
        ToolDefinition(
            id = "stopwatch",
            category = ToolCategory.DATETIME,
            nameRes = R.string.tool_stopwatch,
            descriptionRes = R.string.tool_stopwatch_desc,
            icon = Icons.Outlined.Timer,
            keywords = listOf("stopwatch", "timer", "lap", "elapsed", "کرنومتر", "زمان"),
            recommended = true,
        ),
        ToolDefinition(
            id = "timer",
            category = ToolCategory.DATETIME,
            nameRes = R.string.tool_timer,
            descriptionRes = R.string.tool_timer_desc,
            icon = Icons.Outlined.HourglassBottom,
            keywords = listOf("timer", "countdown", "alarm", "تایمر", "شمارش"),
        ),
        ToolDefinition(
            id = "countdown",
            category = ToolCategory.DATETIME,
            nameRes = R.string.tool_countdown,
            descriptionRes = R.string.tool_countdown_desc,
            icon = Icons.Outlined.HourglassEmpty,
            keywords = listOf("countdown", "target", "date", "شمارش", "معکوس", "تاریخ"),
        ),
        ToolDefinition(
            id = "world_clock",
            category = ToolCategory.DATETIME,
            nameRes = R.string.tool_world_clock,
            descriptionRes = R.string.tool_world_clock_desc,
            icon = Icons.Outlined.Public,
            keywords = listOf("world", "clock", "timezone", "city", "جهان", "ساعت", "شهر"),
        ),
        ToolDefinition(
            id = "date_calculator",
            category = ToolCategory.DATETIME,
            nameRes = R.string.tool_date_calculator,
            descriptionRes = R.string.tool_date_calculator_desc,
            icon = Icons.Outlined.Event,
            keywords = listOf("date", "difference", "add", "subtract", "تاریخ", "تفاوت", "روز"),
            recommended = true,
        ),
        ToolDefinition(
            id = "jalali_converter",
            category = ToolCategory.DATETIME,
            nameRes = R.string.tool_jalali,
            descriptionRes = R.string.tool_jalali_desc,
            icon = Icons.Outlined.CalendarMonth,
            keywords = listOf("jalali", "persian", "gregorian", "calendar", "شمسی", "میلادی", "تقویم"),
            recommended = true,
        ),
        ToolDefinition(
            id = "color_picker",
            category = ToolCategory.COLOR,
            nameRes = R.string.tool_color_picker,
            descriptionRes = R.string.tool_color_picker_desc,
            icon = Icons.Outlined.Colorize,
            keywords = listOf("color", "picker", "hue", "hex", "رنگ", "انتخاب"),
            recommended = true,
        ),
        ToolDefinition(
            id = "gradient_generator",
            category = ToolCategory.COLOR,
            nameRes = R.string.tool_gradient,
            descriptionRes = R.string.tool_gradient_desc,
            icon = Icons.Outlined.Gradient,
            keywords = listOf("gradient", "css", "design", "گرادیان", "رنگ"),
        ),
        ToolDefinition(
            id = "palette_generator",
            category = ToolCategory.COLOR,
            nameRes = R.string.tool_palette,
            descriptionRes = R.string.tool_palette_desc,
            icon = Icons.Outlined.AutoAwesome,
            keywords = listOf("palette", "harmony", "colors", "پالت", "هماهنگ"),
        ),
        ToolDefinition(
            id = "image_colors",
            category = ToolCategory.COLOR,
            nameRes = R.string.tool_image_colors,
            descriptionRes = R.string.tool_image_colors_desc,
            icon = Icons.Outlined.ImageSearch,
            keywords = listOf("image", "color", "extract", "palette", "تصویر", "رنگ", "استخراج"),
        ),
        ToolDefinition(
            id = "font_preview",
            category = ToolCategory.COLOR,
            nameRes = R.string.tool_font_preview,
            descriptionRes = R.string.tool_font_preview_desc,
            icon = Icons.Outlined.TextFields,
            keywords = listOf("font", "typography", "preview", "فونت", "متن"),
        ),
        ToolDefinition(
            id = "password_generator",
            category = ToolCategory.EVERYDAY,
            nameRes = R.string.tool_password,
            descriptionRes = R.string.tool_password_desc,
            icon = Icons.Outlined.Password,
            keywords = listOf("password", "random", "secure", "generator", "رمز", "عبور", "تصادفی"),
            recommended = true,
        ),
        ToolDefinition(
            id = "random_number",
            category = ToolCategory.EVERYDAY,
            nameRes = R.string.tool_random_number,
            descriptionRes = R.string.tool_random_number_desc,
            icon = Icons.Outlined.Numbers,
            keywords = listOf("random", "number", "dice", "تصادفی", "عدد"),
        ),
        ToolDefinition(
            id = "random_selector",
            category = ToolCategory.EVERYDAY,
            nameRes = R.string.tool_random_selector,
            descriptionRes = R.string.tool_random_selector_desc,
            icon = Icons.Outlined.Shuffle,
            keywords = listOf("random", "pick", "selector", "list", "انتخاب", "تصادفی"),
        ),
        ToolDefinition(
            id = "dice",
            category = ToolCategory.EVERYDAY,
            nameRes = R.string.tool_dice,
            descriptionRes = R.string.tool_dice_desc,
            icon = Icons.Outlined.Casino,
            keywords = listOf("dice", "roll", "d4", "d6", "d20", "تاس"),
        ),
        ToolDefinition(
            id = "coin_flip",
            category = ToolCategory.EVERYDAY,
            nameRes = R.string.tool_coin,
            descriptionRes = R.string.tool_coin_desc,
            icon = Icons.Outlined.MonetizationOn,
            keywords = listOf("coin", "flip", "heads", "tails", "شیر", "خط", "سکه"),
        ),
        ToolDefinition(
            id = "notes",
            category = ToolCategory.EVERYDAY,
            nameRes = R.string.tool_notes,
            descriptionRes = R.string.tool_notes_desc,
            icon = Icons.Outlined.Notes,
            keywords = listOf("notes", "memo", "text", "یادداشت", "نوشته"),
            recommended = true,
        ),
        ToolDefinition(
            id = "tasks",
            category = ToolCategory.EVERYDAY,
            nameRes = R.string.tool_tasks,
            descriptionRes = R.string.tool_tasks_desc,
            icon = Icons.Outlined.Checklist,
            keywords = listOf("task", "todo", "checklist", "کار", "فهرست"),
        ),
        ToolDefinition(
            id = "shortcuts",
            category = ToolCategory.EVERYDAY,
            nameRes = R.string.tool_shortcuts,
            descriptionRes = R.string.tool_shortcuts_desc,
            icon = Icons.Outlined.AddToHomeScreen,
            keywords = listOf("shortcut", "home", "pin", "میان‌بر", "صفحه اصلی"),
        ),
        ToolDefinition(
            id = "qr_scanner",
            category = ToolCategory.QR,
            nameRes = R.string.tool_qr_scanner,
            descriptionRes = R.string.tool_qr_scanner_desc,
            icon = Icons.Outlined.QrCode2,
            keywords = listOf("qr", "scan", "camera", "barcode", "اسکن", "دوربین", "کد"),
            recommended = true,
        ),
        ToolDefinition(
            id = "scan_gallery",
            category = ToolCategory.QR,
            nameRes = R.string.tool_scan_gallery,
            descriptionRes = R.string.tool_scan_gallery_desc,
            icon = Icons.Outlined.ImageSearch,
            keywords = listOf("scan", "gallery", "image", "photo", "گالری", "تصویر", "اسکن"),
        ),
        ToolDefinition(
            id = "qr_generator",
            category = ToolCategory.QR,
            nameRes = R.string.tool_qr_generator,
            descriptionRes = R.string.tool_qr_generator_desc,
            icon = Icons.Outlined.QrCode2,
            keywords = listOf("qr", "generate", "text", "url", "link", "ساخت", "کد", "لینک"),
            recommended = true,
        ),
        ToolDefinition(
            id = "wifi_qr",
            category = ToolCategory.QR,
            nameRes = R.string.tool_wifi_qr,
            descriptionRes = R.string.tool_wifi_qr_desc,
            icon = Icons.Outlined.Wifi,
            keywords = listOf("wifi", "qr", "network", "password", "وای‌فای", "شبکه"),
        ),
        ToolDefinition(
            id = "contact_qr",
            category = ToolCategory.QR,
            nameRes = R.string.tool_contact_qr,
            descriptionRes = R.string.tool_contact_qr_desc,
            icon = Icons.Outlined.Person,
            keywords = listOf("contact", "vcard", "qr", "phone", "مخاطب", "کارت"),
        ),
        ToolDefinition(
            id = "barcode_scanner",
            category = ToolCategory.QR,
            nameRes = R.string.tool_barcode_scanner,
            descriptionRes = R.string.tool_barcode_scanner_desc,
            icon = Icons.Outlined.QrCode2,
            keywords = listOf("barcode", "ean", "upc", "scan", "بارکد", "اسکن"),
        ),
        ToolDefinition(
            id = "barcode_generator",
            category = ToolCategory.QR,
            nameRes = R.string.tool_barcode_generator,
            descriptionRes = R.string.tool_barcode_generator_desc,
            icon = Icons.Outlined.QrCode2,
            keywords = listOf("barcode", "generate", "code128", "ean13", "بارکد", "ساخت"),
        ),
        ToolDefinition(
            id = "scan_history",
            category = ToolCategory.QR,
            nameRes = R.string.tool_scan_history,
            descriptionRes = R.string.tool_scan_history_desc,
            icon = Icons.Outlined.History,
            keywords = listOf("scan", "history", "qr", "barcode", "تاریخچه", "اسکن"),
        ),
        ToolDefinition(
            id = "image_viewer",
            category = ToolCategory.IMAGE,
            nameRes = R.string.tool_image_viewer,
            descriptionRes = R.string.tool_image_viewer_desc,
            icon = Icons.Outlined.Image,
            keywords = listOf("image", "viewer", "zoom", "photo", "تصویر", "نمایش"),
            recommended = true,
        ),
        ToolDefinition(
            id = "image_crop",
            category = ToolCategory.IMAGE,
            nameRes = R.string.tool_image_crop,
            descriptionRes = R.string.tool_image_crop_desc,
            icon = Icons.Outlined.Crop,
            keywords = listOf("crop", "trim", "image", "برش", "تصویر"),
        ),
        ToolDefinition(
            id = "image_resize",
            category = ToolCategory.IMAGE,
            nameRes = R.string.tool_image_resize,
            descriptionRes = R.string.tool_image_resize_desc,
            icon = Icons.Outlined.PhotoSizeSelectLarge,
            keywords = listOf("resize", "scale", "dimensions", "تغییر", "اندازه"),
        ),
        ToolDefinition(
            id = "image_transform",
            category = ToolCategory.IMAGE,
            nameRes = R.string.tool_image_transform,
            descriptionRes = R.string.tool_image_transform_desc,
            icon = Icons.Outlined.Rotate90DegreesCcw,
            keywords = listOf("rotate", "flip", "mirror", "چرخش", "آینه"),
        ),
        ToolDefinition(
            id = "image_adjust",
            category = ToolCategory.IMAGE,
            nameRes = R.string.tool_image_adjust,
            descriptionRes = R.string.tool_image_adjust_desc,
            icon = Icons.Outlined.Tune,
            keywords = listOf("brightness", "contrast", "saturation", "روشنایی", "کنتراست"),
        ),
        ToolDefinition(
            id = "image_filters",
            category = ToolCategory.IMAGE,
            nameRes = R.string.tool_image_filters,
            descriptionRes = R.string.tool_image_filters_desc,
            icon = Icons.Outlined.FilterVintage,
            keywords = listOf("filter", "sepia", "mono", "فیلتر", "تصویر"),
        ),
        ToolDefinition(
            id = "image_text",
            category = ToolCategory.IMAGE,
            nameRes = R.string.tool_image_text,
            descriptionRes = R.string.tool_image_text_desc,
            icon = Icons.Outlined.TextFields,
            keywords = listOf("text", "caption", "shape", "متن", "شکل"),
        ),
        ToolDefinition(
            id = "image_compress",
            category = ToolCategory.IMAGE,
            nameRes = R.string.tool_image_compress,
            descriptionRes = R.string.tool_image_compress_desc,
            icon = Icons.Outlined.Compress,
            keywords = listOf("compress", "jpeg", "quality", "فشرده", "حجم"),
        ),
        ToolDefinition(
            id = "image_convert",
            category = ToolCategory.IMAGE,
            nameRes = R.string.tool_image_convert,
            descriptionRes = R.string.tool_image_convert_desc,
            icon = Icons.Outlined.SwapHoriz,
            keywords = listOf("convert", "png", "jpeg", "webp", "تبدیل"),
        ),
        ToolDefinition(
            id = "image_exif",
            category = ToolCategory.IMAGE,
            nameRes = R.string.tool_image_exif,
            descriptionRes = R.string.tool_image_exif_desc,
            icon = Icons.Outlined.Info,
            keywords = listOf("exif", "metadata", "camera", "متادیتا", "دوربین"),
        ),
        ToolDefinition(
            id = "image_collage",
            category = ToolCategory.IMAGE,
            nameRes = R.string.tool_image_collage,
            descriptionRes = R.string.tool_image_collage_desc,
            icon = Icons.Outlined.GridView,
            keywords = listOf("collage", "grid", "combine", "کلاژ", "ترکیب"),
            premium = true,
        ),
        ToolDefinition(
            id = "pdf_viewer",
            category = ToolCategory.PDF,
            nameRes = R.string.tool_pdf_viewer,
            descriptionRes = R.string.tool_pdf_viewer_desc,
            icon = Icons.Outlined.PictureAsPdf,
            keywords = listOf("pdf", "viewer", "read", "search", "bookmark", "پی‌دی‌اف", "خواندن"),
            recommended = true,
        ),
        ToolDefinition(
            id = "pdf_merge",
            category = ToolCategory.PDF,
            nameRes = R.string.tool_pdf_merge,
            descriptionRes = R.string.tool_pdf_merge_desc,
            icon = Icons.Outlined.Add,
            keywords = listOf("pdf", "merge", "combine", "ادغام", "ترکیب"),
            recommended = true,
        ),
        ToolDefinition(
            id = "pdf_extract",
            category = ToolCategory.PDF,
            nameRes = R.string.tool_pdf_extract,
            descriptionRes = R.string.tool_pdf_extract_desc,
            icon = Icons.Outlined.ContentCut,
            keywords = listOf("pdf", "extract", "pages", "range", "استخراج", "صفحه"),
        ),
        ToolDefinition(
            id = "pdf_rotate",
            category = ToolCategory.PDF,
            nameRes = R.string.tool_pdf_rotate,
            descriptionRes = R.string.tool_pdf_rotate_desc,
            icon = Icons.Outlined.RotateRight,
            keywords = listOf("pdf", "rotate", "angle", "چرخش", "زاویه"),
        ),
        ToolDefinition(
            id = "pdf_watermark",
            category = ToolCategory.PDF,
            nameRes = R.string.tool_pdf_watermark,
            descriptionRes = R.string.tool_pdf_watermark_desc,
            icon = Icons.Outlined.Title,
            keywords = listOf("pdf", "watermark", "stamp", "واترمارک", "مهر"),
            premium = true,
        ),
        ToolDefinition(
            id = "pdf_numbers",
            category = ToolCategory.PDF,
            nameRes = R.string.tool_pdf_numbers,
            descriptionRes = R.string.tool_pdf_numbers_desc,
            icon = Icons.Outlined.Numbers,
            keywords = listOf("pdf", "page", "number", "شماره", "صفحه"),
        ),
        ToolDefinition(
            id = "images_to_pdf",
            category = ToolCategory.PDF,
            nameRes = R.string.tool_images_pdf,
            descriptionRes = R.string.tool_images_pdf_desc,
            icon = Icons.Outlined.Image,
            keywords = listOf("image", "pdf", "convert", "تصویر", "تبدیل"),
        ),
        ToolDefinition(
            id = "pdf_to_images",
            category = ToolCategory.PDF,
            nameRes = R.string.tool_pdf_images,
            descriptionRes = R.string.tool_pdf_images_desc,
            icon = Icons.Outlined.PhotoLibrary,
            keywords = listOf("pdf", "images", "png", "render", "تصویر", "استخراج"),
        ),
        ToolDefinition(
            id = "txt_viewer",
            category = ToolCategory.FILE,
            nameRes = R.string.tool_txt_viewer,
            descriptionRes = R.string.tool_txt_viewer_desc,
            icon = Icons.Outlined.Description,
            keywords = listOf("txt", "text", "viewer", "متن", "فایل"),
        ),
        ToolDefinition(
            id = "csv_viewer",
            category = ToolCategory.FILE,
            nameRes = R.string.tool_csv_viewer,
            descriptionRes = R.string.tool_csv_viewer_desc,
            icon = Icons.Outlined.TableChart,
            keywords = listOf("csv", "table", "spreadsheet", "جدول", "فایل"),
        ),
        ToolDefinition(
            id = "json_viewer",
            category = ToolCategory.FILE,
            nameRes = R.string.tool_json_viewer,
            descriptionRes = R.string.tool_json_viewer_desc,
            icon = Icons.Outlined.DataObject,
            keywords = listOf("json", "viewer", "file", "فایل", "نمایش"),
        ),
        ToolDefinition(
            id = "xml_viewer",
            category = ToolCategory.FILE,
            nameRes = R.string.tool_xml_viewer,
            descriptionRes = R.string.tool_xml_viewer_desc,
            icon = Icons.Outlined.Code,
            keywords = listOf("xml", "viewer", "file", "فایل", "نمایش"),
        ),
        ToolDefinition(
            id = "markdown_viewer",
            category = ToolCategory.FILE,
            nameRes = R.string.tool_markdown_viewer,
            descriptionRes = R.string.tool_markdown_viewer_desc,
            icon = Icons.Outlined.Article,
            keywords = listOf("markdown", "md", "viewer", "متن", "مارک‌داون"),
        ),
        ToolDefinition(
            id = "file_info",
            category = ToolCategory.FILE,
            nameRes = R.string.tool_file_info,
            descriptionRes = R.string.tool_file_info_desc,
            icon = Icons.Outlined.Info,
            keywords = listOf("file", "info", "size", "type", "فایل", "اطلاعات", "حجم"),
        ),
        ToolDefinition(
            id = "zip_create",
            category = ToolCategory.FILE,
            nameRes = R.string.tool_zip_create,
            descriptionRes = R.string.tool_zip_create_desc,
            icon = Icons.Outlined.FolderZip,
            keywords = listOf("zip", "create", "archive", "فشرده", "آرشیو"),
        ),
        ToolDefinition(
            id = "zip_extract",
            category = ToolCategory.FILE,
            nameRes = R.string.tool_zip_extract,
            descriptionRes = R.string.tool_zip_extract_desc,
            icon = Icons.Outlined.Unarchive,
            keywords = listOf("zip", "extract", "unzip", "استخراج", "آرشیو"),
        ),
        ToolDefinition(
            id = "text_counter",
            category = ToolCategory.TEXT,
            nameRes = R.string.tool_text_counter,
            descriptionRes = R.string.tool_text_counter_desc,
            icon = Icons.Outlined.Numbers,
            keywords = listOf("word", "character", "count", "counter", "reading", "کلمه", "کاراکتر", "شمارش", "متن"),
            recommended = true,
        ),
        ToolDefinition(
            id = "case_converter",
            category = ToolCategory.TEXT,
            nameRes = R.string.tool_case_converter,
            descriptionRes = R.string.tool_case_converter_desc,
            icon = Icons.Outlined.TextFields,
            keywords = listOf("uppercase", "lowercase", "title", "sentence", "case", "حروف", "بزرگ", "کوچک"),
        ),
        ToolDefinition(
            id = "remove_duplicates",
            category = ToolCategory.TEXT,
            nameRes = R.string.tool_remove_duplicates,
            descriptionRes = R.string.tool_remove_duplicates_desc,
            icon = Icons.Outlined.Layers,
            keywords = listOf("duplicate", "unique", "lines", "تکراری", "یکتا", "خط"),
        ),
        ToolDefinition(
            id = "sort_lines",
            category = ToolCategory.TEXT,
            nameRes = R.string.tool_sort_lines,
            descriptionRes = R.string.tool_sort_lines_desc,
            icon = Icons.Outlined.Sort,
            keywords = listOf("sort", "order", "lines", "alphabetical", "مرتب", "خط", "الفبا"),
        ),
        ToolDefinition(
            id = "reverse_text",
            category = ToolCategory.TEXT,
            nameRes = R.string.tool_reverse_text,
            descriptionRes = R.string.tool_reverse_text_desc,
            icon = Icons.Outlined.SwapHoriz,
            keywords = listOf("reverse", "flip", "mirror", "معکوس", "برعکس"),
        ),
        ToolDefinition(
            id = "whitespace",
            category = ToolCategory.TEXT,
            nameRes = R.string.tool_whitespace,
            descriptionRes = R.string.tool_whitespace_desc,
            icon = Icons.Outlined.SpaceBar,
            keywords = listOf("space", "whitespace", "trim", "clean", "فاصله", "تمیز", "خالی"),
        ),
        ToolDefinition(
            id = "base64",
            category = ToolCategory.TEXT,
            nameRes = R.string.tool_base64,
            descriptionRes = R.string.tool_base64_desc,
            icon = Icons.Outlined.Code,
            keywords = listOf("base64", "encode", "decode", "code", "کد", "رمز"),
            recommended = true,
        ),
        ToolDefinition(
            id = "url_encoder",
            category = ToolCategory.TEXT,
            nameRes = R.string.tool_url_encoder,
            descriptionRes = R.string.tool_url_encoder_desc,
            icon = Icons.Outlined.Link,
            keywords = listOf("url", "uri", "encode", "decode", "percent", "لینک", "آدرس"),
        ),
        ToolDefinition(
            id = "html_entities",
            category = ToolCategory.TEXT,
            nameRes = R.string.tool_html_entities,
            descriptionRes = R.string.tool_html_entities_desc,
            icon = Icons.Outlined.Html,
            keywords = listOf("html", "entity", "escape", "amp", "موجودیت", "اچ‌تی‌ام‌ال"),
        ),
        ToolDefinition(
            id = "text_compare",
            category = ToolCategory.TEXT,
            nameRes = R.string.tool_compare,
            descriptionRes = R.string.tool_compare_desc,
            icon = Icons.Outlined.CompareArrows,
            keywords = listOf("compare", "diff", "difference", "مقایسه", "تفاوت"),
        ),
        ToolDefinition(
            id = "text_extract",
            category = ToolCategory.TEXT,
            nameRes = R.string.tool_extract,
            descriptionRes = R.string.tool_extract_desc,
            icon = Icons.Outlined.ManageSearch,
            keywords = listOf("extract", "url", "email", "number", "regex", "استخراج", "لینک", "ایمیل", "عدد"),
        ),
        ToolDefinition(
            id = "lorem_ipsum",
            category = ToolCategory.TEXT,
            nameRes = R.string.tool_lorem,
            descriptionRes = R.string.tool_lorem_desc,
            icon = Icons.Outlined.Notes,
            keywords = listOf("lorem", "ipsum", "placeholder", "dummy", "متن", "ساختگی", "نمونه"),
        ),
    )

    val all: List<ToolDefinition> = tools
    val byId: Map<String, ToolDefinition> = tools.associateBy { it.id }
    val recommended: List<ToolDefinition> = tools.filter { it.recommended }

    fun byCategory(category: ToolCategory): List<ToolDefinition> =
        tools.filter { it.category == category }

    fun categoriesWithTools(): List<ToolCategory> =
        ToolCategory.entries.filter { category -> tools.any { it.category == category } }

    fun search(context: Context, query: String): List<ToolDefinition> {
        val normalized = query.trim().lowercase(Locale.ROOT)
        if (normalized.isEmpty()) return emptyList()
        val terms = normalized.split(Regex("\\s+")).filter { it.isNotBlank() }
        return tools.filter { tool ->
            val haystack = buildString {
                append(context.getString(tool.nameRes).lowercase(Locale.ROOT)).append(' ')
                append(context.getString(tool.descriptionRes).lowercase(Locale.ROOT)).append(' ')
                append(context.getString(tool.category.titleRes).lowercase(Locale.ROOT)).append(' ')
                append(tool.keywords.joinToString(" ").lowercase(Locale.ROOT))
            }
            terms.all { term -> haystack.contains(term) }
        }
    }

    fun title(context: Context, tool: ToolDefinition): String = context.getString(tool.nameRes)

    fun description(context: Context, tool: ToolDefinition): String =
        context.getString(tool.descriptionRes)

    fun categoryTitle(context: Context, category: ToolCategory): String =
        context.getString(category.titleRes)
}
