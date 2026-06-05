package com.example.proyecto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import com.example.proyecto.ui.screens.RedScreen
import com.example.proyecto.ui.screens.SeguridadScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // ¡Aquí está la magia! El verticalScroll permite deslizar la pantalla
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp) // Un pequeño margen al final
            ) {
                // Primer Módulo
                RedScreen()

                // Divisor visual grueso para separar las dos partes
                Divider(thickness = 4.dp, modifier = Modifier.padding(vertical = 16.dp))

                // Segundo Módulo (¡Aquí está tu encriptación!)
                SeguridadScreen()
            }
        }
    }
}