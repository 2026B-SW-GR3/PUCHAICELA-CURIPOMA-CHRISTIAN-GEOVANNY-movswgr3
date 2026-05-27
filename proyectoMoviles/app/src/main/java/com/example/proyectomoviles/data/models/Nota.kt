package com.example.proyectomoviles.data.models

/**
 * Modelo de datos puro que representa una nota.
 * Esta es una clase de datos inmutable que forma parte de la capa de dominio.
 * 
 * @property id Identificador único de la nota
 * @property contenido Contenido o texto de la nota
 */
data class Nota(
    val id: String,
    val contenido: String
)

