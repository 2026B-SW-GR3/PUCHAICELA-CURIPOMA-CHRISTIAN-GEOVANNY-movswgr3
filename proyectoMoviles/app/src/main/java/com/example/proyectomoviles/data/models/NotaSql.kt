package com.example.proyectomoviles.data.models

import com.example.proyectomoviles.AppDatabase

/**
 * Modelo de datos para persistencia relacional usando SQLDelight.
 * Esta clase representa una nota en la base de datos SQL.
 *
 * La tabla SQL se define en AppDatabase.sq
 *
 * @property id Identificador único de la nota (clave primaria)
 * @property contenido Contenido o texto de la nota
 */
data class NotaSql(
    val id: String,
    val contenido: String
)

/**
 * Extensión para convertir desde la query de SQLDelight a NotaSql
 */
fun AppDatabase.Companion.notaFromCursor(
    id: String,
    contenido: String
): NotaSql = NotaSql(
    id = id,
    contenido = contenido
)

