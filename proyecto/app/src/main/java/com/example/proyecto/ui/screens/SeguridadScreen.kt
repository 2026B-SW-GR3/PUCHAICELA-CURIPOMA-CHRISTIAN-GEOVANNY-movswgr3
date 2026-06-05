package com.example.proyecto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.proyecto.ui.viewmodels.SeguridadViewModel

@Composable
fun SeguridadScreen(viewModel: SeguridadViewModel = hiltViewModel()) {
    val mensaje by viewModel.mensaje.collectAsState()

    var inputKey by remember { mutableStateOf("") }
    var inputValue by remember { mutableStateOf("") }

    // 0 = SharedPrefs, 1 = DataStore, 2 = Encrypted
    var tipoAlmacenamiento by remember { mutableStateOf(0) }
    val opciones = listOf("SharedPrefs (Inseguro)", "DataStore (Moderno)", "Encrypted (Seguro)")

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Bóveda de Secretos", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = inputKey,
            onValueChange = { inputKey = it },
            label = { Text("Llave del secreto") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = inputValue,
            onValueChange = { inputValue = it },
            label = { Text("Valor a guardar (Déjalo vacío para recuperar)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Selecciona el compartimento:")

        opciones.forEachIndexed { index, texto ->
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                RadioButton(
                    selected = tipoAlmacenamiento == index,
                    onClick = { tipoAlmacenamiento = index }
                )
                Text(texto)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = { viewModel.guardarSecreto(inputKey, inputValue, tipoAlmacenamiento) },
                enabled = inputKey.isNotBlank() && inputValue.isNotBlank()
            ) {
                Text("Guardar Secreto")
            }

            Button(
                onClick = { viewModel.recuperarSecreto(inputKey, tipoAlmacenamiento) },
                enabled = inputKey.isNotBlank()
            ) {
                Text("Recuperar Secreto")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Notificación genérica de existencia
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = mensaje,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}