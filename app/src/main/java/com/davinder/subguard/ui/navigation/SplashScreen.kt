package com.davinder.subguard.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davinder.subguard.ui.components.SubGuardShield
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SplashDestination { ONBOARDING, EMAIL_VERIFICATION, WELCOME, DASHBOARD }

// Splash hamesha dark navy rehta hai (theme se independent)
private val SplashBgTop = Color(0xFF040A1C)
private val SplashBgBottom = Color(0xFF0A1633)
private val SplashBlue = Color(0xFF2563EB)
private val SplashCyan = Color(0xFF22D3EE)
private val SplashSoft = Color(0xFFCBD5E1)

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToEmailVerification: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    resolveDestination: suspend () -> SplashDestination
) {
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }

    // Logo ke peeche halka pulsing glow
    val infinite = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    LaunchedEffect(Unit) {
        coroutineScope {
            // Destination background me resolve hoti hai, animation ke saath-saath
            val destination = async {
                runCatching { resolveDestination() }.getOrDefault(SplashDestination.WELCOME)
            }

            launch { logoAlpha.animateTo(1f, tween(350)) }
            // Pop-in: thoda overshoot karke settle hota hai
            launch {
                logoScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                delay(450)
                textAlpha.animateTo(1f, tween(500))
            }
            val bar = launch {
                delay(300)
                progress.animateTo(1f, tween(1700, easing = LinearEasing))
            }

            bar.join() // kam se kam ~2 sec brand dikhega
            when (destination.await()) {
                SplashDestination.ONBOARDING -> onNavigateToOnboarding()
                SplashDestination.EMAIL_VERIFICATION -> onNavigateToEmailVerification()
                SplashDestination.WELCOME -> onNavigateToWelcome()
                SplashDestination.DASHBOARD -> onNavigateToDashboard()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SplashBgTop, SplashBgBottom))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            // Glow + Shield logo
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .graphicsLayer { alpha = logoAlpha.value }
                        .background(
                            Brush.radialGradient(
                                listOf(SplashCyan.copy(alpha = glowAlpha * 0.5f), Color.Transparent)
                            ),
                            CircleShape
                        )
                )
                SubGuardShield(
                    modifier = Modifier
                        .size(width = 168.dp, height = 140.dp)
                        .graphicsLayer {
                            scaleX = logoScale.value
                            scaleY = logoScale.value
                            alpha = logoAlpha.value
                        },
                    showSpeedLines = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { alpha = textAlpha.value }
            ) {
                Row {
                    Text(
                        text = "Sub",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 38.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Guard",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 38.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = SplashCyan
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Track  •  Manage  •  Save",
                    style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.8.sp),
                    color = Color.White
                )
            }
        }

        // Bottom: loading bar + tagline
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 56.dp)
                .graphicsLayer { alpha = textAlpha.value },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.value)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(SplashBlue, SplashCyan)))
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "Smart Subscriptions\nfor a Better You",
                style = MaterialTheme.typography.bodyMedium,
                color = SplashSoft,
                textAlign = TextAlign.Center
            )
        }
    }
}