package com.diploma.fuelstats.data.remote.stats

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface StatsRemoteApi {

    @POST("/v1/stats")
    suspend fun sendStats(
        @Body request: SendStatsRequestDto
    ): SendStatsResponseDto

    @GET("/v1/stats/model")
    suspend fun getStatsByModel(
        @Query("brand") brand: String,
        @Query("model") model: String
    ): GroupStatsResponseDto

    @GET("/v1/stats/type")
    suspend fun getStatsByType(
        @Query("vehicle_type") vehicleType: String
    ): GroupStatsResponseDto
}