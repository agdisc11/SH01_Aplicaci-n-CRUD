package com.upchiapas.mitiendita.products.data.remote

import com.upchiapas.mitiendita.products.data.remote.dto.ProductDto
import com.upchiapas.mitiendita.products.data.remote.dto.ProductRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Endpoints del recurso /products.
 * El token NO se pasa aquí: lo agrega el AuthInterceptor de OkHttp.
 */
interface ProductApi {

    @GET("products")
    suspend fun getProducts(): List<ProductDto>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): ProductDto

    @POST("products")
    suspend fun createProduct(@Body body: ProductRequestDto): ProductDto

    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body body: ProductRequestDto): ProductDto

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int)
}
