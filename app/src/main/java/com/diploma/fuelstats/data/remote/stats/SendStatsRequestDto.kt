package com.diploma.fuelstats.data.remote.stats

import com.google.gson.annotations.SerializedName

data class SendStatsRequestDto(
    @SerializedName("vehicle_id")
    val vehicleId: String,

    @SerializedName("brand")
    val brand: String,

    @SerializedName("model")
    val model: String,

    @SerializedName("vehicle_type")
    val vehicleType: String,

    @SerializedName("manual_avg_consumption_l_per_100km")
    val manualAvgConsumptionLPer100Km: Double?,

    @SerializedName("obd_avg_consumption_l_per_100km")
    val obdAvgConsumptionLPer100Km: Double?,

    @SerializedName("avg_refuel_liters")
    val avgRefuelLiters: Double?,

    @SerializedName("avg_distance_between_refuels_km")
    val avgDistanceBetweenRefuelsKm: Double?,

    @SerializedName("manual_records_count")
    val manualRecordsCount: Int,

    @SerializedName("telemetry_samples_count")
    val telemetrySamplesCount: Int,

    @SerializedName("updated_at")
    val updatedAt: String
)