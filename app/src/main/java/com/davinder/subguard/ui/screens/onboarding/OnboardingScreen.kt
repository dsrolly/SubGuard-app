package com.davinder.subguard.ui.screens.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davinder.subguard.ui.components.SubGuardShield
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch

// Onboarding hamesha light look me rehti hai (mockup jaisi), theme se independent,
// isliye text/button kisi bhi mode me gayab nahi honge.
private val OnbTitle = Color(0xFF0F172A)
private val OnbBody = Color(0xFF64748B)
private val OnbIndigo = Color(0xFF4F46E5)
private val OnbTeal = Color(0xFF14B8A6)
private val OnbBgTop = Color(0xFFEAF0FF)
private val OnbBgBottom = Color(0xFFFFFFFF)

private data class OnboardingPage(val title: String, val description: String)

private val pages = listOf(
    OnboardingPage(
        title = "Track All Your\nSubscriptions",
        description = "Manage your digital & offline subscriptions in one place."
    ),
    OnboardingPage(
        title = "Never Miss\na Renewal",
        description = "Get advance notifications before your next payment."
    ),
    OnboardingPage(
        title = "Know Where\nYour Money Goes",
        description = "Track monthly & annual spending with clear insights."
    ),
    OnboardingPage(
        title = "Your Data,\nYour Control",
        description = "Secure, private and always in your hands."
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.size - 1

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = OnbBgBottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(OnbBgTop, OnbBgBottom)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                // Skip (top right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isLastPage) {
                        TextButton(onClick = onFinished) {
                            Text(
                                text = "Skip",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = OnbBody
                            )
                        }
                    }
                }

                // Pages
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { index ->
                    val page = pages[index]

                    // Swipe karte waqt fade + scale
                    val pageOffset = (pagerState.currentPage - index) + pagerState.currentPageOffsetFraction
                    val fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = 0.3f + 0.7f * fraction
                                scaleX = 0.88f + 0.12f * fraction
                                scaleY = 0.88f + 0.12f * fraction
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when (index) {
                            0 -> TrackIllustration()
                            1 -> RenewalIllustration()
                            2 -> SpendingIllustration()
                            else -> PrivacyIllustration()
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Text(
                            text = page.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                lineHeight = 32.sp
                            ),
                            color = OnbTitle,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = page.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnbBody,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dots + CTA
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pages.size) { i ->
                            val selected = pagerState.currentPage == i
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(if (selected) 24.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) OnbIndigo else Color(0xFFCBD5E1))
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (!isLastPage) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                onFinished()
                            }
                        },
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(0.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(OnbIndigo, OnbTeal))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isLastPage) "Get Started" else "Next",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Illustrations (sab Compose shapes/Canvas se bane hain, no images)  */
/* ------------------------------------------------------------------ */

@Composable
private fun IllustrationFrame(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier.size(width = 290.dp, height = 240.dp),
        contentAlignment = Alignment.Center
    ) {
        // soft blue blob
        Box(
            modifier = Modifier
                .size(220.dp)
                .background(
                    Brush.radialGradient(listOf(Color(0xFFD9E6FF), Color(0x00D9E6FF))),
                    CircleShape
                )
        )
        // ground shadow
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
                .size(width = 190.dp, height = 14.dp)
                .background(Color(0x1A1E3A8A), CircleShape)
        )
        content()
    }
}

/* ---------- 1. Track all subscriptions: tablet + app tiles + plants ---------- */

@Composable
private fun TrackIllustration() {
    IllustrationFrame {
        Plant(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 2.dp, bottom = 14.dp)
        )
        Plant(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 2.dp, bottom = 14.dp)
                .graphicsLayer { scaleX = -1f }
        )

        Box(
            modifier = Modifier
                .size(width = 172.dp, height = 162.dp)
                .shadow(14.dp, RoundedCornerShape(20.dp), spotColor = Color(0x551E3A8A))
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E3A8A))
                .padding(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF8FAFF)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Generic app tiles (brand logos nahi, simple icons)
                        AppTile(Color(0xFF111827), Icons.Filled.PlayArrow, Color(0xFFEF4444))
                        AppTile(Color(0xFF111827), Icons.Filled.MusicNote, Color(0xFF22C55E))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AppTile(Color.White, Icons.Filled.Cloud, Color(0xFFF59E0B), bordered = true)
                        AppTile(Color(0xFFE0F2FE), Icons.Filled.Photo, Color(0xFF0284C7))
                    }
                }
            }
        }
    }
}

@Composable
private fun AppTile(
    background: Color,
    icon: ImageVector,
    iconTint: Color,
    bordered: Boolean = false
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(shape)
            .background(background)
            .then(if (bordered) Modifier.border(1.dp, Color(0xFFE2E8F0), shape) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun Plant(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 64.dp, height = 100.dp)) {
        val w = size.width
        val h = size.height
        drawRoundRect(
            color = Color(0xFF6D5DF6),
            topLeft = Offset(w * 0.25f, h * 0.74f),
            size = Size(w * 0.5f, h * 0.24f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        fun leaf(angle: Float, color: Color) {
            rotate(degrees = angle, pivot = Offset(w * 0.5f, h * 0.76f)) {
                drawOval(
                    color = color,
                    topLeft = Offset(w * 0.42f, h * 0.08f),
                    size = Size(w * 0.16f, h * 0.66f)
                )
            }
        }
        leaf(-30f, Color(0xFF16855A))
        leaf(30f, Color(0xFF22A06B))
        leaf(0f, Color(0xFF34C38F))
    }
}

/* ---------- 2. Never miss a renewal: calendar + bell + clock ---------- */

@Composable
private fun RenewalIllustration() {
    IllustrationFrame {
        Box(
            modifier = Modifier
                .size(width = 176.dp, height = 164.dp)
                .offset(x = (-16).dp, y = (-10).dp)
                .shadow(14.dp, RoundedCornerShape(18.dp), spotColor = Color(0x551E3A8A))
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .background(Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF2563EB))))
                )
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    repeat(3) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            repeat(4) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFDDE5FF))
                                )
                            }
                        }
                    }
                }
            }
            // calendar rings
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 40.dp, top = 6.dp)
                    .size(width = 8.dp, height = 20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFBFD0FF))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 40.dp, top = 6.dp)
                    .size(width = 8.dp, height = 20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFBFD0FF))
            )
        }

        Icon(
            imageVector = Icons.Filled.Notifications,
            contentDescription = null,
            tint = Color(0xFF14B8A6),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 30.dp)
                .size(92.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 26.dp)
                .size(42.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF0E9AA7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AccessTime,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

/* ---------- 3. Know where your money goes: bars + trend + rupee coin ---------- */

@Composable
private fun SpendingIllustration() {
    IllustrationFrame {
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-22).dp, y = 16.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            listOf(56, 86, 116, 148).forEachIndexed { i, h ->
                Box(
                    modifier = Modifier
                        .width(30.dp)
                        .height(h.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(
                            Brush.verticalGradient(
                                if (i == 3) listOf(Color(0xFF3B82F6), Color(0xFF2563EB))
                                else listOf(Color(0xFF7C83F5), Color(0xFF5B5BEA))
                            )
                        )
                )
            }
        }

        // trend arrow
        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-10).dp, y = (-34).dp)
                .size(width = 210.dp, height = 150.dp)
        ) {
            val trend = Path().apply {
                moveTo(size.width * 0.04f, size.height * 0.78f)
                lineTo(size.width * 0.34f, size.height * 0.48f)
                lineTo(size.width * 0.54f, size.height * 0.60f)
                lineTo(size.width * 0.92f, size.height * 0.14f)
            }
            val teal = Color(0xFF14B8A6)
            drawPath(
                path = trend,
                color = teal,
                style = Stroke(width = 7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            val end = Offset(size.width * 0.92f, size.height * 0.14f)
            drawLine(teal, end, Offset(end.x - 30f, end.y + 2f), strokeWidth = 7f, cap = StrokeCap.Round)
            drawLine(teal, end, Offset(end.x - 4f, end.y + 30f), strokeWidth = 7f, cap = StrokeCap.Round)
        }

        // rupee coin
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 34.dp)
                .size(64.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "₹",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

/* ---------- 4. Your data, your control: shield + lock ---------- */

@Composable
private fun PrivacyIllustration() {
    IllustrationFrame {
        SubGuardShield(
            modifier = Modifier
                .size(width = 196.dp, height = 164.dp)
                .offset(x = (-10).dp, y = (-6).dp),
            showSpeedLines = false
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 34.dp, bottom = 34.dp)
                .size(58.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF3B5BDB), Color(0xFF1E3A8A)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}