package com.diploma.fuelstats.data.remote.auth

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("/v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): AuthResponseDto

    @POST("/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): AuthResponseDto
}