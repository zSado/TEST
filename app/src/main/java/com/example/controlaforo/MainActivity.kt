package com.example.controlaforo

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.controlaforo.ui.theme.ControlAforoTheme
import com.example.controlaforo.ui.theme.HistorialScreen
import com.example.controlaforo.ui.theme.LoginScreen
import com.example.controlaforo.ui.theme.MainScreen
import com.example.controlaforo.ui.theme.ModoTema
import com.example.controlaforo.ui.theme.PerfilTema
import com.example.controlaforo.ui.theme.PerfilesScreen
import com.google.firebase.auth.FirebaseAuth


enum class Pantalla { PRINCIPAL, HISTORIAL, PERFILES }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        val prefs = getSharedPreferences("ajustes_tema", Context.MODE_PRIVATE)

        setContent {
            var perfil by remember {
                mutableStateOf(PerfilTema.desdeNombre(prefs.getString("perfil", null)))
            }
            var modo by remember {
                mutableStateOf(ModoTema.desdeNombre(prefs.getString("modo", null)))
            }
            val oscuro = when (modo) {
                ModoTema.SISTEMA -> isSystemInDarkTheme()
                ModoTema.CLARO -> false
                ModoTema.OSCURO -> true
            }

            ControlAforoTheme(darkTheme = oscuro, perfil = perfil) {
                var isLoggedIn by remember {
                    mutableStateOf(FirebaseAuth.getInstance().currentUser != null)
                }
                var pantalla by rememberSaveable { mutableStateOf(Pantalla.PRINCIPAL) }


                BackHandler(enabled = isLoggedIn && pantalla != Pantalla.PRINCIPAL) {
                    pantalla = Pantalla.PRINCIPAL
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (isLoggedIn) {
                        when (pantalla) {
                            Pantalla.PRINCIPAL -> MainScreen(
                                onLogout = {
                                    FirebaseAuth.getInstance().signOut()
                                    isLoggedIn = false
                                    pantalla = Pantalla.PRINCIPAL
                                },
                                onOpenHistorial = { pantalla = Pantalla.HISTORIAL },
                                onOpenPerfiles = { pantalla = Pantalla.PERFILES }
                            )

                            Pantalla.HISTORIAL -> HistorialScreen(
                                onBack = { pantalla = Pantalla.PRINCIPAL }
                            )

                            Pantalla.PERFILES -> PerfilesScreen(
                                perfilActual = perfil,
                                modoActual = modo,
                                onPerfilChange = {
                                    perfil = it
                                    prefs.edit().putString("perfil", it.name).apply()
                                },
                                onModoChange = {
                                    modo = it
                                    prefs.edit().putString("modo", it.name).apply()
                                },
                                onBack = { pantalla = Pantalla.PRINCIPAL }
                            )
                        }
                    } else {
                        Box(modifier = Modifier.padding(innerPadding)) {
                            LoginScreen(
                                onLoginSuccess = {
                                    isLoggedIn = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
