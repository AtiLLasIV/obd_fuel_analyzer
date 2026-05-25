package com.diploma.obd

import kotlinx.coroutines.flow.StateFlow

interface ObdClient {
    suspend fun connect(address: String): Boolean

    suspend fun disconnect()

    /**
     * Текущее состояние соединения
     */
    val isConnected: StateFlow<Boolean>

    /**
     * Скорость автомобиля, км/ч
     */
    suspend fun getSpeedKmh(): Double?

    /**
     * Обороты двигателя, об/мин
     */
    suspend fun getRpm(): Double?

    /**
     * Уровень топлива в баке, %
     */
    suspend fun getFuelLevelPercent(): Double?

    /**
     * Массовый расход воздуха, г/с
     */
    suspend fun getMafGramsPerSec(): Double?

    /**
     * Температура охлаждающей жидкости
     */
    suspend fun getCoolantTempC(): Double?

    /**
     * Температура окружающего воздуха
     */
    suspend fun getAmbientTempC(): Double?

    /**
     * Температура воздуха на впуске
     */
    suspend fun getIntakeAirTempC(): Double?

    /**
     * Нагрузка двигателя, %
     */
    suspend fun getEngineLoadPercent(): Double?

    /**
     * Мгновенный расход топлива, л/ч
     */
    suspend fun getEngineFuelRateLph(): Double?

    /**
     * Время работы двигателя с момента запуска
     */
    suspend fun getRunTimeSinceEngineStartSec(): Int?

    /**
     * Положение дроссельной заслонки
     */
    suspend fun getThrottlePositionPercent(): Double?

    suspend fun getVin(): String?
}
