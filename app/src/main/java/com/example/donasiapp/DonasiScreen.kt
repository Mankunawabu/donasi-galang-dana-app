package com.example.donasiapp.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.donasiapp.Donasi
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonasiScreen(navController: NavController, kategori: String?) {
    var nama by remember { mutableStateOf("") }
    var jumlah by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }
    var metodePembayaran by remember { mutableStateOf("") }
    var showConfirmPopup by remember { mutableStateOf(false) } // ✅ Pop-up konfirmasi
    var showSuccessPopup by remember { mutableStateOf(false) } // ✅ Pop-up sukses
    var errorMessage by remember { mutableStateOf("") }

    val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("donasi")

    val kategoriValid = listOf("Kemanusiaan", "Pendidikan", "Kesehatan", "Tanaman")
    val kategoriFinal = kategori?.takeIf { it in kategoriValid } ?: "Kemanusiaan"

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Form Donasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFF3F0), Color(0xFFFFE0E6))
                    )
                )
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Kategori: $kategoriFinal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Donatur") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = jumlah,
                    onValueChange = { input -> jumlah = input.filter { it.isDigit() } },
                    label = { Text("Jumlah Donasi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan (Opsional)") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Metode Pembayaran:", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    listOf("BRI", "BCA", "Dana", "GoPay").forEach { metode ->
                        Button(
                            onClick = { metodePembayaran = metode },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (metodePembayaran == metode) Color(0xFFFF4081) else Color.LightGray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(metode, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                ElevatedButton(
                    onClick = {
                        errorMessage = ""

                        if (nama.isBlank() || jumlah.isBlank() || metodePembayaran.isBlank()) {
                            errorMessage = "Semua kolom harus diisi!"
                            scope.launch { snackbarHostState.showSnackbar(errorMessage) }
                            return@ElevatedButton
                        }

                        val jumlahInt = jumlah.toIntOrNull()
                        if (jumlahInt == null || jumlahInt <= 0) {
                            errorMessage = "Jumlah harus berupa angka positif!"
                            scope.launch { snackbarHostState.showSnackbar(errorMessage) }
                            return@ElevatedButton
                        }

                        showConfirmPopup = true // ✅ Munculkan pop-up konfirmasi sebelum donasi
                    },
                    enabled = nama.isNotBlank() && jumlah.isNotBlank() && metodePembayaran.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.elevatedButtonColors(containerColor = Color(0xFFFF4081))
                ) {
                    Text(text = "Donasi Sekarang", color = Color.White, fontSize = 16.sp)
                }

                // ✅ Pop-up Konfirmasi
                if (showConfirmPopup) {
                    AlertDialog(
                        onDismissRequest = { showConfirmPopup = false },
                        title = { Text("Konfirmasi Donasi", fontWeight = FontWeight.Bold) },
                        text = {
                            Text("Apakah Anda yakin ingin berdonasi sebesar Rp $jumlah dengan metode $metodePembayaran?")
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showConfirmPopup = false
                                    val newRef = database.push()
                                    val sdf =
                                        SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                                    val formattedTime = sdf.format(Date())

                                    val donasiBaru = Donasi(
                                        nama = nama,
                                        jumlah = jumlah,
                                        catatan = catatan,
                                        metodePembayaran = metodePembayaran,
                                        kategori = kategoriFinal,
                                        waktu = formattedTime
                                    )

                                    newRef.setValue(donasiBaru)
                                        .addOnSuccessListener { showSuccessPopup = true }
                                        .addOnFailureListener {
                                            errorMessage = "Gagal menyimpan data, coba lagi!"
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    errorMessage
                                                )
                                            }
                                        }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFFF4081
                                    )
                                ) // ✅ Warna hijau
                            ) {
                                Text(
                                    "Ya",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ) // ✅ Teks putih
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = { showConfirmPopup = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFD3D3D3
                                    )
                                ) // ✅ Warna merah
                            ) {
                                Text(
                                    "Batal",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ) // ✅ Teks putih
                            }
                        }
                    )
                }

// ✅ Pop-up Sukses setelah donasi berhasil
                if (showSuccessPopup) {
                    AlertDialog(
                        onDismissRequest = { showSuccessPopup = false },
                        title = { Text("Donasi Berhasil!", fontWeight = FontWeight.Bold) },
                        text = { Text("Terima kasih $nama atas donasi Anda di kategori $kategoriFinal.") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showSuccessPopup = false
                                    navController.navigate(route = "dashboard")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFFF4081
                                    )
                                ) // ✅ Warna biru
                            ) {
                                Text(
                                    "OK",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ) // ✅ Teks putih
                            }
                        }
                    )
                }
            }
        }
    }
}
