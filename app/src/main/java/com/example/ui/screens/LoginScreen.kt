package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.R
import com.example.ui.theme.GeoPrimaryDark
import com.example.ui.theme.GeoPrimaryLight
import com.example.ui.theme.GeoPrimaryContainerDark
import com.example.ui.theme.GeoPrimaryContainerLight
import com.example.ui.theme.GeoSecondaryDark
import com.example.ui.viewmodel.MainViewModel
import com.example.util.BiometricHelper

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val loginError by viewModel.loginError.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var enteredPin by remember { mutableStateOf("") }
    var lastClickTime by remember { mutableStateOf(0L) }
    val biometricStatus = remember { BiometricHelper.checkBiometricStatus(context) }

    val triggerBiometricAuth: () -> Unit = {
        val activity = context as? FragmentActivity
        if (activity != null) {
            BiometricHelper.authenticate(
                activity = activity,
                title = "SIMDes Desa Cimanggu",
                subtitle = "Pindai sidik jari Anda untuk masuk sebagai Petugas",
                negativeButtonText = "Gunakan PIN",
                onSuccess = {
                    viewModel.loginWithBiometric()
                    Toast.makeText(context, "Selamat datang, ${userProfile.namaPetugas}", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // Auto-trigger biometric authentication on screen launch if enabled
    LaunchedEffect(Unit) {
        if (isBiometricEnabled && biometricStatus == BiometricHelper.BiometricStatus.AVAILABLE) {
            triggerBiometricAuth()
        }
    }

    val handleDigitClick: (String) -> Unit = { digit ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= 150L) {
            lastClickTime = currentTime
            if (enteredPin.length < 4) {
                val newPin = enteredPin + digit
                enteredPin = newPin
                viewModel.clearLoginError()
                if (newPin.length == 4) {
                    val success = viewModel.loginWithPin(newPin)
                    if (!success) {
                        enteredPin = ""
                    } else {
                        Toast.makeText(context, "Login Berhasil", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val handleBackspace: () -> Unit = {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= 100L) {
            lastClickTime = currentTime
            if (enteredPin.isNotEmpty()) {
                enteredPin = enteredPin.dropLast(1)
                viewModel.clearLoginError()
            }
        }
    }

    val handleClear: () -> Unit = {
        enteredPin = ""
        viewModel.clearLoginError()
    }

    val handleLoginSubmit: () -> Unit = {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= 200L) {
            lastClickTime = currentTime
            if (enteredPin.isNotBlank()) {
                val success = viewModel.loginWithPin(enteredPin)
                if (!success) {
                    enteredPin = ""
                } else {
                    Toast.makeText(context, "Login Berhasil", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 1. Village Nature & Kantor Desa Background with Soft Blur
        Image(
            painter = painterResource(id = R.drawable.img_bg_login_desa),
            contentDescription = "Latar Belakang Desa Cimanggu",
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 16.dp),
            contentScale = ContentScale.Crop
        )

        // 2. Harmonized App Theme Gradient Overlay (Deep Purple / Violet / Indigo)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xDD1E1435), // Deep primary purple dark overlay
                            Color(0xBD2B1B4D),
                            Color(0xC8362261),
                            Color(0xF0150D27)
                        )
                    )
                )
        )

        // Decorative subtle theme glow reflections
        Box(
            modifier = Modifier
                .size(360.dp)
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GeoPrimaryDark.copy(alpha = 0.25f),
                            GeoPrimaryContainerDark.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 3. Main Glassmorphic Login Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header: Desa Cimanggu Crest & Branding with Default App Primary Purple
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .shadow(elevation = 18.dp, shape = CircleShape, spotColor = GeoPrimaryLight)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                GeoPrimaryLight,
                                Color(0xFF7C62BD),
                                Color(0xFF4F378B)
                            )
                        )
                    )
                    .border(
                        width = 2.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                GeoPrimaryDark,
                                GeoPrimaryContainerLight,
                                Color(0xFFEFB8C8)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = "Kantor Desa Cimanggu",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Village Tag Pill in Theme Colors
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = GeoPrimaryContainerDark.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeoPrimaryDark.copy(alpha = 0.6f)),
                modifier = Modifier.shadow(6.dp, RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏛️ PEMERINTAH DESA CIMANGGU",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = GeoPrimaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "SIMDes Kependudukan",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Sistem Informasi & Pelayanan Administrasi Warga",
                style = MaterialTheme.typography.bodyMedium,
                color = GeoSecondaryDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Glassmorphic Card Container matching App Palette
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 24.dp, shape = RoundedCornerShape(28.dp), spotColor = GeoPrimaryLight),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xDD231A3D)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GeoPrimaryDark.copy(alpha = 0.6f),
                            GeoPrimaryLight.copy(alpha = 0.35f),
                            Color(0x20381E72)
                        )
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Biometric Banner / Quick Login Button if enabled
                    if (isBiometricEnabled) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { triggerBiometricAuth() }
                                .shadow(8.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            color = GeoPrimaryContainerDark.copy(alpha = 0.85f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                Brush.horizontalGradient(
                                    listOf(GeoPrimaryDark, GeoPrimaryLight)
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(GeoPrimaryLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Login Biometrik",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Masuk Cepat dengan Biometrik",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Sentuh sensor sidik jari perangkat",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GeoPrimaryDark
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.2f))
                            )
                            Text(
                                text = "ATAU MASUKKAN PIN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = GeoSecondaryDark,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.2f))
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    } else {
                        Text(
                            text = "MASUKKAN PIN PETUGAS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            ),
                            color = GeoPrimaryDark
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 4 Glowing PIN Dots Indicator in Theme Primary Accent
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 4) {
                            val isFilled = i < enteredPin.length
                            val dotColor by animateColorAsState(
                                targetValue = if (isFilled) GeoPrimaryDark else Color.White.copy(alpha = 0.35f),
                                animationSpec = tween(150),
                                label = "dotColor"
                            )
                            Box(
                                modifier = Modifier
                                    .size(if (isFilled) 20.dp else 16.dp)
                                    .shadow(
                                        elevation = if (isFilled) 8.dp else 0.dp,
                                        shape = CircleShape,
                                        spotColor = GeoPrimaryDark
                                    )
                                    .clip(CircleShape)
                                    .background(dotColor)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isFilled) GeoPrimaryDark else Color.White.copy(alpha = 0.45f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = loginError != null,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFB3261E).copy(alpha = 0.88f),
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ ${loginError ?: ""}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Numeric Keypad in Harmonized Palette
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val row1 = listOf("1", "2", "3")
                        val row2 = listOf("4", "5", "6")
                        val row3 = listOf("7", "8", "9")

                        for (row in listOf(row1, row2, row3)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (key in row) {
                                    AppThemeKeypadButton(
                                        text = key,
                                        modifier = Modifier.weight(1f),
                                        onClick = { handleDigitClick(key) }
                                    )
                                }
                            }
                        }

                        // Bottom Row: Biometric Shortcut / C, 0, Backspace
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Bottom Left Key: Biometric quick trigger if enabled, otherwise Clear "C"
                            if (isBiometricEnabled) {
                                AppThemeIconButton(
                                    icon = Icons.Default.Fingerprint,
                                    tint = GeoPrimaryDark,
                                    backgroundColor = GeoPrimaryContainerDark.copy(alpha = 0.85f),
                                    modifier = Modifier.weight(1f),
                                    onClick = triggerBiometricAuth
                                )
                            } else {
                                AppThemeKeypadButton(
                                    text = "C",
                                    isAction = true,
                                    modifier = Modifier.weight(1f),
                                    onClick = handleClear
                                )
                            }

                            // Center Key: "0"
                            AppThemeKeypadButton(
                                text = "0",
                                modifier = Modifier.weight(1f),
                                onClick = { handleDigitClick("0") }
                            )

                            // Right Key: Backspace
                            AppThemeIconButton(
                                icon = Icons.AutoMirrored.Filled.Backspace,
                                tint = Color(0xFFFF8A80),
                                backgroundColor = Color(0x28FFFFFF),
                                modifier = Modifier.weight(1f),
                                onClick = handleBackspace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Submit Button with App Theme Primary Color
                    Button(
                        onClick = handleLoginSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp), spotColor = GeoPrimaryLight),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeoPrimaryLight,
                            contentColor = Color.White
                        ),
                        enabled = enteredPin.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = GeoPrimaryDark
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Buka SIMDes",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Footer Security Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = GeoPrimaryDark,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Aman & Terenkripsi • Data Warga Terlindungi",
                    style = MaterialTheme.typography.bodySmall,
                    color = GeoSecondaryDark
                )
            }
        }
    }
}

@Composable
private fun AppThemeKeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    isAction: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isAction) Color(0x33FFFFFF) else Color(0x1FFFFFFF),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAction) GeoPrimaryDark.copy(alpha = 0.5f) else Color(0x28FFFFFF)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = if (isAction) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = if (isAction) 20.sp else 22.sp
                ),
                color = if (isAction) GeoPrimaryDark else Color.White
            )
        }
    }
}

@Composable
private fun AppThemeIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
