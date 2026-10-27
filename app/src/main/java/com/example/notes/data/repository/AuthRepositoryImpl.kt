package com.example.notes.data.repository

import com.example.notes.data.api.AuthApi
import com.example.notes.data.dto.LoginRequestDto
import com.example.notes.data.dto.LoginResponseDto
import com.example.notes.data.local.SessionStorage
import com.example.notes.domain.model.AuthException
import com.example.notes.domain.model.AuthUser
import com.example.notes.domain.repository.AuthRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/** Реализация контракта: ходит в сеть, запоминает сессию и переводит ошибки Retrofit в доменные. */
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val session: SessionStorage
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthUser> =
        call { api.login(LoginRequestDto(email, password)) }

    override suspend fun register(email: String, password: String): Result<AuthUser> =
        call { api.register(LoginRequestDto(email, password)) }

    private suspend fun call(request: suspend () -> LoginResponseDto): Result<AuthUser> =
        try {
            val response = request()
            session.save(response.email, response.token)
            Result.success(AuthUser(email = response.email, token = response.token))
        } catch (e: CancellationException) {
            throw e // отмена корутины (выход с экрана) — не ошибка
        } catch (e: HttpException) {
            Result.failure(
                when (e.code()) {
                    401 -> AuthException.InvalidCredentials()
                    409 -> AuthException.EmailTaken()
                    else -> e
                }
            )
        } catch (e: IOException) {
            Result.failure(AuthException.Network())
        }
}
