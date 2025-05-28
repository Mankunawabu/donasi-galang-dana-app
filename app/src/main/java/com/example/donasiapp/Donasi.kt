package com.example.donasiapp

import java.text.SimpleDateFormat
import java.util.*

data class Donasi(
    val nama: String = "",
    val kategori: String = "",
    val metodePembayaran: String = "",
    val catatan: String = "", // ✅ Tambahkan field catatan
    val jumlah: String = "",
    val waktu: String = getCurrentTimeFormatted()
) {
    companion object {
        fun getCurrentTimeFormatted(): String {
            val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            return formatter.format(Date())
        }
    }
}
