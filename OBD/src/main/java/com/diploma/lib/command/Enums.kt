package com.lib.obd.command

private const val BIT_POS_0 = 0
private const val BIT_POS_1 = 1
private const val BIT_POS_2 = 2
private const val BIT_POS_3 = 3
private const val BIT_POS_4 = 4
private const val BIT_POS_5 = 5
private const val BIT_POS_6 = 6
private const val BIT_POS_7 = 7

enum class ObdProtocols(
    val displayName: String,
    internal val command: String,
) {
    UNKNOWN("Unknown Protocol", ""),
    AUTO("Auto", "0"),
    SAE_J1850_PWM("SAE J1850 PWM", "1"),
    SAE_J1850_VPW("SAE J1850 VPW", "2"),
    ISO_9141_2("ISO 9141-2", "3"),
    ISO_14230_4_KWP("ISO 14230-4 (KWP 5BAUD)", "4"),
    ISO_14230_4_KWP_FAST("ISO 14230-4 (KWP FAST)", "5"),
    ISO_15765_4_CAN("ISO 15765-4 (CAN 11/500)", "6"),
    ISO_15765_4_CAN_B("ISO 15765-4 (CAN 29/500)", "7"),
    ISO_15765_4_CAN_C("ISO 15765-4 (CAN 11/250)", "8"),
    ISO_15765_4_CAN_D("ISO 15765-4 (CAN 29/250)", "9"),
    SAE_J1939_CAN("SAE J1939 (CAN 29/250)", "A"),
}

enum class AdaptiveTimingMode(
    val displayName: String,
    internal val command: String,
) {
    OFF("Off", "0"),
    AUTO_1("Auto 1", "1"),
    AUTO_2("Auto 2", "2"),
}

enum class Switcher(
    internal val command: String,
) {
    ON("1"),
    OFF("0"),
}

enum class Monitors(
    internal val displayName: String,
    internal val isSparkIgnition: Boolean? = null,
    internal val bitPos: Int,
) {
    MISFIRE("Misfire", bitPos = BIT_POS_0),
    FUEL_SYSTEM("Fuel System", bitPos = BIT_POS_1),
    COMPREHENSIVE_COMPONENT("Comprehensive Component", bitPos = BIT_POS_2),
    CATALYST("Catalyst (CAT)", true, BIT_POS_0),
    HEATED_CATALYST("Heated Catalyst", true, BIT_POS_1),
    EVAPORATIVE_SYSTEM("Evaporative (EVAP) System", true, BIT_POS_2),
    SECONDARY_AIR_SYSTEM("Secondary Air System", true, BIT_POS_3),
    AC_REFRIGERANT("A/C Refrigerant", true, BIT_POS_4),
    OXYGEN_SENSOR("Oxygen (O2) Sensor", true, BIT_POS_5),
    OXYGEN_SENSOR_HEATER("Oxygen Sennsor Heater", true, BIT_POS_6),
    EGR_SYSTEM("EGR (Exhaust Gas Recirculation) and/or VVT System", true, BIT_POS_7),
    NMHC_CATALYST("NMHC Catalyst", false, BIT_POS_0),
    NOX_SCR_MONITOR("NOx/SCR Aftertreatment", false, BIT_POS_1),
    BOOST_PRESSURE("Boost Pressure", false, BIT_POS_3),
    EXHAUST_GAS_SENSOR("Exhaust Gas Sensor", false, BIT_POS_5),
    PM_FILTER("PM Filter", false, BIT_POS_6),
    EGR_VVT_SYSTEM("EGR (Exhaust Gas Recirculation) and/or VVT System", false, BIT_POS_7),
}
