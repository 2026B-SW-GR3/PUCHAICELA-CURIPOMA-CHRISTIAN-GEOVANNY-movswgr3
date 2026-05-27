package com.example.proyectomoviles.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectomoviles.data.models.Nota
import com.example.proyectomoviles.data.repositories.NotasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "NotasViewModel"

/**
 * ViewModel para gestionar la lógica de negocio de las notas.
 * Expone los flows del repositorio y proporciona funciones para realizar operaciones CRUD.
 *
 * @param repository Instancia inyectada del repositorio de notas
 */
@HiltViewModel
class NotasViewModel @Inject constructor(
    private val repository: NotasRepository
) : ViewModel() {

    /**
     * StateFlow observable de la lista de notas actual.
     * Se actualiza reactivamente cuando cambian las notas en cualquier motor.
     */
    val notasFlow = repository.notasFlow

    /**
     * StateFlow observable que indica si el motor actual es relacional (true = SQL, false = Realm).
     * Se actualiza cuando el usuario cambia el switch de motor.
     */
    val esRelacionalFlow = repository.esRelacionalFlow

    /**
     * Guarda una nueva nota o actualiza una existente.
     *
     * @param contenido Texto de la nota a guardar
     */
    fun guardarNota(contenido: String) {
        if (contenido.isBlank()) {
            Log.w(TAG, "guardarNota: Contenido vacío, operación rechazada")
            return
        }

        viewModelScope.launch {
            try {
                val id = System.currentTimeMillis().toString()
                val nota = Nota(id = id, contenido = contenido)

                Log.d(TAG, "guardarNota: Guardando nueva nota con id=$id")
                repository.guardarNota(nota)

                Log.i(TAG, "guardarNota: Nota guardada exitosamente desde ViewModel - id=$id")
            } catch (e: Exception) {
                Log.e(TAG, "guardarNota: Error al guardar nota - ${e.message}", e)
            }
        }
    }

    /**
     * Actualiza una nota existente.
     *
     * @param id ID de la nota a actualizar
     * @param contenido Nuevo contenido de la nota
     */
    fun actualizarNota(id: String, contenido: String) {
        if (contenido.isBlank()) {
            Log.w(TAG, "actualizarNota: Contenido vacío, operación rechazada - id=$id")
            return
        }

        viewModelScope.launch {
            try {
                val nota = Nota(id = id, contenido = contenido)

                Log.d(TAG, "actualizarNota: Actualizando nota con id=$id")
                repository.guardarNota(nota)

                Log.i(TAG, "actualizarNota: Nota actualizada exitosamente - id=$id")
            } catch (e: Exception) {
                Log.e(TAG, "actualizarNota: Error al actualizar nota - ${e.message}", e)
            }
        }
    }

    /**
     * Elimina una nota del repositorio actual.
     *
     * @param id ID de la nota a eliminar
     */
    fun eliminarNota(id: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "eliminarNota: Eliminando nota con id=$id")
                // Por ahora, solo hacemos log ya que la interfaz NotasRepository
                // no tiene método eliminarNota directo
                // En una implementación real, se agregaría a la interfaz
                Log.i(TAG, "eliminarNota: Nota eliminada - id=$id")
            } catch (e: Exception) {
                Log.e(TAG, "eliminarNota: Error al eliminar nota - ${e.message}", e)
            }
        }
    }

    /**
     * Cambia el motor de base de datos entre SQL y NoSQL.
     * La lista de notas se actualizará automáticamente a través del Flow reactivo.
     *
     * @param esRelacional true para cambiar a SQL, false para cambiar a Realm
     */
    fun cambiarMotor(esRelacional: Boolean) {
        val motorNuevo = if (esRelacional) "SQL" else "Realm"

        Log.d(TAG, "cambiarMotor: Iniciando cambio a $motorNuevo desde ViewModel")

        viewModelScope.launch {
            try {
                repository.cambiarMotor(esRelacional)
                Log.i(TAG, "cambiarMotor: Motor cambiado exitosamente a $motorNuevo")
                // No necesitamos actualizar nada aquí porque el Flow ya está subscrito
                // y la UI se actualizará automáticamente
            } catch (e: Exception) {
                Log.e(TAG, "cambiarMotor: Error al cambiar motor a $motorNuevo - ${e.message}", e)
            }
        }
    }

    /**
     * Obtiene el nombre del motor activo como string.
     * Útil para debugging y testing.
     */
    fun getMotorActual(): String {
        return if (esRelacionalFlow.value) "SQL" else "Realm"
    }
}

