package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Barang
import com.example.data.Gudang
import com.example.data.Supplier
import com.example.ui.InventoryViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterBarangScreen(
    viewModel: InventoryViewModel,
    onLaunchCameraScan: (onCode: (String) -> Unit) -> Unit
) {
    val itemsList by viewModel.filteredBarang.collectAsState()
    val allKategori by viewModel.allKategori.collectAsState()
    val allGudang by viewModel.allGudang.collectAsState()
    val allSupplier by viewModel.allSupplier.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeCategory by viewModel.categoryFilter.collectAsState()
    val lowStockOnly by viewModel.lowStockFilter.collectAsState()

    var showFormSheet by remember { mutableStateOf(false) }
    var selectedItemForEdit by remember { mutableStateOf<Barang?>(null) }
    var selectedItemForDetail by remember { mutableStateOf<Barang?>(null) }

    val formattedCurrency = remember { NumberFormat.getCurrencyInstance(Locale("in", "ID")) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // --- HEADER SEARCH AND QUICK UTILS BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Large Modern filled Search input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Cari Nama / Kode / Barcode...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("item_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )

                // Quick Floating camera launch icon
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable {
                            onLaunchCameraScan { scannedCode ->
                                viewModel.updateSearchQuery(scannedCode)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Quick scan",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // --- FILTER PILL ROW ---
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter 1: Low stock alert filter
                item {
                    FilterChip(
                        selected = lowStockOnly,
                        onClick = { viewModel.toggleLowStockFilter() },
                        label = { Text("⚠️ Stok Kritis", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = if (lowStockOnly) {
                            { Icon(Icons.Default.Check, "Check", modifier = Modifier.size(14.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.error
                        )
                    )
                }

                // Filter 2: All items trigger
                item {
                    FilterChip(
                        selected = activeCategory == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("Semua Kategori", fontSize = 11.sp) }
                    )
                }

                // Listing categories
                items(allKategori) { cat ->
                    val isSelected = activeCategory == cat.name
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCategory(if (isSelected) null else cat.name) },
                        label = { Text(cat.name, fontSize = 11.sp) }
                    )
                }
            }

            // --- MAIN LIST AREA ---
            if (itemsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak Ada Barang Ditemukan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Atur kata kunci pencarian Anda atau tambahkan barang baru.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(itemsList, key = { it.kodeBarang }) { item ->
                        val isCrit = item.stok <= item.minimumStok
                        val warehouseName = allGudang.find { it.id == item.gudangId }?.code ?: "GD-?"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("barang_card_${item.kodeBarang}")
                                .clickable { selectedItemForDetail = item },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = item.kategori,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Warehouse tags
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "Loc",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = warehouseName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Custom Graphical Category Icon
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                when (item.fotoPresetName) {
                                                    "beverage" -> Color(0xFFE8F5E9)
                                                    "electronic" -> Color(0xFFE3F2FD)
                                                    "spare" -> Color(0xFFFFF3E0)
                                                    "box" -> Color(0xFFF3E5F5)
                                                    else -> Color(0xFFECEFF1)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (item.fotoPresetName) {
                                                "beverage" -> Icons.Default.LocalCafe
                                                "electronic" -> Icons.Default.Computer
                                                "spare" -> Icons.Default.Build
                                                "box" -> Icons.Default.Inventory2
                                                else -> Icons.Default.Category
                                            },
                                            contentDescription = "preset",
                                            tint = when (item.fotoPresetName) {
                                                "beverage" -> Color(0xFF2E7D32)
                                                "electronic" -> Color(0xFF1565C0)
                                                "spare" -> Color(0xFFEF6C00)
                                                "box" -> Color(0xFF6A1B9A)
                                                else -> Color(0xFF37474F)
                                            },
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.namaBarang,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Kode: ${item.kodeBarang} • Satuan: ${item.satuan}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }

                                    // Stock Level Block
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${item.stok} ${item.satuan}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isCrit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (isCrit) MaterialTheme.colorScheme.errorContainer
                                                    else Color(0xFFE8F5E9)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isCrit) "Kritis" else "Aman",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCrit) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.15f))

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(text = "Harga Modal / Jual:", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = FormattedRupiah(item.hargaModal),
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(text = " » ", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                            Text(
                                                text = FormattedRupiah(item.hargaJual),
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // --- HIGH CONTRAST BARCODE RENDERING CANVASES ---
                                    Column(horizontalAlignment = Alignment.End) {
                                        Canvas(
                                            modifier = Modifier
                                                .width(76.dp)
                                                .height(24.dp)
                                                .border(0.5.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                                                .background(Color.White)
                                        ) {
                                            // Draw barcode vector lines
                                            val w = size.width
                                            val h = size.height
                                            val lineThicknesses = listOf(1f, 3f, 1f, 2f, 4f, 1f, 2f, 1f, 3f, 1f, 4f, 2f, 1f, 3f, 1f, 1f)
                                            val spaceSteps = listOf(2f, 1f, 3f, 2f, 1f, 3f, 1f, 2f, 2f, 1f, 2f, 3f, 1f, 2f, 1f, 1f)

                                            var tempX = 4f
                                            for (idx in lineThicknesses.indices) {
                                                if (tempX > w - 6f) break
                                                drawLine(
                                                    color = Color.Black,
                                                    start = Offset(tempX, 2f),
                                                    end = Offset(tempX, h - 2f),
                                                    strokeWidth = lineThicknesses[idx]
                                                )
                                                tempX += lineThicknesses[idx] + spaceSteps[idx]
                                            }
                                        }
                                        Text(
                                            text = item.barcode.take(13),
                                            fontSize = 8.sp,
                                            fontFamily = FontFamily.Monospace,
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

        // --- FLOATING ACTION BUTTON (CRUD INSERT) ---
        FloatingActionButton(
            onClick = {
                selectedItemForEdit = null
                showFormSheet = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_item_fab"),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Insert Item", tint = MaterialTheme.colorScheme.onPrimary)
        }

        // --- PRODUCT DETAILS DIALOG ---
        if (selectedItemForDetail != null) {
            val d = selectedItemForDetail!!
            val assignedGudang = allGudang.find { it.id == d.gudangId }
            val assignedSupplier = allSupplier.find { it.id == d.supplierId }

            AlertDialog(
                onDismissRequest = { selectedItemForDetail = null },
                confirmButton = {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                selectedItemForEdit = d
                                selectedItemForDetail = null
                                showFormSheet = true
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit")
                        }

                        Button(
                            onClick = {
                                viewModel.deleteProduct(d)
                                selectedItemForDetail = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, "Hapus", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hapus")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedItemForDetail = null }) {
                        Text("Tutup")
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Info, "Info", tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Detail Informasi Barang", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = d.namaBarang, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                Text(text = "Kategori: ${d.kategori} • Satuan: ${d.satuan}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }

                        DetailRow(label = "Kode Barang", value = d.kodeBarang)
                        DetailRow(label = "Nomor Barcode", value = d.barcode)
                        DetailRow(label = "Tingkat Stok", value = "${d.stok} ${d.satuan} (Minimum: ${d.minimumStok})")
                        DetailRow(label = "Gudang Lokasi", value = assignedGudang?.name ?: "GD-Main sunter")
                        DetailRow(label = "Supplier Utama", value = assignedSupplier?.name ?: "PT. Sinar Abadi")
                        DetailRow(label = "Harga Modal", value = FormattedRupiah(d.hargaModal))
                        DetailRow(label = "Harga Jual", value = FormattedRupiah(d.hargaJual))
                        
                        val margin = d.hargaJual - d.hargaModal
                        val profitPercent = if (d.hargaModal > 0) (margin/d.hargaModal)*100 else 0.0
                        DetailRow(label = "Proyeksi Profit", value = "${FormattedRupiah(margin)} (${String.format("%.1f", profitPercent)}%)")

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Keterangan Tambahan:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = d.keterangan.ifEmpty { "Tidak ada keterangan tambahan." },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }

        // --- DYNAMIC FORM SHEET (CRUD ADD / UPDATE) ---
        if (showFormSheet) {
            val isEdit = selectedItemForEdit != null

            var kodeBarang by remember { mutableStateOf(selectedItemForEdit?.kodeBarang ?: "") }
            var barcode by remember { mutableStateOf(selectedItemForEdit?.barcode ?: "") }
            var namaBarang by remember { mutableStateOf(selectedItemForEdit?.namaBarang ?: "") }
            var kategori by remember { mutableStateOf(selectedItemForEdit?.kategori ?: "Elektronik") }
            var satuan by remember { mutableStateOf(selectedItemForEdit?.satuan ?: "Unit") }
            var selectedGudangId by remember { mutableStateOf(selectedItemForEdit?.gudangId ?: (allGudang.firstOrNull()?.id ?: 1)) }
            var hargaModalStr by remember { mutableStateOf(selectedItemForEdit?.hargaModal?.toLong()?.toString() ?: "") }
            var hargaJualStr by remember { mutableStateOf(selectedItemForEdit?.hargaJual?.toLong()?.toString() ?: "") }
            var minStokStr by remember { mutableStateOf(selectedItemForEdit?.minimumStok?.toString() ?: "10") }
            var currentStokStr by remember { mutableStateOf(selectedItemForEdit?.stok?.toString() ?: "0") }
            var supplierId by remember { mutableStateOf(selectedItemForEdit?.supplierId ?: (allSupplier.firstOrNull()?.id ?: 1)) }
            var keterangan by remember { mutableStateOf(selectedItemForEdit?.keterangan ?: "") }
            var fotoPresetName by remember { mutableStateOf(selectedItemForEdit?.fotoPresetName ?: "electronic") }

            var selectGudangExpanded by remember { mutableStateOf(false) }
            var selectSupplierExpanded by remember { mutableStateOf(false) }

            val keyboardController = LocalSoftwareKeyboardController.current

            AlertDialog(
                onDismissRequest = { showFormSheet = false },
                confirmButton = {
                    Button(
                        onClick = {
                            if (kodeBarang.isEmpty() || namaBarang.isEmpty()) {
                                return@Button
                            }

                            val hModal = hargaModalStr.toDoubleOrNull() ?: 0.0
                            val hJual = hargaJualStr.toDoubleOrNull() ?: 0.0
                            val minStok = minStokStr.toIntOrNull() ?: 5
                            val stokVal = currentStokStr.toIntOrNull() ?: 0

                            val b = Barang(
                                kodeBarang = kodeBarang.trim(),
                                barcode = barcode.ifEmpty { kodeBarang },
                                namaBarang = namaBarang.trim(),
                                kategori = kategori,
                                satuan = satuan.trim(),
                                gudangId = selectedGudangId,
                                hargaModal = hModal,
                                hargaJual = hJual,
                                minimumStok = minStok,
                                stok = stokVal,
                                fotoPresetName = fotoPresetName,
                                supplierId = supplierId,
                                keterangan = keterangan.trim()
                            )

                            viewModel.addOrUpdateBarang(b)
                            showFormSheet = false
                            keyboardController?.hide()
                        }
                    ) {
                        Text(if (isEdit) "Simpan Perubahan" else "Daftarkan Barang")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFormSheet = false }) {
                        Text("Batal")
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isEdit) Icons.Default.EditNote else Icons.Default.AddBox,
                            contentDescription = "form",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (isEdit) "Koreksi Formulir Stok" else "Tambah Model Barang", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Kode Barang (Editable only when creating)
                        OutlinedTextField(
                            value = kodeBarang,
                            onValueChange = { kodeBarang = it },
                            label = { Text("Kode Barang (Misal: TECH007)") },
                            enabled = !isEdit,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Barcode Scan field with camera buttons!
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("EAN Barcode Code") },
                            trailingIcon = {
                                Row {
                                    IconButton(
                                        onClick = {
                                            // Auto-generate barcode numbers
                                            barcode = "BAR-" + Random.nextInt(100000, 999999).toString()
                                        }
                                    ) {
                                        Icon(Icons.Default.Autorenew, "Generate")
                                    }
                                    IconButton(
                                        onClick = {
                                            onLaunchCameraScan { code ->
                                                barcode = code
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.QrCodeScanner, "Scan")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Item label name
                        OutlinedTextField(
                            value = namaBarang,
                            onValueChange = { namaBarang = it },
                            label = { Text("Nama Lengkap Barang") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Category Select Grid
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Filter Kategori:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    listOf("Elektronik", "Makanan & Minuman", "Suku Cadang", "Alat Tulis", "Pakaian").forEach { c ->
                                        val isSelected = kategori == c
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { 
                                                kategori = c
                                                // auto swap picture presets
                                                fotoPresetName = when (c) {
                                                    "Makanan & Minuman" -> "beverage"
                                                    "Elektronik" -> "electronic"
                                                    "Suku Cadang" -> "spare"
                                                    "Alat Tulis" -> "box"
                                                    else -> "pack"
                                                }
                                            },
                                            label = { Text(c.take(12), fontSize = 9.sp) },
                                            modifier = Modifier.padding(2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Satuan
                        OutlinedTextField(
                            value = satuan,
                            onValueChange = { satuan = it },
                            label = { Text("Satuan (Pcs, Dus, Pack, Kg)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Warehouse Dropdown Choice selection
                        ExposedDropdownMenuBox(
                            expanded = selectGudangExpanded,
                            onExpandedChange = { selectGudangExpanded = !selectGudangExpanded }
                        ) {
                            val activeGudObj = allGudang.find { it.id == selectedGudangId }
                            OutlinedTextField(
                                value = activeGudObj?.name ?: "GD-${selectedGudangId}",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Simpan Di Gudang") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = selectGudangExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = selectGudangExpanded,
                                onDismissRequest = { selectGudangExpanded = false }
                            ) {
                                allGudang.forEach { g ->
                                    DropdownMenuItem(
                                        text = { Text("${g.code} - ${g.name}") },
                                        onClick = {
                                            selectedGudangId = g.id
                                            selectGudangExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Prices Row
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = hargaModalStr,
                                onValueChange = { hargaModalStr = it },
                                label = { Text("Modal (HPP)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = hargaJualStr,
                                onValueChange = { hargaJualStr = it },
                                label = { Text("Harga Jual") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }

                        // Stock limits
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = minStokStr,
                                onValueChange = { minStokStr = it },
                                label = { Text("Minimum Stok") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = currentStokStr,
                                onValueChange = { currentStokStr = it },
                                label = { Text("Stok Awal") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                enabled = !isEdit // editing stock directly is prohibited, must use Masuk/Keluar receipts!
                            )
                        }

                        // Supplier Dropdown Choices
                        ExposedDropdownMenuBox(
                            expanded = selectSupplierExpanded,
                            onExpandedChange = { selectSupplierExpanded = !selectSupplierExpanded }
                        ) {
                            val activeSupObj = allSupplier.find { it.id == supplierId }
                            OutlinedTextField(
                                value = activeSupObj?.name ?: "Supplier-${supplierId}",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Koneksi Supplier") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = selectSupplierExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = selectSupplierExpanded,
                                onDismissRequest = { selectSupplierExpanded = false }
                            ) {
                                allSupplier.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s.name) },
                                        onClick = {
                                            supplierId = s.id
                                            selectSupplierExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Keterangan
                        OutlinedTextField(
                            value = keterangan,
                            onValueChange = { keterangan = it },
                            label = { Text("Keterangan Tambahan / Lokasi Rak") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun FormattedRupiah(value: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(value).replace(",00", "")
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
