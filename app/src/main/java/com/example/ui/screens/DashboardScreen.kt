package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.drawBehind
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaksi
import com.example.ui.InventoryViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: InventoryViewModel,
    onNavigateToBarang: () -> Unit,
    onNavigateToTransaksi: (String) -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToGudang: () -> Unit,
    onNavigateToLaporan: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val activeGudangId by viewModel.selectedGudangId.collectAsState()
    val allGudang by viewModel.allGudang.collectAsState()

    // Counts
    val totalItemsCount by viewModel.totalBarangCount.collectAsState()
    val uniqueItemsCount by viewModel.totalBarangUnique.collectAsState()
    val lowStockCount by viewModel.lowStockWarningCount.collectAsState()
    val txInTodayCount by viewModel.barangMasukHariIni.collectAsState()
    val txOutTodayCount by viewModel.barangKeluarHariIni.collectAsState()

    val recentTransactions by viewModel.transactions.collectAsState()

    val activeGudangName = remember(activeGudangId, allGudang) {
        if (activeGudangId == null) "Semua Gudang"
        else allGudang.find { it.id == activeGudangId }?.name ?: "Gudang Pilihan"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
            .drawBehind {
                // Glow 1: Top-left Sapphire Blue glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF0F52BA).copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.15f),
                        radius = if (size.width > 0f) size.width * 0.8f else 1f
                    )
                )
                // Glow 2: Bottom-right purple glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.10f), Color.Transparent),
                        center = Offset(size.width * 0.9f, size.height * 0.75f),
                        radius = if (size.width > 0f) size.width * 0.8f else 1f
                    )
                )
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("dashboard_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // --- PROMINENT ENTERPRISE HEADER WORKSPACE ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "GUDANG PERGUDANGAN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activeGudangName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = "Role",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentUser?.role ?: "Staff",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Petugas Aktif",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.70f)
                            )
                            Text(
                                text = currentUser?.name ?: "Operator",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Nomor ID Kerja",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.70f)
                            )
                            Text(
                                text = "TNSP-00${currentUser?.id ?: 1}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }

        // --- STATS COUNT CARDS (Grid elements) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Stock total card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory,
                                    contentDescription = "Total Stock",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Total Stok", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = NumberFormat.getInstance().format(totalItemsCount),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$uniqueItemsCount jenis barang",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Minimum Stock warning card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (lowStockCount > 0) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                         else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (lowStockCount > 0) MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Low Stock",
                                    tint = if (lowStockCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Stok Kritis",
                                fontSize = 11.sp,
                                color = if (lowStockCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = lowStockCount.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (lowStockCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "perlu dipesan segera",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // Sub rows for transactions in and out
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Inward items
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Entries",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Masuk Hari Ini", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = "$txInTodayCount Transaksi",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }

                // Outward items
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFC62828).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingDown,
                                contentDescription = "Releases",
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Keluar Hari Ini", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = "$txOutTodayCount Transaksi",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                }
            }
        }

        // --- HAND-DRAWN CUSTOM CHART VECTOR ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "GRAFIK OUTFLOW & INFLOW MINGGUAN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Aktivitas Gudang Realtime",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Masuk", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFE53E3E)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Keluar", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Draw our Canvas charting area
                    val chartBlue = MaterialTheme.colorScheme.primary
                    val chartRed = Color(0xFFE53E3E)
                    val labelColor = MaterialTheme.colorScheme.secondary

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        val width = size.width
                        val height = size.height

                        // Draw Grid Lines helper (horizontal divisions)
                        val gridCount = 4
                        for (i in 0..gridCount) {
                            val y = i * (height / gridCount)
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.15f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1f
                            )
                        }

                        // Coordinates path for Masuk (IN) line points
                        // Mon, Tue, Wed, Thu, Fri, Sat, Sun
                        val pointsIn = listOf(0.15f, 0.40f, 0.30f, 0.75f, 0.55f, 0.85f, 0.90f)
                        // Coordinates path for Keluar (OUT)
                        val pointsOut = listOf(0.35f, 0.20f, 0.60f, 0.35f, 0.70f, 0.45f, 0.50f)

                        val stepX = width / (pointsIn.size - 1)

                        // 1. Draw Inflow curve
                        val pathIn = Path()
                        val areaPathIn = Path()
                        areaPathIn.moveTo(0f, height)

                        pointsIn.forEachIndexed { i, factor ->
                            val x = i * stepX
                            val y = height - (factor * height * 0.8f) // cap height factor to fit grid padding
                            if (i == 0) {
                                pathIn.moveTo(x, y)
                                areaPathIn.lineTo(x, y)
                            } else {
                                pathIn.lineTo(x, y)
                                areaPathIn.lineTo(x, y)
                            }
                        }
                        areaPathIn.lineTo(width, height)
                        areaPathIn.close()

                        // Draw area gradient under Inflow
                        drawPath(
                            path = areaPathIn,
                            brush = Brush.verticalGradient(
                                colors = listOf(chartBlue.copy(alpha = 0.15f), Color.Transparent)
                            )
                        )

                        // Draw primary bold line
                        drawPath(
                            path = pathIn,
                            color = chartBlue,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // 2. Draw Outflow curve
                        val pathOut = Path()
                        val areaPathOut = Path()
                        areaPathOut.moveTo(0f, height)

                        pointsOut.forEachIndexed { i, factor ->
                            val x = i * stepX
                            val y = height - (factor * height * 0.8f)
                            if (i == 0) {
                                pathOut.moveTo(x, y)
                                areaPathOut.lineTo(x, y)
                            } else {
                                pathOut.lineTo(x, y)
                                areaPathOut.lineTo(x, y)
                            }
                        }
                        areaPathOut.lineTo(width, height)
                        areaPathOut.close()

                        // Area brush for outflow
                        drawPath(
                            path = areaPathOut,
                            brush = Brush.verticalGradient(
                                colors = listOf(chartRed.copy(alpha = 0.1f), Color.Transparent)
                            )
                        )

                        // Outward Stroke
                        drawPath(
                            path = pathOut,
                            color = chartRed,
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                    }

                    // Weekday labels row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min").forEach { day ->
                            Text(text = day, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        // --- DASHBOARD ACTIONS LIST ---
        item {
            Text(
                text = "Navigasi Menu Gudang",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick Action Master Barang
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { onNavigateToBarang() }
                            .padding(16.dp)
                    ) {
                        Column {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = "Master",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Master Barang", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Kelola stok & item", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    // Quick Action Scan Barcode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                            .clickable { onNavigateToScan() }
                            .padding(16.dp)
                    ) {
                        Column {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Scan Kamera", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Deteksi instan", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Barang Masuk Clickable
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToTransaksi("MASUK") },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(
                                imageVector = Icons.Default.AddBox,
                                contentDescription = "Masuk",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Barang Masuk", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Restok dari supplier", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    // Barang Keluar Clickable
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToTransaksi("KELUAR") },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(
                                imageVector = Icons.Default.IndeterminateCheckBox,
                                contentDescription = "Keluar",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Barang Keluar", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Rilis atau jual stok", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Mutasi Gudang Clickable
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToGudang() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Mutasi",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Mutasi Gudang", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Transfer antar gudang", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    // Reports Laporan Clickable
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToLaporan() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Assessment",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Laporan Stok", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("PDF & Excel spreadsheets", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        // --- RECENT COMPLETED ACTIVITIES FEED ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aktivitas Gudang Terbaru",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Lihat Semua",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLaporan() }
                )
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada riwayat transaksi dicatat.", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        } else {
            items(recentTransactions.take(3)) { tx ->
                val timeStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(tx.tanggal))
                val isMasuk = tx.tipe == "MASUK"
                val statusLabel = if (tx.isApproved) "Selesai" else "Menunggu Approval"
                val statusColor = if (tx.isApproved) Color(0xFF2E7D32) else Color(0xFFE65100)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isMasuk) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isMasuk) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = tx.tipe,
                                tint = if (isMasuk) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.invoiceNo,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isMasuk) "Pemasukan Barang" else "Pengeluaran Barang",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "•", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = timeStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(statusColor.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = statusLabel,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Opr: ${tx.operatorUsername}",
                                fontSize = 9.sp,
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
