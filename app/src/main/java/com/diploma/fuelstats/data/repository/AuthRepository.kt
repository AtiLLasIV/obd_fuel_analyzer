package com.diploma.fuelstats.data.repository

import com.diploma.fuelstats.data.remote.auth.AuthApi
import com.diploma.fuelstats.data.remote.auth.LoginRequestDto
import com.diploma.fuelstats.data.remote.auth.RegisterRequestDto

class AuthRepository(
    private val authApi: AuthApi
) {
    suspend fun login(email: String, password: String): String {
        return authApi.login(LoginRequestDto(email, password)).token
    }

    suspend fun register(email: String, password: String): String {
        return authApi.register(RegisterRequestDto(email, password)).token
    }
}