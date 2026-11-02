package com.example.controlaforo.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth


enum class PerfilTema(
    val etiqueta: String,
    val descripcion: String,
    val primarioClaro: Color,
    val secundarioClaro: Color,
    val terciarioClaro: Color,
    val primarioOscuro: Color,
    val secundarioOscuro: Color,
    val terciarioOscuro: Color
) {
    SISTEMA(
        "Sistema",
        "Colores dinámicos del teléfono (Android 12 o superior)",
        Purple40, PurpleGrey40, Pink40,
        Purple80, PurpleGrey80, Pink80
    ),
    MORADO(
        "Morado clásico",
        "El tema por defecto de la aplicación",
        Purple40, PurpleGrey40, Pink40,
        Purple80, PurpleGrey80, Pink80
    ),
    AZUL(
        "Azul océano",
        "Tonos fríos y sobrios",
        Color(0xFF1565C0), Color(0xFF546E7A), Color(0xFF00838F),
        Color(0xFF90CAF9), Color(0xFFB0BEC5), Color(0xFF80DEEA)
    ),
    VERDE(
        "Verde bosque",
        "Tonos naturales y relajados",
        Color(0xFF2E7D32), Color(0xFF5D6B4F), Color(0xFF6D4C41),
        Color(0xFFA5D6A7), Color(0xFFC5CFB8), Color(0xFFBCAAA4)
    ),
    NARANJA(
        "Naranja cálido",
        "Tonos energéticos y llamativos",
        Color(0xFFE65100), Color(0xFF795548), Color(0xFFF9A825),
        Color(0xFFFFCC80), Color(0xFFD7CCC8), Color(0xFFFFE082)
    );

    companion object {
        fun desdeNombre(nombre: String?): PerfilTema =
            values().firstOrNull { it.name == nombre } ?: SISTEMA
    }
}


enum class ModoTema(val etiqueta: String) {
    SISTEMA("Sistema"),
    CLARO("Claro"),
    OSCURO("Oscuro");

    companion object {
        fun desdeNombre(nombre: String?): ModoTema =
            values().firstOrNull { it.name == nombre } ?: SISTEMA
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilesScreen(
    perfilActual: PerfilTema,
    modoActual: ModoTema,
    onPerfilChange: (PerfilTema) -> Unit,
    onModoChange: (ModoTema) -> Unit,
    onBack: () -> Unit
) {
    val email = FirebaseAuth.getInstance().currentUser?.email ?: "Usuario desconocido"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfiles de tema") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Volver") }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Sesión activa",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(text = email, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            item {
                Text(text = "Modo de pantalla", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModoTema.values().forEach { modo ->
                        FilterChip(
                            selected = modo == modoActual,
                            onClick = { onModoChange(modo) },
                            label = { Text(modo.etiqueta) }
                        )
                    }
                }
            }

            item {
                Text(text = "Perfil de color", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            items(PerfilTema.values().toList()) { perfil ->
                PerfilCard(
                    perfil = perfil,
                    seleccionado = perfil == perfilActual,
                    onClick = { onPerfilChange(perfil) }
                )
            }
        }
    }
}

@Composable
private fun PerfilCard(perfil: PerfilTema, seleccionado: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        border = if (seleccionado) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = perfil.etiqueta, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = perfil.descripcion,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (seleccionado) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✔ En uso",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Muestra(perfil.primarioClaro)
                Muestra(perfil.secundarioClaro)
                Muestra(perfil.terciarioClaro)
            }
        }
    }
}

@Composable
private fun Muestra(color: Color) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(color = color, shape = CircleShape)
    )
}
