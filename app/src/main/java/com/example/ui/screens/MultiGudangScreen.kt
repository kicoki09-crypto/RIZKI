package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Gudang
import com.example.data.MutasiGudang
import com.example.ui.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiGudangScreen(
    viewModel: InventoryViewModel
) {
    val allGudang by viewModel.allGudang.collectAsState()
    val allBarang by viewModel.allBarang.collectAsState()
    val mutationsList by viewModel.mutations.collectAsState()
    val activeGudangId by viewModel.selectedGudangId.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var activeTab by remember { mutableStateOf("gudang") } // "gudang", "mutasi"

    var showAddGudangDialog by remember { mutableStateOf(false) }
    var showCreateMutasiDialog by remember { mutableStateOf(false) }

    // Gudang form states
    var gudangCode by remember { mutableStateOf("") }
    var gudangName by remember { mutableStateOf("") }
    var gudangLocation by remember { mutableStateOf("") }

    // Mutasi form states
    var selectedItemBarangKode by remember { mutableStateOf("") }
    var targetGudangId by remember { mutableStateOf<Int?>(null) }
    var mutasiQty by remember { mutableStateOf("") }
    var mutasiNotes by remember { mutableStateOf("") }

    var selectBarangExpanded by remember { mutableStateOf(false) }
    var selectTargetGudangExpanded by remember { mutableStateOf(false) }

    val activeGudangObj = remember(activeGudangId, allGudang) {
        allGudang.find { it.id == activeGudangId }
    }

    val userRole = currentUser?.role ?: "Staff Gudang"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- CONTROL TAB NAVIGATION BAR ---
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabButton(
                        label = "Daftar Gudang",
                        isSelected = activeTab == "gudang",
                        activeColor = MaterialTheme.colorScheme.primary,
                        inactiveColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f)
                    ) {
                        activeTab = "gudang"
                    }

                    TabButton(
                        label = "Mutasi / Transfer",
                        isSelected = activeTab == "mutasi",
                        activeColor = MaterialTheme.colorScheme.primary,
                        inactiveColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f)
                    ) {
                        activeTab = "mutasi"
                    }
                }
            }
        }

        // --- SUB SECTION AREA ---
        Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            if (activeTab == "gudang") {
                // --- LISTING GUDANG WAREHOUSES ---
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Daftar Gudang Registrasi", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Tampilkan gudang logistik aktif", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                        }

                        if (userRole == "Admin") {
                            Button(
                                onClick = { showAddGudangDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, "add")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Registrasi Gudang", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(allGudang) { g ->
                            val isAssigned = currentUser?.assignedGudangId == g.id || currentUser?.assignedGudangId == null
                            val isFocused = activeGudangId == g.id

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isFocused) 2.dp else 0.dp,
                                        color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        // Allow selection focus if authorized
                                        if (isAssigned) {
                                            viewModel.changeActiveGudang(g.id)
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isFocused) MaterialTheme.colorScheme.surface 
                                                     else if (!isAssigned) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                                     else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isFocused) MaterialTheme.colorScheme.primary 
                                                else MaterialTheme.colorScheme.primaryContainer
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warehouse,
                                            contentDescription = "Gudang",
                                            tint = if (isFocused) MaterialTheme.colorScheme.onPrimary 
                                                   else MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(g.name, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                        Text("Kode: ${g.code} • Lokasi: ${g.location}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                    }

                                    // Display access locks
                                    if (isFocused) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Active",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    } else if (!isAssigned) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // --- MULTI WAREHOUSE STOCK MUTATIONS VIEW ---
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Arsip Mutasi Antar Gudang", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Mutasi dari gudang aktif saat ini", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        }

                        Button(
                            onClick = {
                                if (activeGudangId == null) {
                                    viewModel.pushNotification("Asal Gudang Kosong", "Pilih gudang aktif Anda pada tab 'Daftar Gudang' sebelum memutasi barang!", "warning")
                                    return@Button
                                }
                                showCreateMutasiDialog = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.SwapHoriz, "swap")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buat Mutasi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (mutationsList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada mutasi terdaftar.", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 90.dp)
                        ) {
                            items(mutationsList) { m ->
                                val origGudang = allGudang.find { it.id == m.dariGudangId }?.code ?: "GD-${m.dariGudangId}"
                                val destGudang = allGudang.find { it.id == m.keGudangId }?.code ?: "GD-${m.keGudangId}"
                                val itemDetail = allBarang.find { it.kodeBarang == m.kodeBarang }
                                val itemLabel = itemDetail?.namaBarang ?: m.kodeBarang

                                val pending = m.status == "PENDING"
                                val isSupervisor = userRole == "Supervisor" || userRole == "Admin"

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
                                            Text(
                                                text = m.nomorMutasi,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        when (m.status) {
                                                            "COMPLETED" -> Color(0xFFE8F5E9)
                                                            "PENDING" -> Color(0xFFFFF3E0)
                                                            else -> Color(0xFFFFEBEE)
                                                        }
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = m.status,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (m.status) {
                                                        "COMPLETED" -> Color(0xFF2E7D32)
                                                        "PENDING" -> Color(0xFFE65100)
                                                        else -> Color(0xFFC62828)
                                                    }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(text = itemLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text(text = "Jumlah: ${m.qty} unit", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Pathway Arrow indicator
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(origGudang, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }

                                            Icon(Icons.Default.ArrowForward, "to", modifier = Modifier.size(14.dp))

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(destGudang, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }

                                        if (m.notes.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(text = "Memo: ${m.notes}", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                        }

                                        // Quick interactive supervisor approvals on card
                                        if (pending && isSupervisor) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        viewModel.updateMutationState(m.id, "COMPLETED") {}
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                    contentPadding = PaddingValues(vertical = 4.dp)
                                                ) {
                                                    Icon(Icons.Default.Check, "approve", modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Sahkan Transfer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.updateMutationState(m.id, "REJECTED") {}
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                                    contentPadding = PaddingValues(vertical = 4.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, "reject", modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Tolak", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

        // --- REGISTRATION GUDANG DIALOG ---
        if (showAddGudangDialog) {
            val kbController = LocalSoftwareKeyboardController.current

            AlertDialog(
                onDismissRequest = { showAddGudangDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            if (gudangCode.isEmpty() || gudangName.isEmpty()) return@Button
                            val g = Gudang(code = gudangCode.trim().uppercase(), name = gudangName.trim(), location = gudangLocation.trim())
                            viewModel.addGudang(g)
                            showAddGudangDialog = false
                            gudangCode = ""
                            gudangName = ""
                            gudangLocation = ""
                            kbController?.hide()
                        }
                    ) {
                        Text("Registrasi")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddGudangDialog = false }) { Text("Batal") }
                },
                title = { Text("Registrasi Cabang Gudang Baru", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = gudangCode, onValueChange = { gudangCode = it }, label = { Text("Kode Cabang (GD-xxx)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = gudangName, onValueChange = { gudangName = it }, label = { Text("Nama Binaan Gudang") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = gudangLocation, onValueChange = { gudangLocation = it }, label = { Text("Alamat Lokasi Fisik") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            )
        }

        // --- INITIATE TRANSFER MUTASI DIALOG ---
        if (showCreateMutasiDialog) {
            val activeGudangItems = allBarang.filter { it.gudangId == activeGudangId }
            val otherGudangs = allGudang.filter { it.id != activeGudangId }

            AlertDialog(
                onDismissRequest = { showCreateMutasiDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            if (selectedItemBarangKode.isEmpty() || targetGudangId == null) return@Button
                            val qtyVal = mutasiQty.toIntOrNull() ?: return@Button

                            viewModel.submitMutation(
                                selectedItemBarangKode,
                                targetGudangId!!,
                                qtyVal,
                                mutasiNotes.trim()
                            ) { success, msg ->
                                if (success) {
                                    showCreateMutasiDialog = false
                                    selectedItemBarangKode = ""
                                    targetGudangId = null
                                    mutasiQty = ""
                                    mutasiNotes = ""
                                }
                            }
                        }
                    ) {
                        Text("Ajukan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateMutasiDialog = false }) { Text("Batal") }
                },
                title = { Text("Ajukan Mutasi Pergudangan", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Gudang Asal: ${activeGudangObj?.name ?: "GD-$activeGudangId"}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

                        // 1. SELECT ITEM BARANG
                        ExposedDropdownMenuBox(
                            expanded = selectBarangExpanded,
                            onExpandedChange = { selectBarangExpanded = !selectBarangExpanded }
                        ) {
                            val selectedBarangObj = allBarang.find { it.kodeBarang == selectedItemBarangKode }
                            val labelText = if (selectedBarangObj != null) "${selectedBarangObj.namaBarang} (Stok: ${selectedBarangObj.stok})" else "PILIH BARANG"
                            
                            OutlinedTextField(
                                value = labelText,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Model Barang") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = selectBarangExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = selectBarangExpanded,
                                onDismissRequest = { selectBarangExpanded = false }
                            ) {
                                for (b in activeGudangItems) {
                                    DropdownMenuItem(
                                        text = { Text("${b.namaBarang} (${b.stok} unit)") },
                                        onClick = {
                                            selectedItemBarangKode = b.kodeBarang
                                            selectBarangExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // 2. SELECT DESTINATION TARGET GUDANG
                        ExposedDropdownMenuBox(
                            expanded = selectTargetGudangExpanded,
                            onExpandedChange = { selectTargetGudangExpanded = !selectTargetGudangExpanded }
                        ) {
                            val tarGudObj = allGudang.find { it.id == targetGudangId }
                            val labelText = tarGudObj?.name ?: "PILIH GUDANG TUJUAN"

                            OutlinedTextField(
                                value = labelText,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Gudang Tujuan") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = selectTargetGudangExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = selectTargetGudangExpanded,
                                onDismissRequest = { selectTargetGudangExpanded = false }
                            ) {
                                for (g in otherGudangs) {
                                    DropdownMenuItem(
                                        text = { Text(g.name) },
                                        onClick = {
                                            targetGudangId = g.id
                                            selectTargetGudangExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // 3. INPUT QUANTITY
                        OutlinedTextField(
                            value = mutasiQty,
                            onValueChange = { mutasiQty = it },
                            label = { Text("Kapasitas Qty") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 4. NOTE DETAILS
                        OutlinedTextField(
                            value = mutasiNotes,
                            onValueChange = { mutasiNotes = it },
                            label = { Text("Memo Mutasi") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )
                    }
                }
            )
        }
    }
}
