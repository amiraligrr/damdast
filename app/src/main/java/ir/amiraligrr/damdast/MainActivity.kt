package ir.amiraligrr.damdast

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.amiraligrr.damdast.data.DamdastViewModel

class MainActivity : AppCompatActivity() {
    private var shortcutTarget by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        shortcutTarget = intent?.getStringExtra(EXTRA_SHORTCUT)
        setContent {
            val viewModel: DamdastViewModel = viewModel()
            DamdastRoot(
                viewModel = viewModel,
                shortcutTarget = shortcutTarget,
                onShortcutConsumed = { shortcutTarget = null },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        shortcutTarget = intent.getStringExtra(EXTRA_SHORTCUT)
    }

    companion object {
        const val EXTRA_SHORTCUT = "damdast.shortcut"
    }
}
