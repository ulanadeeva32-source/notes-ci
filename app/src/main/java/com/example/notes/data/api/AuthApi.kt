package com.example.notes.data.api

import com.example.notes.data.dto.LoginRequestDto
import com.example.notes.data.dto.LoginResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @POST("auth/register")
    suspend fun register(@Body request: LoginRequestDto): LoginResponseDto
}
