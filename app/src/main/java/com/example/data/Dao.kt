package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    // --- USERS ---
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    // --- GUDANG ---
    @Query("SELECT * FROM gudang")
    fun getAllGudangFlow(): Flow<List<Gudang>>

    @Query("SELECT * FROM gudang")
    suspend fun getAllGudang(): List<Gudang>

    @Query("SELECT * FROM gudang WHERE id = :id LIMIT 1")
    suspend fun getGudangById(id: Int): Gudang?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGudang(gudang: Gudang)

    @Delete
    suspend fun deleteGudang(gudang: Gudang)

    // --- KATEGORI ---
    @Query("SELECT * FROM kategori")
    fun getAllKategoriFlow(): Flow<List<Kategori>>

    @Query("SELECT * FROM kategori")
    suspend fun getAllKategori(): List<Kategori>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKategori(kategori: Kategori)

    // --- SUPPLIER ---
    @Query("SELECT * FROM supplier")
    fun getAllSupplierFlow(): Flow<List<Supplier>>

    @Query("SELECT * FROM supplier")
    suspend fun getAllSupplier(): List<Supplier>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier)

    // --- BARANG (ITEMS) ---
    @Query("SELECT * FROM barang")
    fun getAllBarangFlow(): Flow<List<Barang>>

    @Query("SELECT * FROM barang WHERE kodeBarang = :kodeBarang LIMIT 1")
    suspend fun getBarangByKode(kodeBarang: String): Barang?

    @Query("SELECT * FROM barang WHERE barcode = :barcode LIMIT 1")
    suspend fun getBarangByBarcode(barcode: String): Barang?

    @Query("UPDATE barang SET stok = :newStok WHERE kodeBarang = :kodeBarang")
    suspend fun updateBarangStok(kodeBarang: String, newStok: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBarang(barang: Barang)

    @Delete
    suspend fun deleteBarang(barang: Barang)

    // --- TRANSAKSI ---
    @Query("SELECT * FROM transaksi ORDER BY tanggal DESC")
    fun getAllTransaksiFlow(): Flow<List<Transaksi>>

    @Query("SELECT * FROM transaksi WHERE id = :id LIMIT 1")
    suspend fun getTransaksiById(id: Int): Transaksi?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaksi(transaksi: Transaksi): Long

    @Query("UPDATE transaksi SET isApproved = :approved WHERE id = :transaksiId")
    suspend fun updateTransactionApproval(transaksiId: Int, approved: Boolean)

    // --- TRANSAKSI DETAIL ---
    @Query("SELECT * FROM transaksi_detail WHERE transaksiId = :transaksiId")
    fun getDetailsForTransaksiFlow(transaksiId: Int): Flow<List<TransaksiDetail>>

    @Query("SELECT * FROM transaksi_detail WHERE transaksiId = :transaksiId")
    suspend fun getDetailsForTransaksi(transaksiId: Int): List<TransaksiDetail>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaksiDetails(details: List<TransaksiDetail>)

    // --- MUTASI GUDANG ---
    @Query("SELECT * FROM mutasi_gudang ORDER BY tanggal DESC")
    fun getAllMutasiFlow(): Flow<List<MutasiGudang>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMutasi(mutasi: MutasiGudang): Long

    @Query("UPDATE mutasi_gudang SET status = :status WHERE id = :id")
    suspend fun updateMutasiStatus(id: Int, status: String)

    @Query("SELECT * FROM mutasi_gudang WHERE id = :id LIMIT 1")
    suspend fun getMutasiById(id: Int): MutasiGudang?
}
