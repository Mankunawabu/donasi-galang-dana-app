package com.example.donasiapp.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("donasi")
    var donasiList by remember { mutableStateOf<List<Pair<String, Donasi>>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Ambil data dari Firebase secara real-time
    LaunchedEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = snapshot.children.mapNotNull { data ->
                    val donasi = data.getValue(Donasi::class.java)
                    donasi?.let { Pair(data.key ?: "", it) }
                }
                donasiList = tempList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HistoryScreen", "Gagal mengambil data: ${error.message}")
            }
        }
        database.addValueEventListener(listener)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Donasi", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column {
                if (donasiList.isEmpty()) {
                    Text(
                        text = "Belum ada donasi.",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn {
                        items(donasiList, key = { it.first }) { (id, donasi) ->
                            DonasiItem(id, donasi, database, snackbarHostState)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DonasiItem(
    id: String,
    donasi: Donasi,
    database: DatabaseReference,
    snackbarHostState: SnackbarHostState
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Konfirmasi Hapus") },
            text = { Text("Apakah Anda yakin ingin menghapus donasi ini?") },
            confirmButton = {
                Button(
                    onClick = {
                        database.child(id).removeValue().addOnSuccessListener {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Donasi berhasil dihapus!")
                            }
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081))
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text("Batal", color = Color.White)
                }
            }
        )
    }

    if (showEditDialog) {
        EditDonasiDialog(id, donasi, database) { showEditDialog = false }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Nama: ${donasi.nama}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Kategori: ${donasi.kategori}", fontSize = 14.sp, color = Color.Gray)
            Text("Jumlah: Rp ${donasi.jumlah}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Metode: ${donasi.metodePembayaran}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF880E4F))
            Text("Catatan: ${donasi.catatan}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Waktu: ${donasi.waktu}", fontSize = 12.sp, color = Color.Gray)

                Row {
                    Button(
                        onClick = { showEditDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
                    ) {
                        Text("Edit", color = Color.White)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081))
                    ) {
                        Text("Hapus", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun EditDonasiDialog(id: String, donasi: Donasi, database: DatabaseReference, onDismiss: () -> Unit) {
    var catatan by remember { mutableStateOf(donasi.catatan) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Catatan Donasi") },
        text = {
            Column {
                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedDonasi = donasi.copy(catatan = catatan) // Hanya mengubah catatan
                    database.child(id).setValue(updatedDonasi)
                        .addOnSuccessListener { onDismiss() }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
            ) {
                Text("Simpan", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) {
                Text("Batal", color = Color.White)
            }
        }
    )
}

