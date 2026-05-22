package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaksi
import com.example.ui.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: InventoryViewModel
) {
    val allTransactions by viewModel.transactions.collectAsState(initial = emptyList())
    val allBarang by viewModel.allBarang.collectAsState(initial = emptyList())
    val allGudang by viewModel.allGudang.collectAsState(initial = emptyList())

    var activeTabFilter by remember { mutableStateOf("SEMUA") } // "SEMUA", "MASUK", "KELUAR"
    var showExportSuccessBanner by remember { mutableStateOf<String?>(null) }
    var isCompilingLoader by remember { mutableStateOf(false) }

    // Computations
    val totalCapitalValuation = remember(allBarang) {
        allBarang.sumOf { it.stok * it.hargaModal }
    }
    val totalSellingValuation = remember(allBarang) {
        allBarang.sumOf { it.stok * it.hargaJual }
    }
    val potentialGrossProfit = remember(totalCapitalValuation, totalSellingValuation) {
        totalSellingValuation - totalCapitalValuation
    }

    val filteredList = remember(allTransactions, activeTabFilter) {
        if (activeTabFilter == "SEMUA") allTransactions
        else allTransactions.filter { it.tipe == activeTabFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- VALUATION FINANCIAL OVERVIEW PANEL ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "VALUASI STOK FINANSIAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Akurasi Laporan Gudang",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Nilai Kapital (HPP)", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = FormattedRupiahString(totalCapitalValuation),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Nilai Jual Jaring", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = FormattedRupiahString(totalSellingValuation),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Estimasi Margin Profit", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = FormattedRupiahString(potentialGrossProfit),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    // Profit percentage growth badge representation
                    val growthPercent = if (totalCapitalValuation > 0.0) (potentialGrossProfit / totalCapitalValuation) * 100 else 0.0
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+${String.format("%.1f", growthPercent)}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }

        // --- FILTER PILL SELECTORS & ACTIONS ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter Tab Row
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("SEMUA", "MASUK", "KELUAR").forEach { opt ->
                        val isSel = activeTabFilter == opt
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSel) MaterialTheme.colorScheme.primary 
                                    else Color.Transparent
                                )
                                .clickable { activeTabFilter = opt }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = opt,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Export buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Excel
                    IconButton(
                        onClick = {
                            isCompilingLoader = true
                            viewModel.simulateExcelExport("Laporan_Stok_Gudang") { output ->
                                isCompilingLoader = false
                                showExportSuccessBanner = output
                            }
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    ) {
                        Icon(Icons.Default.GridOn, "excel", tint = Color.White, modifier = Modifier.size(16.dp))
                    }

                    // PDF
                    IconButton(
                        onClick = {
                            isCompilingLoader = true
                            viewModel.simulatePDFExport("Laporan_Ringkasan_Transaksi") { output ->
                                isCompilingLoader = false
                                showExportSuccessBanner = output
                            }
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    ) {
                        Icon(Icons.Default.PictureAsPdf, "pdf", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- LOADER SPINNER OR EXPORTS OUTPUT INDICATOR ---
        if (isCompilingLoader) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text("Menghimpun Dokumen Digital...", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        // --- ARCHIVE TRANSACTIONS LOG VIEW ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Archive kosong untuk kriteria filter terpilih.", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(filteredList) { tx ->
                        val isIn = tx.tipe == "MASUK"
                        val formattedDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(tx.tanggal))
                        val activeGudName = allGudang.find { it.id == tx.gudangId }?.code ?: "GD-${tx.gudangId}"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(if (isIn) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isIn) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                                contentDescription = tx.tipe,
                                                tint = if (isIn) Color(0xFF2E7D32) else Color(0xFFC62828),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(tx.invoiceNo, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(text = formattedDate, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(activeGudName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                if (tx.notes.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Catatan: ${tx.notes}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Penata: ${tx.operatorUsername}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                            .clickable {
                                                isCompilingLoader = true
                                                viewModel.simulatePDFExport(tx.invoiceNo) { output ->
                                                    isCompilingLoader = false
                                                    showExportSuccessBanner = output
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Download, "dl", modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Unduh Tiket", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- DOWNLOAD SUCCESS BANNER SHEET DIALOG ---
        if (showExportSuccessBanner != null) {
            AlertDialog(
                onDismissRequest = { showExportSuccessBanner = null },
                confirmButton = {
                    Button(onClick = { showExportSuccessBanner = null }) {
                        Text("Mengerti")
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDone, "saved", tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Export Sukses", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text(showExportSuccessBanner!!, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            )
        }
    }
}
