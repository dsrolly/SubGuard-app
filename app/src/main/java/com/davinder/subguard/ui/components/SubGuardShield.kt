package com.davinder.subguard.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

private val ShieldCyan = Color(0xFF22D3EE)
private val ShieldBlue = Color(0xFF2563EB)
private val ShieldDeep = Color(0xFF1D4ED8)
private val ShieldMid = Color(0xFF0EA5E9)

/**
 * SubGuard ka shield-with-check logo, Canvas se bana hai.
 * Size hamesha 1.2 : 1 (width : height) rakho, jaise Modifier.size(width = 156.dp, height = 130.dp).
 * showSpeedLines = true => left side par splash wali speed lines.
 * Colors fixed hain, isliye kisi bhi theme me visible rehta hai.
 */
@Composable
fun SubGuardShield(
    modifier: Modifier = Modifier,
    showSpeedLines: Boolean = false
) {
    Canvas(modifier = modifier) {
        // Virtual space: 120 x 100
        val sx = size.width / 120f
        val sy = size.height / 100f
        val cx = if (showSpeedLines) 70f else 60f

        fun px(v: Float, s: Float = 1f) = (cx + (v - 70f) * s) * sx
        fun py(v: Float, s: Float = 1f) = (50f + (v - 50f) * s) * sy

        fun shield(s: Float): Path = Path().apply {
            moveTo(px(70f, s), py(4f, s))
            lineTo(px(108f, s), py(18f, s))
            lineTo(px(108f, s), py(48f, s))
            cubicTo(px(108f, s), py(72f, s), px(88f, s), py(88f, s), px(70f, s), py(96f, s))
            cubicTo(px(52f, s), py(88f, s), px(32f, s), py(72f, s), px(32f, s), py(48f, s))
            lineTo(px(32f, s), py(18f, s))
            close()
        }

        // Speed lines
        if (showSpeedLines) {
            val lineColor = ShieldCyan.copy(alpha = 0.7f)
            val w = 4f * sx
            drawLine(lineColor, Offset(8f * sx, 36f * sy), Offset(26f * sx, 36f * sy), strokeWidth = w, cap = StrokeCap.Round)
            drawLine(lineColor, Offset(2f * sx, 50f * sy), Offset(24f * sx, 50f * sy), strokeWidth = w, cap = StrokeCap.Round)
            drawLine(lineColor, Offset(10f * sx, 64f * sy), Offset(26f * sx, 64f * sy), strokeWidth = w, cap = StrokeCap.Round)
        }

        // Outer shield (cyan -> blue)
        drawPath(
            path = shield(1f),
            brush = Brush.linearGradient(
                colors = listOf(ShieldCyan, ShieldBlue),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            )
        )

        // Inner shield (depth)
        drawPath(
            path = shield(0.8f),
            brush = Brush.linearGradient(
                colors = listOf(ShieldMid, ShieldDeep),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            )
        )

        // White check mark
        val check = Path().apply {
            moveTo(px(54f), py(52f))
            lineTo(px(66f), py(64f))
            lineTo(px(88f), py(38f))
        }
        drawPath(
            path = check,
            color = Color.White,
            style = Stroke(
                width = size.width * 0.055f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}