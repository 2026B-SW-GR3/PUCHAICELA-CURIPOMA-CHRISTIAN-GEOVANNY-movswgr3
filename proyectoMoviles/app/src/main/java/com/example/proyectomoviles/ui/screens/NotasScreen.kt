package com.example.proyectomoviles.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.proyectomoviles.data.models.Nota
import com.example.proyectomoviles.ui.viewmodels.NotasViewModel

/**
 * Pantalla principal de Notas con CRUD completo.
 *
 * Características:
 * - Switch en TopAppBar para cambiar entre SQL y Realm
 * - Chip indicador del motor activo (SQL/Realm)
 * - Formulario para crear notas
 * - Lista reactiva de notas
 * - Soporte para eliminar notas
 * - Actualización instantánea con collectAsState()
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotasScreen(
    viewModel: NotasViewModel = hiltViewModel()
) {
    // Observar los flows reactivos del ViewModel
    val notas by viewModel.notasFlow.collectAsState(initial = emptyList())
    val esRelacional by viewModel.esRelacionalFlow.collectAsState(initial = false)

    // Estado local para el formulario
    var contenidoNota by remember { mutableStateOf("") }
    var notaEnEdicion: Nota? by remember { mutableStateOf(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Notas") },
                actions = {
                    // Switch en el TopAppBar para cambiar motor
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .align(Alignment.CenterVertically),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (esRelacional) "SQL" else "Realm",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = esRelacional,
                            onCheckedChange = { nuevoValor ->
                                viewModel.cambiarMotor(nuevoValor)
                            },
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // 1. INDICADOR VISUAL DEL MOTOR ACTIVO (Chip)
            IndicadorMotorActivo(esRelacional = esRelacional)

            Spacer(modifier = Modifier.height(16.dp))

            // 2. FORMULARIO PARA CREAR/EDITAR NOTAS
            FormularioNota(
                contenido = contenidoNota,
                onContenidoChange = { contenidoNota = it },
                notaEnEdicion = notaEnEdicion,
                onGuardar = {
                    if (notaEnEdicion != null) {
                        // Editar nota existente
                        viewModel.actualizarNota(notaEnEdicion!!.id, contenidoNota)
                    } else {
                        // Crear nueva nota
                        viewModel.guardarNota(contenidoNota)
                    }
                    // Limpiar formulario
                    contenidoNota = ""
                    notaEnEdicion = null
                },
                onCancelar = {
                    contenidoNota = ""
                    notaEnEdicion = null
                },
                editandoNota = notaEnEdicion != null
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. LISTA REACTIVA DE NOTAS
            ListaNotas(
                notas = notas,
                motorActual = if (esRelacional) "SQL" else "Realm",
                onEditar = { nota ->
                    contenidoNota = nota.contenido
                    notaEnEdicion = nota
                },
                onEliminar = { nota ->
                    viewModel.eliminarNota(nota.id)
                }
            )
        }
    }
}

/**
 * Componente que muestra un Chip indicador del motor activo.
 * Cambio de color dinámico según el motor.
 */
@Composable
private fun IndicadorMotorActivo(esRelacional: Boolean) {
    val colorMotor = if (esRelacional) {
        Color(0xFF4CAF50) // Verde para SQL
    } else {
        Color(0xFF2196F3) // Azul para Realm
    }

    val textoMotor = if (esRelacional) {
        "📊 Usando SQLite (SQL)"
    } else {
        "📄 Usando Realm (NoSQL)"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        color = colorMotor.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = 2.dp,
            color = colorMotor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = textoMotor,
                style = MaterialTheme.typography.labelLarge,
                color = colorMotor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Componente del formulario para crear o editar notas.
 */
@Composable
private fun FormularioNota(
    contenido: String,
    onContenidoChange: (String) -> Unit,
    notaEnEdicion: Nota?,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
    editandoNota: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    ) {
        Text(
            text = if (editandoNota) "Editar Nota" else "Nueva Nota",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TextField(
            value = contenido,
            onValueChange = onContenidoChange,
            label = { Text("Contenido de la nota") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            maxLines = 5,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onGuardar,
                enabled = contenido.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text(if (editandoNota) "Actualizar" else "Guardar")
            }

            if (editandoNota) {
                OutlinedButton(
                    onClick = onCancelar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}

/**
 * Componente que muestra la lista de notas de forma reactiva.
 * Se actualiza automáticamente cuando cambia el estado mediante collectAsState.
 */
@Composable
private fun ListaNotas(
    notas: List<Nota>,
    motorActual: String,
    onEditar: (Nota) -> Unit,
    onEliminar: (Nota) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Encabezado con contador
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Notas (${notas.size})",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Motor: $motorActual",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        if (notas.isEmpty()) {
            // Estado vacío
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay notas en $motorActual\n👇 Crea una nueva nota",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            // Lista de notas
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = notas,
                    key = { nota -> nota.id }
                ) { nota ->
                    TarjetaNota(
                        nota = nota,
                        onEditar = { onEditar(nota) },
                        onEliminar = { onEliminar(nota) }
                    )
                }
            }
        }
    }
}

/**
 * Componente individual de una nota en la lista.
 */
@Composable
private fun TarjetaNota(
    nota: Nota,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Contenido de la nota
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = nota.contenido,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp),
                    maxLines = 3
                )

                Text(
                    text = "ID: ${nota.id.take(8)}...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Botones de acción
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = onEditar,
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text("✏️", fontSize = androidx.compose.material3.LocalTextStyle.current.fontSize)
                }

                IconButton(
                    onClick = onEliminar,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Eliminar nota",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}