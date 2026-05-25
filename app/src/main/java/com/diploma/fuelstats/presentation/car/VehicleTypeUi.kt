package com.diploma.fuelstats.presentation.car

data class VehicleTypeOption(
    val value: String,
    val label: String
) {
    override fun toString(): String = label
}

object VehicleTypeUi {

    val options = listOf(
        VehicleTypeOption("sedan", "Седан"),
        VehicleTypeOption("hatchback", "Хэтчбек"),
        VehicleTypeOption("wagon", "Универсал"),
        VehicleTypeOption("crossover", "Кроссовер"),
        VehicleTypeOption("suv", "Внедорожник"),
        VehicleTypeOption("coupe", "Купе"),
        VehicleTypeOption("minivan", "Минивэн"),
        VehicleTypeOption("pickup", "Пикап"),
        VehicleTypeOption("other", "Другое")
    )

    fun labelOf(value: String): String {
        return options.firstOrNull { it.value == value }?.label ?: "Другое"
    }

    fun indexOf(value: String): Int {
        val index = options.indexOfFirst { it.value == value }
        return if (index >= 0) index else options.lastIndex
    }
}