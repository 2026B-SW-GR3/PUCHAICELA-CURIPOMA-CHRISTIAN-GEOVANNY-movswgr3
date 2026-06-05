package com.example.proyecto.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.network.JsonPlaceholderApi
import com.example.proyecto.network.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RedViewModel @Inject constructor(
    private val api: JsonPlaceholderApi
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _postActual = MutableStateFlow<Post?>(null)
    val postActual = _postActual.asStateFlow()

    private val _mensaje = MutableStateFlow("")
    val mensaje = _mensaje.asStateFlow()

    fun fetchPost(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getPost(id)
                if (response.isSuccessful) {
                    _postActual.value = response.body()
                    _mensaje.value = "Post cargado con éxito"
                } else {
                    _mensaje.value = "Error al obtener el post"
                }
            } catch (e: Exception) {
                _mensaje.value = "Error de red: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePost(id: Int, title: String, body: String) {
        val postOriginal = _postActual.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val postModificado = postOriginal.copy(title = title, body = body)
                val response = api.updatePost(id, postModificado)

                if (response.isSuccessful && response.code() == 200) {
                    _postActual.value = response.body()
                    _mensaje.value = "Actualización 200 OK confirmada"
                } else {
                    _mensaje.value = "Fallo al actualizar"
                }
            } catch (e: Exception) {
                _mensaje.value = "Error de red: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}