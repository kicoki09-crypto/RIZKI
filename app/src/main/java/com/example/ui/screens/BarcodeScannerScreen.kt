package com.example.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.Barang
import com.example.ui.InventoryViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BarcodeScannerScreen(
    viewModel: InventoryViewModel,
    onBarcodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Capture standard items in the system to supply simulated scan triggers
    val itemsInSystem by viewModel.allBarang.collectAsState()

    // Animating laser scanner line
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserOffsetY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
    ) {
        if (hasCameraPermission) {
            // Live Device Camera Feed View
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview
                            )
                        } catch (exc: Exception) {
                            Log.e("Scanner", "Use case binding failed", exc)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Permission missing fallback aesthetic drawing
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera Required",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Kamera tidak aktif atau izin ditolak.\nSilakan gunakan Panel Simulator di bawah.",
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // --- SCANNING RETICLE OVERLAY ---
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Scan Kamera / Simulator",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            // Target Square Reticle
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer beautiful bounding brackets
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {
                    // Top-Left Header Arc
                    Box(modifier = Modifier.size(20.dp).align(Alignment.TopStart).border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 8.dp)), contentAlignment = Alignment.Center) {}
                    // Top-Right Header Arc
                    Box(modifier = Modifier.size(20.dp).align(Alignment.TopEnd).border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(topEnd = 8.dp)), contentAlignment = Alignment.Center) {}
                    // Bottom-Left Header Arc
                    Box(modifier = Modifier.size(20.dp).align(Alignment.BottomStart).border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(bottomStart = 8.dp)), contentAlignment = Alignment.Center) {}
                    // Bottom-Right Header Arc
                    Box(modifier = Modifier.size(20.dp).align(Alignment.BottomEnd).border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(bottomEnd = 8.dp)), contentAlignment = Alignment.Center) {}
                }

                // Laser Guideline Animation
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(0.9f)
                        .height(2.6.dp)
                        .offset(y = (260 * laserOffsetY).dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color.Cyan, Color.Red, Color.Cyan, Color.Transparent)
                            )
                        )
                )

                Text(
                    text = "Arahkan Kamera ke Barcode",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
                )
            }

            // --- BARCODE EMULATOR CONSOLE HUB (For Streamed Emulator compatibility) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f))
                        )
                    )
                    .padding(bottom = 24.dp, top = 16.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚡ BARCODE SIMULATOR CONSOLE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Ketuk produk di bawah untuk mengirim data scan secara instan:",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(vertical = 4.dp),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (itemsInSystem.isEmpty()) {
                            Text(
                                text = "Tidak ada barang terdaftar.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(itemsInSystem) { barang ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .clickable {
                                                // Call simulation callback
                                                onBarcodeScanned(barang.barcode)
                                            }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = barang.namaBarang,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Code: ${barang.barcode}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
