package com.upchiapas.mitiendita.products.data.mapper

import com.upchiapas.mitiendita.products.data.remote.dto.ProductDto
import com.upchiapas.mitiendita.products.data.remote.dto.ProductRequestDto
import com.upchiapas.mitiendita.products.domain.model.Product
import java.time.LocalDate

/** Conversiones DTO <-> dominio. Solo las usa ProductRepositoryImpl. */
fun ProductDto.toDomain(): Product = Product(
    id = id,
    name = name,
    category = category,
    price = price,
    stock = stock,
    expirationDate = LocalDate.parse(expirationDate)
)

fun List<ProductDto>.toDomain(): List<Product> = map { it.toDomain() }

fun Product.toRequestDto(): ProductRequestDto = ProductRequestDto(
    name = name.trim(),
    category = category,
    price = price,
    stock = stock,
    expirationDate = expirationDate.toString() // ISO-8601: yyyy-MM-dd
)
