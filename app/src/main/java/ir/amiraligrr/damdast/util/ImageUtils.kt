package ir.amiraligrr.damdast.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {
    suspend fun loadBitmap(context: Context, uri: Uri, maxDimension: Int = 2048): Bitmap? =
        withContext(Dispatchers.IO) {
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                        val width = info.size.width
                        val height = info.size.height
                        if (width > maxDimension || height > maxDimension) {
                            val scale = maxDimension.toFloat() / maxOf(width, height)
                            decoder.setTargetSize(
                                (width * scale).toInt().coerceAtLeast(1),
                                (height * scale).toInt().coerceAtLeast(1),
                            )
                        }
                        decoder.isMutableRequired = true
                    }
                } else {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeStream(input, null, options)
                        options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, maxDimension)
                        options.inJustDecodeBounds = false
                        context.contentResolver.openInputStream(uri)?.use { second ->
                            BitmapFactory.decodeStream(second, null, options)
                        }
                    }
                }
            }.getOrNull()
        }

    fun calculateSampleSize(width: Int, height: Int, maxDimension: Int): Int {
        var sample = 1
        var largest = maxOf(width, height)
        while (largest / 2 >= maxDimension) {
            largest /= 2
            sample *= 2
        }
        return sample
    }

    suspend fun bitmapFromStream(input: InputStream): Bitmap? = withContext(Dispatchers.IO) {
        runCatching { BitmapFactory.decodeStream(input) }.getOrNull()
    }

    fun exifOrientation(context: Context, uri: Uri): Int = runCatching {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        } ?: ExifInterface.ORIENTATION_NORMAL
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    fun saveBitmap(bitmap: Bitmap, file: File, format: Bitmap.CompressFormat, quality: Int = 92): Boolean {
        return runCatching {
            FileOutputStream(file).use { output ->
                bitmap.compress(format, quality.coerceIn(1, 100), output)
            }
            true
        }.getOrDefault(false)
    }

    fun dominantColors(bitmap: Bitmap, maxColors: Int = 8): List<ArgbColor> {
        val sample = Bitmap.createScaledBitmap(bitmap, 96, 96, true)
        val buckets = HashMap<Int, Int>()
        val pixels = IntArray(sample.width * sample.height)
        sample.getPixels(pixels, 0, sample.width, 0, 0, sample.width, sample.height)
        pixels.forEach { pixel ->
            val alpha = AndroidColor.alpha(pixel)
            if (alpha < 32) return@forEach
            val red = (AndroidColor.red(pixel) / 32) * 32 + 16
            val green = (AndroidColor.green(pixel) / 32) * 32 + 16
            val blue = (AndroidColor.blue(pixel) / 32) * 32 + 16
            val key = (red shl 16) or (green shl 8) or blue
            buckets[key] = (buckets[key] ?: 0) + 1
        }
        if (sample !== bitmap) sample.recycle()
        return buckets.entries
            .sortedByDescending { it.value }
            .take(maxColors)
            .map { entry ->
                ArgbColor(
                    alpha = 255,
                    red = (entry.key shr 16) and 0xFF,
                    green = (entry.key shr 8) and 0xFF,
                    blue = entry.key and 0xFF,
                )
            }
    }
}
