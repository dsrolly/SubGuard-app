package com.davinder.subguard.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.davinder.subguard.data.local.PreferencesManager

/**
 * Returns a lambda that gives a short vibration tick, but only if the
 * "Haptic feedback" switch in Settings is ON.
 *
 * Direct Vibrator use is intentional: Compose's HapticFeedbackType.TextHandleMove
 * does nothing on many phones (Realme/Oppo/Vivo etc.), so the toggle looked "dead".
 *
 * Needs in AndroidManifest.xml:  <uses-permission android:name="android.permission.VIBRATE" />
 */
@Composable
fun rememberHaptic(): () -> Unit {
    val context = LocalContext.current.applicationContext
    val prefs = remember(context) { PreferencesManager(context) }
    val enabled by prefs.hapticEnabled.collectAsState(initial = true)
    val enabledState = rememberUpdatedState(enabled)

    return remember(context) {
        val action: () -> Unit = {
            if (enabledState.value) performTick(context)
        }
        action
    }
}

/** Plain vibration tick (ignores the Settings switch). Used by the Settings switch itself. */
fun performTick(context: Context) {
    try {
        val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator == null || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(18)
        }
    } catch (_: Exception) {
        // Never crash the UI because of haptics.
    }
}