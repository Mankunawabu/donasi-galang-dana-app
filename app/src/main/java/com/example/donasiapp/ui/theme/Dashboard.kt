package com.example.donasiapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(navController: NavController) {
    var totalDonasi by remember { mutableStateOf(0L) } // Gunakan Long untuk nilai besar
    val targetDonasi = 10_000_000L

    // Fungsi untuk memformat angka dengan titik pemisah ribuan
    fun formatRupiah(amount: Long): String {
        val format = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return format.format(amount)
    }

    // Ambil data dari Firebase
    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance().reference.child("donasi")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var total = 0L
                for (donasiSnapshot in snapshot.children) {
                    try {
                        val donasi = donasiSnapshot.getValue(Donasi::class.java)
                        val jumlahString = donasi?.jumlah ?: "0"  // Pastikan jumlah tidak null
                        val jumlah = jumlahString.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L // Hanya ambil angka
                        total += jumlah
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                totalDonasi = total
            }

            override fun onCancelled(error: DatabaseError) {
                error.toException().printStackTrace()
            }
        })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF3F0), Color(0xFFFFE0E6))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Card Header
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFFF4081)),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selamat Datang, Donatur!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Donasi telah membantu banyak orang. Terima kasih atas kebaikan Anda!",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar Donasi
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Rp ${formatRupiah(totalDonasi)} sudah terkumpul dari Rp ${formatRupiah(targetDonasi)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = (totalDonasi.toFloat() / targetDonasi).coerceIn(0f, 1f), // Batasi agar tidak error
                        color = Color(0xFFFF4081),
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pilihan Kategori Donasi
            Text(
                text = "Pilih Kategori",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Daftar kategori donasi
            val kategoriDonasi = listOf("Kemanusiaan", "Pendidikan", "Kesehatan", "Tanaman")

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                kategoriDonasi.chunked(2).forEach { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowItems.forEach { kategori ->
                            ElevatedCard(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFFF4081)),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { navController.navigate("donasi/$kategori") }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Donasi $kategori",
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol ke Riwayat Donasi
            OutlinedButton(
                onClick = { navController.navigate("history") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF333333)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Riwayat Donasi")
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}
