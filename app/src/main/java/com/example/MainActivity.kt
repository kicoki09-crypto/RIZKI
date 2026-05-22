package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.ui.InventoryViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Direct instantiation of the VM without complex dependency chains for maximum robustness
        val viewModel = ViewModelProvider(this)[InventoryViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val currentUser by viewModel.currentUser.collectAsState()
                val toastFlow = viewModel.toastEvent
                val context = LocalContext.current

                // Toast Handler
                LaunchedEffect(Unit) {
                    toastFlow.collect { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }

                if (currentUser == null) {
                    // Gateway Authentication Splash Screen
                    AuthScreen(viewModel = viewModel) {
                        Toast.makeText(context, "Selamat datang di Inventory TNSP", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Secure logged-in frame system with full scaffolding
                    MainConsoleScaffolding(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainConsoleScaffolding(
    viewModel: InventoryViewModel
) {
    val context = LocalContext.current
    var activeScreen by remember { mutableStateOf("dashboard") } // "dashboard", "barang", "transaksi", "gudang", "laporan"
    var transactDirectionHint by remember { mutableStateOf("MASUK") } // helper to guide Tab selector click direction

    // Global Barcode Scanning Camera overlay hooks
    var isBarcodeScreenOpen by remember { mutableStateOf(false) }
    var currentBarcodeCallback by remember { mutableStateOf<((String) -> Unit)?>(null) }

    // Alert stack slider drawer states
    var showNotificationDrawer by remember { mutableStateOf(false) }
    val notificationList by viewModel.notifications.collectAsState()

    fun launchDeviceScanner(onScan: (String) -> Unit) {
        currentBarcodeCallback = onScan
        isBarcodeScreenOpen = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = Color(0xFF0F52BA),
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "INVENTORY SYSTEM TNSP",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = when (activeScreen) {
                                    "dashboard" -> "Dashboard Pemantauan"
                                    "barang" -> "Katalog Master Barang"
                                    "transaksi" -> "Kelola Transaksi"
                                    "gudang" -> "Multi-Warehouse & Mutasi"
                                    else -> "Ringkasan Laporan Finansial"
                                },
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.75f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        // Quick slider notification center badge
                        BadgedBox(
                            badge = {
                                if (notificationList.isNotEmpty()) {
                                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                                        Text(notificationList.size.toString())
                                    }
                                }
                            },
                            modifier = Modifier
                                .padding(start = 12.dp, end = 4.dp)
                                .clickable {
                                    showNotificationDrawer = true
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alert notifications",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        // Professional Logout control trigger
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Sign out",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars
            ) {
                // TAB 1: DASHBOARD
                NavigationBarItem(
                    selected = activeScreen == "dashboard",
                    onClick = { activeScreen = "dashboard" },
                    icon = { Icon(Icons.Default.Dashboard, "Dash") },
                    label = { Text("Dashboard", fontSize = 10.sp) }
                )

                // TAB 2: CATALOGUE
                NavigationBarItem(
                    selected = activeScreen == "barang",
                    onClick = { activeScreen = "barang" },
                    icon = { Icon(Icons.Default.Inventory2, "Items") },
                    label = { Text("Barang", fontSize = 10.sp) }
                )

                // TAB 3: POS TRANS
                NavigationBarItem(
                    selected = activeScreen == "transaksi",
                    onClick = { 
                        transactDirectionHint = "MASUK"
                        activeScreen = "transaksi" 
                    },
                    icon = { Icon(Icons.Default.AddShoppingCart, "Tx") },
                    label = { Text("Transaksi", fontSize = 10.sp) }
                )

                // TAB 4: MULTI GUDANG
                NavigationBarItem(
                    selected = activeScreen == "gudang",
                    onClick = { activeScreen = "gudang" },
                    icon = { Icon(Icons.Default.Warehouse, "Warehouse") },
                    label = { Text("Gudang", fontSize = 10.sp) }
                )

                // TAB 5: REPORTS
                NavigationBarItem(
                    selected = activeScreen == "laporan",
                    onClick = { activeScreen = "laporan" },
                    icon = { Icon(Icons.Default.Description, "Reports") },
                    label = { Text("Laporan", fontSize = 10.sp) }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Interactive animation slide router
            Crossfade(
                targetState = activeScreen,
                animationSpec = tween(250),
                label = "router"
            ) { screen ->
                when (screen) {
                    "dashboard" -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToBarang = { activeScreen = "barang" },
                            onNavigateToTransaksi = { direction ->
                                transactDirectionHint = direction
                                activeScreen = "transaksi"
                            },
                            onNavigateToScan = {
                                launchDeviceScanner { scanCode ->
                                    // Simulated scan action: Navigate directly to barang catalogue and run search trigger
                                    viewModel.updateSearchQuery(scanCode)
                                    activeScreen = "barang"
                                }
                            },
                            onNavigateToGudang = { activeScreen = "gudang" },
                            onNavigateToLaporan = { activeScreen = "laporan" }
                        )
                    }
                    "barang" -> {
                        MasterBarangScreen(
                            viewModel = viewModel,
                            onLaunchCameraScan = { scanCallback ->
                                launchDeviceScanner(scanCallback)
                            }
                        )
                    }
                    "transaksi" -> {
                        TransactionScreen(
                            viewModel = viewModel,
                            initialType = transactDirectionHint,
                            onLaunchCameraScan = { scanCallback ->
                                launchDeviceScanner(scanCallback)
                            }
                        )
                    }
                    "gudang" -> {
                        MultiGudangScreen(viewModel = viewModel)
                    }
                    "laporan" -> {
                        ReportsScreen(viewModel = viewModel)
                    }
                }
            }

            // Global modal Drawer overlapping for system notification pile
            if (showNotificationDrawer) {
                // Simple overlay custom layout mimicking a sheet
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { showNotificationDrawer = false }
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .fillMaxWidth(0.85f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable(enabled = false) {} // block click bypass
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NotificationsActive, "bell", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pusat Notifikasi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }

                            IconButton(onClick = { showNotificationDrawer = false }) {
                                Icon(Icons.Default.Close, "close")
                            }
                        }

                        // Options to sweep alerts
                        Button(
                            onClick = { viewModel.clearNotifications() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Text("Hapus Semua Riwayat", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (notificationList.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Tidak ada notifikasi system baru.", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(notificationList) { notify ->
                                    val colorType = if (notify.type == "warning") Color(0xFFC62828) 
                                                    else if (notify.type == "success") Color(0xFF2E7D32) 
                                                    else MaterialTheme.colorScheme.primary

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = colorType.copy(alpha = 0.08f))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(colorType))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(notify.title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colorType)
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(notify.message, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Global Overlay Scanner Portal
            if (isBarcodeScreenOpen) {
                BarcodeScannerScreen(
                    viewModel = viewModel,
                    onBarcodeScanned = { activeResultCode ->
                        currentBarcodeCallback?.invoke(activeResultCode)
                        isBarcodeScreenOpen = false
                        Toast.makeText(context, "Terdeteksi Code: $activeResultCode", Toast.LENGTH_SHORT).show()
                    },
                    onDismiss = {
                        isBarcodeScreenOpen = false
                    }
                )
            }
        }
    }
}
