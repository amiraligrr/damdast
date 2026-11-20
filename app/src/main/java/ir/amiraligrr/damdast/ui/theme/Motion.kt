package ir.amiraligrr.damdast.ui.theme

import android.animation.ValueAnimator
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            !ValueAnimator.areAnimatorsEnabled()
        } else {
            false
        }
    }
}

object Motion {
    const val Fast = 160
    const val Medium = 260
    const val Slow = 420
    const val EntranceStagger = 45
}
