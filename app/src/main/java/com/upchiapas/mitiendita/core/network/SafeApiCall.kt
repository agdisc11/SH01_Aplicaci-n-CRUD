package com.upchiapas.mitiendita.core.network

import com.upchiapas.mitiendita.core.error.AppException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Ejecuta una llamada a la API y envuelve el resultado en [Result].
 * Cualquier excepción técnica se traduce a un [AppException].
 * Solo la usan los repositorios (capa de datos).
 *
 * @param onHttpError permite a cada repositorio dar un significado propio a un código HTTP
 * (por ejemplo, 400 en /login = credenciales inválidas). Si regresa null se usa el mapeo general.
 */
suspend fun <T> safeApiCall(
    onHttpError: (code: Int, body: String) -> AppException? = { _, _ -> null },
    block: suspend () -> T
): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: HttpException) {
    val body = e.response()?.errorBody()?.string().orEmpty()
    Result.failure(onHttpError(e.code(), body) ?: e.toAppException())
} catch (e: IOException) {
    Result.failure(AppException.Network())
} catch (e: SerializationException) {
    Result.failure(AppException.Unknown("Respuesta inesperada del servidor."))
} catch (e: Exception) {
    Result.failure(AppException.Unknown(e.message))
}

private fun HttpException.toAppException(): AppException = when (code()) {
    401, 403 -> AppException.SessionExpired()
    404 -> AppException.NotFound()
    else -> AppException.Unknown("Error del servidor (HTTP ${code()}).")
}
