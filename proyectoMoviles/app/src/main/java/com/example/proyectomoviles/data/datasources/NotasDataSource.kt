package com.example.proyectomoviles.data.datasources

import com.example.proyectomoviles.data.models.Nota
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz abstracta para las fuentes de datos.
 * Define el contrato que deben cumplir todas las implementaciones
 * de fuentes de datos (Realm, SQLDelight, etc).
 */
interface NotasDataSource {

    /**
     * Obtiene todas las notas como un Flow reactivo.
     */
    fun getAllNotas(): Flow<List<Nota>>

    /**
     * Guarda una nota en la fuente de datos.
     * Si existe una nota con el mismo ID, la actualiza.
     */
    suspend fun guardarNota(nota: Nota)

    /**
     * Obtiene una nota específica por su ID.
     */
    suspend fun getNotaById(id: String): Nota?

    /**
     * Elimina una nota específica.
     */
    suspend fun eliminarNota(id: String)

    /**
     * Obtiene todas las notas de una sola vez (no reactivo).
     * Útil para migraciones de datos.
     */
    suspend fun getAllNotasDirect(): List<Nota>

    /**
     * Limpia todas las notas de la fuente de datos.
     */
    suspend fun limpiarTodas()
}

