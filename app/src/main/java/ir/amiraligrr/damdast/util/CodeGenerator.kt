package ir.amiraligrr.damdast.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.common.BitMatrix

object CodeGenerator {
    fun qr(content: String, size: Int = 1024): Bitmap {
        require(content.isNotBlank()) { "Content is empty" }
        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8",
        )
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        return matrixToBitmap(matrix)
    }

    fun barcode(content: String, format: BarcodeFormat, width: Int = 1200, height: Int = 420): Bitmap {
        val hints = mapOf(
            EncodeHintType.MARGIN to 12,
            EncodeHintType.CHARACTER_SET to "UTF-8",
        )
        val matrix = MultiFormatWriter().encode(content, format, width, height, hints)
        return matrixToBitmap(matrix)
    }

    fun wifiPayload(ssid: String, password: String, security: String, hidden: Boolean): String {
        fun escape(value: String): String = value
            .replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace(":", "\\:")
            .replace("\"", "\\\"")
        val type = if (security == "None") "nopass" else security
        return buildString {
            append("WIFI:T:").append(type).append(';')
            append("S:").append(escape(ssid)).append(';')
            if (security != "None") append("P:").append(escape(password)).append(';')
            if (hidden) append("H:true;")
            append(';')
        }
    }

    fun contactPayload(name: String, phone: String, email: String, organization: String): String {
        fun escape(value: String): String = value
            .replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace("\n", "\\n")
        return buildString {
            append("BEGIN:VCARD\n")
            append("VERSION:3.0\n")
            append("FN:").append(escape(name)).append('\n')
            if (phone.isNotBlank()) append("TEL;TYPE=CELL:").append(escape(phone)).append('\n')
            if (email.isNotBlank()) append("EMAIL:").append(escape(email)).append('\n')
            if (organization.isNotBlank()) append("ORG:").append(escape(organization)).append('\n')
            append("END:VCARD")
        }
    }

    private fun matrixToBitmap(matrix: BitMatrix): Bitmap {
        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (matrix.get(x, y)) Color.BLACK else Color.WHITE
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }
}
