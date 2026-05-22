package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.data.Transaksi
import com.example.ui.InventoryViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    viewModel: InventoryViewModel,
    initialType: String = "MASUK",
    onLaunchCameraScan: (onCode: (String) -> Unit) -> Unit
) {
    val draftTipe by viewModel.draftTipe.collectAsState()
    val cartItems by viewModel.transactionCart.collectAsState()
    val allSupplier by viewModel.allSupplier.collectAsState()
    val selectedSupplierId by viewModel.draftSupplierId.collectAsState()
    val draftNotes by viewModel.draftNotes.collectAsState()
    val allBarang by viewModel.allBarang.collectAsState()
    val activeGudangId by viewModel.selectedGudangId.collectAsState()
    val allGudang by viewModel.allGudang.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var itemSearchQuery by remember { mutableStateOf("") }
    var selectedCompletedTxInvoice by remember { mutableStateOf<String?>(null) }
    var txStatusMessage by remember { mutableStateOf("") }

    var selectSupplierExpanded by remember { mutableStateOf(false) }

    val activeGudangName = remember(activeGudangId, allGudang) {
        if (activeGudangId == null) "Semua Gudang"
        else allGudang.find { it.id == activeGudangId }?.name ?: "Gudang Pilihan"
    }

    // Set initial configuration
    LaunchedEffect(initialType) {
        viewModel.setDraftType(initialType)
    }

    val scaffoldScrollContentPadding = 8.dp

    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // --- HEADER SELECT MANDATE ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "POS TRANSAKSI GUDANG TNSP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Lokasi Operasi: $activeGudangName",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // MASUK/KELUAR Tabs Segmented Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TabButton(
                            label = "Barang Masuk (Restock)",
                            isSelected = draftTipe == "MASUK",
                            activeColor = Color(0xFF2E7D32),
                            inactiveColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.setDraftType("MASUK")
                        }

                        TabButton(
                            label = "Barang Keluar (Dispat)",
                            isSelected = draftTipe == "KELUAR",
                            activeColor = Color(0xFFC62828),
                            inactiveColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.setDraftType("KELUAR")
                        }
                    }
                }
            }

            // --- CART DETAILS PANEL & SUPPLIER INPUT ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Keranjang Transaksi (${cartItems.size} Item)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        // Button to add item to Cart
                        Button(
                            onClick = { showAddItemDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (draftTipe == "MASUK") Color(0xFF2E7D32) else Color(0xFFC62828)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.AddShoppingCart, "Add", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tambah Barang", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // If inward, show necessary Supplier pick options
                    if (draftTipe == "MASUK") {
                        ExposedDropdownMenuBox(
                            expanded = selectSupplierExpanded,
                            onExpandedChange = { selectSupplierExpanded = !selectSupplierExpanded }
                        ) {
                            val selectedSup = allSupplier.find { it.id == selectedSupplierId }
                            OutlinedTextField(
                                value = selectedSup?.name ?: "PILIH SUPPLIER ASAL (Wajib)",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Asal Supplier", fontSize = 11.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = selectSupplierExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .menuAnchor(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = selectSupplierExpanded,
                                onDismissRequest = { selectSupplierExpanded = false }
                            ) {
                                allSupplier.forEach { sup ->
                                    DropdownMenuItem(
                                        text = { Text(sup.name) },
                                        onClick = {
                                            viewModel.selectDraftSupplier(sup.id)
                                            selectSupplierExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Card Item details scrollable
                    Box(modifier = Modifier.weight(1f)) {
                        if (cartItems.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.ProductionQuantityLimits,
                                        contentDescription = "Empty",
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Keranjang Belum Terisi",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Tambahkan barang yang akan dimasukkan/dikeluarkan.",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(horizontal = 24.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(cartItems.values.toList()) { cartItem ->
                                    CartRow(
                                        item = cartItem.barang,
                                        qty = cartItem.qty,
                                        onQtyChanged = { newQty ->
                                            viewModel.updateCartQty(cartItem.barang.kodeBarang, newQty)
                                        },
                                        onRemove = {
                                            viewModel.removeFromCart(cartItem.barang.kodeBarang)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                    // Notes details and Submit buttons
                    OutlinedTextField(
                        value = draftNotes,
                        onValueChange = { viewModel.updateDraftNotes(it) },
                        placeholder = { Text("Memo / Catatan Referensi SPK / Invoice...", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Final Submission CTA
                    Button(
                        onClick = {
                            if (draftTipe == "MASUK" && selectedSupplierId == null) {
                                viewModel.pushNotification("Validasi Error", "Pemasukan barang wajib mengikat data supplier!", "warning")
                                return@Button
                            }

                            viewModel.checkoutDraftTransaction { success, msg ->
                                txStatusMessage = msg
                                if (success) {
                                    // Extract simple time stamp sequence to show PDF ticket simulator
                                    selectedCompletedTxInvoice = "Invoice Simulator"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("checkout_transaction_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (draftTipe == "MASUK") Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    ) {
                        Icon(Icons.Default.VerifiedUser, "verified")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentUser?.role == "Staff Gudang" && draftTipe == "KELUAR") "AJUKAN TRANS KELUAR" 
                                   else "PROSES SELESAI & SIMPAN", 
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- ADD ITEM TO CART OVERLAY SHEET DIALOG ---
        if (showAddItemDialog) {
            val matchingItems = allBarang.filter {
                // Filter matching query
                (itemSearchQuery.isEmpty() || it.namaBarang.contains(itemSearchQuery, ignoreCase = true) || it.kodeBarang.contains(itemSearchQuery, ignoreCase = true)) &&
                // Filter matching assigned specific warehouse if designated
                (activeGudangId == null || it.gudangId == activeGudangId)
            }

            AlertDialog(
                onDismissRequest = { showAddItemDialog = false },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showAddItemDialog = false }) { Text("Tutup") }
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pilih Barang", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = {
                                onLaunchCameraScan { code ->
                                    val item = allBarang.find { it.barcode == code }
                                    if (item != null) {
                                        viewModel.addToCart(item, 1)
                                        showAddItemDialog = false
                                    } else {
                                        viewModel.pushNotification("Item tidak ketemu", "Barcode $code tidak terdaftar di sistem!", "warning")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.QrCodeScanner, "Barcode scan")
                        }
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Search bar inside selector
                        OutlinedTextField(
                            value = itemSearchQuery,
                            onValueChange = { itemSearchQuery = it },
                            placeholder = { Text("Masukkan nama / kode barang...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, "search") }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(modifier = Modifier.height(260.dp)) {
                            if (matchingItems.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Tidak ada barang cocok di gudang ini.", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(matchingItems) { barang ->
                                        val quantityInCart = cartItems[barang.kodeBarang]?.qty ?: 0
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.addToCart(barang, 1)
                                                },
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(barang.namaBarang, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text("Kode: ${barang.kodeBarang} • Tersedia: ${barang.stok} ${barang.satuan}", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                                }

                                                if (quantityInCart > 0) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.primary),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(quantityInCart.toString(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    Icon(Icons.Default.AddCircle, "Add", tint = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }

        // --- SUCCESS TRANSACTION MODAL RECEIPT (TRIAL EXPORTS INCLUDED!) ---
        if (selectedCompletedTxInvoice != null) {
            AlertDialog(
                onDismissRequest = { selectedCompletedTxInvoice = null },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.simulatePDFExport(selectedCompletedTxInvoice!!) { status ->
                                    txStatusMessage = status
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
                        ) {
                            Icon(Icons.Default.PictureAsPdf, "PDF")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan PDF Surat Jalan")
                        }

                        Button(
                            onClick = {
                                viewModel.simulateExcelExport(selectedCompletedTxInvoice!!) { status ->
                                    txStatusMessage = status
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.GridOn, "Excel")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Excel Spreadsheet")
                        }

                        TextButton(
                            onClick = { selectedCompletedTxInvoice = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Kembali Ke POS")
                        }
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, "verified", tint = Color(0xFF2E7D32), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Pernyataan Transaksi Selesai", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // High Fidelity Thermal Ticket layout
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)), // thermal paper theme
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "INVENTORY SYSTEM TNSP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = "GUDANG MULTIKANAVAL UTAMA", fontSize = 9.sp, color = Color.DarkGray)
                                Text(text = "- - - - - - - - - - - - - - - - - - - - ", fontSize = 9.sp, color = Color.Gray)
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = txStatusMessage, fontSize = 12.sp, color = Color.DarkGray, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(text = "- - - - - - - - - - - - - - - - - - - - ", fontSize = 9.sp, color = Color.Gray)
                                Text(text = "DOKUMEN VALID ONLINE SECARA HUKUM", fontSize = 8.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun TabButton(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) activeColor else inactiveColor)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@Composable
fun CartRow(
    item: Barang,
    qty: Int,
    onQtyChanged: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.namaBarang, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Kode: ${item.kodeBarang} • Satuan: ${item.satuan}", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
            }

            // Quantity adjust controllers
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Minus button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { onQtyChanged(qty - 1) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Remove, "minus", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                }

                // Quantity scale
                Text(
                    text = qty.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.widthIn(min = 20.dp),
                    textAlign = TextAlign.Center
                )

                // Plus button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { onQtyChanged(qty + 1) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, "plus", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Delete entire row
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, "remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun FormattedRupiahString(value: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(value).replace(",00", "")
}
