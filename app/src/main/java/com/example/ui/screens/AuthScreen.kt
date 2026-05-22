package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.InventoryViewModel

import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: InventoryViewModel,
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("admin123") }
    var selectedRole by remember { mutableStateOf("Admin") }
    var passwordVisible by remember { mutableStateOf(false) }
    var resetMode by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
            .drawBehind {
                // Glow 1: Sapphire Blue top-right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF0F52BA).copy(alpha = 0.20f), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.2f),
                        radius = if (size.width > 0f) size.width * 0.75f else 1f
                    )
                )
                // Glow 2: Indigo bottom-left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF4F46E5).copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.8f),
                        radius = if (size.width > 0f) size.width * 0.75f else 1f
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Enterprise Logo & Title
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warehouse,
                    contentDescription = "Warehouse Logo",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "INVENTORY TNSP",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Text(
                text = "Premium Enterprise Stock Management System",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Main Credential Card with Translucent Rim glassmorphism
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.2.dp,
                        color = Color.White.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (resetMode) "Reset Sandi" else "Otentikasi Anggota",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    if (!resetMode) {
                        // User Name Input
                        OutlinedTextField(
                            value = username,
                            onValueChange = { 
                                username = it
                                errorMessage = "" 
                            },
                            label = { Text("Nama Pengguna (Username)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password Input
                        OutlinedTextField(
                            value = password,
                            onValueChange = { 
                                password = it
                                errorMessage = "" 
                            },
                            label = { Text("Kata Sandi") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock") },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Role Selector Header
                        Text(
                            text = "Pilih Hak Akses Pintu Masuk:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Segmented Role Selectors
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Admin", "Supervisor", "Staff Gudang").forEach { role ->
                                val isSelected = selectedRole == role
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary 
                                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                        )
                                        .clickable { 
                                            selectedRole = role
                                            // Auto-fill mock credentials based on role selection
                                            when (role) {
                                                "Admin" -> {
                                                    username = "admin"
                                                    password = "admin123"
                                                }
                                                "Supervisor" -> {
                                                    username = "supervisor"
                                                    password = "super123"
                                                }
                                                "Staff Gudang" -> {
                                                    username = "staff"
                                                    password = "staff123"
                                                }
                                            }
                                            errorMessage = ""
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = role.replace(" Gudang", ""),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                                else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        if (errorMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Sign In Button
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                viewModel.login(username, selectedRole) { success ->
                                    if (success) {
                                        onLoginSuccess()
                                    } else {
                                        errorMessage = "Kombinasi pengguna / sandi untuk role '$selectedRole' tidak cocok!"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Login, contentDescription = "Login")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("MASUK SYSTEM", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Reset password button trigger
                        Text(
                            text = "Lupa kata sandi?",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable { 
                                    resetMode = true 
                                    statusMessage = ""
                                    errorMessage = ""
                                }
                                .padding(4.dp)
                        )
                    } else {
                        // Password Reset layout
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Masukkan Username Anda") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (statusMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = statusMessage,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                viewModel.resetPassword(username) { message ->
                                    statusMessage = message
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Kirim Instruksi Reset")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Kembali ke Login",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable { resetMode = false }
                                .padding(4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Information guide text of preset credentials
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Petunjuk Peninjau Instan:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Gunakan preset username di atas. Akun admin otomatis menguasai semua gudang dan mengesahkan transaksi keluar.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
