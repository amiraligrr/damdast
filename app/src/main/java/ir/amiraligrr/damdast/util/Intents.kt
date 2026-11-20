package ir.amiraligrr.damdast.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

fun copyToClipboard(context: Context, text: String, label: String = "Damdast") {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    manager.setPrimaryClip(ClipData.newPlainText(label, text))
}

fun shareText(context: Context, text: String, title: String? = null): Boolean = runCatching {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, title))
    true
}.getOrDefault(false)

fun openUrl(context: Context, url: String): Boolean = runCatching {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    true
}.getOrDefault(false)

fun openEmail(context: Context, address: String, subject: String = ""): Boolean = runCatching {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:$address")
        putExtra(Intent.EXTRA_SUBJECT, subject)
    }
    context.startActivity(intent)
    true
}.getOrDefault(false)

fun shareFile(context: Context, file: File, mimeType: String, title: String? = null): Boolean =
    runCatching {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
        true
    }.getOrDefault(false)

fun toast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun sharedCacheFile(context: Context, fileName: String): File {
    val directory = File(context.cacheDir, "shared").apply { mkdirs() }
    return File(directory, fileName)
}


fun Context.findActivity(): android.app.Activity? {
    var context: android.content.Context = this
    while (context is android.content.ContextWrapper) {
        if (context is android.app.Activity) return context
        context = context.baseContext
    }
    return null
}
