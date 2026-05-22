package com.example.data

import com.example.data.InventoryDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class InventoryRepository(private val dao: InventoryDao) {

    // Streams of primary tables
    val allUsers: Flow<List<User>> = dao.getAllUsers()
    val allGudang: Flow<List<Gudang>> = dao.getAllGudangFlow()
    val allKategori: Flow<List<Kategori>> = dao.getAllKategoriFlow()
    val allSupplier: Flow<List<Supplier>> = dao.getAllSupplierFlow()
    val allBarang: Flow<List<Barang>> = dao.getAllBarangFlow()
    val allTransaksi: Flow<List<Transaksi>> = dao.getAllTransaksiFlow()
    val allMutasi: Flow<List<MutasiGudang>> = dao.getAllMutasiFlow()

    fun getDetailsForTransaksi(transaksiId: Int): Flow<List<TransaksiDetail>> {
        return dao.getDetailsForTransaksiFlow(transaksiId)
    }

    suspend fun getBarangByKode(kodeBarang: String): Barang? = dao.getBarangByKode(kodeBarang)
    suspend fun getBarangByBarcode(barcode: String): Barang? = dao.getBarangByBarcode(barcode)
    suspend fun getUserByUsername(username: String): User? = dao.getUserByUsername(username)

    // User authentication login persistence simulated securely
    suspend fun authenticateUser(username: String, roleHint: String): User? {
        val dbUser = dao.getUserByUsername(username)
        if (dbUser != null) {
            val token = UUID.randomUUID().toString()
            val updatedUser = dbUser.copy(activeToken = token)
            dao.insertUser(updatedUser)
            return updatedUser
        }
        return null
    }

    suspend fun logoutUser(user: User) {
        dao.insertUser(user.copy(activeToken = null))
    }

    // --- WRITE ACTIONS ---

    suspend fun saveBarang(barang: Barang) {
        dao.insertBarang(barang)
    }

    suspend fun deleteBarang(barang: Barang) {
        dao.deleteBarang(barang)
    }

    suspend fun saveGudang(gudang: Gudang) {
        dao.insertGudang(gudang)
    }

    suspend fun saveSupplier(supplier: Supplier) {
        dao.insertSupplier(supplier)
    }

    suspend fun saveKategori(kategori: Kategori) {
        dao.insertKategori(kategori)
    }

    /**
     * Inserts a transaction and automatically updates product stock!
     * If MASUK: increment stok.
     * If KELUAR: decrement stok (while verifying boundaries).
     */
    suspend fun insertTransaksiWithDetails(transaksi: Transaksi, details: List<TransaksiDetail>): Boolean {
        // Validation check for OUTFLOW
        if (transaksi.tipe == "KELUAR") {
            for (detail in details) {
                val barang = dao.getBarangByKode(detail.kodeBarang) ?: return false
                if (barang.stok < detail.qty) {
                    // Outflow quantity exceeds current stock level
                    return false
                }
            }
        }

        // Insert Transaction Header
        val txId = dao.insertTransaksi(transaksi).toInt()

        // Bind Detail rows to Transaction ID
        val updatedDetails = details.map { it.copy(transaksiId = txId) }
        dao.insertTransaksiDetails(updatedDetails)

        // Adjust stocks for each product if transaction is instantly approved/completed
        if (transaksi.isApproved) {
            applyStockAdjustmentForTx(transaksi.tipe, details)
        }
        return true
    }

    suspend fun approveTx(transaksiId: Int): Boolean {
        val tx = dao.getTransaksiById(transaksiId) ?: return false
        if (tx.isApproved) return true // already approved

        val details = dao.getDetailsForTransaksi(transaksiId)
        // Verify stock once more for outward releases
        if (tx.tipe == "KELUAR") {
            for (detail in details) {
                val barang = dao.getBarangByKode(detail.kodeBarang) ?: return false
                if (barang.stok < detail.qty) {
                    return false // stock insufficient
                }
            }
        }

        dao.updateTransactionApproval(transaksiId, true)
        applyStockAdjustmentForTx(tx.tipe, details)
        return true
    }

    private suspend fun applyStockAdjustmentForTx(tipe: String, details: List<TransaksiDetail>) {
        for (detail in details) {
            val barang = dao.getBarangByKode(detail.kodeBarang)
            if (barang != null) {
                val newStok = if (tipe == "MASUK") {
                    barang.stok + detail.qty
                } else {
                    barang.stok - detail.qty
                }
                dao.updateBarangStok(barang.kodeBarang, newStok)
            }
        }
    }

    // --- MUTATION OF STOCK INTER-WAREHOUSES ---

    suspend fun requestMutasi(mutasi: MutasiGudang): Boolean {
        // Validate source warehouse stock level
        val barang = dao.getBarangByKode(mutasi.kodeBarang) ?: return false
        if (barang.stok < mutasi.qty) {
            return false // insufficient source stock
        }

        dao.insertMutasi(mutasi)
        return true
    }

    suspend fun processMutasi(mutasiId: Int, newStatus: String): Boolean {
        val mutasi = dao.getMutasiById(mutasiId) ?: return false
        if (mutasi.status == "COMPLETED" || mutasi.status == "REJECTED") return false

        if (newStatus == "COMPLETED" || newStatus == "APPROVED") {
            val barang = dao.getBarangByKode(mutasi.kodeBarang) ?: return false
            if (barang.stok < mutasi.qty) return false // validation failure

            // Deduct stock from current warehouse source
            dao.updateBarangStok(barang.kodeBarang, barang.stok - mutasi.qty)

            // Look up if this item also exists in target gudang
            // If yes, increase target stock. If no, create a record clone assigned to the target warehouse!
            val targetGudangRecord = dao.getAllBarangFlow().first().firstOrNull { 
                it.kodeBarang == mutasi.kodeBarang + "_" + mutasi.keGudangId || 
                (it.kodeBarang == mutasi.kodeBarang && it.gudangId == mutasi.keGudangId)
            }

            if (targetGudangRecord != null) {
                dao.updateBarangStok(targetGudangRecord.kodeBarang, targetGudangRecord.stok + mutasi.qty)
            } else {
                // Clone the item characteristics and register it under the destination warehouse with transfer stock
                val targetItem = barang.copy(
                    kodeBarang = barang.kodeBarang + "_" + mutasi.keGudangId,
                    barcode = barang.barcode + "-" + mutasi.keGudangId,
                    gudangId = mutasi.keGudangId,
                    stok = mutasi.qty
                )
                dao.insertBarang(targetItem)
            }

            dao.updateMutasiStatus(mutasiId, "COMPLETED")
        } else {
            dao.updateMutasiStatus(mutasiId, newStatus)
        }
        return true
    }

    // --- HYDRATE SIMULATED INITIAL DATABASE ---

    suspend fun prepopulateDbIfNeeded() {
        // Check if database is empty - use first database read to safely evaluate
        val hasGudangs = dao.getAllGudang().isNotEmpty()
        if (hasGudangs) return

        // 1. Populating default warehouses
        val g1 = Gudang(id = 1, code = "GD-UTAMA", name = "Gudang Utama Jakarta", location = "Sunter Agung, Jakarta Utara")
        val g2 = Gudang(id = 2, code = "GD-BARAT", name = "Gudang Cabang Barat", location = "Cikupa, Tangerang Banten")
        val g3 = Gudang(id = 3, code = "GD-TIMUR", name = "Gudang Hub Timur", location = "Margomulyo, Surabaya")
        dao.insertGudang(g1)
        dao.insertGudang(g2)
        dao.insertGudang(g3)

        // 2. Populating categories
        dao.insertKategori(Kategori(id = 1, name = "Elektronik"))
        dao.insertKategori(Kategori(id = 2, name = "Makanan & Minuman"))
        dao.insertKategori(Kategori(id = 3, name = "Suku Cadang"))
        dao.insertKategori(Kategori(id = 4, name = "Alat Tulis"))
        dao.insertKategori(Kategori(id = 5, name = "Pakaian"))

        // 3. Populating suppliers
        val s1 = Supplier(id = 1, name = "PT. Sinar Abadi Sentosa", contactName = "Rudi Hartono", phone = "021-5551234", address = "Kawasan Industri Pulo Gadung, Jakarta")
        val s2 = Supplier(id = 2, name = "CV. Mitra Global Niaga", contactName = "Sari Merdeka", phone = "031-778844", address = "Kupang Indah, Surabaya")
        val s3 = Supplier(id = 3, name = "PT. Logistik Nusantara Jaya", contactName = "Hadi Wijaya", phone = "021-998811", address = "Soekarno-Hatta, Bandung")
        dao.insertSupplier(s1)
        dao.insertSupplier(s2)
        dao.insertSupplier(s3)

        // 4. Populating users
        val u1 = User(id = 1, username = "admin", name = "Andi Saputra", role = "Admin", assignedGudangId = null)
        val u2 = User(id = 2, username = "supervisor", name = "Budi Hartono", role = "Supervisor", assignedGudangId = 1)
        val u3 = User(id = 3, username = "staff", name = "Chandra Kirana", role = "Staff Gudang", assignedGudangId = 1)
        dao.insertUser(u1)
        dao.insertUser(u2)
        dao.insertUser(u3)

        // 5. Populating standard stock items (barang)
        val b1 = Barang(
            kodeBarang = "BEER123",
            barcode = "8992009033314",
            namaBarang = "Bir Bintang Kaleng 320ml",
            kategori = "Makanan & Minuman",
            satuan = "Dus",
            gudangId = 1,
            hargaModal = 120000.0,
            hargaJual = 145000.0,
            minimumStok = 20,
            stok = 45,
            fotoPresetName = "beverage",
            supplierId = 2,
            keterangan = "Minuman kaleng dingin, simpan pada suhu pendingin."
        )
        val b2 = Barang(
            kodeBarang = "SHAMP009",
            barcode = "8993005011122",
            namaBarang = "Shampoo Pantene Pro-V 150ml",
            kategori = "Makanan & Minuman",
            satuan = "Karton",
            gudangId = 1,
            hargaModal = 240000.0,
            hargaJual = 295000.0,
            minimumStok = 15,
            stok = 5,  // Under stock notice!
            fotoPresetName = "pack",
            supplierId = 1,
            keterangan = "Perawatan rambut. Jauhkan dari sinar matahari langsung."
        )
        val b3 = Barang(
            kodeBarang = "TECH88",
            barcode = "4711081822836",
            namaBarang = "Laptop Asus ZenBook 14",
            kategori = "Elektronik",
            satuan = "Unit",
            gudangId = 1,
            hargaModal = 11500000.0,
            hargaJual = 13499000.0,
            minimumStok = 5,
            stok = 12,
            fotoPresetName = "electronic",
            supplierId = 1,
            keterangan = "Laptop intel core i7, RAM 16GB, SSD 512GB."
        )
        val b4 = Barang(
            kodeBarang = "PHONE22",
            barcode = "8806094723145",
            namaBarang = "Samsung Galaxy A54 5G",
            kategori = "Elektronik",
            satuan = "Unit",
            gudangId = 2, // West warehouse Branch!
            hargaModal = 4200000.0,
            hargaJual = 4999000.0,
            minimumStok = 10,
            stok = 8, // Low stock in West branch!
            fotoPresetName = "electronic",
            supplierId = 1,
            keterangan = "Warna Awesome Graphite, 8GB RAM, 256GB Internal."
        )
        val b5 = Barang(
            kodeBarang = "ALAT77",
            barcode = "8996001301140",
            namaBarang = "Buku Tulis Sinar Dunia 38 Lembar",
            kategori = "Alat Tulis",
            satuan = "Pack",
            gudangId = 1,
            hargaModal = 350000.0,
            hargaJual = 45000.0,
            minimumStok = 30,
            stok = 85,
            fotoPresetName = "box",
            supplierId = 3,
            keterangan = "Kertas putih bergaris isi 10 buku per pack."
        )
        val b6 = Barang(
            kodeBarang = "SPARE44",
            barcode = "4975983200918",
            namaBarang = "Aki Kering GS Astra MF GTZ5S",
            kategori = "Suku Cadang",
            satuan = "Unit",
            gudangId = 3, // East warehouse Hub!
            hargaModal = 185000.0,
            hargaJual = 245000.0,
            minimumStok = 8,
            stok = 15,
            fotoPresetName = "spare",
            supplierId = 3,
            keterangan = "Aki motor kering maintenance free."
        )

        dao.insertBarang(b1)
        dao.insertBarang(b2)
        dao.insertBarang(b3)
        dao.insertBarang(b4)
        dao.insertBarang(b5)
        dao.insertBarang(b6)

        // 6. Populating 2 historic transactions
        val t1 = Transaksi(
            id = 1,
            invoiceNo = "TR-IN-20260522001",
            tipe = "MASUK",
            tanggal = System.currentTimeMillis() - 3600000 * 3, // 3 hours ago
            supplierId = 1,
            gudangId = 1,
            operatorUsername = "admin",
            notes = "Restok awal komputer dari PT. Sinar Abadi.",
            isApproved = true
        )
        dao.insertTransaksi(t1)
        val d1 = TransaksiDetail(
            id = 1,
            transaksiId = 1,
            kodeBarang = "TECH88",
            namaBarang = "Laptop Asus ZenBook 14",
            qty = 5,
            hargaModal = 11500000.0,
            hargaJual = 13499000.0
        )
        dao.insertTransaksiDetails(listOf(d1))

        val t2 = Transaksi(
            id = 2,
            invoiceNo = "TR-OUT-20260522001",
            tipe = "KELUAR",
            tanggal = System.currentTimeMillis() - 3600000 * 1, // 1 hour ago
            supplierId = null,
            gudangId = 1,
            operatorUsername = "staff",
            notes = "Pengambilan untuk display toko.",
            isApproved = true
        )
        dao.insertTransaksi(t2)
        val d2 = TransaksiDetail(
            id = 2,
            transaksiId = 2,
            kodeBarang = "BEER123",
            namaBarang = "Bir Bintang Kaleng 320ml",
            qty = 3,
            hargaModal = 120000.0,
            hargaJual = 145000.0
        )
        dao.insertTransaksiDetails(listOf(d2))

        // 7. Populating 1 Warehouse Mutation
        val mt1 = MutasiGudang(
            id = 1,
            nomorMutasi = "MT-MUT-20260522001",
            tanggal = System.currentTimeMillis() - 3600000 * 5,
            kodeBarang = "ALAT77",
            dariGudangId = 1,
            keGudangId = 2,
            qty = 10,
            status = "COMPLETED",
            notes = "Transfer buku tulis ke cabang barat."
        )
        dao.insertMutasi(mt1)
    }

    private fun valBarcode(code: String): String {
        return "BAR-$code"
    }
}
