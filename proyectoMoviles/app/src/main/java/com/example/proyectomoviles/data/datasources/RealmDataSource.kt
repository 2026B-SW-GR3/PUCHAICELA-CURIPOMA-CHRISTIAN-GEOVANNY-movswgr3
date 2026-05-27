package com.example.proyectomoviles.data.datasources

import android.util.Log
import com.example.proyectomoviles.data.models.Nota
import com.example.proyectomoviles.data.models.NotaRealm
import com.example.proyectomoviles.data.mappers.NotaMappers.toDomainModel
import com.example.proyectomoviles.data.mappers.NotaMappers.toDomainModels
import com.example.proyectomoviles.data.mappers.NotaMappers.toRealmModel
import io.realm.kotlin.Realm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val TAG = "RealmDataSource"

/**
 * Implementación de NotasDataSource para Realm (NoSQL).
 * Maneja la persistencia de notas en Realm Database.
 *
 * @param realm Instancia de Realm para realizar operaciones
 */
class RealmDataSource(
    private val realm: Realm
) : NotasDataSource {

    override fun getAllNotas(): Flow<List<Nota>> {
        Log.d(TAG, "Iniciando getAllNotas desde Realm")
        return realm.query(NotaRealm::class).asFlow().map { results ->
            val notas = results.list.toDomainModels()
            Log.d(TAG, "getAllNotas: Se recuperaron ${notas.size} notas de Realm")
            notas
        }
    }

    override suspend fun guardarNota(nota: Nota) {
        Log.i(TAG, "Guardando nota en Realm: id=${nota.id}, contenido=${nota.contenido.take(50)}...")
        try {
            realm.write {
                val notaExistente = query(NotaRealm::class)
                    .query("id == $0", nota.id)
                    .first()
                    .find()

                if (notaExistente != null) {
                    Log.d(TAG, "Actualizando nota existente: id=${nota.id}")
                    notaExistente.contenido = nota.contenido
                } else {
                    Log.d(TAG, "Insertando nueva nota: id=${nota.id}")
                    copyToRealm(nota.toRealmModel())
                }
            }
            Log.i(TAG, "Nota guardada exitosamente en Realm: id=${nota.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar nota en Realm: id=${nota.id}, error=${e.message}", e)
            throw e
        }
    }

    override suspend fun getNotaById(id: String): Nota? {
        Log.d(TAG, "Buscando nota en Realm: id=$id")
        return try {
            val notaRealm = realm.query(NotaRealm::class)
                .query("id == $0", id)
                .first()
                .find()

            val nota = notaRealm?.toDomainModel()
            if (nota != null) {
                Log.d(TAG, "Nota encontrada en Realm: id=$id")
            } else {
                Log.d(TAG, "Nota NO encontrada en Realm: id=$id")
            }
            nota
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener nota de Realm: id=$id, error=${e.message}", e)
            null
        }
    }

    override suspend fun eliminarNota(id: String) {
        Log.i(TAG, "Eliminando nota de Realm: id=$id")
        try {
            realm.write {
                val nota = query(NotaRealm::class)
                    .query("id == $0", id)
                    .first()
                    .find()

                if (nota != null) {
                    delete(nota)
                    Log.i(TAG, "Nota eliminada de Realm: id=$id")
                } else {
                    Log.w(TAG, "Intento de eliminar nota inexistente en Realm: id=$id")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar nota de Realm: id=$id, error=${e.message}", e)
            throw e
        }
    }

    override suspend fun getAllNotasDirect(): List<Nota> {
        Log.d(TAG, "Obteniendo todas las notas de Realm de forma directa")
        return try {
            val notas = realm.query(NotaRealm::class)
                .find()
                .toDomainModels()
            Log.d(TAG, "getAllNotasDirect: Se recuperaron ${notas.size} notas de Realm")
            notas
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener notas de Realm: error=${e.message}", e)
            emptyList()
        }
    }

    override suspend fun limpiarTodas() {
        Log.w(TAG, "Limpiando TODAS las notas de Realm")
        try {
            realm.write {
                val todasLasNotas = query(NotaRealm::class).find()
                delete(todasLasNotas)
            }
            Log.i(TAG, "Todas las notas han sido eliminadas de Realm")
        } catch (e: Exception) {
            Log.e(TAG, "Error al limpiar Realm: error=${e.message}", e)
            throw e
        }
    }
}

