package com.example.proyecto.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.security.GestorSecretos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeguridadViewModel @Inject constructor(
    private val gestorSecretos: GestorSecretos
) : ViewModel() {

    private val _mensaje = MutableStateFlow("")
    val mensaje = _mensaje.asStateFlow()

    fun guardarSecreto(key: String, value: String, tipo: Int) {
        viewModelScope.launch {
            try {
                when (tipo) {
                    // ¡Nombres corregidos para que coincidan con GestorSecretos!
                    0 -> gestorSecretos.guardarShared(key, value)
                    1 -> gestorSecretos.guardarDataStore(key, value)
                    2 -> gestorSecretos.guardarEncrypted(key, value)
                }
                _mensaje.value = "Secreto guardado exitosamente"
            } catch (e: Exception) {
                _mensaje.value = "Error al guardar: ${e.message}"
            }
        }
    }

    fun recuperarSecreto(key: String, tipo: Int) {
        viewModelScope.launch {
            try {
                val resultado = when (tipo) {
                    // ¡Nombres corregidos aquí también!
                    0 -> gestorSecretos.leerShared(key)
                    1 -> gestorSecretos.leerDataStore(key)
                    2 -> gestorSecretos.leerEncrypted(key)
                    else -> null
                }

                if (resultado != null) {
                    _mensaje.value = "Valor revelado: $resultado"
                } else {
                    _mensaje.value = "El secreto no existe en este compartimento"
                }
            } catch (e: Exception) {
                _mensaje.value = "Error al leer: ${e.message}"
            }
        }
    }
}