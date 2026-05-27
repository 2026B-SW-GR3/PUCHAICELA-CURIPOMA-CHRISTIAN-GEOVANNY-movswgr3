package com.example.proyectomoviles.data.mappers

import com.example.proyectomoviles.data.models.Nota
import com.example.proyectomoviles.data.models.NotaRealm

/**
 * Objeto con funciones de extensión para mapear entre Nota y NotaRealm.
 * Facilita la conversión entre el modelo de dominio y el modelo de persistencia Realm.
 */
object NotaMappers {

    /**
     * Convierte una entidad NotaRealm a un modelo de dominio Nota.
     */
    fun NotaRealm.toDomainModel(): Nota = Nota(
        id = this.id,
        contenido = this.contenido
    )

    /**
     * Convierte un modelo de dominio Nota a una entidad NotaRealm.
     */
    fun Nota.toRealmModel(): NotaRealm = NotaRealm().apply {
        id = this@toRealmModel.id
        contenido = this@toRealmModel.contenido
    }

    /**
     * Convierte una lista de entidades NotaRealm a una lista de modelos de dominio.
     */
    fun List<NotaRealm>.toDomainModels(): List<Nota> = this.map { it.toDomainModel() }
}

