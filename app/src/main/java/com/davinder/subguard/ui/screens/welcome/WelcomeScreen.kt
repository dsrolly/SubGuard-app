package com.davinder.subguard.ui.screens.welcome

import com.davinder.subguard.ui.components.PremiumSurface
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davinder.subguard.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

// Exact Colors from Target Image 2
private val SubColorLight = Color(0xFF0F172A)     // Dark Navy Black for "Sub"
private val GuardColor = Color(0xFF0284C7)        // Vivid Electric Sky Blue for "Guard"
private val SubtitleSlate = Color(0xFF475569)     // High-readability Dark Slate for description
private val OutlineBorderBlue = Color(0xFF2563EB) // Royal blue for Log In border

// Gradient for "Create Account"
private val CyanBlueGradient = Brush.horizontalGradient(
    listOf(Color(0xFF2563EB), Color(0xFF06B6D4))
)

@Composable
fun WelcomeScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onGoogleSignInSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val context = LocalContext.current
    var isSigningIn by remember { mutableStateOf(false) }

    // Safe Web Client ID fetching without Composable/Context lint error
    val webClientId = remember(context) {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId != 0) context.getString(resId) else ""
    }

    val isDarkBg = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val headingColor = if (isDarkBg) Color.White else Color(0xFF090D16)
    val subTextColor = if (isDarkBg) Color(0xFF94A3B8) else SubtitleSlate
    val subBrandColor = if (isDarkBg) Color.White else SubColorLight
    val loginButtonBorder = if (isDarkBg) Color(0xFF60A5FA) else OutlineBorderBlue
    val loginButtonText = if (isDarkBg) Color(0xFF60A5FA) else OutlineBorderBlue

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener { authTask ->
                            isSigningIn = false
                            if (authTask.isSuccessful) {
                                onGoogleSignInSuccess()
                            } else {
                                Toast.makeText(
                                    context,
                                    authTask.exception?.localizedMessage ?: "Authentication failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    isSigningIn = false
                    Toast.makeText(context, "Google Sign-In failed: Token null", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                isSigningIn = false
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        } else {
            isSigningIn = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 26.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.35f))

            // 1. EXACT 3D ORIGINAL APP ICON (Loaded directly from drawable)
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = Color(0x400284C7)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF020817)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_subguard_logo),
                    contentDescription = "SubGuard Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. DUAL COLOR WORDMARK: "Sub" (Dark Navy) + "Guard" (Sky Blue)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Sub",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        fontSize = 30.sp
                    ),
                    color = subBrandColor
                )
                Text(
                    text = "Guard",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        fontSize = 30.sp
                    ),
                    color = GuardColor
                )
            }

            Spacer(modifier = Modifier.weight(0.45f))

            // 3. HEADING & HIGH-CONTRAST SUBTITLE (Left-Aligned, clean spacing)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.6).sp,
                        fontSize = 28.sp
                    ),
                    color = headingColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Track your subscriptions and\nstay in control.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp,
                        fontSize = 15.sp
                    ),
                    color = subTextColor
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // 4. ACTION BUTTONS
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Button 1: Create Account (Vibrant Cyan-Blue Pill)
                Button(
                    onClick = onNavigateToRegister,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(0.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CyanBlueGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Create Account",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                // Button 2: Log In (White Surface with crisp border)
                OutlinedButton(
                    onClick = onNavigateToLogin,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = loginButtonText
                    ),
                    border = BorderStroke(1.6.dp, loginButtonBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = "Log In",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = loginButtonText
                    )
                }

                // Divider: "or"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "or",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }

                // Button 3: Continue with Google
                PremiumSurface(
                    onClick = {
                        if (!isSigningIn) {
                            isSigningIn = true
                            val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                            if (webClientId.isNotBlank()) {
                                gsoBuilder.requestIdToken(webClientId)
                            }
                            val client = GoogleSignIn.getClient(context, gsoBuilder.build())

                            client.signOut().addOnCompleteListener {
                                googleSignInLauncher.launch(client.signInIntent)
                            }
                        }
                    },
                    enabled = !isSigningIn,
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shadowElevation = 3.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSigningIn) {
                            CircularProgressIndicator(
                                color = GuardColor,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            GoogleGLogo(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.6f))

            // 5. GUEST LINK & SUBTITLE
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onContinueAsGuest)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Continue as Guest",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = loginButtonText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Guest data is not saved",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp
                    ),
                    color = subTextColor,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/** Google's 4-color Vector "G" logo */
@Composable
private fun GoogleGLogo(modifier: Modifier = Modifier) {
    val blue = Color(0xFF4285F4)
    val green = Color(0xFF34A853)
    val yellow = Color(0xFFFBBC05)
    val red = Color(0xFFEA4335)

    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.2f
        val inset = stroke / 2f
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(inset, inset)

        fun segment(color: Color, start: Float, sweep: Float) {
            drawArc(
                color = color,
                startAngle = start,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke)
            )
        }

        segment(red, 215f, 95f)
        segment(yellow, 145f, 70f)
        segment(green, 55f, 90f)
        segment(blue, 0f, 55f)

        drawLine(
            color = blue,
            start = Offset(size.width / 2f, size.height / 2f),
            end = Offset(size.width - inset, size.height / 2f),
            strokeWidth = stroke
        )
    }
}