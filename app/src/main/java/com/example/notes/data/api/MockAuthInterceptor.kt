package com.example.notes.data.api

import android.content.Context
import com.example.notes.data.dto.LoginRequestDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException

/**
 * Заглушка сервера, чтобы приложение работало без настоящего бэкенда.
 *  - POST auth/register: создаёт аккаунт (409, если email занят)
 *  - POST auth/login: проверяет email и пароль (401, если неверно)
 *  - offline@example.com: имитация ошибки сети (IOException)
 * Аккаунты хранятся на телефоне (SharedPreferences) и переживают перезапуск.
 * Готовый аккаунт: user@example.com / 123456.
 * Чтобы подключить реальный сервер, достаточно убрать этот перехватчик в NetworkModule.
 */
class MockAuthInterceptor(context: Context) : Interceptor {

    private val gson = Gson()
    private val json = "application/json".toMediaType()
    private val prefs = context.getSharedPreferences("mock_server", Context.MODE_PRIVATE)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        if (request.method != "POST" || !(path.endsWith("auth/login") || path.endsWith("auth/register"))) {
            return chain.proceed(request)
        }

        val buffer = Buffer()
        request.body?.writeTo(buffer)
        val body = gson.fromJson(buffer.readUtf8(), LoginRequestDto::class.java)
        val email = body.email.trim().lowercase()

        Thread.sleep(1200) // имитация задержки сети, чтобы был виден индикатор загрузки

        if (email == "offline@example.com") throw IOException("Network is unreachable")

        val users = loadUsers()
        return if (path.endsWith("auth/register")) {
            if (users.containsKey(email)) {
                reply(request, 409, "Conflict", """{"message":"Email already registered"}""")
            } else {
                users[email] = body.password
                saveUsers(users)
                ok(request, email)
            }
        } else {
            if (users[email] == body.password) {
                ok(request, email)
            } else {
                reply(request, 401, "Unauthorized", """{"message":"Invalid credentials"}""")
            }
        }
    }

    private fun ok(request: Request, email: String) =
        reply(request, 200, "OK", """{"email":"$email","token":"mock-jwt-token-${email.hashCode()}"}""")

    private fun reply(request: Request, code: Int, message: String, payload: String) =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(message)
            .body(payload.toResponseBody(json))
            .build()

    private fun loadUsers(): MutableMap<String, String> {
        val type = object : TypeToken<MutableMap<String, String>>() {}.type
        val saved: MutableMap<String, String>? = gson.fromJson(prefs.getString("users", null), type)
        return (saved ?: mutableMapOf()).also { it.putIfAbsent("user@example.com", "123456") }
    }

    private fun saveUsers(users: Map<String, String>) {
        prefs.edit().putString("users", gson.toJson(users)).apply()
    }
}
