package com.example.controlaforo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun ControlAforoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    // Perfil de color elegido en PerfilesScreen
    perfil: PerfilTema = PerfilTema.SISTEMA,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        perfil == PerfilTema.SISTEMA && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        perfil == PerfilTema.SISTEMA -> if (darkTheme) DarkColorScheme else LightColorScheme

        darkTheme -> darkColorScheme(
            primary = perfil.primarioOscuro,
            secondary = perfil.secundarioOscuro,
            tertiary = perfil.terciarioOscuro
        )

        else -> lightColorScheme(
            primary = perfil.primarioClaro,
            secondary = perfil.secundarioClaro,
            tertiary = perfil.terciarioClaro
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
