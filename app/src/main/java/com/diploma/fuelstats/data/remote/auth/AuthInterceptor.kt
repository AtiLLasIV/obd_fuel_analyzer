package com.diploma.fuelstats.data.remote.auth

import com.diploma.fuelstats.data.local.auth.AuthSessionStorage
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val authSessionStorage: AuthSessionStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = authSessionStorage.getAccessToken()

        val request = if (token.isNullOrBlank()) {
            originalRequest
        } else {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }

        return chain.proceed(request)
    }
}