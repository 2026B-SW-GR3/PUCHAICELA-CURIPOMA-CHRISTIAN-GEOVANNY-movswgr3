package com.example.proyectomoviles.data.repositories

import android.util.Log
import com.example.proyectomoviles.data.datasources.NotasDataSource
import com.example.proyectomoviles.data.models.Nota
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.Flow

private const val TAG = "NotasRepositoryImpl"

/**
 * Implementación de NotasRepository que gestiona la conmutación entre motores de BD.
 *
 * Esta clase actúa como intermediaria entre la UI y las fuentes de datos,
 * permitiendo cambiar dinámicamente entre Realm (NoSQL) y SQLDelight (SQL)
 * sin afectar al otro motor.
 *
 * @param realmDataSource Fuente de datos para Realm
 * @param sqlDataSource Fuente de datos para SQLDelight
 * @param esRelacionalInicial true para iniciar con SQL, false para Realm
 */
class NotasRepositoryImpl(
    private val realmDataSource: NotasDataSource,
    private val sqlDataSource: NotasDataSource,
    esRelacionalInicial: Boolean = false
) : NotasRepository {

    // StateFlow para rastrear el motor activo
    private val _esRelacionalFlow = MutableStateFlow(esRelacionalInicial)
    override val esRelacionalFlow: StateFlow<Boolean> = _esRelacionalFlow.asStateFlow()

    // StateFlow que actualiza automáticamente cuando cambia el motor activo
    // StateFlow que actualiza automáticamente cuando cambia el motor activo
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override val notasFlow: Flow<List<Nota>> = _esRelacionalFlow
        .flatMapLatest { esRelacional ->
            val dataSource = if (esRelacional) sqlDataSource else realmDataSource
            val motor = if (esRelacional) "SQL" else "Realm"
            Log.d(TAG, "Observando notas desde motor activo: $motor")
            dataSource.getAllNotas()
        }

    init {
        Log.i(TAG, "NotasRepositoryImpl inicializado con motor: ${if (esRelacionalInicial) "SQL" else "Realm"}")
    }

    /**
     * Obtiene la fuente de datos activa según el motor configurado.
     */
    private fun getDataSourceActiva(): NotasDataSource {
        return if (_esRelacionalFlow.value) {
            Log.d(TAG, "Usando DataSource: SQL")
            sqlDataSource
        } else {
            Log.d(TAG, "Usando DataSource: Realm")
            realmDataSource
        }
    }

    /**
     * Obtiene el nombre del motor activo para logs.
     */
    private fun getNombreMotorActivo(): String {
        return if (_esRelacionalFlow.value) "SQL" else "Realm"
    }

    /**
     * Guarda una nota en el motor de base de datos activo.
     * Solo afecta al motor activo, el otro permanece intacto.
     */
    override suspend fun guardarNota(nota: Nota) {
        val motorActivo = getNombreMotorActivo()
        Log.i(TAG, "guardarNota: Iniciando guardado en motor activo: $motorActivo")

        try {
            if (nota.id.isBlank()) {
                Log.e(TAG, "guardarNota: ID de nota vacío, operación rechazada")
                throw IllegalArgumentException("El ID de la nota no puede estar vacío")
            }

            if (nota.contenido.isBlank()) {
                Log.e(TAG, "guardarNota: Contenido de nota vacío, operación rechazada")
                throw IllegalArgumentException("El contenido de la nota no puede estar vacío")
            }

            Log.d(TAG, "guardarNota: Validaciones pasadas, guardando en $motorActivo")
            Log.d(TAG, "guardarNota: Nota a guardar - id=${nota.id}, contenido=${nota.contenido.take(50)}...")

            val dataSourceActiva = getDataSourceActiva()
            dataSourceActiva.guardarNota(nota)

            Log.i(TAG, "guardarNota: Nota guardada exitosamente en $motorActivo - id=${nota.id}")
        } catch (e: Exception) {
            Log.e(TAG, "guardarNota: Error al guardar en $motorActivo - ${e.message}", e)
            throw e
        }
    }

    /**
     * Cambia el motor de base de datos entre SQL y NoSQL.
     * Conmutación instantánea SIN migración para mantener los almacenes independientes
     * (Requisito de la rúbrica).
     */
    override suspend fun cambiarMotor(esRelacional: Boolean) {
        val motorAnterior = getNombreMotorActivo()
        val motorNuevo = if (esRelacional) "SQL" else "Realm"

        Log.i(TAG, "cambiarMotor: Iniciando cambio de motor de $motorAnterior a $motorNuevo")

        try {
            // Cambiar el motor activo (Esto dispara la reactividad en Compose)
            _esRelacionalFlow.value = esRelacional
            Log.i(TAG, "cambiarMotor: Motor cambiado exitosamente a $motorNuevo")
        } catch (e: Exception) {
            Log.e(TAG, "cambiarMotor: Error durante la conmutación a $motorNuevo - ${e.message}", e)
            throw e
        }
    }

    /**
     * Método auxiliar para obtener todas las notas del motor activo.
     * Útil para debugging y testing.
     */
    suspend fun getAllNotas(): List<Nota> {
        Log.d(TAG, "getAllNotas: Obteniendo todas las notas del motor activo (${getNombreMotorActivo()})")
        return try {
            val notas = getDataSourceActiva().getAllNotasDirect()
            Log.d(TAG, "getAllNotas: ${notas.size} notas obtenidas")
            notas
        } catch (e: Exception) {
            Log.e(TAG, "getAllNotas: Error al obtener notas - ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Método auxiliar para obtener una nota específica.
     */
    suspend fun getNotaById(id: String): Nota? {
        Log.d(TAG, "getNotaById: Buscando nota con id=$id en motor activo (${getNombreMotorActivo()})")
        return try {
            val nota = getDataSourceActiva().getNotaById(id)
            if (nota != null) {
                Log.d(TAG, "getNotaById: Nota encontrada - id=$id")
            } else {
                Log.d(TAG, "getNotaById: Nota no encontrada - id=$id")
            }
            nota
        } catch (e: Exception) {
            Log.e(TAG, "getNotaById: Error al obtener nota id=$id - ${e.message}", e)
            null
        }
    }

    /**
     * Elimina una nota del motor activo.
     * ¡AQUÍ ESTÁ LA CORRECCIÓN DEL OVERRIDE!
     */
    override suspend fun eliminarNota(id: String) {
        Log.i(TAG, "eliminarNota: Eliminando nota con id=$id del motor activo (${getNombreMotorActivo()})")
        try {
            getDataSourceActiva().eliminarNota(id)
            Log.i(TAG, "eliminarNota: Nota eliminada exitosamente - id=$id")
        } catch (e: Exception) {
            Log.e(TAG, "eliminarNota: Error al eliminar nota id=$id - ${e.message}", e)
            throw e
        }
    }

    /**
     * Obtiene el estado actual (para debugging).
     */
    fun getEstadoActual(): String {
        val motor = getNombreMotorActivo()
        return "Motor activo: $motor, esRelacional: ${_esRelacionalFlow.value}"
    }
}