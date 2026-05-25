package com.diploma.fuelstats.data.repository

import com.diploma.fuelstats.data.remote.stats.GroupStatsResponseDto
import com.diploma.fuelstats.data.remote.stats.SendStatsRequestDto
import com.diploma.fuelstats.data.remote.stats.StatsRemoteApi

class RemoteStatsRepository(
    private val statsRemoteApi: StatsRemoteApi
) {

    suspend fun sendStats(request: SendStatsRequestDto) {
        statsRemoteApi.sendStats(request)
    }

    suspend fun getStatsByModel(
        brand: String,
        model: String
    ): GroupStatsResponseDto {
        return statsRemoteApi.getStatsByModel(
            brand = brand,
            model = model
        )
    }

    suspend fun getStatsByType(vehicleType: String): GroupStatsResponseDto {
        return statsRemoteApi.getStatsByType(vehicleType)
    }
}