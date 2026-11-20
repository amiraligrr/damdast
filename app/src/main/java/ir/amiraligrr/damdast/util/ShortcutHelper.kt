package ir.amiraligrr.damdast.util

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import ir.amiraligrr.damdast.MainActivity
import ir.amiraligrr.damdast.R

object ShortcutHelper {
    fun requestPinnedShortcut(context: Context, toolId: String, label: String): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) return false
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "ir.amiraligrr.damdast.SHORTCUT"
            putExtra(MainActivity.EXTRA_SHORTCUT, toolId)
        }
        val icon = IconCompat.createWithResource(context, R.mipmap.ic_launcher)
        val shortcut = ShortcutInfoCompat.Builder(context, "damdast_$toolId")
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(icon)
            .setIntent(intent)
            .build()
        return runCatching {
            ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
        }.getOrDefault(false)
    }
}
