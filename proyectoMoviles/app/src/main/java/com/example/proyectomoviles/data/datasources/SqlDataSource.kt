package com.example.proyectomoviles.data.datasources

import android.util.Log
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.proyectomoviles.AppDatabase
import com.example.proyectomoviles.data.models.Nota
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val TAG = "SqlDataSource"

/**
 * Implementación de NotasDataSource para SQLDelight (SQL).
 * Maneja la persistencia de notas en SQLite.
 */
class SqlDataSource(
    database: AppDatabase
) : NotasDataSource {

    // ⚠️ ATENCIÓN: Si tu archivo SQL se llama "Nota.sq", cambia "appDatabaseQueries" por "notaQueries"
    private val queries = database.appDatabaseQueries

    override fun getAllNotas(): Flow<List<Nota>> {
        Log.d(TAG, "Iniciando getAllNotas desde SQLDelight")

        // 1. Llamamos a la consulta
        // 2. La convertimos a Flow
        // 3. Emitimos la lista en el hilo de IO (Background)
        // 4. Mapeamos la entidad de SQL a tu modelo de Dominio
        return queries.getAllNotas()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { listaEntidades ->
                val notas = listaEntidades.map { row ->
                    Nota(
                        id = row.id,
                        contenido = row.contenido
                    )
                }
                Log.d(TAG, "getAllNotas: Se recuperaron ${notas.size} notas de SQL")
                notas
            }
    }

    override suspend fun guardarNota(nota: Nota) {
        Log.i(TAG, "Guardando nota en SQLDelight: id=${nota.id}, contenido=${nota.contenido.take(50)}...")
        try {
            val notaExistente = getNotaById(nota.id)

            if (notaExistente != null) {
                Log.d(TAG, "Actualizando nota existente: id=${nota.id}")
                queries.updateNota(
                    contenido = nota.contenido,
                    id = nota.id
                )
            } else {
                Log.d(TAG, "Insertando nueva nota: id=${nota.id}")
                queries.insertNota(
                    id = nota.id,
                    contenido = nota.contenido
                )
            }
            Log.i(TAG, "Nota guardada exitosamente en SQLDelight: id=${nota.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar nota en SQLDelight: id=${nota.id}, error=${e.message}", e)
            throw e
        }
    }

    override suspend fun getNotaById(id: String): Nota? {
        Log.d(TAG, "Buscando nota en SQLDelight: id=$id")
        return try {
            val notaRow = queries.getNotaById(id).executeAsOneOrNull()
            val nota = notaRow?.let {
                Nota(
                    id = it.id,
                    contenido = it.contenido
                )
            }

            if (nota != null) {
                Log.d(TAG, "Nota encontrada en SQLDelight: id=$id")
            } else {
                Log.d(TAG, "Nota NO encontrada en SQLDelight: id=$id")
            }
            nota
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener nota de SQLDelight: id=$id, error=${e.message}", e)
            null
        }
    }

    override suspend fun eliminarNota(id: String) {
        Log.i(TAG, "Eliminando nota de SQLDelight: id=$id")
        try {
            val notaExistente = getNotaById(id)
            if (notaExistente != null) {
                queries.deleteNota(id)
                Log.i(TAG, "Nota eliminada de SQLDelight: id=$id")
            } else {
                Log.w(TAG, "Intento de eliminar nota inexistente en SQLDelight: id=$id")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar nota de SQLDelight: id=$id, error=${e.message}", e)
            throw e
        }
    }

    override suspend fun getAllNotasDirect(): List<Nota> {
        Log.d(TAG, "Obteniendo todas las notas de SQLDelight de forma directa")
        return try {
            val notas = queries.getAllNotas().executeAsList().map { row ->
                Nota(
                    id = row.id,
                    contenido = row.contenido
                )
            }
            Log.d(TAG, "getAllNotasDirect: Se recuperaron ${notas.size} notas de SQL")
            notas
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener notas de SQLDelight: error=${e.message}", e)
            emptyList()
        }
    }

    override suspend fun limpiarTodas() {
        Log.w(TAG, "Limpiando TODAS las notas de SQLDelight")
        try {
            queries.deleteAllNotas()
            Log.i(TAG, "Todas las notas han sido eliminadas de SQLDelight")
        } catch (e: Exception) {
            Log.e(TAG, "Error al limpiar SQLDelight: error=${e.message}", e)
            throw e
        }
    }
}