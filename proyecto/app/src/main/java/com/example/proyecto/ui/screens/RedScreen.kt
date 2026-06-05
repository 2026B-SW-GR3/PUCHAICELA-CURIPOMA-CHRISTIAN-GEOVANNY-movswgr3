package com.example.proyecto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.proyecto.ui.viewmodels.RedViewModel

@Composable
fun RedScreen(viewModel: RedViewModel = hiltViewModel()) {
    val isLoading by viewModel.isLoading.collectAsState()
    val postActual by viewModel.postActual.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    var inputId by remember { mutableStateOf("") }
    var editTitle by remember { mutableStateOf("") }
    var editBody by remember { mutableStateOf("") }

    // Sincronizar campos cuando se descarga un post
    LaunchedEffect(postActual) {
        postActual?.let {
            editTitle = it.title
            editBody = it.body
        }
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Conectividad REST", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Bloque de Consulta (GET)
        OutlinedTextField(
            value = inputId,
            onValueChange = { inputId = it },
            label = { Text("ID del Post (ej: 1)") },
            enabled = !isLoading, // Deshabilita durante tránsito
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { viewModel.fetchPost(inputId.toIntOrNull() ?: 1) },
            enabled = !isLoading && inputId.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("GET: Consultar Servidor")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bloque de Actualización (PUT)
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        OutlinedTextField(
            value = editTitle,
            onValueChange = { editTitle = it },
            label = { Text("Título editable") },
            enabled = !isLoading && postActual != null,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = editBody,
            onValueChange = { editBody = it },
            label = { Text("Contenido editable") },
            enabled = !isLoading && postActual != null,
            modifier = Modifier.fillMaxWidth().height(100.dp)
        )

        Button(
            onClick = {
                postActual?.let { viewModel.updatePost(it.id, editTitle, editBody) }
            },
            enabled = !isLoading && postActual != null,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("PUT: Actualizar en Servidor")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(mensaje, color = MaterialTheme.colorScheme.primary)
    }
}