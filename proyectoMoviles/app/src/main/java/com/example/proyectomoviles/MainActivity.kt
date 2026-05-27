package com.example.proyectomoviles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.proyectomoviles.ui.screens.NotasScreen
import com.example.proyectomoviles.ui.theme.ProyectoMovilesTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Actividad principal de la aplicación.
 * Aquí se inyecta la interfaz de usuario con Compose.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectoMovilesTheme {
                // Mostrar la pantalla de notas
                NotasScreen()
            }
        }
    }
}
