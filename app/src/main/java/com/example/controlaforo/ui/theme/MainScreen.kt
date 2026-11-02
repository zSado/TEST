package com.example.controlaforo.ui.theme

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onOpenHistorial: () -> Unit = {},
    onOpenPerfiles: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Usuario desconocido"

    var personasActuales by remember { mutableStateOf(0) }
    var aforoMaximo by remember { mutableStateOf(3) }
    var estadoBloqueo by remember { mutableStateOf(false) }
    var luzIndicadora by remember { mutableStateOf("verde") }

    var showDialog by remember { mutableStateOf(false) }
    var nuevoAforoInput by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        val docRef = db.collection("control_presencia").document("espacio_principal")
        val listener = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {
                personasActuales = snapshot.getLong("personas_actuales")?.toInt() ?: 0
                aforoMaximo = snapshot.getLong("aforo_maximo")?.toInt() ?: 3
                estadoBloqueo = snapshot.getBoolean("estado_bloqueo") ?: false
                luzIndicadora = snapshot.getString("luz_indicadora") ?: "verde"
            }
        }
        onDispose { listener.remove() }
    }

    fun actualizarAforo(nuevoValor: Int, tipoAccion: String) {
        val bloqueo = nuevoValor >= aforoMaximo
        val luz = if (bloqueo) "roja" else "verde"

        val datosEspacio = mapOf(
            "personas_actuales" to nuevoValor,
            "estado_bloqueo" to bloqueo,
            "luz_indicadora" to luz
        )

        db.collection("control_presencia").document("espacio_principal")
            .update(datosEspacio)
            .addOnSuccessListener {
                val historialItem = mapOf(
                    "accion" to tipoAccion,
                    "usuario" to currentUserEmail,
                    "personas_resultado" to nuevoValor,
                    "fecha_hora" to Timestamp.now()
                )
                db.collection("historial_movimientos").add(historialItem)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al actualizar aforo", Toast.LENGTH_SHORT).show()
            }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize()) {


            TextButton(
                onClick = {
                    nuevoAforoInput = aforoMaximo.toString()
                    showDialog = true
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Text(text = "⚙️ Cambiar Aforo", color = MaterialTheme.colorScheme.primary)
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Control de Aforo", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(color = if (luzIndicadora == "verde") Color(0xFF4CAF50) else Color(0xFFF44336), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = luzIndicadora.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(text = "$personasActuales / $aforoMaximo", fontSize = 56.sp, fontWeight = FontWeight.Bold)
                Text(text = "Personas en el espacio principal", fontSize = 16.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(48.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = { if (personasActuales > 0) actualizarAforo(personasActuales - 1, "Salida") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(text = "Salida (-1)", fontSize = 16.sp, color = Color.White)
                    }

                    Button(
                        onClick = { if (personasActuales < aforoMaximo) actualizarAforo(personasActuales + 1, "Entrada") },
                        enabled = !estadoBloqueo && personasActuales < aforoMaximo,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(text = "Entrada (+1)", fontSize = 16.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    OutlinedButton(onClick = onOpenHistorial) {
                        Text(text = "📋 Historial")
                    }
                    OutlinedButton(onClick = onOpenPerfiles) {
                        Text(text = "🎨 Perfiles")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onLogout) {
                    Text(text = "Cerrar Sesión", color = MaterialTheme.colorScheme.primary)
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Modificar Aforo Máximo") },
                    text = {
                        OutlinedTextField(
                            value = nuevoAforoInput,
                            onValueChange = { nuevoAforoInput = it },
                            label = { Text("Nuevo límite") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val nuevoLimite = nuevoAforoInput.toIntOrNull()
                            if (nuevoLimite != null && nuevoLimite > 0) {
                                val bloqueoActualizado = personasActuales >= nuevoLimite
                                val luzActualizada = if (bloqueoActualizado) "roja" else "verde"

                                db.collection("control_presencia").document("espacio_principal")
                                    .update(
                                        mapOf(
                                            "aforo_maximo" to nuevoLimite,
                                            "estado_bloqueo" to bloqueoActualizado,
                                            "luz_indicadora" to luzActualizada
                                        )
                                    )
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Aforo máximo actualizado", Toast.LENGTH_SHORT).show()
                                        showDialog = false
                                    }
                            } else {
                                Toast.makeText(context, "Ingresa un número válido", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text("Guardar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}
