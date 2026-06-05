package com.example.proyecto.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface JsonPlaceholderApi {
    // Consulta: Envío de petición a /posts/{id}
    @GET("/posts/{id}")
    suspend fun getPost(@Path("id") id: Int): Response<Post>

    // Actualización: Envío del JSON modificado de vuelta al recurso
    @PUT("/posts/{id}")
    suspend fun updatePost(@Path("id") id: Int, @Body post: Post): Response<Post>
}