package com.diploma.fuelstats.data.remote.stats

import com.google.gson.annotations.SerializedName

data class GroupStatsResponseDto(
    @SerializedName("group_name")
    val groupName: String,

    @SerializedName("manual_avg_consumption_l_per_100km")
    val manualAvgConsumptionLPer100Km: Double?,

    @SerializedName("obd_avg_consumption_l_per_100km")
    val obdAvgConsumptionLPer100Km: Double?,

    @SerializedName("avg_refuel_liters")
    val avgRefuelLiters: Double?,

    @SerializedName("avg_distance_between_refuels_km")
    val avgDistanceBetweenRefuelsKm: Double?,

    @SerializedName("vehicles_count")
    val vehiclesCount: Int
)