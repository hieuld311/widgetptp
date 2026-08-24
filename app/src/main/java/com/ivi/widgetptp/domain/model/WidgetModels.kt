package com.ivi.widgetptp.domain.model

import java.time.Instant
import java.time.ZoneId
import java.util.Locale

enum class WidgetType {
    DATE_TIME,
    DRIVER_HEALTH,
    WEATHER,
    TIRE_PRESSURE,
    LOGO_DISPLAY,
    NAVIGATION,
}

data class DateTimeSnapshot(
    val instant: Instant,
    val locale: Locale,
    val zoneId: ZoneId,
    val is24HourFormat: Boolean,
)

data class DriverHealthData(
    val heartRateBpm: Int,
    val respirationRateBrpm: Int,
)

enum class WeatherCondition {
    CLEAR_SKY,
    FEW_CLOUDS,
    SCATTERED_CLOUDS,
    BROKEN_CLOUDS,
    SHOWER_RAIN,
    RAIN,
    LIGHT_RAIN,
    THUNDERSTORM,
    SNOW,
    MIST,
    DRIZZLE,
}

data class WeatherData(
    val locationName: String,
    val temperatureCelsius: Double,
    val condition: WeatherCondition,
    val isDaylight: Boolean,
)

data class NavigationData(
    val destination: String?,
    val eta: String?,
    val suggestedPoi: String?,
    val remainingDistanceKilometers: Double? = null,
    val remainingDurationMinutes: Int? = null,
    val routeProgress: Float? = null,
)

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT,
}
