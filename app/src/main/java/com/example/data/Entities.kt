package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val name: String,
    val role: String, // "Admin", "Supervisor", "Staff Gudang"
    val assignedGudangId: Int? = null, // for multi-warehouse view filtering
    val activeToken: String? = null
)

@Entity(tableName = "gudang")
data class Gudang(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String,
    val name: String,
    val location: String
)

@Entity(tableName = "kategori")
data class Kategori(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(tableName = "supplier")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val contactName: String,
    val phone: String,
    val address: String
)

@Entity(tableName = "barang")
data class Barang(
    @PrimaryKey val kodeBarang: String,
    val barcode: String,
    val namaBarang: String,
    val kategori: String,
    val satuan: String,
    val gudangId: Int, // Current Warehouse
    val hargaModal: Double,
    val hargaJual: Double,
    val minimumStok: Int,
    val stok: Int, // Realtime stock tracking helper
    val fotoPresetName: String, // preset illustration name: "electronic", "box", "food", "beverage", "apparel"
    val supplierId: Int,
    val keterangan: String
)

@Entity(tableName = "transaksi")
data class Transaksi(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceNo: String,
    val tipe: String, // "MASUK" (IN), "KELUAR" (OUT)
    val tanggal: Long, // timestamp
    val supplierId: Int?, // ONLY if type is MASUK
    val gudangId: Int, // Which Warehouse
    val operatorUsername: String,
    val notes: String = "",
    val isApproved: Boolean = true // For supervisor optional approval flow
)

@Entity(tableName = "transaksi_detail")
data class TransaksiDetail(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val transaksiId: Int,
    val kodeBarang: String,
    val namaBarang: String,
    val qty: Int,
    val hargaModal: Double,
    val hargaJual: Double
)

@Entity(tableName = "mutasi_gudang")
data class MutasiGudang(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nomorMutasi: String,
    val tanggal: Long,
    val kodeBarang: String,
    val dariGudangId: Int,
    val keGudangId: Int,
    val qty: Int,
    val status: String, // "PENDING", "APPROVED", "COMPLETED", "REJECTED"
    val notes: String = ""
)
