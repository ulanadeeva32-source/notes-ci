package com.example.notes.domain.usecase

import com.example.notes.domain.model.AuthException
import com.example.notes.domain.model.AuthUser
import com.example.notes.domain.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Тест без эмулятора: вместо настоящего репозитория передаём подделку. */
class AuthUseCaseTest {

    private class FakeAuthRepository(
        private val result: Result<AuthUser>
    ) : AuthRepository {
        var calls = 0
        override suspend fun login(email: String, password: String): Result<AuthUser> {
            calls++
            return result
        }

        override suspend fun register(email: String, password: String): Result<AuthUser> {
            calls++
            return result
        }
    }

    private val user = AuthUser("user@example.com", "token")

    @Test
    fun `invalid email does not reach repository`() = runTest {
        val repo = FakeAuthRepository(Result.success(user))
        val result = AuthUseCase(repo)("not-an-email", "123456")

        assertTrue(result.exceptionOrNull() is AuthException.InvalidEmail)
        assertEquals(0, repo.calls)
    }

    @Test
    fun `empty password does not reach repository`() = runTest {
        val repo = FakeAuthRepository(Result.success(user))
        val result = AuthUseCase(repo)("user@example.com", "  ")

        assertTrue(result.exceptionOrNull() is AuthException.EmptyPassword)
        assertEquals(0, repo.calls)
    }

    @Test
    fun `valid data returns user from repository`() = runTest {
        val repo = FakeAuthRepository(Result.success(user))
        val result = AuthUseCase(repo)(" user@example.com ", "123456")

        assertEquals(user, result.getOrNull())
        assertEquals(1, repo.calls)
    }

    @Test
    fun `repository error is passed through`() = runTest {
        val repo = FakeAuthRepository(Result.failure(AuthException.InvalidCredentials()))
        val result = AuthUseCase(repo)("user@example.com", "wrong")

        assertTrue(result.exceptionOrNull() is AuthException.InvalidCredentials)
    }

    @Test
    fun `short password is rejected on registration`() = runTest {
        val repo = FakeAuthRepository(Result.success(user))
        val result = AuthUseCase(repo).register("new@example.com", "123")

        assertTrue(result.exceptionOrNull() is AuthException.WeakPassword)
        assertEquals(0, repo.calls)
    }

    @Test
    fun `valid registration reaches repository`() = runTest {
        val repo = FakeAuthRepository(Result.success(user))
        val result = AuthUseCase(repo).register("new@example.com", "123456")

        assertEquals(user, result.getOrNull())
        assertEquals(1, repo.calls)
    }
}
