package com.davinder.subguard.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davinder.subguard.data.firebase.UserSession
import com.davinder.subguard.util.DEFAULT_DISPLAY_NAME

// Brand gradient (same indigo -> teal as the rest of the app). Swap with GradientAccents if you prefer.
val LogoIndigo = Color(0xFF6366F1)
val LogoTeal = Color(0xFF14B8A6)

// The check mark "draws itself" once per app launch, not every time you return to a tab.
private var introPlayed = false

/** Shield with a check mark: the "guard" in SubGuard. */
@Composable
fun SubGuardShield(modifier: Modifier = Modifier, checkProgress: () -> Float = { 1f }) {
    Canvas(modifier.size(width = 22.dp, height = 26.dp)) {
        val w = size.width
        val h = size.height
        val shield = Path().apply {
            moveTo(w * 0.5f, 0f)
            lineTo(w, h * 0.16f)
            cubicTo(w, h * 0.62f, w * 0.78f, h * 0.88f, w * 0.5f, h)
            cubicTo(w * 0.22f, h * 0.88f, 0f, h * 0.62f, 0f, h * 0.16f)
            close()
        }
        drawPath(
            shield,
            brush = Brush.linearGradient(listOf(LogoIndigo, LogoTeal), start = Offset.Zero, end = Offset(w, h))
        )

        val check = Path().apply {
            moveTo(w * 0.28f, h * 0.50f)
            lineTo(w * 0.44f, h * 0.65f)
            lineTo(w * 0.74f, h * 0.34f)
        }
        val measure = PathMeasure()
        measure.setPath(check, false)
        val partial = Path()
        measure.getSegment(0f, measure.length * checkProgress().coerceIn(0f, 1f), partial, true)
        drawPath(
            partial,
            color = Color.White,
            style = Stroke(width = w * 0.11f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/** Shield + "Sub" (plain) + "Guard" (brand gradient). */
@Composable
fun SubGuardWordmark(modifier: Modifier = Modifier, showTagline: Boolean = false) {
    val progress = remember { Animatable(if (introPlayed) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!introPlayed) {
            progress.animateTo(1f, tween(durationMillis = 800, delayMillis = 250))
            introPlayed = true
        }
    }
    val textBrush = remember { Brush.horizontalGradient(listOf(LogoIndigo, LogoTeal)) }
    val plain = MaterialTheme.colorScheme.onBackground

    Row(modifier.semantics { contentDescription = "SubGuard" }, verticalAlignment = Alignment.CenterVertically) {
        SubGuardShield(checkProgress = { progress.value })
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = plain, fontWeight = FontWeight.SemiBold)) { append("Sub") }
                    withStyle(SpanStyle(brush = textBrush, fontWeight = FontWeight.ExtraBold)) { append("Guard") }
                },
                fontSize = 22.sp,
                letterSpacing = (-0.4).sp
            )
            if (showTagline) {
                Text(
                    "SUBSCRIPTION TRACKER",
                    fontSize = 8.sp,
                    letterSpacing = 1.6.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun initialsOf(name: String, email: String): String {
    val source = name.trim().takeIf { it.isNotEmpty() && it != DEFAULT_DISPLAY_NAME }
    val letters = if (source != null) {
        val words = source.split(Regex("\\s+"))
        if (words.size >= 2) "${words.first().first()}${words.last().first()}" else words.first().take(2)
    } else {
        email.trim().take(1)
    }
    return letters.uppercase().ifEmpty { "?" }
}

@Composable
fun ProfileAvatar(
    initials: String,
    isGuest: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    container: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier.size(size).clip(CircleShape).background(container),
        contentAlignment = Alignment.Center
    ) {
        if (isGuest) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = content, modifier = Modifier.size(size * 0.55f))
        } else {
            Text(initials, color = content, fontWeight = FontWeight.SemiBold, fontSize = (size.value * 0.38f).sp)
        }
    }
}

/** Slim brand bar for the 4 main tabs: wordmark on the left, profile avatar on the right. */
@Composable
fun SubGuardTopBar(onProfileClick: () -> Unit) {
    val user by UserSession.user.collectAsState()
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SubGuardWordmark()
            ProfileAvatar(
                initials = initialsOf(user.displayName, user.email),
                isGuest = !user.isSignedIn,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onProfileClick)
                    .semantics { contentDescription = "Profile" }
            )
        }
    }
}