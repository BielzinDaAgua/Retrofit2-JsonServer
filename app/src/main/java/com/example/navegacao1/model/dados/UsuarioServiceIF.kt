package com.example.navegacao1.model.dados

import retrofit2.http.*

interface UsuarioServiceIF {

    @GET("usuarios")
    suspend fun listar(): List<Usuario>

    @GET("usuarios/{id}")
    suspend fun buscarPorId(@Path("id") id: String): Usuario

    @POST("usuarios")
    suspend fun inserir(@Body usuario: Usuario): Usuario

    @DELETE("usuarios/{id}")
    suspend fun remover(@Path("id") id: String)
}
