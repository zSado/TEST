package com.example.controlaforo.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Un registro de la colección "historial_movimientos" (lo escribe MainScreen).
data class Movimiento(
    val id: String,
    val accion: String,
    val usuario: String,
    val personasResultado: Int,
    val fecha: Date?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()

    var movimientos by remember { mutableStateOf<List<Movimiento>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) }

    // Escucha en tiempo real los últimos 100 movimientos, del más nuevo al más antiguo.
    DisposableEffect(Unit) {
        val listener = db.collection("historial_movimientos")
            .orderBy("fecha_hora", Query.Direction.DESCENDING)
            .limit(100L)
            .addSnapshotListener { snapshot, e ->
                cargando = false
                if (e != null) {
                    error = "No se pudo cargar el historial."
                    return@addSnapshotListener
                }
                error = null
                movimientos = snapshot?.documents?.map { doc ->
                    Movimiento(
                        id = doc.id,
                        accion = doc.getString("accion") ?: "-",
                        usuario = doc.getString("usuario") ?: "Desconocido",
                        personasResultado = doc.getLong("personas_resultado")?.toInt() ?: 0,
                        fecha = doc.getTimestamp("fecha_hora")?.toDate()
                    )
                } ?: emptyList()
            }
        onDispose { listener.remove() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de movimientos") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Volver") }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                cargando -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                error != null -> Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )

                movimientos.isEmpty() -> Text(
                    text = "Aún no hay movimientos registrados.",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )

                else -> {
                    val entradas = movimientos.count { it.accion == "Entrada" }
                    val salidas = movimientos.count { it.accion == "Salida" }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            ResumenHistorial(
                                total = movimientos.size,
                                entradas = entradas,
                                salidas = salidas
                            )
                        }
                        items(movimientos, key = { it.id }) { mov ->
                            MovimientoItem(
                                movimiento = mov,
                                fechaTexto = mov.fecha?.let { formatoFecha.format(it) } ?: "Sin fecha"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenHistorial(total: Int, entradas: Int, salidas: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DatoResumen(titulo = "Registros", valor = total.toString())
            DatoResumen(titulo = "Entradas", valor = entradas.toString())
            DatoResumen(titulo = "Salidas", valor = salidas.toString())
        }
    }
}

@Composable
private fun DatoResumen(titulo: String, valor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = valor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = titulo, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MovimientoItem(movimiento: Movimiento, fechaTexto: String) {
    val esEntrada = movimiento.accion == "Entrada"
    // Mismos colores que los botones de MainScreen.
    val colorAccion = if (esEntrada) Color(0xFF388E3C) else Color(0xFFD32F2F)

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(color = colorAccion, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = movimiento.accion,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = movimiento.usuario, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(
                    text = fechaTexto,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = movimiento.personasResultado.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "personas",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
