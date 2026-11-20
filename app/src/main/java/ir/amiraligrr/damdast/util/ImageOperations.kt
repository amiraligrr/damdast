package ir.amiraligrr.damdast.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import kotlin.math.ceil

object ImageOperations {
    fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix().apply {
            postScale(if (horizontal) -1f else 1f, if (vertical) -1f else 1f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun crop(bitmap: Bitmap, left: Float, top: Float, right: Float, bottom: Float): Bitmap {
        val x = (bitmap.width * left.coerceIn(0f, 0.9f)).toInt()
        val y = (bitmap.height * top.coerceIn(0f, 0.9f)).toInt()
        val w = (bitmap.width * (1f - left - right)).toInt().coerceAtLeast(1)
        val h = (bitmap.height * (1f - top - bottom)).toInt().coerceAtLeast(1)
        return Bitmap.createBitmap(bitmap, x.coerceAtMost(bitmap.width - 1), y.coerceAtMost(bitmap.height - 1), w.coerceAtMost(bitmap.width - x), h.coerceAtMost(bitmap.height - y))
    }

    fun resize(bitmap: Bitmap, width: Int, height: Int): Bitmap =
        Bitmap.createScaledBitmap(bitmap, width, height, true)

    fun adjust(bitmap: Bitmap, brightness: Float, contrast: Float, saturation: Float): Bitmap {
        val matrix = ColorMatrix().apply { setSaturation(saturation) }
        val contrastScale = (contrast + 100f) / 100f
        val brightnessOffset = brightness * 2.55f
        val adjustment = ColorMatrix(
            floatArrayOf(
                contrastScale, 0f, 0f, 0f, brightnessOffset,
                0f, contrastScale, 0f, 0f, brightnessOffset,
                0f, 0f, contrastScale, 0f, brightnessOffset,
                0f, 0f, 0f, 1f, 0f,
            ),
        )
        matrix.postConcat(adjustment)
        return applyColorMatrix(bitmap, matrix)
    }

    fun filter(bitmap: Bitmap, index: Int): Bitmap {
        val matrix = when (index) {
            0 -> return bitmap
            1 -> ColorMatrix().apply { setSaturation(0f) }
            2 -> ColorMatrix(
                floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f,
                ),
            )

            3 -> ColorMatrix(
                floatArrayOf(
                    1.08f, 0f, 0f, 0f, 12f,
                    0f, 1.02f, 0f, 0f, 2f,
                    0f, 0f, 0.90f, 0f, -8f,
                    0f, 0f, 0f, 1f, 0f,
                ),
            )

            4 -> ColorMatrix(
                floatArrayOf(
                    0.92f, 0f, 0f, 0f, -8f,
                    0f, 1f, 0f, 0f, 2f,
                    0f, 0f, 1.12f, 0f, 12f,
                    0f, 0f, 0f, 1f, 0f,
                ),
            )

            5 -> ColorMatrix(
                floatArrayOf(
                    -1f, 0f, 0f, 0f, 255f,
                    0f, -1f, 0f, 0f, 255f,
                    0f, 0f, -1f, 0f, 255f,
                    0f, 0f, 0f, 1f, 0f,
                ),
            )

            else -> ColorMatrix(
                floatArrayOf(
                    0.62f, 0.32f, 0.12f, 0f, 22f,
                    0.22f, 0.58f, 0.18f, 0f, 12f,
                    0.12f, 0.24f, 0.52f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f,
                ),
            )
        }
        return applyColorMatrix(bitmap, matrix)
    }

    private fun applyColorMatrix(bitmap: Bitmap, matrix: ColorMatrix): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(matrix)
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return output
    }

    fun addText(
        bitmap: Bitmap,
        text: String,
        color: Int,
        sizePercent: Float,
        position: Int,
    ): Bitmap {
        if (text.isBlank()) return bitmap
        val output = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = output.height * (0.04f + sizePercent * 0.08f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(4f, 0f, 2f, Color.BLACK)
        }
        val lines = text.lines()
        val lineHeight = paint.textSize * 1.2f
        val totalHeight = lineHeight * lines.size
        var y = when (position) {
            0 -> paint.textSize + output.height * 0.02f
            2 -> output.height - totalHeight + paint.textSize
            else -> output.height / 2f - totalHeight / 2f + paint.textSize
        }
        lines.forEach { line ->
            val width = paint.measureText(line)
            canvas.drawText(line, (output.width - width) / 2f, y, paint)
            y += lineHeight
        }
        return output
    }

    fun addShape(bitmap: Bitmap, circle: Boolean, color: Int, sizePercent: Float): Bitmap {
        val output = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = output.width * 0.012f
        }
        val width = output.width * (0.2f + sizePercent * 0.6f)
        val height = output.height * (0.2f + sizePercent * 0.6f)
        val left = (output.width - width) / 2f
        val top = (output.height - height) / 2f
        if (circle) {
            canvas.drawOval(RectF(left, top, left + width, top + height), paint)
        } else {
            canvas.drawRoundRect(
                RectF(left, top, left + width, top + height),
                output.width * 0.03f,
                output.width * 0.03f,
                paint,
            )
        }
        return output
    }

    fun collage(images: List<Bitmap>, backgroundColor: Int = Color.WHITE, gap: Int = 24): Bitmap {
        require(images.isNotEmpty()) { "No images" }
        val count = images.size.coerceAtMost(4)
        val columns = if (count == 1) 1 else 2
        val rows = ceil(count / columns.toDouble()).toInt()
        val cell = 760
        val width = columns * cell + gap * (columns + 1)
        val height = rows * cell + gap * (rows + 1)
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output).apply { drawColor(backgroundColor) }
        images.take(count).forEachIndexed { index, source ->
            val column = index % columns
            val row = index / columns
            val left = gap + column * (cell + gap)
            val top = gap + row * (cell + gap)
            val scale = maxOf(cell.toFloat() / source.width, cell.toFloat() / source.height)
            val drawWidth = source.width * scale
            val drawHeight = source.height * scale
            val dx = left + (cell - drawWidth) / 2f
            val dy = top + (cell - drawHeight) / 2f
            canvas.save()
            canvas.clipRect(left.toFloat(), top.toFloat(), (left + cell).toFloat(), (top + cell).toFloat())
            canvas.drawBitmap(source, dx, dy, Paint(Paint.FILTER_BITMAP_FLAG))
            canvas.restore()
        }
        return output
    }
}
