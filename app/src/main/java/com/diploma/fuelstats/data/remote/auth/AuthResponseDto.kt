package com.diploma.fuelstats.data.remote.auth

import com.google.gson.annotations.SerializedName

data class AuthResponseDto(
    @SerializedName("user_id")
    val userId: String? = null,
    @SerializedName("token")
    val token: String
)