package com.example.proyectomoviles.data.repositories

import com.example.proyectomoviles.data.models.Nota
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface NotasRepository {
    // Fíjate que aquí dice Flow, no StateFlow
    val notasFlow: Flow<List<Nota>>
    val esRelacionalFlow: StateFlow<Boolean>

    suspend fun guardarNota(nota: Nota)
    suspend fun cambiarMotor(esRelacional: Boolean)
    // Fíjate que esté esta línea:
    suspend fun eliminarNota(id: String)
}